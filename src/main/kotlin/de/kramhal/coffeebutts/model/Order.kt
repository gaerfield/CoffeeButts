package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import de.kramhal.coffeebutts.repositories.OrderRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*

// TODO make this an inline-class as soon as https://jira.spring.io/browse/DATACMNS-1517 is fixed
data class OrderId(val id: String = UUID.randomUUID().toString()) : Serializable

@Component
internal class FrontDesk(
        private val eventBus: EventBus,
        private val orderRepository: OrderRepository
) {
    data class Ordered(val order: Order) : EventBus.Event

    private val log = KotlinLogging.logger {}

    private val ordersInProcess = mutableMapOf<OrderId,Order>()
    init {
        GlobalScope.launch {
            eventBus.on<Invoice.Paid>().onEach { markPayment(it) }
        }

        GlobalScope.launch {
            eventBus.on<Barista.Processed>().onEach { markFinished(it) }
        }
    }

    suspend fun placeOrder(type: List<Coffee.Type>): OrderId {
        val order = orderRepository.save(Order(type))
        ordersInProcess[order.id] = order
        GlobalScope.launch { eventBus.send(Ordered(order)) }
        return order.id
    }


    suspend fun markPayment(paid: Invoice.Paid) {
        val order = orderRepository.findById(paid.orderId)
        order.wasPaid()
        orderRepository.save(order)
        log.info { "$order was paid" }
    }

    private suspend fun markFinished(processed: Barista.Processed) {
        val order = orderRepository.findById(processed.orderId)
        order.wasProcessed()
        orderRepository.save(order)
        log.info { "${order.id} is ready for pick up (if not already)." }
    }

    data class Delivered(val orderId: OrderId) : EventBus.Event

    suspend fun receiveCoffees(orderId: OrderId): Flow<Coffee> {
        val order = ordersInProcess[orderId]
        if(order == null) {
            log.info { "order [$orderId] was already delivered." }
            return emptyFlow()
        }
//        if(!order.isDeliverable()) {
//            log.info { "Please pay first!" }
//            return emptyFlow()
//        }
        return with(order.coffees) {
            if(this == null) {
                log.info { "Barista has not started yet" }
                return@with emptyFlow<Coffee>()
            }
            consumeAsFlow()
        }
    }
}

internal class Coffee(
        val type: Type
) {
    enum class Type { LatteMachiatto, Cappucino, Espresso, DoubleEspresso }
}

@Document
internal data class Order(
        val requestedCoffees: List<Coffee.Type>,
        @Transient val id: OrderId = OrderId()
) {
    var hasBeenPaid: Boolean = false
        private set
    var hasBeenProcessed: Boolean = false
        private set
    @Id private val idData = id.id

    @Transient
    var coffees : ReceiveChannel<Coffee>? = null
    @Transient
    private val coffs = Channel<Coffee>(requestedCoffees.size)

    fun wasPaid() { hasBeenPaid = true }
    fun isDeliverable() = hasBeenPaid

    fun wasProcessed() { hasBeenProcessed=true; coffs.close() }

    suspend fun addCoffee(coffee: Coffee) = coffs.send(coffee)
    @FlowPreview
    fun getCoffees() = coffs.consumeAsFlow()
}
