package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.data.remote.ApiClient
import com.smartv2v.app.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null
) {
    var serverUrl by remember { mutableStateOf(TokenManager.getServerBaseUrl()) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

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
                text = "SYSTEM SETTINGS",
                color = NeoObsidian,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "KTOR BACKEND ENDPOINT",
                    color = NeoObsidian,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server Base URL") },
                    leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = NeoObsidian) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeoObsidian,
                        unfocusedBorderColor = NeoCardBorder,
                        focusedLabelColor = NeoObsidian
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        ApiClient.init(serverUrl)
                        saveMessage = "Server URL updated successfully!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoObsidian),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = NeoCardSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAVE CONFIGURATION", color = NeoCardSurface, fontWeight = FontWeight.Bold)
                }

                saveMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(msg, color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
