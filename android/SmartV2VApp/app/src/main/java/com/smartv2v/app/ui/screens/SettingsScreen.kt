package com.smartv2v.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null
) {
    var serverUrl by remember { mutableStateOf(TokenManager.getServerBaseUrl()) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Settings",
                        fontWeight = FontWeight.Bold,
                        color = NeoObsidian,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NeoObsidian,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeoCardSurface
                )
            )
        },
        containerColor = NeoLightBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Main Connection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NeoCardSurface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeoLightBg,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = EmeraldAccent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BACKEND SERVER URL",
                                color = NeoObsidian,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Connect to Render Cloud or Local Wi-Fi",
                                color = NeoTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = {
                            serverUrl = it
                            saveMessage = null
                        },
                        label = { Text("Server Base URL (e.g. https://...onrender.com)") },
                        leadingIcon = {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = EmeraldAccent)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeoObsidian,
                            unfocusedBorderColor = NeoCardBorder,
                            focusedLabelColor = NeoObsidian
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    saveMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SafeGreenBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg, color = SafeGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Button: SAVE & RETURN
                    Button(
                        onClick = {
                            val trimmed = serverUrl.trim()
                            if (trimmed.isNotBlank()) {
                                ApiClient.init(trimmed)
                                onBack?.invoke()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeoObsidian),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = NeoCardSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SAVE & RETURN TO APP",
                            color = NeoCardSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Button: Save Only
                    OutlinedButton(
                        onClick = {
                            val trimmed = serverUrl.trim()
                            if (trimmed.isNotBlank()) {
                                ApiClient.init(trimmed)
                                saveMessage = "Server URL saved: $trimmed"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Save Only", color = NeoObsidian, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Third Button: Cancel / Go Back
                    TextButton(
                        onClick = { onBack?.invoke() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("← Cancel and Go Back", color = NeoTextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
