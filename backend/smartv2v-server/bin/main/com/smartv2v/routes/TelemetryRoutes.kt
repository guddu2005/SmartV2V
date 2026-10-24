package com.smartv2v.routes

import com.smartv2v.dto.ApiResponse
import com.smartv2v.dto.TelemetryRequest
import com.smartv2v.services.TelemetryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.telemetryRoutes(telemetryService: TelemetryService) {
    authenticate("auth-jwt") {
        route("/api/telemetry") {

            post("/vehicle/{vehicleId}") {
                try {
                    val vehicleId = call.parameters["vehicleId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, "Invalid vehicle ID"))

                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asInt()

                    val request = call.receive<TelemetryRequest>()
                    val response = telemetryService.saveTelemetry(userId, vehicleId, request)

                    call.respond(HttpStatusCode.Created, ApiResponse(
                        success = true,
                        message = "Telemetry data recorded successfully",
                        data = response
                    ))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, e.localizedMessage ?: "Forbidden"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, "Failed to store telemetry: ${e.localizedMessage}"))
                }
            }

            get("/vehicle/{vehicleId}/latest") {
                try {
                    val vehicleId = call.parameters["vehicleId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, "Invalid vehicle ID"))

                    val telemetry = telemetryService.getLatestTelemetry(vehicleId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "No telemetry found for vehicle"))

                    call.respond(HttpStatusCode.OK, ApiResponse(
                        success = true,
                        message = "Latest telemetry fetched",
                        data = telemetry
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(false, e.localizedMessage ?: "Error"))
                }
            }
        }
    }
}
