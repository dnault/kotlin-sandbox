package com.couchbase.client.dcp

import com.couchbase.client.dcp.internal.netty.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() {
    System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)



    println(doIt2())
}


fun doIt2() = runBlocking {

    val time = measureTimeMillis {
        val one = async(CoroutineName("doot")) { doSomethingOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingTwo() }
        println("${one.await() + two.await()}")
    }
    println("Took $time ms")

    return@runBlocking "DOOOONE"
}

suspend fun doSomethingOne(): Int {
    delay(1000)
    return 13
}

suspend fun doSomethingTwo(): Int {
    delay(1000)
    return 29
}


fun doIt() = runBlocking {


    withTimeout(1300) {
        repeat(1000) { i ->
            println("I'm sleeping ${i}")
            delay(500)
        }
    }

//
//    val startTime = System.currentTimeMillis()
//    val job = launch {
//        var nextPrintTime = startTime
//        var i = 0
//        while (isActive) {
////            try {
////                yield()
////            } catch (t: CancellationException) {
////                println("Oh no, I've been cancelled!")
////                throw t
////            }
//            if (System.currentTimeMillis() >= nextPrintTime) {
//                println("job: I'm sleeping ${i++}")
//                nextPrintTime += 500
//            }
//        }
//
//    }
//    delay(1300)
//    println("main: I'm tired of waiting")
//    job.cancelAndJoin()
//    println("main: Now I can quit")


}
