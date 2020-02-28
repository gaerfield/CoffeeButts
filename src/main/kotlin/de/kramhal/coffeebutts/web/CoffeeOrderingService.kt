/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package de.kramhal.coffeebutts.web

import de.kramhal.coffeebutts.model.Coffee
import de.kramhal.coffeebutts.model.Order
import de.kramhal.coffeebutts.repositories.OrderRepository
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/coffee")
internal class CoffeeOrderingService(
        private val orderRepository: OrderRepository
) {

    data class CoffeeOrder(val type: Coffee.Type)

    @GetMapping("/orders1")
    suspend fun placeOrder(): String {
        val order = Order(Coffee(Coffee.Type.Cappucino))
        println(order)
        return orderRepository.save(order).awaitSingle().id
    }


    @GetMapping("/orders")
    suspend fun allOrders() =
            orderRepository.findAll().asFlow()

    @GetMapping("/orders/{id}")
    suspend fun byId(@PathVariable id: String) =
            orderRepository.findById(id).awaitSingle()
}