package com.nurra.islamic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nurra.islamic.model.DoaCategory
import com.nurra.islamic.model.DoaItem
import com.nurra.islamic.repository.DoaRepository
import com.nurra.islamic.repository.LocalDoaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class DoaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: DoaRepository = LocalDoaRepository(application.applicationContext)

    private val _searchQuery = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(DoaCategory.SEMUA)
    val categoryState: StateFlow<DoaCategory> = _selectedCategory.asStateFlow()

    val doas: StateFlow<List<DoaItem>> = combine(
        repository.getDoas(),
        _searchQuery,
        _selectedCategory
    ) { allDoas, query, category ->
        allDoas.filter { doa ->
            val matchesCategory = (category == DoaCategory.SEMUA) || (doa.category == category)
            val matchesSearch = query.isBlank() ||
                    doa.title.contains(query, ignoreCase = true) ||
                    doa.indonesianTranslation.contains(query, ignoreCase = true) ||
                    doa.latinText.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: DoaCategory) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(doaId: String) {
        repository.toggleFavoriteDoa(doaId)
    }
}
