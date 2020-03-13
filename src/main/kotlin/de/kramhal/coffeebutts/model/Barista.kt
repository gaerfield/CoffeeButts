package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
internal class Barista(
        private val eventBus: EventBus
) {

    data class Processing(val order: Order) : EventBus.Event
    data class Processed(val orderId: OrderId) : EventBus.Event

    private val log = KotlinLogging.logger {}
    private val orders = BroadcastChannel<Order>(10)

    init {
        eventBus.on<FrontDesk.Ordered>().onEach { orders.send(it.order) }
        GlobalScope.launch { orders.consumeEach { processOrder(it) } }
    }

    private suspend fun processOrder(order: Order) {
        // validiere ... genügend Milch?
        order.coffees = GlobalScope.produce(capacity = order.requestedCoffees.size) {
            eventBus.send(Processing(order))
            order.requestedCoffees.forEach {
                log.info { "${order.id}: Creating $it" }
                delay(2500)
                send(Coffee(it))
            }
            eventBus.send(Processed(order.id))
        }
    }
}

@Component
internal class Barista2(
        private val eventBus: EventBus
) {
    private val log = KotlinLogging.logger {}
    private final val orders = Channel<Order>(10)

    init {
        eventBus.on<FrontDesk.Ordered>().onEach {
            log.info { "Received order for ${it.order.requestedCoffees}" }
            orders.send(it.order)
        }
        GlobalScope.launch(Dispatchers.IO) { orders.consumeEach { processOrder(it) } }
    }

    suspend fun processOrder(order: Order) {
        log.info { "start processing order for ${order.requestedCoffees}" }
        order.requestedCoffees.forEach {
            delay(2500)
            order.addCoffee(Coffee(it))
            log.info { "finished creating the coffee $it" }
        }
        log.info { "finished processing the order" }
        order.wasProcessed()
    }

}