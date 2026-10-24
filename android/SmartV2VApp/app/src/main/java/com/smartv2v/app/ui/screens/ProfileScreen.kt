package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
                text = "DRIVER PROFILE",
                color = NeoObsidian,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            shape = CircleShape,
            color = NeoObsidian.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = NeoObsidian, modifier = Modifier.size(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = userName, color = NeoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = userEmail, color = NeoTextSecondary, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ProfileRow("Active Vehicle ID", if (activeVehicleId != -1) "Vehicle #$activeVehicleId" else "None")
                ProfileRow("Role", "Driver Node Operator")
                ProfileRow("Session Status", "Authenticated (JWT)")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                .height(50.dp)
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = NeoTextSecondary, fontSize = 14.sp)
        Text(value, color = NeoObsidian, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
