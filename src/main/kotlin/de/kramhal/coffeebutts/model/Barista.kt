package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.stereotype.Component

@ExperimentalCoroutinesApi
@Component
internal class Barista {

    class Processing(val orderId: OrderId) : EventBus.Event
    class Processed(val orderId: OrderId) : EventBus.Event

    private val log = KotlinLogging.logger {}
    private val orders = BroadcastChannel<Order>(10)

    init {
        GlobalScope.launch { EventBus.on<FrontDesk.Ordered>().consumeEach { orders.send(it.order) } }
        GlobalScope.launch { orders.consumeEach { processOrder(it) } }
    }

    private suspend fun processOrder(order: Order) {
        // validiere ... genügend Milch?
        EventBus.send(Processing(order.id))
        order.requestedCoffees.forEach {
            log.info { "${order.id}: Creating $it" }
            delay(1000)
            order.coffees.send(Coffee(it))
        }
        EventBus.send(Processed(order.id))
    }
}