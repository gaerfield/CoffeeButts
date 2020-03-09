package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@ExperimentalCoroutinesApi
@Component
internal class Barista {

    init {
        GlobalScope.launch {
            EventBus.on<FrontDesk.Ordered>().consumeEach { createCoffees(it.order) }
        }
    }

    class Processing(val orderId: OrderId) : EventBus.Event
    class Processed(val orderId: OrderId) : EventBus.Event

    private suspend fun createCoffees(order: Order) {
        // validiere ... genügend Milch?
        EventBus.send(Processing(order.id))
        order.coffeeType.forEach {
            delay(10)
            order.coffees.add(Coffee(it))
        }
        EventBus.send(Processed(order.id))
    }
}