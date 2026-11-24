package com.nurra.islamic.model

data class Hadith(
    val id: String,
    val title: String,
    val arabicText: String,
    val indonesianTranslation: String,
    val source: String, // e.g., HR. Bukhari
    val bookName: String, // e.g., Shahih Bukhari
    val hadithNumber: Int // e.g., 13
)
