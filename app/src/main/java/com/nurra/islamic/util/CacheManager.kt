package com.nurra.islamic.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nurra.islamic.model.Ayah
import com.nurra.islamic.model.DailyPrayerSchedule
import com.nurra.islamic.model.DoaItem
import com.nurra.islamic.model.Hadith
import com.nurra.islamic.model.Surah
import java.io.File

class CacheManager(private val context: Context) {

    private val gson = Gson()

    fun savePrayerSchedule(key: String, schedule: DailyPrayerSchedule) {
        try {
            val file = File(context.cacheDir, "prayer_$key.json")
            file.writeText(gson.toJson(schedule))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPrayerSchedule(key: String): DailyPrayerSchedule? {
        return try {
            val file = File(context.cacheDir, "prayer_$key.json")
            if (file.exists()) {
                gson.fromJson(file.readText(), DailyPrayerSchedule::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun saveSurahs(surahs: List<Surah>) {
        try {
            val file = File(context.cacheDir, "surahs_cache_v2.json")
            file.writeText(gson.toJson(surahs))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSurahs(): List<Surah>? {
        return try {
            val file = File(context.cacheDir, "surahs_cache_v2.json")
            if (file.exists()) {
                val type = object : TypeToken<List<Surah>>() {}.type
                gson.fromJson(file.readText(), type)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun saveSurahAyahs(surahId: Int, ayahs: List<Ayah>) {
        try {
            val file = File(context.cacheDir, "surah_ayahs_$surahId.json")
            file.writeText(gson.toJson(ayahs))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSurahAyahs(surahId: Int): List<Ayah>? {
        return try {
            val file = File(context.cacheDir, "surah_ayahs_$surahId.json")
            if (file.exists()) {
                val type = object : TypeToken<List<Ayah>>() {}.type
                gson.fromJson(file.readText(), type)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun saveDoas(doas: List<DoaItem>) {
        try {
            val file = File(context.cacheDir, "doas_cache_v2.json")
            file.writeText(gson.toJson(doas))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDoas(): List<DoaItem>? {
        return try {
            val file = File(context.cacheDir, "doas_cache_v2.json")
            if (file.exists()) {
                val type = object : TypeToken<List<DoaItem>>() {}.type
                gson.fromJson(file.readText(), type)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun saveHadith(hadith: Hadith) {
        try {
            val file = File(context.cacheDir, "daily_hadith.json")
            file.writeText(gson.toJson(hadith))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHadith(): Hadith? {
        return try {
            val file = File(context.cacheDir, "daily_hadith.json")
            if (file.exists()) {
                gson.fromJson(file.readText(), Hadith::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
