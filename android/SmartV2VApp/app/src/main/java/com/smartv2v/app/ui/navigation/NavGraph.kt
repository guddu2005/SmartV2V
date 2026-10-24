package com.smartv2v.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.smartv2v.app.telemetry.TelemetryTransmissionService
import com.smartv2v.app.ui.screens.*
import com.smartv2v.app.ui.viewmodel.AuthViewModel
import com.smartv2v.app.ui.viewmodel.VehicleViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    vehicleViewModel: VehicleViewModel,
    transmissionService: TelemetryTransmissionService
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(authViewModel = authViewModel) { route ->
                navController.navigate(route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(authViewModel = authViewModel) { route ->
                navController.navigate(route)
            }
        }

        composable(Screen.Register.route) {
            RegisterScreen(authViewModel = authViewModel) { route ->
                navController.navigate(route)
            }
        }

        composable(Screen.VehicleRegistration.route) {
            VehicleRegistrationScreen(
                vehicleViewModel = vehicleViewModel,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.VehicleRegistration.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MyVehicles.route) {
            MyVehiclesScreen(
                vehicleViewModel = vehicleViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                transmissionService = transmissionService,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.SensorDetails.route) {
            SensorDetailsScreen(
                transmissionService = transmissionService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CollisionRisk.route) {
            CollisionRiskScreen(
                transmissionService = transmissionService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                transmissionService = transmissionService,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
