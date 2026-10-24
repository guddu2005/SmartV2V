package com.smartv2v.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateVehicleRequest(
    val vehicleNumber: String,
    val deviceId: String
)

@Serializable
data class UpdateVehicleRequest(
    val vehicleNumber: String? = null,
    val deviceId: String? = null,
    val active: Boolean? = null
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
