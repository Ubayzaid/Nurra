package com.nurra.islamic.model

data class UserLocation(
    val id: String = "loc-default",
    val cityName: String = "Jakarta",
    val subAdministrativeArea: String = "Jakarta Pusat",
    val administrativeArea: String = "DKI Jakarta",
    val countryName: String = "Indonesia",
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val source: String = "MANUAL", // GPS or MANUAL
    val apiCityId: String = "1301", // myQuran API city ID (e.g. 1301 for Jakarta Pusat, 1302 for Kab. Tangerang)
    val timezoneId: String = "Asia/Jakarta", // Asia/Jakarta, Asia/Makassar, Asia/Jayapura
    val updatedAt: Long = System.currentTimeMillis()
)
