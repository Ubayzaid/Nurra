package com.nurra.islamic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nurra.islamic.model.UserLocation
import com.nurra.islamic.repository.LocalLocationRepository
import com.nurra.islamic.repository.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LocationUiState(
    val activeLocation: UserLocation = UserLocation(),
    val searchQuery: String = "",
    val searchResults: List<UserLocation> = emptyList(),
    val isSearching: Boolean = false,
    val isGpsDetecting: Boolean = false,
    val errorMessage: String? = null,
    val isSheetVisible: Boolean = false
)

class LocationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: LocationRepository = LocalLocationRepository(application.applicationContext)

    val activeLocation: StateFlow<UserLocation> = repository.getActiveLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserLocation())

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun setSheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(
            isSheetVisible = visible,
            errorMessage = null,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }

        // Debounce search by 400ms to avoid excessive geocoding calls
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.value = _uiState.value.copy(isSearching = true, errorMessage = null)
            val results = repository.searchLocations(query)
            _uiState.value = _uiState.value.copy(
                searchResults = results,
                isSearching = false,
                errorMessage = if (results.isEmpty()) "Lokasi '$query' tidak ditemukan" else null
            )
        }
    }

    fun selectLocation(location: UserLocation) {
        repository.updateActiveLocation(location)
        _uiState.value = _uiState.value.copy(
            isSheetVisible = false,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun detectGpsLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGpsDetecting = true, errorMessage = null)
            val result = repository.getCurrentGpsLocation()
            result.onSuccess { location ->
                repository.updateActiveLocation(location)
                _uiState.value = _uiState.value.copy(
                    isGpsDetecting = false,
                    isSheetVisible = false
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isGpsDetecting = false,
                    errorMessage = exception.message ?: "Gagal mendeteksi lokasi GPS"
                )
            }
        }
    }
}
