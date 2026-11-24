package com.nurra.islamic.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class AppThemeMode {
    SYSTEM,          // System Default (Dynamic Color Light/Dark based on device)
    LIGHT,           // Light Theme
    DARK,            // Dark Theme
    DYNAMIC_LIGHT,   // Dynamic Light (Dynamic Wallpaper Light Mode)
    DYNAMIC_DARK,    // Dynamic Dark (Dynamic Wallpaper Dark Mode)
    AMOLED           // AMOLED Black (#000000 Pure Pitch Black)
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isDynamicColorEnabled: Boolean = true,
    val isAmoledBlackEnabled: Boolean = false,
    val isAutoLocationEnabled: Boolean = false,
    val quranFontSizeSp: Float = 24f,
    val isAdhanNotificationEnabled: Boolean = true,
    val isPrePrayerReminderEnabled: Boolean = false,
    val prePrayerMinutes: Int = 10,
    val isSubuhNotifEnabled: Boolean = true,
    val isDzuhurNotifEnabled: Boolean = true,
    val isAsharNotifEnabled: Boolean = true,
    val isMaghribNotifEnabled: Boolean = true,
    val isIsyaNotifEnabled: Boolean = true,
    val isNotificationSoundEnabled: Boolean = true,
    val notificationSoundType: String = "DEFAULT",
    val isDailyAyahNotificationEnabled: Boolean = true,
    val isTasbihVibrationEnabled: Boolean = true,
    val isTasbihSoundEnabled: Boolean = false
)

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    fun updateThemeMode(mode: AppThemeMode)
    fun toggleDynamicColor(enabled: Boolean)
    fun toggleAmoledBlack(enabled: Boolean)
    fun toggleAutoLocation(enabled: Boolean)
    fun updateQuranFontSize(sizeSp: Float)
    fun toggleAdhanNotification(enabled: Boolean)
    fun togglePrePrayerReminder(enabled: Boolean)
    fun updatePrePrayerMinutes(minutes: Int)
    fun togglePrayerNotif(prayerName: String, enabled: Boolean)
    fun toggleNotificationSound(enabled: Boolean)
    fun updateNotificationSoundType(type: String)
    fun toggleDailyAyahNotification(enabled: Boolean)
    fun toggleTasbihVibration(enabled: Boolean)
    fun toggleTasbihSound(enabled: Boolean)
}

// DataStore Preferences extension on Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nurra_settings")

class LocalSettingsRepository(
    private val context: Context? = null
) : SettingsRepository {

    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    private val KEY_AMOLED_BLACK = booleanPreferencesKey("amoled_black")
    private val KEY_AUTO_LOCATION = booleanPreferencesKey("auto_location")
    private val KEY_QURAN_FONT_SIZE = floatPreferencesKey("quran_font_size")

    private val KEY_ADHAN_NOTIF = booleanPreferencesKey("adhan_notif")
    private val KEY_PRE_PRAYER_NOTIF = booleanPreferencesKey("pre_prayer_notif")
    private val KEY_PRE_PRAYER_MINUTES = intPreferencesKey("pre_prayer_minutes")

    private val KEY_NOTIF_SUBUH = booleanPreferencesKey("notif_subuh")
    private val KEY_NOTIF_DZUHUR = booleanPreferencesKey("notif_dzuhur")
    private val KEY_NOTIF_ASHAR = booleanPreferencesKey("notif_ashar")
    private val KEY_NOTIF_MAGHRIB = booleanPreferencesKey("notif_maghrib")
    private val KEY_NOTIF_ISYA = booleanPreferencesKey("notif_isya")

    private val KEY_NOTIF_SOUND = booleanPreferencesKey("notif_sound")
    private val KEY_NOTIF_SOUND_TYPE = stringPreferencesKey("notif_sound_type")

    private val KEY_DAILY_AYAH_NOTIF = booleanPreferencesKey("daily_ayah_notif")
    private val KEY_TASBIH_VIBE = booleanPreferencesKey("tasbih_vibe")

    private val inMemorySettings = MutableStateFlow(AppSettings())

    override fun getSettings(): Flow<AppSettings> {
        if (context == null) return inMemorySettings.asStateFlow()

        return context.dataStore.data.map { prefs ->
            val modeStr = prefs[KEY_THEME_MODE] ?: AppThemeMode.SYSTEM.name
            val mode = try {
                AppThemeMode.valueOf(modeStr)
            } catch (e: Exception) {
                AppThemeMode.SYSTEM
            }

            AppSettings(
                themeMode = mode,
                isDynamicColorEnabled = prefs[KEY_DYNAMIC_COLOR] ?: true,
                isAmoledBlackEnabled = prefs[KEY_AMOLED_BLACK] ?: false,
                isAutoLocationEnabled = prefs[KEY_AUTO_LOCATION] ?: false,
                quranFontSizeSp = prefs[KEY_QURAN_FONT_SIZE] ?: 24f,
                isAdhanNotificationEnabled = prefs[KEY_ADHAN_NOTIF] ?: true,
                isPrePrayerReminderEnabled = prefs[KEY_PRE_PRAYER_NOTIF] ?: false,
                prePrayerMinutes = prefs[KEY_PRE_PRAYER_MINUTES] ?: 10,
                isSubuhNotifEnabled = prefs[KEY_NOTIF_SUBUH] ?: true,
                isDzuhurNotifEnabled = prefs[KEY_NOTIF_DZUHUR] ?: true,
                isAsharNotifEnabled = prefs[KEY_NOTIF_ASHAR] ?: true,
                isMaghribNotifEnabled = prefs[KEY_NOTIF_MAGHRIB] ?: true,
                isIsyaNotifEnabled = prefs[KEY_NOTIF_ISYA] ?: true,
                isNotificationSoundEnabled = prefs[KEY_NOTIF_SOUND] ?: true,
                notificationSoundType = prefs[KEY_NOTIF_SOUND_TYPE] ?: "DEFAULT",
                isDailyAyahNotificationEnabled = prefs[KEY_DAILY_AYAH_NOTIF] ?: true,
                isTasbihVibrationEnabled = prefs[KEY_TASBIH_VIBE] ?: true
            )
        }
    }

    override fun updateThemeMode(mode: AppThemeMode) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_THEME_MODE] = mode.name
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(themeMode = mode)
        }
    }

    override fun toggleDynamicColor(enabled: Boolean) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_DYNAMIC_COLOR] = enabled
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(isDynamicColorEnabled = enabled)
        }
    }

    override fun toggleAmoledBlack(enabled: Boolean) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_AMOLED_BLACK] = enabled
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(isAmoledBlackEnabled = enabled)
        }
    }

    override fun toggleAutoLocation(enabled: Boolean) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_AUTO_LOCATION] = enabled
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(isAutoLocationEnabled = enabled)
        }
    }

    override fun updateQuranFontSize(sizeSp: Float) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_QURAN_FONT_SIZE] = sizeSp
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(quranFontSizeSp = sizeSp)
        }
    }

    override fun toggleAdhanNotification(enabled: Boolean) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_ADHAN_NOTIF] = enabled
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(isAdhanNotificationEnabled = enabled)
        }
    }

    override fun togglePrePrayerReminder(enabled: Boolean) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_PRE_PRAYER_NOTIF] = enabled
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(isPrePrayerReminderEnabled = enabled)
        }
    }

    override fun updatePrePrayerMinutes(minutes: Int) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_PRE_PRAYER_MINUTES] = minutes
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(prePrayerMinutes = minutes)
        }
    }

    override fun togglePrayerNotif(prayerName: String, enabled: Boolean) {
        val key = when (prayerName.lowercase()) {
            "subuh" -> KEY_NOTIF_SUBUH
            "dzuhur", "zuhur" -> KEY_NOTIF_DZUHUR
            "ashar", "asar" -> KEY_NOTIF_ASHAR
            "maghrib", "magrib" -> KEY_NOTIF_MAGHRIB
            "isya" -> KEY_NOTIF_ISYA
            else -> null
        }
        if (key != null && context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[key] = enabled
                }
            }
        }
    }

    override fun toggleNotificationSound(enabled: Boolean) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_NOTIF_SOUND] = enabled
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(isNotificationSoundEnabled = enabled)
        }
    }

    override fun updateNotificationSoundType(type: String) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_NOTIF_SOUND_TYPE] = type
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(notificationSoundType = type)
        }
    }

    override fun toggleDailyAyahNotification(enabled: Boolean) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_DAILY_AYAH_NOTIF] = enabled
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(isDailyAyahNotificationEnabled = enabled)
        }
    }

    override fun toggleTasbihVibration(enabled: Boolean) {
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_TASBIH_VIBE] = enabled
                }
            }
        } else {
            inMemorySettings.value = inMemorySettings.value.copy(isTasbihVibrationEnabled = enabled)
        }
    }

    override fun toggleTasbihSound(enabled: Boolean) {
        // Reserved
    }
}
