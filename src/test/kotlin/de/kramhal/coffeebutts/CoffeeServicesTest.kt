package de.kramhal.coffeebutts

import de.kramhal.coffeebutts.Consumer.fast
import de.kramhal.coffeebutts.Consumer.slow
import de.kramhal.coffeebutts.FlowOperator.buffered
import de.kramhal.coffeebutts.FlowOperator.none
import de.kramhal.coffeebutts.Producer.concurrent
import de.kramhal.coffeebutts.Producer.sequential
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

/**
 * This example takes a look into two different approaches for typical problems with asynchronous proccessing of stream-data in kotlin:
 * - asynchronous processing using channels
 * - asynchronous processing using flows
 *
 * Problem #1 - Reading of data is slow on producer-side, because:
 *   a) either it is read from a single source (like HDD)
 *   b) or it is read from multiple slow sources (like different rest-endpoints)
 * Problem #2 - given we have only one consumer available, consuming data is:
 *   a) either slow
 *   b) or fast
 *
 * Solving (1b) can be solved by using multiple coroutines, while with (1a) this approach is not possible.
 * If only one slow consumer is available (2a), than the only option is parallelize reading and consuming,
 * so they aren't blocking each other.
 *
 * The code is only meant to compare channels and flows when trying to solve these problems and should answer.
 * How to using channels solving concurrent-producing?
 * How to provide a flow-operator allowing to split reading and consuming concurrently?
 * How could a flow be constructed for concurrent-producing?
 *
 * The printout is meant for validating the expectations. The demo produces every time the numbers from 1 to 10
 * with a delay of 100ms. The consumer just returns this number, in case of slow with a delay of 100ms. So the
 * expectation would be:
 * - sequential producing + fast consuming = about 1000ms
 * - sequential producing + slow consuming = about 2000ms
 * - sequential producing + parallel slow consuming = 1100ms
 * - concurrently producing + slow consuming = 1100ms
 * - concurrently producing + fast consuming = 100ms
 */

/** only for simulating long execution time */
suspend fun longRunningTask(i: Int): Int { delay(100); return i }

enum class Producer {
    /** Data is emitted sequentially (like reading entries from an sql-table). */
    sequential,
    /** Data is emitted concurrently (like different web-requests where we wait for an answer). */
    concurrent
}
enum class Consumer {
    /** Processing Data without extra costs. */
    fast,
    /** Processing Data involves expensive calculations. */
    slow
}
enum class FlowOperator {
    /** data is processed sequentially by the flow */
    none,
    /** producing and processing data is done concurrently by using two different flows */
    buffered
}

class Channels(
        private val consumer: Consumer = slow
) {

    suspend fun executionTime() = measureTimeMillis {
        coroutineScope {
            val channel = Channel<Int>()
            val all = (1..10).map { async { channel.send(longRunningTask(it)) } }
            launch { all.awaitAll(); channel.close() }
            val flow = flow {
                channel.consumeEach {
                    emit( if(consumer == fast) it else longRunningTask(it) )
                }
            }
            launch { flow.collect { println(it) } }
        }
    }
}

class Flows(
        private val producer: Producer = sequential,
        private val consumer: Consumer = slow,
        private val flowOperator: FlowOperator = none
){
    private suspend fun producer() = when(producer) {
        sequential -> flow { (1..10).forEach { emit(longRunningTask(it)) } }
        concurrent -> channelFlow { (1..10).map { launch { send(longRunningTask(it)) } } }
    }
    private suspend fun consumer(ints: Flow<Int>) = when(consumer) {
        slow -> ints.collect { println(longRunningTask(it)) }
        fast -> ints.collect { println(it) }
    }

    suspend fun executionTime() = measureTimeMillis {
        when(flowOperator) {
            none -> consumer(producer())
            buffered -> consumer(producer().buffer())
        }
    }

}

fun main() {
    println(
        """
            Channels with concurrent slow producer and:
              - slow Consumer:                                 ${runBlocking { Channels(consumer = slow).executionTime() }}
              - fast Consumer:                                 ${runBlocking { Channels(consumer = fast).executionTime() }}
            Flows
              - sequential slow producer and:
                - slow consumer                       ${runBlocking { Flows(producer = sequential, consumer = slow, flowOperator = none).executionTime() }}
                - slow consumer running concurrently  ${runBlocking { Flows(producer = sequential, consumer = slow, flowOperator = buffered).executionTime() }}
                - fast consumer                       ${runBlocking { Flows(producer = sequential, consumer = fast, flowOperator = none).executionTime() }}
                - fast consumer running concurrently  ${runBlocking { Flows(producer = sequential, consumer = fast, flowOperator = buffered).executionTime() }}
              - concurrent slow producer and:
                - slow consumer                       ${runBlocking { Flows(producer = concurrent, consumer = slow, flowOperator = none).executionTime() }}
                - slow consumer running concurrently  ${runBlocking { Flows(producer = concurrent, consumer = slow, flowOperator = buffered).executionTime() }}
                - fast consumer                       ${runBlocking { Flows(producer = concurrent, consumer = fast, flowOperator = none).executionTime() }}
                - fast consumer running concurrently  ${runBlocking { Flows(producer = concurrent, consumer = fast, flowOperator = buffered).executionTime() }}
        """.trimIndent()
    )
}