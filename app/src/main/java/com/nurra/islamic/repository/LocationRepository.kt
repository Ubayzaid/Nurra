package com.nurra.islamic.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nurra.islamic.api.ApiClient
import com.nurra.islamic.api.CityDto
import com.nurra.islamic.model.UserLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone

interface LocationRepository {
    fun getActiveLocation(): Flow<UserLocation>
    fun updateActiveLocation(location: UserLocation)
    suspend fun searchLocations(query: String): List<UserLocation>
    suspend fun getCurrentGpsLocation(): Result<UserLocation>
    suspend fun findMyQuranCityId(cityName: String, adminArea: String): String
}

class LocalLocationRepository(
    private val context: Context? = null
) : LocationRepository {

    private val TAG = "NurraLocationRepo"

    private val KEY_CITY_NAME = stringPreferencesKey("loc_city_name")
    private val KEY_SUB_ADMIN = stringPreferencesKey("loc_sub_admin")
    private val KEY_ADMIN = stringPreferencesKey("loc_admin")
    private val KEY_COUNTRY = stringPreferencesKey("loc_country")
    private val KEY_LATITUDE = doublePreferencesKey("loc_latitude")
    private val KEY_LONGITUDE = doublePreferencesKey("loc_longitude")
    private val KEY_SOURCE = stringPreferencesKey("loc_source")
    private val KEY_API_CITY_ID = stringPreferencesKey("loc_api_city_id")
    private val KEY_TIMEZONE_ID = stringPreferencesKey("loc_timezone_id")
    private val KEY_UPDATED_AT = longPreferencesKey("loc_updated_at")

    private val inMemoryLocation = MutableStateFlow(UserLocation())

    // Cached myQuran official city list
    private var cachedMyQuranCities: List<CityDto>? = null

    override fun getActiveLocation(): Flow<UserLocation> {
        if (context == null) return inMemoryLocation.asStateFlow()

        return context.dataStore.data.map { prefs ->
            val loc = UserLocation(
                id = "loc-${prefs[KEY_UPDATED_AT] ?: 0L}",
                cityName = prefs[KEY_CITY_NAME] ?: "Jakarta",
                subAdministrativeArea = prefs[KEY_SUB_ADMIN] ?: "Jakarta Pusat",
                administrativeArea = prefs[KEY_ADMIN] ?: "DKI Jakarta",
                countryName = prefs[KEY_COUNTRY] ?: "Indonesia",
                latitude = prefs[KEY_LATITUDE] ?: -6.2088,
                longitude = prefs[KEY_LONGITUDE] ?: 106.8456,
                source = prefs[KEY_SOURCE] ?: "MANUAL",
                apiCityId = prefs[KEY_API_CITY_ID] ?: "1301",
                timezoneId = prefs[KEY_TIMEZONE_ID] ?: determineTimezone(prefs[KEY_LONGITUDE] ?: 106.8456),
                updatedAt = prefs[KEY_UPDATED_AT] ?: System.currentTimeMillis()
            )
            Log.d(TAG, "Active Location Loaded: ${loc.cityName} (${loc.administrativeArea}), myQuranCityId=${loc.apiCityId}, Timezone=${loc.timezoneId}, Lat=${loc.latitude}, Lng=${loc.longitude}")
            loc
        }
    }

    override fun updateActiveLocation(location: UserLocation) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val apiCityId = findMyQuranCityId(location.cityName, location.administrativeArea)
                val tzId = determineTimezone(location.longitude)

                context.dataStore.edit { prefs ->
                    prefs[KEY_CITY_NAME] = location.cityName
                    prefs[KEY_SUB_ADMIN] = location.subAdministrativeArea
                    prefs[KEY_ADMIN] = location.administrativeArea
                    prefs[KEY_COUNTRY] = location.countryName
                    prefs[KEY_LATITUDE] = location.latitude
                    prefs[KEY_LONGITUDE] = location.longitude
                    prefs[KEY_SOURCE] = location.source
                    prefs[KEY_API_CITY_ID] = apiCityId
                    prefs[KEY_TIMEZONE_ID] = tzId
                    prefs[KEY_UPDATED_AT] = System.currentTimeMillis()
                }

                Log.d(TAG, "Location Updated & Saved: ${location.cityName}, myQuranCityId=$apiCityId, tz=$tzId")
            }
        } else {
            inMemoryLocation.value = location
        }
    }

    override suspend fun searchLocations(query: String): List<UserLocation> {
        if (query.isBlank() || context == null) return emptyList()

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("id", "ID"))
                val addresses = geocoder.getFromLocationName(query, 5) ?: emptyList()

                addresses.mapIndexed { index, address ->
                    val city = address.locality
                        ?: address.subAdminArea
                        ?: address.adminArea
                        ?: query
                    val subAdmin = address.subAdminArea ?: address.subLocality ?: ""
                    val admin = address.adminArea ?: ""
                    val country = address.countryName ?: "Indonesia"

                    val apiCityId = findMyQuranCityId(city, admin)
                    val tzId = determineTimezone(address.longitude)

                    UserLocation(
                        id = "search-$index-${System.currentTimeMillis()}",
                        cityName = city,
                        subAdministrativeArea = subAdmin,
                        administrativeArea = admin,
                        countryName = country,
                        latitude = address.latitude,
                        longitude = address.longitude,
                        source = "MANUAL",
                        apiCityId = apiCityId,
                        timezoneId = tzId,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geocoding search failed: ${e.message}")
                emptyList()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentGpsLocation(): Result<UserLocation> {
        if (context == null) return Result.failure(Exception("Context is null"))

        return withContext(Dispatchers.IO) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!isGpsEnabled && !isNetworkEnabled) {
                    return@withContext Result.failure(Exception("Layanan GPS / Lokasi tidak aktif. Aktifkan GPS pada HP."))
                }

                var lastKnownLoc: Location? = null
                if (isGpsEnabled) {
                    lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                if (lastKnownLoc == null && isNetworkEnabled) {
                    lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                val lat = lastKnownLoc?.latitude ?: -6.2088
                val lng = lastKnownLoc?.longitude ?: 106.8456

                val geocoder = Geocoder(context, Locale("id", "ID"))
                val addresses = geocoder.getFromLocation(lat, lng, 1)

                val userLoc = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality ?: address.subAdminArea ?: "Jakarta"
                    val subAdmin = address.subAdminArea ?: address.subLocality ?: ""
                    val admin = address.adminArea ?: ""
                    val country = address.countryName ?: "Indonesia"

                    val apiCityId = findMyQuranCityId(city, admin)
                    val tzId = determineTimezone(lng)

                    UserLocation(
                        id = "gps-${System.currentTimeMillis()}",
                        cityName = city,
                        subAdministrativeArea = subAdmin,
                        administrativeArea = admin,
                        countryName = country,
                        latitude = lat,
                        longitude = lng,
                        source = "GPS",
                        apiCityId = apiCityId,
                        timezoneId = tzId,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    val apiCityId = findMyQuranCityId("Jakarta", "DKI Jakarta")
                    UserLocation(
                        id = "gps-${System.currentTimeMillis()}",
                        cityName = "Lokasi Saya",
                        subAdministrativeArea = "",
                        administrativeArea = "",
                        countryName = "Indonesia",
                        latitude = lat,
                        longitude = lng,
                        source = "GPS",
                        apiCityId = apiCityId,
                        timezoneId = determineTimezone(lng),
                        updatedAt = System.currentTimeMillis()
                    )
                }

                Log.d(TAG, "GPS Location Detected: ${userLoc.cityName}, Lat=$lat, Lng=$lng, myQuranCityId=${userLoc.apiCityId}")
                Result.success(userLoc)
            } catch (e: Exception) {
                Log.e(TAG, "GPS location failed: ${e.message}")
                Result.failure(e)
            }
        }
    }

    override suspend fun findMyQuranCityId(cityName: String, adminArea: String): String {
        return withContext(Dispatchers.IO) {
            try {
                if (cachedMyQuranCities == null) {
                    val response = ApiClient.apiService.getAllCities()
                    if (response.isSuccessful && response.body()?.status == true) {
                        cachedMyQuranCities = response.body()?.data
                    }
                }

                val cities = cachedMyQuranCities ?: emptyList()
                if (cities.isEmpty()) return@withContext "1301" // Fallback to Jakarta

                val cleanCity = cityName.uppercase().replace("KOTA", "").replace("KABUPATEN", "").replace("KAB.", "").trim()
                val cleanAdmin = adminArea.uppercase().replace("KABUPATEN", "").replace("KAB.", "").trim()

                // Special handling for Pasarkemis / Tangerang region -> Map to KAB. TANGERANG (id 1104)
                if (cleanCity.contains("PASARKEMIS") || cleanCity.contains("TANGERANG") || cleanAdmin.contains("TANGERANG") || cleanAdmin.contains("BANTEN")) {
                    val tangerangKab = cities.find { it.lokasi.contains("KAB. TANGERANG") }
                    if (tangerangKab != null) {
                        Log.d(TAG, "MyQuran Special Mapping: Pasarkemis/Tangerang -> KAB. TANGERANG (id=${tangerangKab.id})")
                        return@withContext tangerangKab.id
                    }
                }

                // 1. Direct match with city/district name
                var matched = cities.find { c ->
                    val locName = c.lokasi.uppercase()
                    locName.contains(cleanCity) || cleanCity.contains(locName.replace("KAB.", "").replace("KOTA", "").trim())
                }

                // 2. Fallback search by admin province area
                if (matched == null && cleanAdmin.isNotBlank()) {
                    matched = cities.find { c -> c.lokasi.uppercase().contains(cleanAdmin) }
                }

                val cityId = matched?.id ?: "1301"
                Log.d(TAG, "MyQuran City Mapping: input='$cityName ($adminArea)' -> matched='${matched?.lokasi}' (id=$cityId)")
                cityId
            } catch (e: Exception) {
                Log.e(TAG, "myQuran city mapping exception: ${e.message}")
                "1301"
            }
        }
    }

    private fun determineTimezone(longitude: Double): String {
        val sysTz = TimeZone.getDefault().id
        if (sysTz.contains("Asia/Jakarta") || sysTz.contains("Asia/Makassar") || sysTz.contains("Asia/Jayapura")) {
            return sysTz
        }

        return when {
            longitude < 115.0 -> "Asia/Jakarta"  // WIB (UTC+7)
            longitude in 115.0..125.0 -> "Asia/Makassar" // WITA (UTC+8)
            else -> "Asia/Jayapura"  // WIT (UTC+9)
        }
    }
}
