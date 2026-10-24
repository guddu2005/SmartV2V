package com.smartv2v.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: String
)

@Serializable
data class CreateVehicleRequest(
    val vehicleNumber: String,
    val deviceId: String
)

@Serializable
data class VehicleResponse(
    val id: Int,
    val vehicleNumber: String,
    val deviceId: String,
    val active: Boolean,
    val userId: Int,
    val createdAt: String
)

@Serializable
data class TelemetryRequest(
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val heading: Double,
    val accelerationX: Double,
    val accelerationY: Double,
    val accelerationZ: Double,
    val gyroscopeX: Double,
    val gyroscopeY: Double,
    val gyroscopeZ: Double
)

@Serializable
data class TelemetryResponse(
    val id: Long,
    val vehicleId: Int,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val heading: Double,
    val accelerationX: Double,
    val accelerationY: Double,
    val accelerationZ: Double,
    val gyroscopeX: Double,
    val gyroscopeY: Double,
    val gyroscopeZ: Double,
    val timestamp: String
)

@Serializable
data class RiskResponse(
    val id: Long? = null,
    val vehicleAId: Int,
    val vehicleBId: Int,
    val distance: Double,
    val relativeSpeed: Double,
    val relativeAcceleration: Double,
    val headingDifference: Double,
    val accelerationMagnitude: Double,
    val gyroscopeMagnitude: Double,
    val ttc: Double,
    val riskProbability: Double,
    val riskLevel: String,
    val timestamp: String
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
