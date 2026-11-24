package com.nurra.islamic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nurra.islamic.model.DailyPrayerSchedule
import com.nurra.islamic.model.FeaturedAyah
import com.nurra.islamic.model.Hadith
import com.nurra.islamic.model.PrayerType
import com.nurra.islamic.model.Surah
import com.nurra.islamic.model.UserLocation
import com.nurra.islamic.repository.HadithRepository
import com.nurra.islamic.repository.LocalHadithRepository
import com.nurra.islamic.repository.LocalLocationRepository
import com.nurra.islamic.repository.LocalPrayerRepository
import com.nurra.islamic.repository.LocalQuranRepository
import com.nurra.islamic.repository.LocationRepository
import com.nurra.islamic.repository.PrayerRepository
import com.nurra.islamic.repository.QuranRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class LiveCountdownState(
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0,
    val hoursFormatted: String = "00",
    val minutesFormatted: String = "00",
    val secondsFormatted: String = "00",
    val nextPrayerName: String = "Dzuhur",
    val nextPrayerTime: String = "12:08 WIB",
    val prevPrayerName: String = "Terbit",
    val prevPrayerTime: String = "05:48 WIB",
    val progress: Float = 0.75f
)

data class IbadahSummary(
    val completedPrayersCount: Int = 4,
    val totalPrayersCount: Int = 5,
    val tasbihCountToday: Int = 132,
    val lastReadSurahName: String = "Al-Kahf",
    val lastReadAyahNumber: Int = 10,
    val lastReadSurahId: Int = 18
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val quranRepository: QuranRepository = LocalQuranRepository(application.applicationContext)
    private val prayerRepository: PrayerRepository = LocalPrayerRepository(application.applicationContext)
    private val hadithRepository: HadithRepository = LocalHadithRepository()
    private val locationRepository: LocationRepository = LocalLocationRepository(application.applicationContext)

    val activeLocationState: StateFlow<UserLocation> = locationRepository.getActiveLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserLocation())

    val prayerScheduleState: StateFlow<DailyPrayerSchedule?> = activeLocationState.flatMapLatest { loc ->
        prayerRepository.getDailySchedule(loc.apiCityId, loc.timezoneId, "today")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val surahsState: StateFlow<List<Surah>> = quranRepository.getSurahs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _liveCountdown = MutableStateFlow(LiveCountdownState())
    val liveCountdownState: StateFlow<LiveCountdownState> = _liveCountdown.asStateFlow()

    private val _ibadahSummary = MutableStateFlow(IbadahSummary())
    val ibadahSummaryState: StateFlow<IbadahSummary> = _ibadahSummary.asStateFlow()

    private val _currentHadith = MutableStateFlow(hadithRepository.getDailyHadith())
    val currentHadithState: StateFlow<Hadith> = _currentHadith.asStateFlow()

    private val _isHadithLoading = MutableStateFlow(false)
    val isHadithLoadingState: StateFlow<Boolean> = _isHadithLoading.asStateFlow()

    private var tickerJob: Job? = null

    init {
        startLiveCountdownTicker()
    }

    fun startLiveCountdownTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                updateLiveCountdown()
                delay(1000) // Ticker runs every 1 second locally
            }
        }
    }

    fun refreshRandomHadith() {
        viewModelScope.launch {
            _isHadithLoading.value = true
            delay(500) // Smooth loading feedback
            _currentHadith.value = hadithRepository.getRandomHadith()
            _isHadithLoading.value = false
        }
    }

    private fun updateLiveCountdown() {
        val schedule = prayerScheduleState.value
        val now = Calendar.getInstance()
        val currentMs = now.timeInMillis

        // Prayer times for today
        val prayerTimePairs = schedule?.prayerList?.mapNotNull { item ->
            val timeParts = item.time.replace(" WIB", "").replace(" WITA", "").replace(" WIT", "").trim().split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toIntOrNull() ?: return@mapNotNull null
                val minute = timeParts[1].toIntOrNull() ?: return@mapNotNull null

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                Pair(item, cal.timeInMillis)
            } else null
        } ?: emptyList()

        if (prayerTimePairs.isEmpty()) {
            calculateFallbackCountdown(currentMs)
            return
        }

        // Find the FIRST upcoming prayer where targetMs > currentMs
        var upcoming = prayerTimePairs.firstOrNull { it.second > currentMs }
        var prevPrayer = prayerTimePairs.lastOrNull { it.second <= currentMs }

        // If current time is past Isya today, target Subuh of tomorrow
        if (upcoming == null) {
            val tomorrowSubuhCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                val subuhItem = schedule?.prayerList?.find { it.type == PrayerType.SUBUH }
                val parts = (subuhItem?.time ?: "04:35 WIB").replace(" WIB", "").replace(" WITA", "").replace(" WIT", "").trim().split(":")
                set(Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toIntOrNull() ?: 4)
                set(Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 35)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val subuhItem = schedule?.prayerList?.find { it.type == PrayerType.SUBUH }
                ?: prayerTimePairs.first().first

            upcoming = Pair(subuhItem, tomorrowSubuhCal.timeInMillis)
            prevPrayer = prayerTimePairs.last()
        }

        if (prevPrayer == null) {
            prevPrayer = prayerTimePairs.first()
        }

        val targetMs = upcoming.second
        val prevMs = prevPrayer.second

        val diffMs = (targetMs - currentMs).coerceAtLeast(0)

        val totalSecs = diffMs / 1000
        val hours = (totalSecs / 3600).toInt()
        val minutes = ((totalSecs % 3600) / 60).toInt()
        val seconds = (totalSecs % 60).toInt()

        val totalIntervalMs = (targetMs - prevMs).coerceAtLeast(1)
        val elapsedMs = (currentMs - prevMs).coerceAtLeast(0)
        val progress = (elapsedMs.toFloat() / totalIntervalMs.toFloat()).coerceIn(0f, 1f)

        _liveCountdown.value = LiveCountdownState(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            hoursFormatted = "%02d".format(hours),
            minutesFormatted = "%02d".format(minutes),
            secondsFormatted = "%02d".format(seconds),
            nextPrayerName = upcoming.first.name,
            nextPrayerTime = upcoming.first.time,
            prevPrayerName = prevPrayer.first.name,
            prevPrayerTime = prevPrayer.first.time,
            progress = progress
        )
    }

    private fun calculateFallbackCountdown(currentMs: Long) {
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 8)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (targetCal.timeInMillis < currentMs) {
            targetCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val diffMs = (targetCal.timeInMillis - currentMs).coerceAtLeast(0)
        val totalSecs = diffMs / 1000
        val hours = (totalSecs / 3600).toInt()
        val minutes = ((totalSecs % 3600) / 60).toInt()
        val seconds = (totalSecs % 60).toInt()

        _liveCountdown.value = LiveCountdownState(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            hoursFormatted = "%02d".format(hours),
            minutesFormatted = "%02d".format(minutes),
            secondsFormatted = "%02d".format(seconds),
            nextPrayerName = "Dzuhur",
            nextPrayerTime = "12:08 WIB",
            prevPrayerName = "Terbit",
            prevPrayerTime = "05:48 WIB",
            progress = 0.75f
        )
    }

    fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 3..10 -> "Selamat Pagi 🌅"
            in 11..14 -> "Selamat Siang ☀️"
            in 15..17 -> "Selamat Sore 🌤️"
            else -> "Selamat Malam 🌙"
        }
    }

    fun getFeaturedAyah(): FeaturedAyah {
        return quranRepository.getFeaturedAyah()
    }

    fun getDailyHadith(): Hadith {
        return _currentHadith.value
    }
}
