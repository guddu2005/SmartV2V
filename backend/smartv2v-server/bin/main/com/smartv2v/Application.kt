package com.smartv2v

import com.smartv2v.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val env = applicationEngineEnvironment {
        connector {
            host = "0.0.0.0"
            port = 8080
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
