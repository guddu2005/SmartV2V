package com.smartv2v.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.telemetry.TelemetryTransmissionService
import com.smartv2v.app.ui.theme.*

@Composable
fun CollisionRiskScreen(
    transmissionService: TelemetryTransmissionService,
    onBack: (() -> Unit)? = null
) {
    val monitoringState by transmissionService.monitoringState.collectAsState()

    val riskLevel = monitoringState.currentRiskLevel
    val riskProbPct = (monitoringState.currentRiskProbability * 100).toInt()
    val distance = monitoringState.distanceToVehicleB
    val relSpeed = monitoringState.relativeSpeedToB
    val ttc = monitoringState.ttcToB

    val riskColor = when (riskLevel) {
        "HIGH" -> AlertRed
        "MEDIUM" -> WarningOrange
        else -> SafeGreen
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoLightBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = NeoObsidian
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "V2V COLLISION RISK RADAR",
                color = NeoObsidian,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High Risk Siren Alert Banner
        if (riskLevel == "HIGH") {
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
                        contentDescription = "Siren Active",
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

        // Circular Radar Visualization Card
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = this.center
                        val radius = size.minDimension / 2.0f

                        // Concentric circles
                        drawCircle(color = NeoCardBorder, radius = radius, style = Stroke(width = 2.dp.toPx()))
                        drawCircle(color = NeoCardBorder, radius = radius * 0.66f, style = Stroke(width = 1.5.dp.toPx()))
                        drawCircle(color = NeoCardBorder, radius = radius * 0.33f, style = Stroke(width = 1.dp.toPx()))

                        // Center vehicle A indicator
                        drawCircle(color = NeoObsidian, radius = 8.dp.toPx())

                        // Vehicle B indicator if monitoring
                        if (monitoringState.isMonitoring) {
                            val offsetY = -radius * 0.45f
                            drawCircle(color = riskColor, radius = 10.dp.toPx(), center = center.copy(y = center.y + offsetY))
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (monitoringState.isMonitoring) {
                            Text(
                                text = "${riskProbPct}%",
                                color = riskColor,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = riskLevel,
                                color = riskColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "OFFLINE",
                                color = NeoTextSecondary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!monitoringState.isMonitoring && monitoringState.transmissionCount == 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Risk prediction unavailable.\nStart monitoring on Dashboard to calculate risk.",
                        color = NeoTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Kinematics Feature Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "FEATURE BREAKDOWN",
                        color = NeoObsidian,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RiskMetricRow("Target Vehicle", "Vehicle #${monitoringState.targetVehicleBId}")
                    RiskMetricRow("V2V Distance", "%.2f meters".format(distance))
                    RiskMetricRow("Relative Speed", "%.2f km/h".format(relSpeed))
                    RiskMetricRow("Time-to-Collision (TTC)", if (ttc >= 999.0) "Safe (>999s)" else "%.2f sec".format(ttc))
                    RiskMetricRow("ML Risk Level", riskLevel)
                }
            }
        }
    }
}

@Composable
fun RiskMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = NeoTextSecondary, fontSize = 13.sp)
        Text(value, color = NeoTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
