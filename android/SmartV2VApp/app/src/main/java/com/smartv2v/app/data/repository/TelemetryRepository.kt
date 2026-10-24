package com.smartv2v.app.data.repository

import com.smartv2v.app.data.model.TelemetryRequest
import com.smartv2v.app.data.model.TelemetryResponse
import com.smartv2v.app.data.remote.ApiClient

class TelemetryRepository {

    suspend fun sendTelemetry(vehicleId: Int, request: TelemetryRequest): Result<TelemetryResponse> {
        return try {
            val response = ApiClient.getService().sendTelemetry(vehicleId, request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Telemetry upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestTelemetry(vehicleId: Int): Result<TelemetryResponse> {
        return try {
            val response = ApiClient.getService().getLatestTelemetry(vehicleId)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Failed to fetch telemetry"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
