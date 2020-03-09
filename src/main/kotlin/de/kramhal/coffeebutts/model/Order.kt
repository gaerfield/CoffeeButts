package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import de.kramhal.coffeebutts.repositories.InvoiceRepository
import de.kramhal.coffeebutts.repositories.OrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*

// TODO make this an inline-class as soon as https://jira.spring.io/browse/DATACMNS-1517 is fixed
class OrderId(val id: String = UUID.randomUUID().toString()) : Serializable

@ExperimentalCoroutinesApi
@Component
internal class FrontDesk(
        private val orderRepository: OrderRepository
) {
    class Ordered(val order: Order) : EventBus.Event

    init {
        GlobalScope.launch {
            EventBus.on<Invoice.Paid>().consumeEach { markPayment(it) }
        }

        GlobalScope.launch {
            EventBus.on<Barista.Processed>().consumeEach { prepareCoffeeDelivery(it) }
        }
    }

    suspend fun placeOrder(type: List<Coffee.Type>): OrderId {
        val order = orderRepository.save(Order(type)).awaitSingle()
        GlobalScope.launch { EventBus.send(Ordered(order)) }
        return order.id
    }


    suspend fun markPayment(paid: Invoice.Paid) {
        val order = orderRepository.findById(paid.orderId).awaitSingle()
        order.paid()
        deliverCoffee(order)
    }

    private suspend fun prepareCoffeeDelivery(processed: Barista.Processed) {
        val order = orderRepository.findById(processed.orderId).awaitSingle()
        deliverCoffee(order)
    }

    class Delivered(orderId: OrderId, coffees: Set<Coffee>) : EventBus.Event

    private suspend fun deliverCoffee(order: Order) {
        if(order.isDeliverable())
            EventBus.send(Delivered(order.id, order.coffees))
    }
}

internal class Coffee(
        val type: Type
) {
    enum class Type { LatteMachiatto, Cappucino, Espresso, DoubleEspresso }
}

@ExperimentalCoroutinesApi
internal data class Order(
        val coffeeType: List<Coffee.Type>,
        @Id val id: OrderId = OrderId(),
        @Version val version: Long? = null
) {
    var paid: Boolean = false
        private set
    val coffees: MutableSet<Coffee> = mutableSetOf()

    fun paid() {
        // more validation
        paid = true
    }

    fun isDeliverable() = paid && coffeeType.size == coffees.size
}
