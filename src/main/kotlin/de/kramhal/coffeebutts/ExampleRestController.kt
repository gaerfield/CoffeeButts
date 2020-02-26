package de.kramhal.coffeebutts

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

@RestController
class CoroutinesRestController {
    private val client: WebClient = WebClient.create("http://localhost:8080")

    data class IcndbJoke(val type: String, val value: Joke)
    data class Joke(val id: Int, val joke: String, val categories: List<String>)

    fun jokesClient() =
            WebClient.create("http://api.icndb.com/jokes")

    private suspend fun retrieveJoke() = jokesClient().get()
            .uri("/random")
            .retrieve()
            .awaitBody<IcndbJoke>()
            .value.joke

    @GetMapping("/suspend")
    suspend fun suspendingEndpoint(): String {
        delay(5000)
        return retrieveJoke()
    }

    @GetMapping("/flow")
    fun flowEndpoint() = flow {
        delay(5000)
        emit(retrieveJoke())
        delay(5000)
        emit(retrieveJoke())
    }

    @GetMapping("/deferred")
    fun deferredEndpoint() = GlobalScope.async {
        delay(5000)
        retrieveJoke()
    }

    @GetMapping("/sequential")
    suspend fun sequential(): List<String> {
        val banner1 = client
                .get()
                .uri("/suspend")
                .accept(MediaType.APPLICATION_JSON)
                .awaitExchange()
                .awaitBody<String>()
        val banner2 = client
                .get()
                .uri("/suspend")
                .accept(MediaType.APPLICATION_JSON)
                .awaitExchange()
                .awaitBody<String>()
        return listOf(banner1, banner2)
    }

    @GetMapping("/parallel")
    suspend fun parallel(): List<String> = coroutineScope {
        val deferredBanner1: Deferred<String> = async {
            client
                    .get()
                    .uri("/suspend")
                    .accept(MediaType.APPLICATION_JSON)
                    .awaitExchange()
                    .awaitBody<String>()
        }
        val deferredBanner2: Deferred<String> = async {
            client
                    .get()
                    .uri("/suspend")
                    .accept(MediaType.APPLICATION_JSON)
                    .awaitExchange()
                    .awaitBody<String>()
        }
        listOf(deferredBanner1.await(), deferredBanner2.await())
    }

    @GetMapping("/error")
    suspend fun error() {
        throw IllegalStateException()
    }

    @GetMapping("/cancel")
    suspend fun cancel() {
        throw CancellationException()
    }

}