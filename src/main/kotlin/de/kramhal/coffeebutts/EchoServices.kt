package de.kramhal.coffeebutts

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.router
import java.util.concurrent.atomic.AtomicLong

@Configuration
class EchoRoutes(
        private val echoServices: EchoServices
) {

    @Bean
    fun echoRouter() = router {
        GET("/echo1") { echoServices::echo12 }
    }
}
//val echoHandler = EchoServices()
//val route = coRouter {
//    GET("/person/{id}", echoHandler::echo1)
//    GET("/person", handler::listPeople)
//    POST("/person", handler::createPerson)
//}

@Component
class EchoServices {
    private val template = "Hello, %s!"
    private val counter = AtomicLong(1)
    private val log = KotlinLogging.logger {}

    data class Echo(val id: Long, val content: String)

    suspend fun calculateEcho(seconds: Int): Echo {
        val id = counter.getAndIncrement()
        val delay = ((Math.random() * (seconds-1)).toLong()+1) * 1000
//        val delay = (seconds * 1000).toLong()
        delay(delay)
        return Echo(id, String.format(template, "${delay/1000}")).also { println(it) }
    }

    suspend fun echo12(request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                    .bodyAndAwait( echo1() )
    }


    fun echo1(): Flow<Echo> {
        val channel = Channel<Echo>()
        val deffereds = (1..5).map {
            GlobalScope.async { channel.send(calculateEcho(it)) }
        }
        GlobalScope.async { deffereds.awaitAll(); channel.close() }
        return flow {
            channel.consumeEach { emit(it) }
        }

    }

    @GetMapping(value = ["/echo2"], produces = [MediaType.APPLICATION_STREAM_JSON_VALUE])
    suspend fun echo2(): Flow<Echo> {
        return flow {
            (1..5).map {
                GlobalScope.async {
                    emit(calculateEcho(it))
                }
            }
        }
    }

    @GetMapping(value = ["/echo3"], produces = [MediaType.APPLICATION_STREAM_JSON_VALUE])
    suspend fun echo3(): Flow<Echo> {
        return (1..5).map {
            GlobalScope.async {
                calculateEcho(5-it)
            }
        }.asFlow().buffer().map { it.await() }
    }

    @GetMapping(value = ["/echo4"], produces = [MediaType.APPLICATION_STREAM_JSON_VALUE])
    suspend fun echo4(): Flow<Echo> {
        return channelFlow {
            (1..10).map {
                launch {
                    send(calculateEcho(20-it))
                }
            }
        }
    }
}