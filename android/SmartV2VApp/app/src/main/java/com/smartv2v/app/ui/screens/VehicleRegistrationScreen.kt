package com.smartv2v.app.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.ui.navigation.Screen
import com.smartv2v.app.ui.theme.*
import com.smartv2v.app.ui.viewmodel.VehicleViewModel

@Composable
fun VehicleRegistrationScreen(
    vehicleViewModel: VehicleViewModel,
    onNavigate: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var vehicleNumber by remember { mutableStateOf("VH-NODE-01") }
    var deviceId by remember { mutableStateOf(Build.MODEL ?: "ANDROID-DEVICE-01") }
    val uiState by vehicleViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoLightBg)
            .padding(24.dp)
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
                text = "REGISTER VEHICLE NODE",
                color = NeoObsidian,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = NeoObsidian,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = AlertRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text("Vehicle Identification / License Plate") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = NeoObsidian) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeoObsidian,
                        unfocusedBorderColor = NeoCardBorder,
                        focusedLabelColor = NeoObsidian
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text("Hardware Telemetry Device ID") },
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NeoObsidian) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeoObsidian,
                        unfocusedBorderColor = NeoCardBorder,
                        focusedLabelColor = NeoObsidian
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (vehicleNumber.isNotBlank() && deviceId.isNotBlank()) {
                            vehicleViewModel.registerVehicle(vehicleNumber, deviceId) {
                                onNavigate(Screen.Dashboard.route)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoObsidian),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = NeoCardSurface, modifier = Modifier.size(24.dp))
                    } else {
                        Text("REGISTER & BIND VEHICLE", color = NeoCardSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
