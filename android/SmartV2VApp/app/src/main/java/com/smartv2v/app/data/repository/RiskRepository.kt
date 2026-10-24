package com.smartv2v.app.data.repository

import com.smartv2v.app.data.model.RiskResponse
import com.smartv2v.app.data.remote.ApiClient

class RiskRepository {

    suspend fun getCollisionRisk(vehicleAId: Int, vehicleBId: Int): Result<RiskResponse> {
        return try {
            val response = ApiClient.getService().getCollisionRisk(vehicleAId, vehicleBId)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Collision risk check failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
