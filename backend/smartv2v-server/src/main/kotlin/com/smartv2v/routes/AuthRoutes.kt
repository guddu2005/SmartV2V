package com.smartv2v.routes

import com.smartv2v.dto.ApiResponse
import com.smartv2v.dto.LoginRequest
import com.smartv2v.dto.RegisterRequest
import com.smartv2v.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userService: UserService) {
    route("/api/auth") {
        
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val response = userService.register(request)
                call.respond(HttpStatusCode.Created, ApiResponse(
                    success = true,
                    message = "User registered successfully",
                    data = response
                ))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(
                    success = false,
                    message = e.message ?: "Invalid registration request"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(
                    success = false,
                    message = "Registration failed: ${e.localizedMessage}"
                ))
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val response = userService.login(request)
                call.respond(HttpStatusCode.OK, ApiResponse(
                    success = true,
                    message = "Login successful",
                    data = response
                ))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(
                    success = false,
                    message = e.message ?: "Invalid email or password"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(
                    success = false,
                    message = "Login failed: ${e.localizedMessage}"
                ))
            }
        }
    }
}
