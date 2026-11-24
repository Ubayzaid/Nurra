package com.nurra.islamic.model

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameLatin: String,
    val translationIndonesian: String,
    val verseCount: Int,
    val revelationType: String, // Makkiyah or Madaniyah
    val audioUrl: String = "",
    val isBookmarked: Boolean = false
)

data class Ayah(
    val id: Int,
    val surahId: Int,
    val verseNumber: Int,
    val arabicText: String,
    val latinText: String,
    val indonesianTranslation: String,
    val isBookmarked: Boolean = false,
    val isPlaying: Boolean = false
)

data class FeaturedAyah(
    val arabicText: String,
    val latinText: String,
    val translation: String,
    val surahName: String,
    val verseRef: String
)
