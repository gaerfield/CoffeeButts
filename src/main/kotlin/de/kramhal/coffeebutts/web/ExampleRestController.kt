package de.kramhal.coffeebutts.web

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.json

@Configuration
class CoroutinesRestRouter(
        val services: CoroutinesRestController
) {
    @Bean
    fun coRestRouter() = coRouter {
        "/co".nest {
            GET("suspend") { ok().bodyValueAndAwait(services.suspendingEndpoint()) }
            GET("flow") { ok().contentType(MediaType.APPLICATION_STREAM_JSON).bodyAndAwait(services.flowEndpoint()) }
            GET("deferred") { ok().bodyValueAndAwait(services.deferredEndpointAsync().await()) }
            GET("sequential") { ok().bodyValueAndAwait(services.sequential()) }
            GET("parallel") { ok().bodyValueAndAwait(services.parallel()) }
        }
        GET("error") { ok().json().bodyValueAndAwait(services.error()) }
        GET("cancel") { ok().json().bodyValueAndAwait(services.cancel()) }
    }
}

@Component
class CoroutinesRestController {

    private object CallMyself {
        private val endpoint: WebClient = WebClient.create("http://localhost:8080")
        suspend operator fun invoke() = endpoint
                .get().uri("/co/suspend")
                .accept(MediaType.APPLICATION_JSON)
                .awaitExchange()
                .awaitBody<String>()
    }

    private object RetrieveJoke {
        private data class IcndbJoke(val type: String, val value: Joke)
        private data class Joke(val id: Int, val joke: String, val categories: List<String>)
        private val endpoint = WebClient.create("http://api.icndb.com/jokes")

        suspend operator fun invoke() = endpoint.get()
                .uri("/random")
                .retrieve()
                .awaitBody<IcndbJoke>()
                .value.joke
    }

    suspend fun suspendingEndpoint(): String {
        delay(5000)
        return RetrieveJoke()
    }

    fun flowEndpoint() = flow {
        delay(5000)
        emit(RetrieveJoke())
        delay(5000)
        emit(RetrieveJoke())
    }

    fun deferredEndpointAsync() = GlobalScope.async {
        delay(5000)
        RetrieveJoke()
    }

    suspend fun sequential(): List<String> {
        val banner1 = CallMyself()
        val banner2 = CallMyself()
        return listOf(banner1, banner2)
    }

    suspend fun parallel(): List<String> = coroutineScope {
        val deferredBanner1: Deferred<String> = async { CallMyself() }
        val deferredBanner2: Deferred<String> = async { CallMyself() }
        listOf(deferredBanner1.await(), deferredBanner2.await())
    }

    suspend fun error() {
        throw IllegalStateException()
    }

    suspend fun cancel() {
        throw CancellationException()
    }

}