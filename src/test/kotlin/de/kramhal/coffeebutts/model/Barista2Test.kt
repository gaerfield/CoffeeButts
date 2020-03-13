package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

internal class Barista2Test {

    private val eventBus = EventBus()
    private val sut = Barista2(eventBus)

    @Test
    fun `test barista is producing requested coffees`() = runBlockingTest {
        val order = Order(listOf(Coffee.Type.Cappucino, Coffee.Type.LatteMachiatto, Coffee.Type.DoubleEspresso, Coffee.Type.Espresso))
        eventBus.send(FrontDesk.Ordered(order))
        sut.processOrder(order)
        val coffees = order.getCoffees().toSet()

        assertEquals(order.requestedCoffees.size, coffees.size)
    }


}