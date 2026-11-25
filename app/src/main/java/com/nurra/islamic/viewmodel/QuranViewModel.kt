package com.nurra.islamic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nurra.islamic.model.Ayah
import com.nurra.islamic.model.Surah
import com.nurra.islamic.repository.LocalQuranRepository
import com.nurra.islamic.repository.LocalSettingsRepository
import com.nurra.islamic.repository.QuranRepository
import com.nurra.islamic.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class QuranFilterTab {
    ALL, BOOKMARKS
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuranViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: QuranRepository = LocalQuranRepository(application.applicationContext)
    private val settingsRepository: SettingsRepository = LocalSettingsRepository(application.applicationContext)

    private val searchQuery = MutableStateFlow("")
    val searchState = searchQuery.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val selectedTab = MutableStateFlow(QuranFilterTab.ALL)
    val tabState = selectedTab.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuranFilterTab.ALL)

    val ibadahSummaryState: StateFlow<IbadahSummary> = settingsRepository.getSettings().map { settings ->
        IbadahSummary(
            completedPrayersCount = 4,
            totalPrayersCount = 5,
            tasbihCountToday = 132,
            lastReadSurahName = settings.lastReadSurahName,
            lastReadAyahNumber = settings.lastReadAyahNumber,
            lastReadSurahId = settings.lastReadSurahId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IbadahSummary())

    val surahs: StateFlow<List<Surah>> = combine(
        repository.getSurahs(),
        searchQuery,
        selectedTab
    ) { surahList, query, tab ->
        var filtered = surahList
        if (tab == QuranFilterTab.BOOKMARKS) {
            filtered = filtered.filter { it.isBookmarked }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.nameLatin.contains(query, ignoreCase = true) ||
                        it.translationIndonesian.contains(query, ignoreCase = true) ||
                        it.id.toString().contains(query)
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedAyahs: StateFlow<List<Ayah>> = repository.getBookmarkedAyahs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedSurahId = MutableStateFlow(1)

    val currentSurahDetail: StateFlow<Surah?> = selectedSurahId.flatMapLatest { id ->
        repository.getSurahs().combine(MutableStateFlow(id)) { list, surahId ->
            list.find { it.id == surahId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentAyahs: StateFlow<List<Ayah>> = selectedSurahId.flatMapLatest { id ->
        repository.getAyahsBySurahId(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSelectedTab(tab: QuranFilterTab) {
        selectedTab.value = tab
    }

    fun selectSurah(surahId: Int) {
        selectedSurahId.value = surahId
        val surah = repository.getSurahById(surahId)
        if (surah != null) {
            settingsRepository.updateLastReadQuran(surahId, surah.nameLatin, 1)
        }
    }

    fun updateLastReadPosition(surahId: Int, surahName: String, ayahNumber: Int) {
        settingsRepository.updateLastReadQuran(surahId, surahName, ayahNumber)
    }

    fun toggleSurahBookmark(surahId: Int) {
        repository.toggleBookmarkSurah(surahId)
    }

    fun toggleAyahBookmark(ayahId: Int) {
        repository.toggleBookmarkAyah(ayahId)
    }

    fun getSurahById(id: Int): Surah? {
        return repository.getSurahById(id)
    }
}
