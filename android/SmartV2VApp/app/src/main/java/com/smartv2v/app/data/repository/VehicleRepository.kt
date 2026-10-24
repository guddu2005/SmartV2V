package com.smartv2v.app.data.repository

import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.data.model.CreateVehicleRequest
import com.smartv2v.app.data.model.VehicleResponse
import com.smartv2v.app.data.remote.ApiClient

class VehicleRepository {

    suspend fun createVehicle(vehicleNumber: String, deviceId: String): Result<VehicleResponse> {
        return try {
            val response = ApiClient.getService().createVehicle(CreateVehicleRequest(vehicleNumber, deviceId))
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                TokenManager.saveActiveVehicleId(body.data.id)
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Vehicle creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyVehicles(): Result<List<VehicleResponse>> {
        return try {
            val response = ApiClient.getService().getMyVehicles()
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to fetch vehicles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
