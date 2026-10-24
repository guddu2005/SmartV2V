package com.smartv2v

import com.smartv2v.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val env = applicationEngineEnvironment {
        connector {
            host = "0.0.0.0"
            this.port = port
        }
        module(Application::module)
    }
    embeddedServer(Netty, env).start(wait = true)
}

fun Application.module() {
    configureDatabases()
    configureSecurity()
    configureSerialization()
    configureHTTP()
    configureRouting()
}
