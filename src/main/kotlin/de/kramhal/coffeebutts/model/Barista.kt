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
    data class Processed(val order: Order) : EventBus.Event

    private val log = KotlinLogging.logger {}
    private final val orders = Channel<Order>(10)

    init {
        eventBus.on<FrontDesk.Ordered> {
            log.info { "Received order for ${it.order.requestedCoffees}" }
            orders.send(it.order)
        }
        GlobalScope.launch { orders.consumeEach { processOrder(it) } }
    }

    private suspend fun processOrder(order: Order) {
        log.info { "start processing order for [${order.id}] = [${order.requestedCoffees}]" }
        // validiere ... genügend Milch?
        eventBus.send(Processing(order))
        order.requestedCoffees.forEach {
            delay(2500)
            order.addCoffee(Coffee(it))
            log.info { "finished creating the coffee $it" }
        }
        order.wasProcessed()
        log.info { "finished processing the order [${order.id}]" }
        eventBus.send(Processed(order))
    }
}