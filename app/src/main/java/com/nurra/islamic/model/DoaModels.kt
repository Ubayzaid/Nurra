package com.nurra.islamic.model

enum class DoaCategory(val displayName: String) {
    SEMUA("Semua"),
    HARIAN("Doa Harian"),
    DZIKIR_PAGI("Dzikir Pagi"),
    DZIKIR_PETANG("Dzikir Petang"),
    SALAT("Dzikir Salat"),
    QURAN("Doa Al-Quran")
}

data class DoaItem(
    val id: String,
    val title: String,
    val category: DoaCategory,
    val arabicText: String,
    val latinText: String,
    val indonesianTranslation: String,
    val source: String = "",
    val readCount: Int = 1,
    val isFavorite: Boolean = false
)

data class DhikrPreset(
    val id: String,
    val nameLatin: String,
    val nameArabic: String,
    val meaning: String,
    val defaultTarget: Int = 33
)
