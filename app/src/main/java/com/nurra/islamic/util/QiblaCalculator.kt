package com.nurra.islamic.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaCalculator {

    // Kaaba, Makkah Al-Mukarramah Coordinates
    private const val KAABA_LATITUDE = 21.422487
    private const val KAABA_LONGITUDE = 39.826206

    /**
     * Calculates the spherical initial bearing (in degrees, 0..360) from user location to Kaaba.
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLngRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLngRad = kaabaLngRad - userLngRad

        val y = sin(deltaLngRad) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLngRad)

        var bearingRad = atan2(y, x)
        var bearingDeg = Math.toDegrees(bearingRad).toFloat()

        if (bearingDeg < 0) {
            bearingDeg += 360f
        }

        return bearingDeg
    }

    /**
     * Converts a bearing in degrees to cardinal direction label (e.g. 295.2° NW).
     */
    fun formatBearingLabel(bearingDeg: Float): String {
        val direction = when (bearingDeg) {
            in 22.5f..67.5f -> "NE"
            in 67.5f..112.5f -> "E"
            in 112.5f..157.5f -> "SE"
            in 157.5f..202.5f -> "S"
            in 202.5f..247.5f -> "SW"
            in 247.5f..292.5f -> "W"
            in 292.5f..337.5f -> "NW"
            else -> "N"
        }
        return "%.1f° %s".format(bearingDeg, direction)
    }
}
