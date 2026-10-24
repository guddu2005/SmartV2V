package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.ui.navigation.Screen
import com.smartv2v.app.ui.theme.*
import com.smartv2v.app.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit
) {
    var email by remember { mutableStateOf("test@gmail.com") }
    var password by remember { mutableStateOf("test123") }
    val uiState by authViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoLightBg)
            .padding(24.dp)
    ) {
        // Top right settings gear icon to configure server IP
        IconButton(
            onClick = { onNavigate(Screen.Settings.route) },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Server Settings",
                tint = NeoObsidian
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VEHICLE NODE LOGIN",
                    color = NeoObsidian,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Server: ${TokenManager.getServerBaseUrl()}",
                    color = NeoTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = AlertRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeoObsidian) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeoObsidian,
                        unfocusedBorderColor = NeoCardBorder,
                        focusedLabelColor = NeoObsidian
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeoObsidian) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                        authViewModel.login(email, password) {
                            if (authViewModel.uiState.value.hasRegisteredVehicle) {
                                onNavigate(Screen.Dashboard.route)
                            } else {
                                onNavigate(Screen.VehicleRegistration.route)
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
                        Text("AUTHENTICATE NODE", color = NeoCardSurface, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { onNavigate(Screen.Register.route) }) {
                    Text("New Driver? Register Vehicle Node", color = NeoObsidian)
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(onClick = { onNavigate(Screen.Settings.route) }) {
                    Text("⚙️ Change Server IP Address", color = NeoTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
