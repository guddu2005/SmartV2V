package com.smartv2v.routes

import com.smartv2v.dto.ApiResponse
import com.smartv2v.dto.CreateVehicleRequest
import com.smartv2v.dto.UpdateVehicleRequest
import com.smartv2v.services.VehicleService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.vehicleRoutes(vehicleService: VehicleService) {
    authenticate("auth-jwt") {
        route("/api/vehicles") {

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asInt()

                    val request = call.receive<CreateVehicleRequest>()
                    val vehicle = vehicleService.createVehicle(userId, request)

                    call.respond(HttpStatusCode.Created, ApiResponse(
                        success = true,
                        message = "Vehicle registered successfully",
                        data = vehicle
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(
                        success = false,
                        message = "Failed to create vehicle: ${e.localizedMessage}"
                    ))
                }
            }

            get("/my") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asInt()

                    val vehicles = vehicleService.getVehiclesByUserId(userId)
                    call.respond(HttpStatusCode.OK, ApiResponse(
                        success = true,
                        message = "Retrieved user vehicles",
                        data = vehicles
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(
                        success = false,
                        message = "Failed to fetch vehicles: ${e.localizedMessage}"
                    ))
                }
            }

            get("/{id}") {
                try {
                    val vehicleId = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, "Invalid vehicle ID"))

                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asInt()

                    val vehicle = vehicleService.getVehicleById(vehicleId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Vehicle not found"))

                    if (vehicle.userId != userId) {
                        return@get call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, "Unauthorized vehicle access"))
                    }

                    call.respond(HttpStatusCode.OK, ApiResponse(
                        success = true,
                        message = "Vehicle retrieved",
                        data = vehicle
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(false, e.localizedMessage ?: "Error"))
                }
            }

            put("/{id}") {
                try {
                    val vehicleId = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, "Invalid vehicle ID"))

                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asInt()

                    val request = call.receive<UpdateVehicleRequest>()
                    val updated = vehicleService.updateVehicle(userId, vehicleId, request)

                    call.respond(HttpStatusCode.OK, ApiResponse(
                        success = true,
                        message = "Vehicle updated",
                        data = updated
                    ))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, e.localizedMessage ?: "Forbidden"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, e.localizedMessage ?: "Not found"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(false, e.localizedMessage ?: "Error"))
                }
            }

            delete("/{id}") {
                try {
                    val vehicleId = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, "Invalid vehicle ID"))

                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asInt()

                    val success = vehicleService.deleteVehicle(userId, vehicleId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, ApiResponse(true, "Vehicle deleted successfully", data = true))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Vehicle not found or deletion failed", data = false))
                    }
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, e.localizedMessage ?: "Forbidden"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(false, e.localizedMessage ?: "Error"))
                }
            }
        }
    }
}
