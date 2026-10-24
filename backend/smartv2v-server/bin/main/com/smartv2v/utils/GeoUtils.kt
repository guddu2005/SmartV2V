package com.smartv2v.utils

import kotlin.math.*

object GeoUtils {

    /**
     * Earth radius in meters
     */
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates distance between two latitude/longitude points in meters using Haversine formula.
     */
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates heading difference between two vehicle headings in degrees (0 to 180).
     */
    fun calculateHeadingDifference(headingA: Double, headingB: Double): Double {
        val diff = abs(headingA - headingB) % 360.0
        return if (diff > 180.0) 360.0 - diff else diff
    }

    /**
     * Calculates 3D vector magnitude (acceleration or gyroscope).
     */
    fun calculateMagnitude(x: Double, y: Double, z: Double): Double {
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * Calculates relative speed between vehicle A and vehicle B.
     */
    fun calculateRelativeSpeed(speedA: Double, speedB: Double): Double {
        return abs(speedA - speedB)
    }

    /**
     * Calculates Time to Collision (TTC) safely.
     * TTC = distance / closing_speed
     * Handles zero closing speed, negative closing speed, invalid or infinite TTC.
     * Returns 999.0 for safe/infinite TTC scenarios.
     */
    fun calculateTTC(distance: Double, speedA: Double, speedB: Double, headingDiff: Double): Double {
        // Approximate closing speed based on relative velocity vector towards each other
        val speedAMs = speedA / 3.6
        val speedBMs = speedB / 3.6

        // Closing speed projection
        val radDiff = Math.toRadians(headingDiff)
        val closingSpeed = abs(speedAMs - speedBMs * cos(radDiff))

        if (closingSpeed <= 0.01 || distance <= 0.0) {
            return 999.0 // No closing risk / static or moving apart
        }

        val ttc = distance / closingSpeed
        return if (ttc.isInfinite() || ttc.isNaN() || ttc > 999.0) 999.0 else max(0.0, ttc)
    }
}
