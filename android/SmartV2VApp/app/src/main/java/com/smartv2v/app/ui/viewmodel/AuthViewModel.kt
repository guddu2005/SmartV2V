package com.smartv2v.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.data.repository.AuthRepository
import com.smartv2v.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val hasRegisteredVehicle: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val vehicleRepository: VehicleRepository = VehicleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun checkAuthStatus() {
        viewModelScope.launch {
            if (TokenManager.isLoggedIn()) {
                _uiState.value = _uiState.value.copy(isAuthenticated = true)
                checkVehicleStatus()
            } else {
                _uiState.value = AuthUiState(isAuthenticated = false)
            }
        }
    }

    private suspend fun checkVehicleStatus() {
        val result = vehicleRepository.getMyVehicles()
        if (result.isSuccess && !result.getOrNull().isNullOrEmpty()) {
            val firstVehicle = result.getOrNull()!!.first()
            TokenManager.saveActiveVehicleId(firstVehicle.id)
            _uiState.value = _uiState.value.copy(hasRegisteredVehicle = true)
        } else {
            _uiState.value = _uiState.value.copy(hasRegisteredVehicle = false)
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.login(email, pass)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                checkVehicleStatus()
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun register(name: String, email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.register(name, email, pass)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true, hasRegisteredVehicle = false)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        authRepository.logout()
        _uiState.value = AuthUiState(isAuthenticated = false, hasRegisteredVehicle = false)
        onSuccess()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
