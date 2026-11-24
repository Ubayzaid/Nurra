package com.nurra.islamic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nurra.islamic.model.Ayah
import com.nurra.islamic.model.Surah
import com.nurra.islamic.repository.LocalQuranRepository
import com.nurra.islamic.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class QuranFilterTab {
    ALL, BOOKMARKS
}

class QuranViewModel(
    private val repository: QuranRepository = LocalQuranRepository()
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    val searchState = searchQuery.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val selectedTab = MutableStateFlow(QuranFilterTab.ALL)
    val tabState = selectedTab.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuranFilterTab.ALL)

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
