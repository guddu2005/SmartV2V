package com.smartv2v.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object VehicleRegistration : Screen("vehicle_registration")
    object MyVehicles : Screen("my_vehicles")
    object Dashboard : Screen("dashboard")
    object SensorDetails : Screen("sensor_details")
    object CollisionRisk : Screen("collision_risk")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}
