package com.nurra.islamic.model

enum class PrayerType {
    IMSAK, SUBUH, SYURUQ, DZUHUR, ASHAR, MAGHRIB, ISYA
}

data class PrayerTimeItem(
    val type: PrayerType,
    val name: String,
    val time: String, // e.g. "04:32"
    val isNext: Boolean = false,
    val isCompleted: Boolean = false,
    val notificationEnabled: Boolean = true
)

data class LocationInfo(
    val cityName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double
)

data class DailyPrayerSchedule(
    val dateMasehi: String,
    val dateHijriah: String,
    val location: LocationInfo,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val countdownFormatted: String,
    val prayerList: List<PrayerTimeItem>
)
