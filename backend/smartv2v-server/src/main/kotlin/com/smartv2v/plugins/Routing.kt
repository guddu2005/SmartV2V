package com.smartv2v.plugins

import com.smartv2v.routes.authRoutes
import com.smartv2v.routes.riskRoutes
import com.smartv2v.routes.telemetryRoutes
import com.smartv2v.routes.vehicleRoutes
import com.smartv2v.services.MLClientService
import com.smartv2v.services.RiskService
import com.smartv2v.services.TelemetryService
import com.smartv2v.services.UserService
import com.smartv2v.services.VehicleService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val mlUrl = environment.config.propertyOrNull("ml.serviceUrl")?.getString() ?: "http://localhost:8000/predict"

    val userService = UserService()
    val vehicleService = VehicleService()
    val telemetryService = TelemetryService(vehicleService)
    val mlClientService = MLClientService(mlUrl)
    val riskService = RiskService(telemetryService, mlClientService)

    routing {
        get("/") {
            call.respondText("SmartV2V Ktor Backend Server is Running!", ContentType.Text.Plain)
        }

        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }

        authRoutes(userService)
        vehicleRoutes(vehicleService)
        telemetryRoutes(telemetryService)
        riskRoutes(riskService)
    }
}
