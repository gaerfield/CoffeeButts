package de.kramhal.coffeebutts

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlin.system.measureTimeMillis

suspend fun longRunningCalculation(i: Int): Int { delay(100); return i }
suspend fun longRunningProcessing(i: Int): Int { delay(100); return i }

suspend fun theChannelWay()= measureTimeMillis {
    coroutineScope {
        val channel = Channel<Int>()
        val all = (1..10).map { async { channel.send(longRunningCalculation(it)) } }
        launch { all.awaitAll(); channel.close() }
        val flow = flow { channel.consumeEach { emit(longRunningProcessing(it)) } }
        launch { flow.collect { println(it) } }
    }
}

suspend fun emitter() = flow { (1..10).forEach { emit(longRunningCalculation(it)) } }
suspend fun collector(ints: Flow<Int>) = ints.collect { println(longRunningProcessing(it)) }

suspend fun theLongRunningEasyWay()= measureTimeMillis {
    collector(emitter())
}

fun <T> Flow<T>.buffer(size: Int = 0): Flow<T> = flow {
    coroutineScope {
        val channel = produce(capacity = size) {
            collect { send(it) }
        }
        channel.consumeEach { emit(it) }
    }
}

suspend fun theFancyOperatorWay() = measureTimeMillis {
    emitter().buffer().collect { println(longRunningProcessing(it)) }
}

fun main() {
    println(
        """
            using Channels:              ${ runBlocking { theChannelWay() } }
            flows Without Operator:      ${ runBlocking { theLongRunningEasyWay() } }
            flows using custom Operator: ${ runBlocking { theFancyOperatorWay() } }
        """.trimIndent()
    )
}