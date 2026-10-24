package com.smartv2v.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.data.model.VehicleResponse
import com.smartv2v.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VehicleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val vehicles: List<VehicleResponse> = emptyList(),
    val activeVehicle: VehicleResponse? = null
)

class VehicleViewModel(
    private val vehicleRepository: VehicleRepository = VehicleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    fun loadUserVehicles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = vehicleRepository.getMyVehicles()
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                val activeId = TokenManager.getActiveVehicleId()
                val active = list.find { it.id == activeId } ?: list.firstOrNull()
                
                if (active != null) {
                    TokenManager.saveActiveVehicleId(active.id)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    vehicles = list,
                    activeVehicle = active
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load vehicles"
                )
            }
        }
    }

    fun registerVehicle(vehicleNumber: String, deviceId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = vehicleRepository.createVehicle(vehicleNumber, deviceId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                loadUserVehicles()
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Vehicle registration failed"
                )
            }
        }
    }

    fun selectActiveVehicle(vehicle: VehicleResponse) {
        TokenManager.saveActiveVehicleId(vehicle.id)
        _uiState.value = _uiState.value.copy(activeVehicle = vehicle)
    }
}
