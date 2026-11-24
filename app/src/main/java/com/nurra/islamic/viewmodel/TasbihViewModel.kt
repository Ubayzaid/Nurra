package com.nurra.islamic.viewmodel

import androidx.lifecycle.ViewModel
import com.nurra.islamic.model.DhikrPreset
import com.nurra.islamic.repository.DoaRepository
import com.nurra.islamic.repository.LocalDoaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TasbihUiState(
    val counter: Int = 0,
    val lapCount: Int = 0,
    val targetCount: Int = 10,
    val selectedPreset: DhikrPreset = DhikrPreset("preset-1", "Subhanallah", "سُبْحَانَ اللَّهِ", "Maha Suci Allah", 10),
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = false,
    val totalCountEver: Int = 132
)

class TasbihViewModel(
    private val doaRepository: DoaRepository = LocalDoaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasbihUiState())
    val uiState: StateFlow<TasbihUiState> = _uiState.asStateFlow()

    fun getPresets(): List<DhikrPreset> = doaRepository.getDhikrPresets()

    fun incrementCounter() {
        val current = _uiState.value
        val nextCount = current.counter + 1
        val newTotal = current.totalCountEver + 1

        if (nextCount >= current.targetCount && current.targetCount > 0) {
            _uiState.value = current.copy(
                counter = 0,
                lapCount = current.lapCount + 1,
                totalCountEver = newTotal
            )
        } else {
            _uiState.value = current.copy(
                counter = nextCount,
                totalCountEver = newTotal
            )
        }
    }

    fun resetCounter() {
        _uiState.value = _uiState.value.copy(
            counter = 0,
            lapCount = 0
        )
    }

    fun setTargetCount(target: Int) {
        _uiState.value = _uiState.value.copy(
            targetCount = target,
            counter = 0
        )
    }

    fun selectPreset(preset: DhikrPreset) {
        _uiState.value = _uiState.value.copy(
            selectedPreset = preset,
            targetCount = preset.defaultTarget,
            counter = 0,
            lapCount = 0
        )
    }

    fun toggleVibration() {
        _uiState.value = _uiState.value.copy(
            isVibrationEnabled = !_uiState.value.isVibrationEnabled
        )
    }

    fun toggleSound() {
        _uiState.value = _uiState.value.copy(
            isSoundEnabled = !_uiState.value.isSoundEnabled
        )
    }
}
