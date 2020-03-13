package de.kramhal.coffeebutts.infrastructure

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EventBusTest {

    data class TestEvent(val content: String = UUID.randomUUID().toString()) : EventBus.Event
    data class SecondTestEvent(val content: String = UUID.randomUUID().toString()) : EventBus.Event

    @Test
    fun `subscribing to TestEvent and receiving one Event`() = runBlockingTest {
        val eventBus = EventBus()
        val observer = eventBus.recordEvents<TestEvent>(this)

        val expected = TestEvent()
        eventBus.send(expected)
        val actual = observer.finish()
        assertEquals(listOf(expected), actual)
    }

    @Test
    fun `dont receive other type of Events`() = runBlockingTest {
        val eventBus = EventBus()
        val observer = eventBus.recordEvents<TestEvent>(this)

        val notExpected = SecondTestEvent()
        eventBus.send(notExpected)
        assertEquals(listOf(), observer.finish())
    }

    @Test
    fun `receive events in the correct order`() = runBlockingTest {
        val eventBus = EventBus()
        val observer = eventBus.recordEvents<TestEvent>(this)

        val expected = listOf(TestEvent(), TestEvent())
        expected.forEach { eventBus.send(it) }
        assertEquals(expected, observer.finish())
    }

    @Test
    fun `dont receive Events before subscription`() = runBlockingTest {
        val eventBus = EventBus()
        eventBus.send(TestEvent())
        val observer = eventBus.recordEvents<TestEvent>(this)

        val expected = TestEvent()
        eventBus.send(expected)
        assertEquals(listOf(expected), observer.finish())
    }

    @Test
    fun `dont receive same Events twice`() = runBlockingTest {
        val eventBus = EventBus()
        val observer1 = eventBus.recordEvents<TestEvent>(this)

        val expected = TestEvent()
        eventBus.send(expected)
        assertEquals(listOf(expected), observer1.finish())

        val observer2 = eventBus.recordEvents<TestEvent>(this)
        assertEquals(listOf(), observer2.finish())
    }

    @Test
    fun `same Event can be received by different listeners`() = runBlockingTest {
        val eventBus = EventBus()
        val observer1 = eventBus.recordEvents<TestEvent>(this)
        val observer2 = eventBus.recordEvents<TestEvent>(this)

        val expected = TestEvent()
        eventBus.send(expected)
        assertEquals(listOf(expected), observer1.finish())
        assertEquals(listOf(expected), observer2.finish())
    }

//    @Test
//    fun `should receive only events occuring after subscription`() = runBlockingTest {
//        val publisher = ConflatedBroadcastChannel<Int>()
//        val values = mutableListOf<Int>()
//
//        publisher.offer(1)
//        publisher.offer(2)
//
//        val job = launch {
//            publisher.openSubscription().consumeEach { values.add(it) }
//        }
//
//        publisher.offer(3)
//        assertEquals(listOf(3), values)
//        assertEquals(3, publisher.value)
//
//        job.cancel()
//    }
}

internal suspend inline fun <reified T : EventBus.Event> EventBus.recordEvents(scope: CoroutineScope) =
        EventCollector(this.on<T>()).launch(scope)

internal class EventCollector<T : EventBus.Event>(
        private val flow: Flow<T>
) {
    private val values = mutableListOf<T>()
    private lateinit var job: Job
    suspend fun launch(scope: CoroutineScope): EventCollector<T> {
        job = scope.launch { flow.collect { values.add(it) } }
        return this
    }

    fun finish(): List<T> {
        job.cancel()
        return values.toList()
    }
}