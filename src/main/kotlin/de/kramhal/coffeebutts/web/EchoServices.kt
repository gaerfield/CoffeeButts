package de.kramhal.coffeebutts.web

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.util.concurrent.atomic.AtomicLong

@Configuration
class EchoRoutes(
        private val echoServices: EchoServices
) {
    @Bean
    fun echoRouter() = coRouter {
        "/echo".nest {
            GET("/1", echoServices::eins)
            GET("/2", echoServices::zwei)
            GET("/3", echoServices::drei)
            GET("/4") {
                ok().contentType(MediaType.APPLICATION_STREAM_JSON).bodyAndAwait(echoServices.echo4())
            }
        }
    }
}

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
        return Echo(id, String.format(template, "${delay / 1000}")).also { println(it) }
    }

    suspend fun eins(request: ServerRequest) = ok()
            .contentType(MediaType.APPLICATION_STREAM_JSON)
            .bodyAndAwait(echo1())
    suspend fun zwei(request: ServerRequest) = ok()
            .contentType(MediaType.APPLICATION_STREAM_JSON)
            .bodyAndAwait(echo2())
    suspend fun drei(request: ServerRequest) = ok()
            .contentType(MediaType.APPLICATION_STREAM_JSON)
            .bodyAndAwait(echo3())

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

    suspend fun echo2(): Flow<Echo> {
        return flow {
            (1..5).map {
                GlobalScope.async {
                    emit(calculateEcho(it))
                }
            }
        }
    }

    suspend fun echo3(): Flow<Echo> {
        return (1..5).map {
            GlobalScope.async {
                calculateEcho(5-it)
            }
        }.asFlow().buffer().map { it.await() }
    }

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