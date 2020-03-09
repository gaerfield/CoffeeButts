package de.kramhal.coffeebutts.infrastructure

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map

@ExperimentalCoroutinesApi
internal object EventBus {
    // marker-interface
    interface Event

    val bus : BroadcastChannel<Event> = ConflatedBroadcastChannel()

    suspend fun send(o : Event) = bus.send(o)

    inline fun <reified T : Event> on() = bus.openSubscription().filter { it is T }.map { it as T }

}