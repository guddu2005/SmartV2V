package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.telemetry.TelemetryTransmissionService
import com.smartv2v.app.ui.theme.*

@Composable
fun SensorDetailsScreen(
    transmissionService: TelemetryTransmissionService,
    onBack: (() -> Unit)? = null
) {
    val sensorState by transmissionService.sensorManagerService.sensorState.collectAsState()
    val locationState by transmissionService.locationManagerService.locationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoLightBg)
            .padding(16.dp)
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
                text = "HARDWARE SENSORS & GPS",
                color = NeoObsidian,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Accelerometer
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ACCELEROMETER (m/s²)", color = NeoObsidian, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                SensorRow("Accel X", "%.3f".format(sensorState.accelX))
                SensorRow("Accel Y", "%.3f".format(sensorState.accelY))
                SensorRow("Accel Z", "%.3f".format(sensorState.accelZ))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Gyroscope
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("GYROSCOPE (rad/s)", color = NeoObsidian, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                SensorRow("Gyro X", "%.4f".format(sensorState.gyroX))
                SensorRow("Gyro Y", "%.4f".format(sensorState.gyroY))
                SensorRow("Gyro Z", "%.4f".format(sensorState.gyroZ))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GPS Location
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("GPS TELEMETRY", color = NeoObsidian, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                SensorRow("Latitude", "%.6f".format(locationState.latitude))
                SensorRow("Longitude", "%.6f".format(locationState.longitude))
                SensorRow("Speed", "%.2f km/h".format(locationState.speed))
                SensorRow("Heading", "%.1f°".format(locationState.heading))
            }
        }
    }
}

@Composable
fun SensorRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = NeoTextSecondary, fontSize = 13.sp)
        Text(value, color = NeoTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
