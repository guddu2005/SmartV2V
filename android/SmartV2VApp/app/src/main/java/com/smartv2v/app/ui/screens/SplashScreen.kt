package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.ui.navigation.Screen
import com.smartv2v.app.ui.theme.CyanAccent
import com.smartv2v.app.ui.theme.SlateDarkBg
import com.smartv2v.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        authViewModel.checkAuthStatus()
        delay(1500)
        val state = authViewModel.uiState.value
        if (!state.isAuthenticated) {
            onNavigate(Screen.Login.route)
        } else if (!state.hasRegisteredVehicle) {
            onNavigate(Screen.VehicleRegistration.route)
        } else {
            onNavigate(Screen.Dashboard.route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "SmartV2V Logo",
                tint = CyanAccent,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SMART V2V",
                color = CyanAccent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "V2V Safety & Collision Risk Detector",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = CyanAccent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
