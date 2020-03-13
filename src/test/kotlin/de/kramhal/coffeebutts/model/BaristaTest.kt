package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import de.kramhal.coffeebutts.infrastructure.recordEvents
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

internal class BaristaTest {

    private val eventBus = EventBus()
    private val sut = Barista(eventBus)

    @Test
    fun `test barista is producing requested coffees`() = runBlockingTest {

        val events = eventBus.recordEvents<Barista.Processed>(this)

        val order = Order(listOf(Coffee.Type.Cappucino, Coffee.Type.LatteMachiatto, Coffee.Type.DoubleEspresso, Coffee.Type.Espresso))
        eventBus.send(FrontDesk.Ordered(order))

        val actualCoffeeTypes = mutableSetOf<Coffee.Type>()
        val job = launch { order.getCoffees().collect { actualCoffeeTypes.add(it.type) } }
        assertEquals(order.requestedCoffees.toSet(), actualCoffeeTypes )
        assertEquals(order, events.finish().first().order)
        job.cancel()
    }


}