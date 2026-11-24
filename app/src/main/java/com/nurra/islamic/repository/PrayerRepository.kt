package com.nurra.islamic.repository

import android.content.Context
import android.util.Log
import com.nurra.islamic.api.ApiClient
import com.nurra.islamic.model.DailyPrayerSchedule
import com.nurra.islamic.model.LocationInfo
import com.nurra.islamic.model.PrayerTimeItem
import com.nurra.islamic.model.PrayerType
import com.nurra.islamic.util.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

interface PrayerRepository {
    fun getDailySchedule(cityId: String = "1301", timezoneId: String = "Asia/Jakarta", dateStr: String = "today"): Flow<DailyPrayerSchedule>
    fun getAvailableLocations(): List<LocationInfo>
    fun updateLocation(location: LocationInfo)
    fun togglePrayerNotification(prayerType: PrayerType)
}

class LocalPrayerRepository(
    private val context: Context? = null
) : PrayerRepository {

    private val TAG = "NurraPrayerApi"
    private val cacheManager = context?.let { CacheManager(it) }

    private val currentLocation = MutableStateFlow(
        LocationInfo("Jakarta", "Indonesia", -6.2088, 106.8456)
    )

    private val notificationStates = MutableStateFlow(
        mapOf(
            PrayerType.IMSAK to false,
            PrayerType.SUBUH to true,
            PrayerType.SYURUQ to false,
            PrayerType.DZUHUR to true,
            PrayerType.ASHAR to true,
            PrayerType.MAGHRIB to true,
            PrayerType.ISYA to true
        )
    )

    private val availableCities = listOf(
        LocationInfo("Jakarta", "Indonesia", -6.2088, 106.8456),
        LocationInfo("Bandung", "Indonesia", -6.9175, 107.6191),
        LocationInfo("Surabaya", "Indonesia", -7.2575, 112.7521),
        LocationInfo("Yogyakarta", "Indonesia", -7.7956, 110.3695),
        LocationInfo("Medan", "Indonesia", 3.5952, 98.6722),
        LocationInfo("Makassar", "Indonesia", -5.1477, 119.4327),
        LocationInfo("Semarang", "Indonesia", -6.9667, 110.4167),
        LocationInfo("Malang", "Indonesia", -7.9666, 112.6326)
    )

    override fun getDailySchedule(cityId: String, timezoneId: String, dateStr: String): Flow<DailyPrayerSchedule> {
        val tz = TimeZone.getTimeZone(timezoneId)
        val today = Calendar.getInstance(tz)
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).apply {
            timeZone = tz
        }
        val dateMasehi = dateFormat.format(today.time)
        val dateHijriah = "27 Sya'ban 1446 H"

        val year = SimpleDateFormat("yyyy", Locale.US).apply { timeZone = tz }.format(today.time)
        val month = SimpleDateFormat("MM", Locale.US).apply { timeZone = tz }.format(today.time)
        val day = SimpleDateFormat("dd", Locale.US).apply { timeZone = tz }.format(today.time)

        val cacheKey = "${cityId}_$year-$month-$day"
        val cached = cacheManager?.getPrayerSchedule(cacheKey)

        val initialSchedule = cached ?: createFallbackSchedule(dateMasehi, dateHijriah, currentLocation.value)
        val scheduleFlow = MutableStateFlow(initialSchedule)

        // Asynchronously fetch schedule from myQuran v2 API & cache
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "=== PRAYER API DIAGNOSTICS ===")
                Log.d(TAG, "Requesting Prayer Schedule -> myQuran City ID: $cityId, Timezone: $timezoneId, Date: $year/$month/$day")
                Log.d(TAG, "Current Location: ${currentLocation.value.cityName}, Lat: ${currentLocation.value.latitude}, Lng: ${currentLocation.value.longitude}")

                val response = ApiClient.apiService.getPrayerSchedule(cityId, year, month, day)

                if (response.isSuccessful && response.body()?.status == true) {
                    val detail = response.body()?.data
                    val jadwal = detail?.jadwal

                    if (jadwal != null) {
                        Log.d(TAG, "API Response SUCCESS -> Location: ${detail.lokasi} (${detail.daerah})")
                        Log.d(TAG, "Prayer Times from Kemenag RI: Imsak=${jadwal.imsak}, Subuh=${jadwal.subuh}, Terbit=${jadwal.terbit}, Dzuhur=${jadwal.dzuhur}, Ashar=${jadwal.ashar}, Maghrib=${jadwal.maghrib}, Isya=${jadwal.isya}")

                        val imsakStr = jadwal.imsak ?: "04:34"
                        val subuhStr = jadwal.subuh ?: "04:44"
                        val terbitStr = jadwal.terbit ?: "05:57"
                        val dzuhurStr = jadwal.dzuhur ?: "12:10"
                        val asharStr = jadwal.ashar ?: "15:16"
                        val maghribStr = jadwal.maghrib ?: "18:17"
                        val isyaStr = jadwal.isya ?: "19:27"

                        val tzSuffix = when {
                            timezoneId.contains("Makassar") -> "WITA"
                            timezoneId.contains("Jayapura") -> "WIT"
                            else -> "WIB"
                        }

                        val updatedList = listOf(
                            PrayerTimeItem(PrayerType.IMSAK, "Imsak", "$imsakStr $tzSuffix", isNext = false, isCompleted = true, notificationEnabled = notificationStates.value[PrayerType.IMSAK] ?: false),
                            PrayerTimeItem(PrayerType.SUBUH, "Subuh", "$subuhStr $tzSuffix", isNext = false, isCompleted = true, notificationEnabled = notificationStates.value[PrayerType.SUBUH] ?: true),
                            PrayerTimeItem(PrayerType.SYURUQ, "Terbit", "$terbitStr $tzSuffix", isNext = false, isCompleted = true, notificationEnabled = notificationStates.value[PrayerType.SYURUQ] ?: false),
                            PrayerTimeItem(PrayerType.DZUHUR, "Dzuhur", "$dzuhurStr $tzSuffix", isNext = true, isCompleted = false, notificationEnabled = notificationStates.value[PrayerType.DZUHUR] ?: true),
                            PrayerTimeItem(PrayerType.ASHAR, "Ashar", "$asharStr $tzSuffix", isNext = false, isCompleted = false, notificationEnabled = notificationStates.value[PrayerType.ASHAR] ?: true),
                            PrayerTimeItem(PrayerType.MAGHRIB, "Maghrib", "$maghribStr $tzSuffix", isNext = false, isCompleted = false, notificationEnabled = notificationStates.value[PrayerType.MAGHRIB] ?: true),
                            PrayerTimeItem(PrayerType.ISYA, "Isya", "$isyaStr $tzSuffix", isNext = false, isCompleted = false, notificationEnabled = notificationStates.value[PrayerType.ISYA] ?: true)
                        )

                        val newSchedule = DailyPrayerSchedule(
                            dateMasehi = dateMasehi,
                            dateHijriah = dateHijriah,
                            location = LocationInfo(
                                cityName = detail.lokasi ?: currentLocation.value.cityName,
                                countryName = detail.daerah ?: currentLocation.value.countryName,
                                latitude = currentLocation.value.latitude,
                                longitude = currentLocation.value.longitude
                            ),
                            nextPrayerName = "Dzuhur",
                            nextPrayerTime = "$dzuhurStr $tzSuffix",
                            countdownFormatted = "1 Jam 24 Menit",
                            prayerList = updatedList
                        )

                        cacheManager?.savePrayerSchedule(cacheKey, newSchedule)
                        scheduleFlow.value = newSchedule
                    }
                } else {
                    Log.w(TAG, "API Response Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "API Exception: ${e.message}. Using offline cache.")
            }
        }

        return scheduleFlow.asStateFlow()
    }

    private fun createFallbackSchedule(
        dateMasehi: String,
        dateHijriah: String,
        location: LocationInfo
    ): DailyPrayerSchedule {
        val prayers = listOf(
            PrayerTimeItem(PrayerType.IMSAK, "Imsak", "04:34 WIB", isNext = false, isCompleted = true, notificationEnabled = notificationStates.value[PrayerType.IMSAK] ?: false),
            PrayerTimeItem(PrayerType.SUBUH, "Subuh", "04:44 WIB", isNext = false, isCompleted = true, notificationEnabled = notificationStates.value[PrayerType.SUBUH] ?: true),
            PrayerTimeItem(PrayerType.SYURUQ, "Terbit", "05:57 WIB", isNext = false, isCompleted = true, notificationEnabled = notificationStates.value[PrayerType.SYURUQ] ?: false),
            PrayerTimeItem(PrayerType.DZUHUR, "Dzuhur", "12:10 WIB", isNext = true, isCompleted = false, notificationEnabled = notificationStates.value[PrayerType.DZUHUR] ?: true),
            PrayerTimeItem(PrayerType.ASHAR, "Ashar", "15:16 WIB", isNext = false, isCompleted = false, notificationEnabled = notificationStates.value[PrayerType.ASHAR] ?: true),
            PrayerTimeItem(PrayerType.MAGHRIB, "Maghrib", "18:17 WIB", isNext = false, isCompleted = false, notificationEnabled = notificationStates.value[PrayerType.MAGHRIB] ?: true),
            PrayerTimeItem(PrayerType.ISYA, "Isya", "19:27 WIB", isNext = false, isCompleted = false, notificationEnabled = notificationStates.value[PrayerType.ISYA] ?: true)
        )

        return DailyPrayerSchedule(
            dateMasehi = dateMasehi,
            dateHijriah = dateHijriah,
            location = location,
            nextPrayerName = "Dzuhur",
            nextPrayerTime = "12:10 WIB",
            countdownFormatted = "1 Jam 24 Menit",
            prayerList = prayers
        )
    }

    override fun getAvailableLocations(): List<LocationInfo> = availableCities

    override fun updateLocation(location: LocationInfo) {
        currentLocation.value = location
    }

    override fun togglePrayerNotification(prayerType: PrayerType) {
        val current = notificationStates.value.toMutableMap()
        current[prayerType] = !(current[prayerType] ?: false)
        notificationStates.value = current
    }
}
