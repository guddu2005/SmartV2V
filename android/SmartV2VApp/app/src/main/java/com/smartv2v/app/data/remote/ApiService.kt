package com.smartv2v.app.data.remote

import com.smartv2v.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth APIs
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    // Vehicle APIs
    @POST("api/vehicles")
    suspend fun createVehicle(@Body request: CreateVehicleRequest): Response<ApiResponse<VehicleResponse>>

    @GET("api/vehicles/my")
    suspend fun getMyVehicles(): Response<ApiResponse<List<VehicleResponse>>>

    @GET("api/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: Int): Response<ApiResponse<VehicleResponse>>

    // Telemetry APIs
    @POST("api/telemetry/vehicle/{vehicleId}")
    suspend fun sendTelemetry(
        @Path("vehicleId") vehicleId: Int,
        @Body request: TelemetryRequest
    ): Response<ApiResponse<TelemetryResponse>>

    @GET("api/telemetry/vehicle/{vehicleId}/latest")
    suspend fun getLatestTelemetry(@Path("vehicleId") vehicleId: Int): Response<ApiResponse<TelemetryResponse>>

    // Collision Risk API
    @GET("api/risk/latest")
    suspend fun getCollisionRisk(
        @Query("vehicleA") vehicleAId: Int,
        @Query("vehicleB") vehicleBId: Int
    ): Response<ApiResponse<RiskResponse>>
}
