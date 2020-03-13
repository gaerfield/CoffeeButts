package de.kramhal.coffeebutts.infrastructure

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
internal class EventBus {
    // marker-interface
    interface Event

    private val log = KotlinLogging.logger {}
    // Initialize the bus with an empty Event, so we can drop the first element everytime on a new subscription
    // This is neccesary, because on subscription usually the current element is send (although it was added
    // before subscription)
    private val bus = ConflatedBroadcastChannel<Event>(object : Event {})

    suspend fun send(o : Event) {
        log.info { "Event received: $o" }
        bus.send(o)
    }

    final inline fun <reified T : Event> on() =
            bus.asFlow().drop(1).filter { it is T }.map { it as T }

    final inline fun <reified T : Event> on(
            scope: CoroutineScope = GlobalScope,
            crossinline action: suspend (value: T) -> Unit) = scope.launch {
        on<T>().collect(action)
    }
}