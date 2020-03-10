package de.kramhal.coffeebutts.infrastructure

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map
import mu.KotlinLogging

@ExperimentalCoroutinesApi
internal object EventBus {
    // marker-interface
    interface Event

    private val log = KotlinLogging.logger {}
    private val bus : BroadcastChannel<Event> = ConflatedBroadcastChannel()

    suspend fun send(o : Event) {
        log.info { "Event received: $o" }
        bus.send(o)
    }

    inline fun <reified T : Event> on() = bus.openSubscription().filter { it is T }.map { it as T }

}