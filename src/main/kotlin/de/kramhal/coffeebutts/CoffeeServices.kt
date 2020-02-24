/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package de.kramhal.coffeebutts

import de.kramhal.coffeebutts.model.Coffee
import de.kramhal.coffeebutts.model.Order
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.concurrent.atomic.AtomicLong

@RestController
class CoffeeServices {

    data class CoffeeOrder(val type: Coffee.Type)

    @PostMapping("/order")
    fun placeOrder(@RequestBody order: CoffeeOrder) {
        Order(Coffee(order.type))
    }

    private val template = "Hello, %s!"
    private val counter = AtomicLong()
    private val log = KotlinLogging.logger {}

    data class Echo(val id: Long, val content: String)

    suspend fun calculateEcho(millis: Long): Echo {
        val id = counter.getAndIncrement()
        delay(millis)
        return Echo(id, String.format(template, millis)).also { println(it) }
    }

    @GetMapping(value = ["/echo"], produces = [MediaType.APPLICATION_STREAM_JSON_VALUE])
    fun echo(@RequestParam(value = "name", defaultValue = "World") name: String): Flow<Echo> {
        return flow {
            coroutineScope {
                val channel = produce<Echo> {
                    val deffereds = (5 downTo 0).map {
                        GlobalScope.async(Dispatchers.IO) { calculateEcho((Math.random()*1000*it).toLong()) }
                    }
                }
                channel.consumeAsFlow()
            }
        }
//        val deffereds = (5 downTo 0).map {
//            GlobalScope.async(Dispatchers.IO) { calculateEcho((Math.random()*1000*it).toLong()) }
//        }
//        return runBlocking { deffereds.asFlow().map { it.await() } }
    }

}