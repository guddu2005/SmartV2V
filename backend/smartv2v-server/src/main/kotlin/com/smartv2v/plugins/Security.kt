package com.smartv2v.plugins

import com.smartv2v.utils.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val realmConfig = environment.config.propertyOrNull("jwt.realm")?.getString() ?: "SmartV2V Backend Server"

    authentication {
        jwt("auth-jwt") {
            realm = realmConfig
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
