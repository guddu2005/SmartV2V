package com.smartv2v.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val SECRET = "smartv2v-secret-jwt-key-2026-super-secure"
    private const val ISSUER = "http://0.0.0.0:8080/"
    private const val AUDIENCE = "http://0.0.0.0:8080/v2v"
    private const val VALIDITY_IN_MS = 3600_000 * 24 * 7 // 7 days

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(userId: Int, email: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_IN_MS))
            .sign(algorithm)
    }
}
