package com.smartv2v.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MLPredictionRequest(
    val distance: Double,
    @SerialName("relative_speed") val relativeSpeed: Double,
    @SerialName("relative_acceleration") val relativeAcceleration: Double,
    @SerialName("heading_difference") val headingDifference: Double,
    @SerialName("acceleration_magnitude") val accelerationMagnitude: Double,
    @SerialName("gyroscope_magnitude") val gyroscopeMagnitude: Double,
    val ttc: Double,
    @SerialName("speed_a") val speedA: Double,
    @SerialName("speed_b") val speedB: Double
)

@Serializable
data class MLPredictionResponse(
    val prediction: Int,
    @SerialName("risk_probability") val riskProbability: Double,
    @SerialName("risk_percentage") val riskPercentage: Int,
    @SerialName("risk_level") val riskLevel: String
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
