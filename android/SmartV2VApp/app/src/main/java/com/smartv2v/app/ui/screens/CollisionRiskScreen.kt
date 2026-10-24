package com.smartv2v.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.telemetry.TelemetryTransmissionService
import com.smartv2v.app.ui.theme.*
import kotlin.math.*

@Composable
fun CollisionRiskScreen(
    transmissionService: TelemetryTransmissionService,
    onBack: (() -> Unit)? = null
) {
    val monitoringState by transmissionService.monitoringState.collectAsState()
    val scrollState = rememberScrollState()

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

    val riskBgColor = when (riskLevel) {
        "HIGH" -> AlertRedBg
        "MEDIUM" -> WarningOrangeBg
        else -> SafeGreenBg
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

    // Pulse animation for high risk indicator
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
        // Top Header with Back Navigation
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
                        text = "V2V PROXIMITY RADAR",
                        color = NeoObsidian,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Direct C-V2X 5.9 GHz Band",
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

        // High Risk Siren Alert Banner
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
                    Column {
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
                }
            }
        }

        // --- 1. AESTHETIC HIGH-TECH RADAR CANVAS WITH VEHICLE ICONS ---
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
                        text = "360° SPATIAL RADAR",
                        color = NeoObsidian,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Range: 100m",
                        color = NeoTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Radar Scope
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                            ),
                            shape = CircleShape
                        )
                        .border(2.dp, Color(0xFF334155), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Canvas for radar grids and sweeping beam
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = this.center
                        val radius = size.minDimension / 2.0f

                        // Crosshairs (axis lines)
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(center.x, 0f),
                            end = Offset(center.x, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(0f, center.y),
                            end = Offset(size.width, center.y),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Concentric Range Rings (100m, 66m, 33m)
                        drawCircle(color = Color(0xFF1E293B), radius = radius, style = Stroke(width = 1.5.dp.toPx()))
                        drawCircle(color = Color(0xFF1E293B), radius = radius * 0.66f, style = Stroke(width = 1.dp.toPx()))
                        drawCircle(color = Color(0xFF1E293B), radius = radius * 0.33f, style = Stroke(width = 1.dp.toPx()))

                        // Sweeping radar beam gradient
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

                    // --- TARGET VEHICLE (VEHICLE B) AS AN ICON ---
                    if (monitoringState.isMonitoring) {
                        // Calculate offset based on relative distance (clamped to radar range)
                        val maxDisplayRange = 80.0
                        val clampedDist = distance.coerceIn(8.0, maxDisplayRange)
                        val normalizedRadius = (clampedDist / maxDisplayRange) * 85.0
                        // Position target vehicle ahead (y offset negative)
                        val targetOffsetY = -normalizedRadius.dp

                        Box(
                            modifier = Modifier
                                .offset(y = targetOffsetY)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsing risk halo
                            Surface(
                                shape = CircleShape,
                                color = riskColor.copy(alpha = if (riskLevel == "HIGH") 0.3f * pulseScale else 0.15f),
                                modifier = Modifier.size(46.dp)
                            ) {}

                            // Vehicle B Shape with Car Icon
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = riskColor,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = "Target Vehicle B",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Distance Badge Tag Floating next to Target Vehicle
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

                    // --- HOST VEHICLE (VEHICLE A - MY CAR) AS AN ICON IN CENTER ---
                    Box(
                        modifier = Modifier.align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {}

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7),
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "My Car (Host)",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Range Ring Labels (30m, 60m)
                    Text(
                        text = "30m",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-45).dp)
                    )
                    Text(
                        text = "60m",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-80).dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Risk probability percentage and status
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
                        Text(
                            text = "COLLISION PROBABILITY",
                            color = NeoTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(
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
                        Text(
                            text = "RISK ASSESSMENT",
                            color = NeoTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. DUAL-VEHICLE HIGHWAY CORRIDOR VIEW (From Provided Reference) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ROAD CORRIDOR SIMULATION",
                    color = NeoObsidian,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Road Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(NeoSurfaceVariant, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Center dashed lane line
                    Canvas(modifier = Modifier.fillMaxWidth()) {
                        drawLine(
                            color = NeoCardBorder,
                            start = Offset(0f, center.y),
                            end = Offset(size.width, center.y),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Host Vehicle A (My Vehicle)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NeoObsidian,
                                shadowElevation = 3.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = "Host Car",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Host Car", color = NeoObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("My Node", color = NeoTextSecondary, fontSize = 10.sp)
                        }

                        // Middle Gap Indicator Pill
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = NeoCardSurface,
                                shadowElevation = 2.dp,
                                modifier = Modifier.border(1.dp, riskColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Sensors, contentDescription = null, tint = riskColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (monitoringState.isMonitoring) "%.1f m Gap".format(distance) else "-- m Gap",
                                        color = NeoObsidian,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (monitoringState.isMonitoring) "Rel: %.1f km/h".format(relSpeed) else "Idle",
                                color = NeoTextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        // Target Vehicle B (Lead Car)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (monitoringState.isMonitoring) riskColor else NeoTextSecondary,
                                shadowElevation = 3.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = "Lead Car",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Lead Car", color = NeoObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Vehicle #${monitoringState.targetVehicleBId}", color = NeoTextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. KINEMATIC FEATURE BREAKDOWN ---
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "KINEMATIC FEATURE BREAKDOWN",
                    color = NeoObsidian,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                RiskMetricRow("Target Vehicle ID", "Vehicle #${monitoringState.targetVehicleBId}")
                RiskMetricRow("Inter-Vehicle Distance", "%.2f meters".format(distance))
                RiskMetricRow("Relative Velocity", "%.2f km/h".format(relSpeed))
                RiskMetricRow("Time-to-Collision (TTC)", if (ttc >= 999.0) "Safe (>999s)" else "%.2f sec".format(ttc))
                RiskMetricRow("Machine Learning Model", "RandomForest (v2v_collision_model)")
                RiskMetricRow("Predicted Risk Class", riskLevel)
            }
        }
    }
}

@Composable
fun RiskMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = NeoTextSecondary, fontSize = 13.sp)
        Text(value, color = NeoTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
