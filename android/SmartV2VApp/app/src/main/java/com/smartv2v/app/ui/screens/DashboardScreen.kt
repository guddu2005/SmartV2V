package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.telemetry.TelemetryTransmissionService
import com.smartv2v.app.ui.navigation.Screen
import com.smartv2v.app.ui.theme.*

@Composable
fun DashboardScreen(
    transmissionService: TelemetryTransmissionService,
    onNavigate: (String) -> Unit
) {
    val monitoringState by transmissionService.monitoringState.collectAsState()
    val sensorState by transmissionService.sensorManagerService.sensorState.collectAsState()
    val locationState by transmissionService.locationManagerService.locationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoLightBg)
            .padding(16.dp)
    ) {
        Text(
            text = "VEHICLE TELEMETRY DASHBOARD",
            color = NeoObsidian,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // High Risk Siren Alert Banner on Dashboard
        if (monitoringState.currentRiskLevel == "HIGH") {
            Card(
                colors = CardDefaults.cardColors(containerColor = AlertRedBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Siren Alert",
                        tint = AlertRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "🚨 EMERGENCY ALARM: HIGH COLLISION RISK!",
                        color = AlertRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Start / Stop Monitoring Hero Control
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (monitoringState.isMonitoring) EmeraldAccent.copy(alpha = 0.15f) else NeoSurfaceVariant,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (monitoringState.isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (monitoringState.isMonitoring) AlertRed else EmeraldAccent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (monitoringState.isMonitoring) {
                            transmissionService.stopMonitoring()
                        } else {
                            transmissionService.startMonitoring()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (monitoringState.isMonitoring) AlertRed else NeoObsidian
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (monitoringState.isMonitoring) "STOP MONITORING" else "START MONITORING",
                        color = NeoCardSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status: ${monitoringState.lastTransmissionStatus}",
                    color = NeoTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Kinematics Overview Card
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LIVE SENSOR SNAPSHOT",
                    color = NeoObsidian,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricBox("Speed", "%.1f km/h".format(locationState.speed))
                    MetricBox("Heading", "%.0f°".format(locationState.heading))
                    MetricBox("Accel Mag", "%.2f".format(
                        kotlin.math.sqrt(sensorState.accelX * sensorState.accelX + sensorState.accelY * sensorState.accelY + sensorState.accelZ * sensorState.accelZ)
                    ))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Risk Quick Status Card
        val riskColor = when (monitoringState.currentRiskLevel) {
            "HIGH" -> AlertRed
            "MEDIUM" -> WarningOrange
            else -> SafeGreen
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Collision Risk: ${monitoringState.currentRiskLevel}",
                            color = riskColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Probability: %.0f%% | TTC: %.1fs".format(
                                monitoringState.currentRiskProbability * 100,
                                monitoringState.ttcToB
                            ),
                            color = NeoTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = { onNavigate(Screen.CollisionRisk.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoObsidian)
                ) {
                    Text("RADAR", color = NeoCardSurface, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MetricBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = NeoTextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = NeoObsidian, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
