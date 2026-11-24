package com.nurra.islamic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nurra.islamic.repository.AppSettings
import com.nurra.islamic.repository.AppThemeMode
import com.nurra.islamic.repository.LocalSettingsRepository
import com.nurra.islamic.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: SettingsRepository = LocalSettingsRepository(application.applicationContext)

    val settings: StateFlow<AppSettings> = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun updateThemeMode(mode: AppThemeMode) {
        repository.updateThemeMode(mode)
    }

    fun toggleDynamicColor(enabled: Boolean) {
        repository.toggleDynamicColor(enabled)
    }

    fun toggleAmoledBlack(enabled: Boolean) {
        repository.toggleAmoledBlack(enabled)
    }

    fun toggleAutoLocation(enabled: Boolean) {
        repository.toggleAutoLocation(enabled)
    }

    fun togglePrePrayerReminder(enabled: Boolean) {
        repository.togglePrePrayerReminder(enabled)
    }

    fun updatePrePrayerMinutes(minutes: Int) {
        repository.updatePrePrayerMinutes(minutes)
    }

    fun togglePrayerNotif(prayerName: String, enabled: Boolean) {
        repository.togglePrayerNotif(prayerName, enabled)
    }

    fun toggleNotificationSound(enabled: Boolean) {
        repository.toggleNotificationSound(enabled)
    }

    fun updateNotificationSoundType(type: String) {
        repository.updateNotificationSoundType(type)
    }

    fun updateQuranFontSize(sizeSp: Float) {
        repository.updateQuranFontSize(sizeSp)
    }

    fun toggleAdhanNotification(enabled: Boolean) {
        repository.toggleAdhanNotification(enabled)
    }

    fun toggleDailyAyahNotification(enabled: Boolean) {
        repository.toggleDailyAyahNotification(enabled)
    }

    fun toggleTasbihVibration(enabled: Boolean) {
        repository.toggleTasbihVibration(enabled)
    }
}
