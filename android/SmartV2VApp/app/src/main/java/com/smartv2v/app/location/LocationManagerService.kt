package com.smartv2v.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationData(
    val latitude: Double = 30.3165,
    val longitude: Double = 78.0322,
    val speed: Double = 0.0, // km/h
    val heading: Double = 0.0 // degrees
)

class LocationManagerService(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow(LocationData())
    val locationState: StateFlow<LocationData> = _locationState.asStateFlow()

    private var isUpdating = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val lastLoc = result.lastLocation ?: return
            val speedKmh = if (lastLoc.hasSpeed()) lastLoc.speed * 3.6 else 0.0
            val bearingDeg = if (lastLoc.hasBearing()) lastLoc.bearing.toDouble() else 0.0

            _locationState.value = LocationData(
                latitude = lastLoc.latitude,
                longitude = lastLoc.longitude,
                speed = speedKmh,
                heading = bearingDeg
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(intervalMs: Long = 1000L) {
        if (isUpdating) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            isUpdating = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        if (!isUpdating) return
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isUpdating = false
    }

    fun updateManualLocation(lat: Double, lon: Double, speed: Double, heading: Double) {
        _locationState.value = LocationData(lat, lon, speed, heading)
    }
}
