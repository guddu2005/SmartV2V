package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
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
fun MyVehiclesScreen(
    vehicleViewModel: VehicleViewModel,
    onNavigate: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val uiState by vehicleViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vehicleViewModel.loadUserVehicles()
    }

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = "MY VEHICLE FLEET",
                    color = NeoObsidian,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
            IconButton(onClick = { onNavigate(Screen.VehicleRegistration.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle", tint = NeoObsidian)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeoObsidian)
            }
        } else if (uiState.vehicles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No vehicles registered yet.", color = NeoTextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onNavigate(Screen.VehicleRegistration.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeoObsidian)
                    ) {
                        Text("Register First Vehicle", color = NeoCardSurface)
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.vehicles) { vehicle ->
                    val isActive = uiState.activeVehicle?.id == vehicle.id

                    Card(
                        colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vehicleViewModel.selectActiveVehicle(vehicle)
                            }
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
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = if (isActive) EmeraldAccent else NeoTextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = vehicle.vehicleNumber,
                                        color = NeoTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Device ID: ${vehicle.deviceId}",
                                        color = NeoTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (isActive) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = EmeraldAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
