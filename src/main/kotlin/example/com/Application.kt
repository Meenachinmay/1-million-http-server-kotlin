package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

fun main() {
    val cpuCount = Runtime.getRuntime().availableProcessors()
    val dispatcher = Executors.newFixedThreadPool(cpuCount * 2).asCoroutineDispatcher()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module(dispatcher)
    }.start(wait = true)
}

fun Application.module(dispatcher: kotlinx.coroutines.CoroutineDispatcher) {
    configureSerialization()
    configureRouting(dispatcher)
    configureDatabase()
}