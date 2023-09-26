package com.onandor

import com.onandor.dao.DatabaseFactory
import com.onandor.plugins.*
import com.onandor.routes.configureNoteRoutes
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init()
    configureSecurity()
    configureHTTP()
    configureMonitoring()
    configureSerialization()

    // Routing
    configureNoteRoutes()
}
