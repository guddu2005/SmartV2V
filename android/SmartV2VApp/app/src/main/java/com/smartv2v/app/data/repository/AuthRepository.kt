package com.smartv2v.app.data.repository

import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.data.model.AuthResponse
import com.smartv2v.app.data.model.LoginRequest
import com.smartv2v.app.data.model.RegisterRequest
import com.smartv2v.app.data.remote.ApiClient

class AuthRepository {

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = ApiClient.getService().register(RegisterRequest(name, email, password))
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                val authData = body.data
                TokenManager.saveAuth(authData.token, authData.user.name, authData.user.email)
                Result.success(authData)
            } else {
                Result.failure(Exception(body?.message ?: "Registration failed (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = ApiClient.getService().login(LoginRequest(email, password))
            val body = response.body()
            if (response.isSuccessful && body != null && body.success && body.data != null) {
                val authData = body.data
                TokenManager.saveAuth(authData.token, authData.user.name, authData.user.email)
                Result.success(authData)
            } else {
                Result.failure(Exception(body?.message ?: "Login failed (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        TokenManager.clearSession()
    }
}
