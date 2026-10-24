package com.smartv2v.app.telemetry

import android.content.Context
import com.smartv2v.app.audio.SirenAlertManager
import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.data.model.TelemetryRequest
import com.smartv2v.app.data.repository.RiskRepository
import com.smartv2v.app.data.repository.TelemetryRepository
import com.smartv2v.app.location.LocationManagerService
import com.smartv2v.app.sensors.SensorManagerService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MonitoringState(
    val isMonitoring: Boolean = false,
    val lastTransmissionStatus: String = "Idle",
    val transmissionCount: Int = 0,
    val targetVehicleBId: Int = 2,
    val currentRiskProbability: Double = 0.0,
    val currentRiskLevel: String = "LOW",
    val distanceToVehicleB: Double = 0.0,
    val relativeSpeedToB: Double = 0.0,
    val ttcToB: Double = 999.0
)

class TelemetryTransmissionService(
    private val context: Context,
    private val telemetryRepository: TelemetryRepository = TelemetryRepository(),
    private val riskRepository: RiskRepository = RiskRepository()
) {

    val sensorManagerService = SensorManagerService(context)
    val locationManagerService = LocationManagerService(context)
    val sirenAlertManager = SirenAlertManager(context)

    private val _monitoringState = MutableStateFlow(MonitoringState())
    val monitoringState: StateFlow<MonitoringState> = _monitoringState.asStateFlow()

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startMonitoring(intervalMs: Long = 1000L, targetVehicleBId: Int = 2) {
        if (_monitoringState.value.isMonitoring) return

        sensorManagerService.startListening()
        locationManagerService.startLocationUpdates(intervalMs)

        _monitoringState.value = _monitoringState.value.copy(
            isMonitoring = true,
            targetVehicleBId = targetVehicleBId,
            lastTransmissionStatus = "Monitoring Active..."
        )

        job = scope.launch {
            while (isActive && _monitoringState.value.isMonitoring) {
                val activeVehicleId = TokenManager.getActiveVehicleId()
                if (activeVehicleId != -1) {
                    val sensor = sensorManagerService.sensorState.value
                    val location = locationManagerService.locationState.value

                    val req = TelemetryRequest(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        speed = location.speed,
                        heading = location.heading,
                        accelerationX = sensor.accelX,
                        accelerationY = sensor.accelY,
                        accelerationZ = sensor.accelZ,
                        gyroscopeX = sensor.gyroX,
                        gyroscopeY = sensor.gyroY,
                        gyroscopeZ = sensor.gyroZ
                    )

                    val sendResult = telemetryRepository.sendTelemetry(activeVehicleId, req)

                    if (sendResult.isSuccess) {
                        val newCount = _monitoringState.value.transmissionCount + 1

                        // Query risk against target vehicle B if available
                        val riskResult = riskRepository.getCollisionRisk(activeVehicleId, targetVehicleBId)
                        val riskData = riskResult.getOrNull()

                        val newRiskLevel = riskData?.riskLevel ?: "LOW"
                        sirenAlertManager.updateRiskLevel(newRiskLevel)

                        _monitoringState.value = _monitoringState.value.copy(
                            lastTransmissionStatus = "Synced #${newCount}",
                            transmissionCount = newCount,
                            currentRiskProbability = riskData?.riskProbability ?: 0.0,
                            currentRiskLevel = newRiskLevel,
                            distanceToVehicleB = riskData?.distance ?: 0.0,
                            relativeSpeedToB = riskData?.relativeSpeed ?: 0.0,
                            ttcToB = riskData?.ttc ?: 999.0
                        )
                    } else {
                        _monitoringState.value = _monitoringState.value.copy(
                            lastTransmissionStatus = "Transmission Error: ${sendResult.exceptionOrNull()?.message}"
                        )
                    }
                } else {
                    _monitoringState.value = _monitoringState.value.copy(
                        lastTransmissionStatus = "No Active Vehicle Selected"
                    )
                }

                delay(intervalMs)
            }
        }
    }

    fun stopMonitoring() {
        job?.cancel()
        job = null
        sirenAlertManager.stopSiren()
        sensorManagerService.stopListening()
        locationManagerService.stopLocationUpdates()
        _monitoringState.value = _monitoringState.value.copy(
            isMonitoring = false,
            lastTransmissionStatus = "Monitoring Stopped"
        )
    }
}
