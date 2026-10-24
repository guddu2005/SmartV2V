package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.telemetry.TelemetryTransmissionService
import com.smartv2v.app.ui.navigation.Screen
import com.smartv2v.app.ui.theme.*
import com.smartv2v.app.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    transmissionService: TelemetryTransmissionService,
    onNavigate: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val userName = TokenManager.getUserName() ?: "Driver Node"
    val userEmail = TokenManager.getUserEmail() ?: "driver@v2v.net"
    val activeVehicleId = TokenManager.getActiveVehicleId()
    val monitoringState by transmissionService.monitoringState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoLightBg)
            .verticalScroll(scrollState)
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
                text = "DRIVER PROFILE",
                color = NeoObsidian,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            shape = CircleShape,
            color = NeoObsidian.copy(alpha = 0.1f),
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = NeoObsidian, modifier = Modifier.size(44.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = userName, color = NeoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = userEmail, color = NeoTextSecondary, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // FEATURE 5: Driver Safety Scorecard
        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SafeGreenBg,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SAFETY SCORE", color = NeoObsidian, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(monitoringState.safetyGrade, color = SafeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "${monitoringState.safetyScore}/100",
                        color = NeoObsidian,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = NeoCardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                ProfileRow("Harsh Hazard Encounters", "${monitoringState.harshEventsCount} events")
                ProfileRow("V2V Safe Transmissions", "${monitoringState.transmissionCount} cycles")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ProfileRow("Active Vehicle ID", if (activeVehicleId != -1) "Vehicle #$activeVehicleId" else "None")
                ProfileRow("Role", "Driver Node Operator")
                ProfileRow("Session Status", "Authenticated (JWT)")
                ProfileRow("Backend Cloud", "Render + Neon PostgreSQL")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                transmissionService.stopMonitoring()
                authViewModel.logout {
                    onNavigate(Screen.Login.route)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = NeoCardSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Text("DISCONNECT & LOGOUT", color = NeoCardSurface, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = NeoTextSecondary, fontSize = 13.sp)
        Text(value, color = NeoObsidian, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
