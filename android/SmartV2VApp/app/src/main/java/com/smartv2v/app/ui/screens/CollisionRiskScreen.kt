package com.smartv2v.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.telemetry.TelemetryTransmissionService
import com.smartv2v.app.ui.components.InteractiveV2VMapView
import com.smartv2v.app.ui.theme.*
import kotlin.math.*

@Composable
fun CollisionRiskScreen(
    transmissionService: TelemetryTransmissionService,
    onBack: (() -> Unit)? = null
) {
    val monitoringState by transmissionService.monitoringState.collectAsState()
    val locationState by transmissionService.locationManagerService.locationState.collectAsState()
    val scrollState = rememberScrollState()

    var selectedViewMode by remember { mutableStateOf(0) } // 0 = 360 Radar, 1 = Live Map

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

    // Infinite animation for scanning radar beam
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoLightBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NeoObsidian,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = "V2V COLLISION RADAR",
                        color = NeoObsidian,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "AI-Driven Proximity Guidance",
                        color = NeoTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Live status badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (monitoringState.isMonitoring) SafeGreenBg else NeoSurfaceVariant,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = if (monitoringState.isMonitoring) SafeGreen.copy(alpha = 0.4f) else NeoCardBorder,
                    shape = RoundedCornerShape(20.dp)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (monitoringState.isMonitoring) SafeGreen else NeoTextSecondary,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (monitoringState.isMonitoring) "ACTIVE" else "STANDBY",
                        color = if (monitoringState.isMonitoring) SafeGreen else NeoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // High Risk Siren Alert Banner with Voice Guidance Active Indicator (Feature 1)
        if (riskLevel == "HIGH" && monitoringState.isMonitoring) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AlertRedBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CRITICAL COLLISION WARNING",
                            color = AlertRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Vehicle closing rapidly! Immediate braking required.",
                            color = AlertRed.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Voice ADAS Active",
                        tint = AlertRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // View Mode Switcher: [ 📡 360° Radar ] | [ 🗺️ Interactive Live Map ] (Feature 6)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = NeoSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                // Tab 0: Radar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedViewMode == 0) NeoObsidian else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedViewMode = 0 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = if (selectedViewMode == 0) Color.White else NeoTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "360° RADAR",
                            color = if (selectedViewMode == 0) Color.White else NeoTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tab 1: Live Map
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedViewMode == 1) NeoObsidian else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedViewMode = 1 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = if (selectedViewMode == 1) Color.White else NeoTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE MAP",
                            color = if (selectedViewMode == 1) Color.White else NeoTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // VIEW 0: 360° Spatial Radar
        if (selectedViewMode == 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TACTICAL RADAR",
                            color = NeoObsidian,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Radius: 100m",
                            color = NeoTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Radar Scope
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, Color(0xFF334155), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = this.center
                            val radius = size.minDimension / 2.0f

                            // Crosshairs
                            drawLine(color = Color(0xFF1E293B), start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = 1.dp.toPx())
                            drawLine(color = Color(0xFF1E293B), start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 1.dp.toPx())

                            // Rings
                            drawCircle(color = Color(0xFF1E293B), radius = radius, style = Stroke(width = 1.5.dp.toPx()))
                            drawCircle(color = Color(0xFF1E293B), radius = radius * 0.66f, style = Stroke(width = 1.dp.toPx()))
                            drawCircle(color = Color(0xFF1E293B), radius = radius * 0.33f, style = Stroke(width = 1.dp.toPx()))

                            // Rotating Sweep
                            if (monitoringState.isMonitoring) {
                                val sweepRad = Math.toRadians(sweepAngle.toDouble())
                                val endX = center.x + radius * cos(sweepRad).toFloat()
                                val endY = center.y + radius * sin(sweepRad).toFloat()

                                drawLine(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0x0010B981), Color(0xFF22C55E)),
                                        start = center,
                                        end = Offset(endX, endY)
                                    ),
                                    start = center,
                                    end = Offset(endX, endY),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }

                        // Target Vehicle B Icon
                        if (monitoringState.isMonitoring) {
                            val maxDisplayRange = 80.0
                            val clampedDist = distance.coerceIn(8.0, maxDisplayRange)
                            val normalizedRadius = (clampedDist / maxDisplayRange) * 82.0
                            val targetOffsetY = -normalizedRadius.dp

                            Box(
                                modifier = Modifier
                                    .offset(y = targetOffsetY)
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = riskColor.copy(alpha = if (riskLevel == "HIGH") 0.35f * pulseScale else 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {}

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = riskColor,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = "Target Car",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier
                                        .offset(x = 42.dp)
                                        .border(1.dp, riskColor, RoundedCornerShape(10.dp))
                                ) {
                                    Text(
                                        text = "%.1fm".format(distance),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Center Host Vehicle A Icon
                        Box(
                            modifier = Modifier.align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {}

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7),
                                shadowElevation = 6.dp,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = "My Car",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Text("30m", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center).offset(y = (-45).dp))
                        Text("60m", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center).offset(y = (-80).dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (monitoringState.isMonitoring) "${riskProbPct}%" else "--",
                                color = riskColor,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("PROBABILITY", color = NeoTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp),
                            color = NeoCardBorder
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (monitoringState.isMonitoring) riskLevel else "OFFLINE",
                                color = riskColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("RISK ASSESSMENT", color = NeoTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // VIEW 1: Interactive Live OpenStreetMap (Feature 6)
            InteractiveV2VMapView(
                latA = locationState.latitude,
                lonA = locationState.longitude,
                headingA = locationState.heading,
                latB = monitoringState.vehicleBLatitude,
                lonB = monitoringState.vehicleBLongitude,
                headingB = monitoringState.vehicleBHeading,
                distanceMeters = distance,
                riskLevel = riskLevel
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- FEATURE 4: EXPLAINABLE AI (XAI) HAZARD CONTRIBUTOR CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarningOrangeBg,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI EXPLAINABILITY (XAI)",
                            color = NeoObsidian,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Primary Hazard Factor",
                            color = NeoTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeoSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = monitoringState.primaryHazardReason,
                        color = NeoObsidian,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- FEATURE 5: DRIVER SAFETY SCORECARD CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SafeGreenBg,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DRIVER SAFETY SCORE",
                            color = NeoObsidian,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = monitoringState.safetyGrade,
                            color = SafeGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${monitoringState.safetyScore}/100",
                        color = NeoObsidian,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Harsh: ${monitoringState.harshEventsCount}",
                        color = NeoTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Highway Road Corridor View
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ROAD CORRIDOR VIEW",
                    color = NeoObsidian,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(NeoSurfaceVariant, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth()) {
                        drawLine(color = NeoCardBorder, start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 2.dp.toPx())
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeoObsidian,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                            Text("Host (V1)", color = NeoObsidian, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = NeoCardSurface,
                            modifier = Modifier.border(1.dp, riskColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = if (monitoringState.isMonitoring) "%.1f m Gap".format(distance) else "-- m Gap",
                                color = NeoObsidian,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (monitoringState.isMonitoring) riskColor else NeoTextSecondary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                            Text("Lead (V2)", color = NeoObsidian, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Kinematic Breakdown Card
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "KINEMATIC FEATURE MATRIX",
                    color = NeoObsidian,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                RiskMetricRow("Target Vehicle", "Vehicle #${monitoringState.targetVehicleBId}")
                RiskMetricRow("Inter-Vehicle Distance", "%.2f meters".format(distance))
                RiskMetricRow("Relative Velocity", "%.2f km/h".format(relSpeed))
                RiskMetricRow("Time-to-Collision (TTC)", if (ttc >= 999.0) "Safe (>999s)" else "%.2f sec".format(ttc))
                RiskMetricRow("Prediction Algorithm", "Random Forest Classifier")
                RiskMetricRow("Risk Classification", riskLevel)
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = NeoTextSecondary, fontSize = 12.sp)
        Text(value, color = NeoTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
