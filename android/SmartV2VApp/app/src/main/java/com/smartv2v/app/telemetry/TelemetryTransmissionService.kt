package com.smartv2v.app.telemetry

import android.content.Context
import com.smartv2v.app.audio.VoiceAlertManager
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
import kotlin.math.max
import kotlin.math.min

data class MonitoringState(
    val isMonitoring: Boolean = false,
    val lastTransmissionStatus: String = "Idle",
    val transmissionCount: Int = 0,
    val targetVehicleBId: Int = 2,
    val currentRiskProbability: Double = 0.0,
    val currentRiskLevel: String = "LOW",
    val distanceToVehicleB: Double = 0.0,
    val relativeSpeedToB: Double = 0.0,
    val ttcToB: Double = 999.0,
    // Feature 4: Explainable AI / Primary Hazard Reason
    val primaryHazardReason: String = "Clear Safe Buffer Zone",
    // Feature 5: Driver Safety Score Card (0-100) & Grade Tier
    val safetyScore: Int = 98,
    val safetyGrade: String = "A+",
    val harshEventsCount: Int = 0,
    // Feature 6: Vehicle B Telemetry for Live Map
    val vehicleBLatitude: Double = 0.0,
    val vehicleBLongitude: Double = 0.0,
    val vehicleBHeading: Double = 0.0,
    val vehicleBSpeed: Double = 0.0
)

class TelemetryTransmissionService(
    private val context: Context,
    private val telemetryRepository: TelemetryRepository = TelemetryRepository(),
    private val riskRepository: RiskRepository = RiskRepository()
) {

    val sensorManagerService = SensorManagerService(context)
    val locationManagerService = LocationManagerService(context)
    val voiceAlertManager = VoiceAlertManager(context)

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

                        // Query risk against target vehicle B
                        val riskResult = riskRepository.getCollisionRisk(activeVehicleId, targetVehicleBId)
                        val riskData = riskResult.getOrNull()

                        val newRiskLevel = riskData?.riskLevel ?: "LOW"
                        val currentDistance = riskData?.distance ?: 0.0
                        val currentTtc = riskData?.ttc ?: 999.0
                        val currentRelSpeed = riskData?.relativeSpeed ?: 0.0
                        val currentRelAccel = riskData?.relativeAcceleration ?: 0.0
                        val currentHeadingDiff = riskData?.headingDifference ?: 0.0

                        // Feature 1: Trigger TTS voice guidance when risk is HIGH
                        voiceAlertManager.triggerWarning(newRiskLevel, currentDistance, currentTtc)

                        // Feature 4: Explainable AI (XAI) - Derive Primary Hazard Trigger
                        val hazardExplanation = when {
                            currentTtc in 0.01..2.5 -> "Critical Time-to-Collision (TTC: ${"%.1f".format(currentTtc)}s < 2.5s)"
                            currentDistance in 0.01..25.0 && currentRelSpeed > 15.0 -> "Dangerous Headway Closure (${"%.1f".format(currentRelSpeed)} km/h)"
                            currentRelAccel < -2.5 -> "Severe Lead Deceleration (${"%.1f".format(currentRelAccel)} m/s²)"
                            currentHeadingDiff > 40.0 && currentDistance < 35.0 -> "Angular Intersection Conflict (Δθ: ${"%.0f".format(currentHeadingDiff)}°)"
                            currentDistance in 0.01..20.0 -> "Proximity Safety Buffer Violation (< 20m)"
                            else -> "Safe Headway Margin (Distance: ${"%.1f".format(currentDistance)}m)"
                        }

                        // Feature 5: Dynamic Driver Safety Score calculation
                        var currentScore = _monitoringState.value.safetyScore
                        var harshCount = _monitoringState.value.harshEventsCount
                        if (newRiskLevel == "HIGH") {
                            currentScore = max(30, currentScore - 3)
                            harshCount += 1
                        } else if (newRiskLevel == "MEDIUM") {
                            currentScore = max(40, currentScore - 1)
                        } else if (newCount % 12 == 0) {
                            currentScore = min(100, currentScore + 1) // Safe driving bonus
                        }

                        val grade = when {
                            currentScore >= 90 -> "A+ (Safe Driver)"
                            currentScore >= 75 -> "B (Good Headway)"
                            currentScore >= 60 -> "C (Moderate Caution)"
                            else -> "D (High Risk Profile)"
                        }

                        // Feature 6: Fetch target vehicle B telemetry for Map rendering
                        var vBLat = _monitoringState.value.vehicleBLatitude
                        var vBLon = _monitoringState.value.vehicleBLongitude
                        var vBHeading = _monitoringState.value.vehicleBHeading
                        var vBSpeed = _monitoringState.value.vehicleBSpeed

                        val vBTelemetry = telemetryRepository.getLatestTelemetry(targetVehicleBId).getOrNull()
                        if (vBTelemetry != null && vBTelemetry.latitude != 0.0) {
                            vBLat = vBTelemetry.latitude
                            vBLon = vBTelemetry.longitude
                            vBHeading = vBTelemetry.heading
                            vBSpeed = vBTelemetry.speed
                        } else if (location.latitude != 0.0 && currentDistance > 0.0) {
                            // High precision geographic offset projection ahead
                            val radHeading = Math.toRadians(location.heading)
                            val distKm = currentDistance / 1000.0
                            val deltaLat = (distKm / 111.0) * kotlin.math.cos(radHeading)
                            val deltaLon = (distKm / (111.0 * kotlin.math.cos(Math.toRadians(location.latitude)))) * kotlin.math.sin(radHeading)
                            vBLat = location.latitude + deltaLat
                            vBLon = location.longitude + deltaLon
                            vBHeading = location.heading
                            vBSpeed = max(0.0, location.speed - currentRelSpeed)
                        }

                        _monitoringState.value = _monitoringState.value.copy(
                            lastTransmissionStatus = "Synced #${newCount}",
                            transmissionCount = newCount,
                            currentRiskProbability = riskData?.riskProbability ?: 0.0,
                            currentRiskLevel = newRiskLevel,
                            distanceToVehicleB = currentDistance,
                            relativeSpeedToB = currentRelSpeed,
                            ttcToB = currentTtc,
                            primaryHazardReason = hazardExplanation,
                            safetyScore = currentScore,
                            safetyGrade = grade,
                            harshEventsCount = harshCount,
                            vehicleBLatitude = vBLat,
                            vehicleBLongitude = vBLon,
                            vehicleBHeading = vBHeading,
                            vehicleBSpeed = vBSpeed
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
        voiceAlertManager.stop()
        sensorManagerService.stopListening()
        locationManagerService.stopLocationUpdates()
        _monitoringState.value = _monitoringState.value.copy(
            isMonitoring = false,
            lastTransmissionStatus = "Monitoring Stopped"
        )
    }
}
