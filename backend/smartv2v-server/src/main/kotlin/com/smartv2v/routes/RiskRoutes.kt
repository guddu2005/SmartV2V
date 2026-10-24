package com.smartv2v.routes

import com.smartv2v.dto.ApiResponse
import com.smartv2v.services.RiskService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.riskRoutes(riskService: RiskService) {
    authenticate("auth-jwt") {
        route("/api/risk") {

            get("/latest") {
                try {
                    val vehicleAId = call.request.queryParameters["vehicleA"]?.toIntOrNull()
                    val vehicleBId = call.request.queryParameters["vehicleB"]?.toIntOrNull()

                    if (vehicleAId == null || vehicleBId == null) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Unit>(false, "Both vehicleA and vehicleB query parameters are required.")
                        )
                    }

                    if (vehicleAId == vehicleBId) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Unit>(false, "Vehicle A and Vehicle B must be different vehicles.")
                        )
                    }

                    val riskResult = riskService.calculateAndStoreRisk(vehicleAId, vehicleBId)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Collision risk calculated successfully",
                            data = riskResult
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(false, e.localizedMessage ?: "Invalid parameters")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Unit>(false, "Risk estimation error: ${e.localizedMessage}")
                    )
                }
            }
        }
    }
}
