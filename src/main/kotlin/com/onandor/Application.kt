package com.onandor

import com.onandor.dao.DatabaseFactory
import com.onandor.plugins.*
import com.onandor.routes.configureNoteRoutes
import io.ktor.server.application.*
import io.ktor.server.application.*
import io.ktor.server.resources.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init(environment)
    configureSecurity()
    configureHTTP()
    configureMonitoring()
    configureSerialization()

    // Routing
    install(Resources)
    configureNoteRoutes()
}
