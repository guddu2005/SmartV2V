package com.smartv2v.dto

import kotlinx.serialization.Serializable

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
