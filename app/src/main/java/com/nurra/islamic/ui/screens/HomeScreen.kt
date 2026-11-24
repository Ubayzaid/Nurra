package com.nurra.islamic.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nurra.islamic.notification.NotificationScheduler
import com.nurra.islamic.ui.components.DateStepperBar
import com.nurra.islamic.ui.components.HadithCard
import com.nurra.islamic.ui.components.PrayerScheduleTable
import com.nurra.islamic.ui.components.ResumeQuranCard
import com.nurra.islamic.ui.components.SkyHeroSection
import com.nurra.islamic.viewmodel.HomeViewModel
import com.nurra.islamic.viewmodel.LocationViewModel
import com.nurra.islamic.viewmodel.SettingsViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    locationViewModel: LocationViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToQuran: () -> Unit,
    onNavigateToSurahDetail: (Int) -> Unit,
    onNavigateToPrayer: () -> Unit,
    onNavigateToGoa: () -> Unit,
    onNavigateToTasbih: () -> Unit,
    onNavigateToQibla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prayerSchedule by viewModel.prayerScheduleState.collectAsState()
    val liveCountdown by viewModel.liveCountdownState.collectAsState()
    val ibadahSummary by viewModel.ibadahSummaryState.collectAsState()
    val activeLocation by locationViewModel.activeLocation.collectAsState()
    val locationState by locationViewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()

    val notificationScheduler = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Izin notifikasi ditolak. Pengingat adzan tidak dapat muncul.", Toast.LENGTH_SHORT).show()
        }
    }

    // Schedule alarms whenever prayerSchedule or settings change
    LaunchedEffect(prayerSchedule, settings) {
        prayerSchedule?.let { schedule ->
            NotificationScheduler(context).schedulePrayerAlarms(schedule, settings)
        }
    }

    // Real-time calculation of active/next, completed, and DataStore-synced notification status
    val nowMs = System.currentTimeMillis()
    val realTimePrayerList = prayerSchedule?.prayerList?.map { item ->
        val isNext = item.name.equals(liveCountdown.nextPrayerName, ignoreCase = true)

        val timeParts = item.time.replace(" WIB", "").replace(" WITA", "").replace(" WIT", "").trim().split(":")
        val isCompleted = if (timeParts.size == 2) {
            val hour = timeParts[0].toIntOrNull() ?: 0
            val minute = timeParts[1].toIntOrNull() ?: 0
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            nowMs >= cal.timeInMillis
        } else false

        // Sync bell notification state with DataStore Preferences in Settings
        val isNotifEnabledInSettings = when (item.name.lowercase()) {
            "subuh" -> settings.isSubuhNotifEnabled
            "dzuhur", "zuhur" -> settings.isDzuhurNotifEnabled
            "ashar", "asar" -> settings.isAsharNotifEnabled
            "maghrib", "magrib" -> settings.isMaghribNotifEnabled
            "isya" -> settings.isIsyaNotifEnabled
            else -> false
        } && settings.isAdhanNotificationEnabled

        item.copy(
            isNext = isNext,
            isCompleted = isCompleted,
            notificationEnabled = isNotifEnabledInSettings
        )
    } ?: emptyList()

    val currentSchedule = prayerSchedule?.copy(
        location = com.nurra.islamic.model.LocationInfo(
            cityName = activeLocation.cityName,
            countryName = activeLocation.countryName,
            latitude = activeLocation.latitude,
            longitude = activeLocation.longitude
        ),
        prayerList = realTimePrayerList
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Dynamic Sky Hero Canvas with Live Ticker Countdown (Hours : Minutes : Seconds)
            item {
                SkyHeroSection(
                    prayerSchedule = currentSchedule,
                    liveCountdown = liveCountdown,
                    onLocationClick = { locationViewModel.setSheetVisible(true) },
                    onQiblaClick = onNavigateToQibla
                )
            }

            // 2. Date Stepper Navigation Bar (Hijriah PRIMARY, Masehi SECONDARY)
            item {
                DateStepperBar(
                    dateMasehi = currentSchedule?.dateMasehi ?: "Rabu, 26 Februari 2025",
                    dateHijriah = currentSchedule?.dateHijriah ?: "27 Sya'ban 1446 H",
                    onPreviousDay = { },
                    onNextDay = { },
                    onTodayReset = { }
                )
            }

            // 3. Real-Time Prayer Times Schedule Table (Synced Bell Notification Toggle)
            currentSchedule?.let { s ->
                item {
                    PrayerScheduleTable(
                        prayerList = s.prayerList,
                        onNotificationToggle = { prayerType ->
                            val prayerItem = s.prayerList.find { it.type == prayerType }
                            if (prayerItem != null) {
                                // Request permission on Android 13+ if notification permission not granted
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (!hasPermission) {
                                        notificationScheduler.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }

                                val newTargetState = !prayerItem.notificationEnabled
                                settingsViewModel.togglePrayerNotif(prayerItem.name, newTargetState)

                                val toastMsg = if (newTargetState) {
                                    "Pengingat Adzan ${prayerItem.name} Dikitfkan 🔔"
                                } else {
                                    "Pengingat Adzan ${prayerItem.name} Dinonaktifkan 🔕"
                                }
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // 4. Hadits Hari Ini (HR. Bukhari No. 13)
            item {
                HadithCard(
                    hadith = viewModel.getDailyHadith()
                )
            }

            // 5. Lanjutkan Al-Qur'an (Resume Reading)
            item {
                ResumeQuranCard(
                    summary = ibadahSummary,
                    onResumeReading = { surahId -> onNavigateToSurahDetail(surahId) }
                )
            }
        }
    }

    // Modal Bottom Sheet for Location Picker
    if (locationState.isSheetVisible) {
        com.nurra.islamic.ui.components.LocationPickerBottomSheet(
            activeLocation = activeLocation,
            searchQuery = locationState.searchQuery,
            searchResults = locationState.searchResults,
            isSearching = locationState.isSearching,
            isGpsDetecting = locationState.isGpsDetecting,
            errorMessage = locationState.errorMessage,
            onSearchQueryChange = { locationViewModel.onSearchQueryChanged(it) },
            onSelectLocation = { location -> locationViewModel.selectLocation(location) },
            onDetectGpsLocation = { locationViewModel.detectGpsLocation() },
            onDismissRequest = { locationViewModel.setSheetVisible(false) }
        )
    }
}
