package com.nurra.islamic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nurra.islamic.model.DailyPrayerSchedule
import com.nurra.islamic.model.LocationInfo
import com.nurra.islamic.model.PrayerType
import com.nurra.islamic.repository.LocalPrayerRepository
import com.nurra.islamic.repository.PrayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class PrayerViewModel(
    private val repository: PrayerRepository = LocalPrayerRepository()
) : ViewModel() {

    val dailySchedule: StateFlow<DailyPrayerSchedule?> = repository.getDailySchedule()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val isLocationPickerVisible = MutableStateFlow(false)
    val showLocationPicker: StateFlow<Boolean> = isLocationPickerVisible.asStateFlow()

    fun getAvailableLocations(): List<LocationInfo> = repository.getAvailableLocations()

    fun setLocationPickerVisible(visible: Boolean) {
        isLocationPickerVisible.value = visible
    }

    fun selectLocation(location: LocationInfo) {
        repository.updateLocation(location)
        isLocationPickerVisible.value = false
    }

    fun toggleNotification(prayerType: PrayerType) {
        repository.togglePrayerNotification(prayerType)
    }
}
