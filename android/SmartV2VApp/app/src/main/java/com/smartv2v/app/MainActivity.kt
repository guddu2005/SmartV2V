package com.smartv2v.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartv2v.app.telemetry.TelemetryTransmissionService
import com.smartv2v.app.ui.navigation.NavGraph
import com.smartv2v.app.ui.navigation.Screen
import com.smartv2v.app.ui.theme.*
import com.smartv2v.app.ui.viewmodel.AuthViewModel
import com.smartv2v.app.ui.viewmodel.VehicleViewModel

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val vehicleViewModel: VehicleViewModel by viewModels()
    private lateinit var transmissionService: TelemetryTransmissionService

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transmissionService = TelemetryTransmissionService(applicationContext)

        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
            )
        )

        setContent {
            SmartV2VTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomNavItems = listOf(
                    BottomNavItem("Dashboard", Screen.Dashboard.route, Icons.Default.Dashboard),
                    BottomNavItem("Radar", Screen.CollisionRisk.route, Icons.Default.Warning),
                    BottomNavItem("Sensors", Screen.SensorDetails.route, Icons.Default.Sensors),
                    BottomNavItem("Vehicles", Screen.MyVehicles.route, Icons.Default.DirectionsCar),
                    BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person)
                )

                val showBottomBar = currentRoute in bottomNavItems.map { it.route }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = NeoCardSurface
                            ) {
                                bottomNavItems.forEach { item ->
                                    val selected = currentRoute == item.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = if (selected) NeoObsidian else NeoTextSecondary
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = item.title,
                                                color = if (selected) NeoObsidian else NeoTextSecondary
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = NeoLightBg
                    ) {
                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            vehicleViewModel = vehicleViewModel,
                            transmissionService = transmissionService
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::transmissionService.isInitialized) {
            transmissionService.stopMonitoring()
        }
    }
}
