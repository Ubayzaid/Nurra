package com.nurra.islamic.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nurra.islamic.api.ApiClient
import com.nurra.islamic.model.Ayah
import com.nurra.islamic.model.FeaturedAyah
import com.nurra.islamic.model.Surah
import com.nurra.islamic.util.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface QuranRepository {
    fun getSurahs(): Flow<List<Surah>>
    fun getSurahById(id: Int): Surah?
    fun getAyahsBySurahId(surahId: Int): Flow<List<Ayah>>
    fun getBookmarkedAyahs(): Flow<List<Ayah>>
    fun getFeaturedAyah(): FeaturedAyah
    fun toggleBookmarkSurah(surahId: Int)
    fun toggleBookmarkAyah(ayahId: Int)
}

class LocalQuranRepository(
    private val context: Context? = null
) : QuranRepository {

    private val TAG = "NurraQuranRepo"
    private val gson = Gson()
    private val cacheManager = context?.let { CacheManager(it) }

    private val KEY_BOOKMARKED_SURAHS = stringPreferencesKey("bookmarked_surahs")
    private val KEY_BOOKMARKED_AYAHS = stringPreferencesKey("bookmarked_ayahs")

    private val bookmarkedSurahIds = MutableStateFlow<Set<Int>>(setOf(1, 18, 36, 67))
    private val bookmarkedAyahIds = MutableStateFlow<Set<Int>>(setOf(1001, 18010))

    private val fallbackSurahs = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "Pembukaan", 7, "Mekah"),
        Surah(2, "البقرة", "Al-Baqarah", "Sapi Betina", 286, "Madinah"),
        Surah(3, "آل عمران", "Ali 'Imran", "Keluarga 'Imran", 200, "Madinah"),
        Surah(4, "النساء", "An-Nisa'", "Wanita", 176, "Madinah"),
        Surah(5, "المائدة", "Al-Ma'idah", "Hidangan", 120, "Madinah"),
        Surah(6, "الأنعام", "Al-An'am", "Hewan Ternak", 165, "Mekah"),
        Surah(7, "الأعراف", "Al-A'raf", "Tempat yang Tertinggi", 206, "Mekah"),
        Surah(8, "الأنفال", "Al-Anfal", "Rampasan Perang", 75, "Madinah"),
        Surah(9, "التوبة", "At-Tawbah", "Pengampunan", 129, "Madinah"),
        Surah(10, "يونس", "Yunus", "Nabi Yunus", 109, "Mekah"),
        Surah(11, "هود", "Hud", "Nabi Hud", 123, "Mekah"),
        Surah(12, "يوسف", "Yusuf", "Nabi Yusuf", 111, "Mekah"),
        Surah(13, "الرعد", "Ar-Ra'd", "Guruh", 43, "Madinah"),
        Surah(14, "إبراهيم", "Ibrahim", "Nabi Ibrahim", 52, "Mekah"),
        Surah(15, "الحجر", "Al-Hijr", "Gunung Batu", 99, "Mekah"),
        Surah(16, "النحل", "An-Nahl", "Lebah", 128, "Mekah"),
        Surah(17, "الإسراء", "Al-Isra'", "Perjalanan Malam", 111, "Mekah"),
        Surah(18, "الكهف", "Al-Kahf", "Gua", 110, "Mekah"),
        Surah(19, "مريم", "Maryam", "Maryam", 98, "Mekah"),
        Surah(20, "طه", "Taha", "Taha", 135, "Mekah"),
        Surah(21, "الأنبياء", "Al-Anbiya'", "Nabi-Nabi", 112, "Mekah"),
        Surah(22, "الحج", "Al-Hajj", "Haji", 78, "Madinah"),
        Surah(23, "المؤمنون", "Al-Mu'minun", "Orang-Orang Mukmin", 118, "Mekah"),
        Surah(24, "النور", "An-Nur", "Cahaya", 64, "Madinah"),
        Surah(25, "الفرقان", "Al-Furqan", "Pembeda", 77, "Mekah"),
        Surah(26, "الشعراء", "Asy-Syu'ara'", "Penyair", 227, "Mekah"),
        Surah(27, "النمل", "An-Naml", "Semut", 93, "Mekah"),
        Surah(28, "القصص", "Al-Qasas", "Kisah-Kisah", 88, "Mekah"),
        Surah(29, "العنكبوت", "Al-'Ankabut", "Laba-Laba", 69, "Mekah"),
        Surah(30, "الروم", "Ar-Rum", "Bangsa Romawi", 60, "Mekah"),
        Surah(31, "لقمان", "Luqman", "Keluarga Luqman", 34, "Mekah"),
        Surah(32, "السجدة", "As-Sajdah", "Sajdah", 30, "Mekah"),
        Surah(33, "الأحزاب", "Al-Ahzab", "Golongan Bersekutu", 73, "Madaniyah"),
        Surah(34, "سبإ", "Saba'", "Kaum Saba'", 54, "Mekah"),
        Surah(35, "فاطر", "Fatir", "Pencipta", 45, "Mekah"),
        Surah(36, "يس", "Yasin", "Yasin", 83, "Mekah"),
        Surah(37, "الصافات", "As-Saffat", "Yang Berbaris-Baris", 182, "Mekah"),
        Surah(38, "ص", "Sad", "Sad", 88, "Mekah"),
        Surah(39, "الزمر", "Az-Zumar", "Rombongan", 75, "Mekah"),
        Surah(40, "غافر", "Ghafir", "Yang Mengampuni", 85, "Mekah"),
        Surah(41, "فصلت", "Fussilat", "Yang Dijelaskan", 54, "Mekah"),
        Surah(42, "الشورى", "Asy-Syura", "Musyawarah", 53, "Mekah"),
        Surah(43, "الزخرف", "Az-Zukhruf", "Perhiasan", 89, "Mekah"),
        Surah(44, "الدخان", "Ad-Dukhan", "Kabut", 59, "Mekah"),
        Surah(45, "الجاثية", "Al-Jasiyah", "Yang Berlutut", 37, "Mekah"),
        Surah(46, "الأحقاف", "Al-Ahqaf", "Bukit-Bukit Pasir", 35, "Mekah"),
        Surah(47, "محمد", "Muhammad", "Nabi Muhammad", 38, "Madinah"),
        Surah(48, "الفتح", "Al-Fath", "Kemenangan", 29, "Madinah"),
        Surah(49, "الحجرات", "Al-Hujurat", "Kamar-Kamar", 18, "Madinah"),
        Surah(50, "ق", "Qaf", "Qaf", 45, "Mekah"),
        Surah(51, "الذاريات", "Az-Zariyat", "Angin yang Menerbangkan", 60, "Mekah"),
        Surah(52, "الطور", "At-Tur", "Bukit Tursina", 49, "Mekah"),
        Surah(53, "النجم", "An-Najm", "Bintang", 62, "Mekah"),
        Surah(54, "القمر", "Al-Qamar", "Bulan", 55, "Mekah"),
        Surah(55, "الرحمن", "Ar-Rahman", "Yang Maha Pengasih", 78, "Madinah"),
        Surah(56, "الواقعة", "Al-Waqi'ah", "Hari Kiamat", 96, "Mekah"),
        Surah(57, "الحديد", "Al-Hadid", "Besi", 29, "Madinah"),
        Surah(58, "المجادلة", "Al-Mujadilah", "Gugatan", 22, "Madinah"),
        Surah(59, "الحشر", "Al-Hasyr", "Pengusiran", 24, "Madinah"),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "Wanita yang Diuji", 13, "Madinah"),
        Surah(61, "الصف", "As-Saff", "Barisan", 14, "Madinah"),
        Surah(62, "الجمعة", "Al-Jumu'ah", "Hari Jum'at", 11, "Madinah"),
        Surah(63, "المنافقون", "Al-Munafiqun", "Orang-Orang Munafik", 11, "Madinah"),
        Surah(64, "التغابن", "At-Taghabun", "Hari Dinampakkan Kesalahan", 18, "Madinah"),
        Surah(65, "الطلاق", "At-Talaq", "Talak", 12, "Madinah"),
        Surah(66, "التحريم", "At-Tahrim", "Mengharamkan", 12, "Madinah"),
        Surah(67, "الملك", "Al-Mulk", "Kerajaan", 30, "Mekah"),
        Surah(68, "القلم", "Al-Qalam", "Pena", 52, "Mekah"),
        Surah(69, "الحاقة", "Al-Haqqah", "Hari Kiamat", 52, "Mekah"),
        Surah(70, "المعارج", "Al-Ma'arij", "Tempat Naik", 44, "Mekah"),
        Surah(71, "نوح", "Nuh", "Nabi Nuh", 28, "Mekah"),
        Surah(72, "الجن", "Al-Jinn", "Jin", 28, "Mekah"),
        Surah(73, "المزمل", "Al-Muzzammil", "Orang yang Berselimut", 20, "Mekah"),
        Surah(74, "المدثر", "Al-Muddassir", "Orang yang Berkemul", 56, "Mekah"),
        Surah(75, "القيامة", "Al-Qiyamah", "Hari Kiamat", 40, "Mekah"),
        Surah(76, "الإنسان", "Al-Insan", "Manusia", 31, "Madinah"),
        Surah(77, "المرسلات", "Al-Mursalat", "Malaikat Yang Diutus", 50, "Mekah"),
        Surah(78, "النبإ", "An-Naba'", "Berita Besar", 40, "Mekah"),
        Surah(79, "النازعات", "An-Nazi'at", "Malaikat Yang Mencabut", 46, "Mekah"),
        Surah(80, "عبس", "Abasa", "Ia Bermuka Masam", 42, "Mekah"),
        Surah(81, "التكوير", "At-Takwir", "Menggulung", 29, "Mekah"),
        Surah(82, "الانفطار", "Al-Infitar", "Terbelah", 19, "Mekah"),
        Surah(83, "المطففين", "Al-Mutaffifin", "Orang-Orang Curang", 36, "Mekah"),
        Surah(84, "الانشقاق", "Al-Insyiqaq", "Terbelah", 25, "Mekah"),
        Surah(85, "البروج", "Al-Buruj", "Gugusan Bintang", 22, "Mekah"),
        Surah(86, "الطارق", "At-Tariq", "Yang Datang Di Malam Hari", 17, "Mekah"),
        Surah(87, "الأعلى", "Al-A'la", "Yang Maha Tinggi", 19, "Mekah"),
        Surah(88, "الغاشية", "Al-Ghasyiyah", "Hari Pembalasan", 26, "Mekah"),
        Surah(89, "الفجر", "Al-Fajr", "Fajar", 30, "Mekah"),
        Surah(90, "البلد", "Al-Balad", "Negeri", 20, "Mekah"),
        Surah(91, "الشمس", "Asy-Syams", "Matahari", 15, "Mekah"),
        Surah(92, "الليل", "Al-Lail", "Malam", 21, "Mekah"),
        Surah(93, "الضحى", "Ad-Duha", "Waktu Duha", 11, "Mekah"),
        Surah(94, "الشرح", "Asy-Syarh", "Kelapangan", 8, "Mekah"),
        Surah(95, "التين", "At-Tin", "Buah Tin", 8, "Mekah"),
        Surah(96, "العلق", "Al-'Alaq", "Segumpal Darah", 19, "Mekah"),
        Surah(97, "القدر", "Al-Qadr", "Kemuliaan", 5, "Mekah"),
        Surah(98, "البينة", "Al-Bayyinah", "Bukti Nyata", 8, "Madinah"),
        Surah(99, "الزلزلة", "Az-Zalzalah", "Kegoncangan", 8, "Madinah"),
        Surah(100, "العاديات", "Al-'Adiyat", "Kuda Perang Yang Berlari Kencang", 11, "Mekah"),
        Surah(101, "القارعة", "Al-Qari'ah", "Hari Kiamat", 11, "Mekah"),
        Surah(102, "التكاثر", "At-Takasur", "Bermewah-Mewahan", 8, "Mekah"),
        Surah(103, "العصر", "Al-'Asr", "Masa", 3, "Mekah"),
        Surah(104, "الهمزة", "Al-Humazah", "Pengumpat", 9, "Mekah"),
        Surah(105, "الفيل", "Al-Fil", "Gajah", 5, "Mekah"),
        Surah(106, "قريش", "Quraisy", "Suku Quraisy", 4, "Mekah"),
        Surah(107, "الماعون", "Al-Ma'un", "Barang-Barang Yang Berguna", 7, "Mekah"),
        Surah(108, "الكوثر", "Al-Kausar", "Nikmat Yang Banyak", 3, "Mekah"),
        Surah(109, "الكافرون", "Al-Kafirun", "Orang-Orang Kafir", 6, "Mekah"),
        Surah(110, "النصر", "An-Nasr", "Pertolongan", 3, "Madinah"),
        Surah(111, "المسد", "Al-Lahab", "Gejolak Api", 5, "Mekah"),
        Surah(112, "الإخلاص", "Al-Ikhlas", "Ikhlas", 4, "Mekah"),
        Surah(113, "الفلق", "Al-Falaq", "Waktu Subuh", 5, "Mekah"),
        Surah(114, "الناس", "An-Nas", "Manusia", 6, "Mekah")
    )

    private val liveSurahList = MutableStateFlow<List<Surah>>(
        cacheManager?.getSurahs() ?: fallbackSurahs
    )

    private val liveAyahsMap = mutableMapOf<Int, MutableStateFlow<List<Ayah>>>()

    init {
        loadBookmarksFromDataStore()
        fetchSurahsFromApi()
    }

    private fun loadBookmarksFromDataStore() {
        if (context == null) return
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.data.collect { prefs ->
                val surahsJson = prefs[KEY_BOOKMARKED_SURAHS] ?: ""
                val ayahsJson = prefs[KEY_BOOKMARKED_AYAHS] ?: ""

                if (surahsJson.isNotBlank()) {
                    try {
                        val type = object : TypeToken<Set<Int>>() {}.type
                        val set: Set<Int> = gson.fromJson(surahsJson, type)
                        bookmarkedSurahIds.value = set
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (ayahsJson.isNotBlank()) {
                    try {
                        val type = object : TypeToken<Set<Int>>() {}.type
                        val set: Set<Int> = gson.fromJson(ayahsJson, type)
                        bookmarkedAyahIds.value = set
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun sanitizeQuranicArabicText(rawText: String): String {
        return rawText
            .replace("ࣖ", "")
            .replace("ۙ", "")
            .replace("ۚ", "")
            .replace("ۗ", "")
            .replace("ۛ", "")
            .replace("ۜ", "")
            .replace("۠", "")
            .replace("ۡ", "")
            .replace("ۢ", "")
            .replace("[\\u08D0-\\u08FF]".toRegex(), "")
            .trim()
    }

    private fun fetchSurahsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Fetching 114 Surahs from EQuran API v2...")
                val response = ApiClient.eQuranApiService.getAllSurahs()

                if (response.isSuccessful && response.body()?.code == 200) {
                    val dtos = response.body()?.data ?: emptyList()
                    if (dtos.isNotEmpty()) {
                        Log.d(TAG, "Successfully fetched ${dtos.size} surahs from EQuran API v2!")

                        val mappedSurahs = dtos.map { dto ->
                            val formattedNumber = "%03d".format(dto.nomor)
                            val audio = "https://cdn.equran.id/audio-full/Misyari-Rasyid-Al-Afasi/$formattedNumber.mp3"

                            Surah(
                                id = dto.nomor,
                                nameArabic = sanitizeQuranicArabicText(dto.nama),
                                nameLatin = dto.namaLatin,
                                translationIndonesian = dto.arti,
                                verseCount = dto.jumlahAyat,
                                revelationType = dto.tempatTurun,
                                audioUrl = audio
                            )
                        }

                        cacheManager?.saveSurahs(mappedSurahs)
                        liveSurahList.value = mappedSurahs
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching surahs: ${e.message}. Using fallback/cached surahs.")
            }
        }
    }

    override fun getSurahs(): Flow<List<Surah>> {
        return liveSurahList.map { list ->
            val bookmarks = bookmarkedSurahIds.value
            list.map { surah ->
                surah.copy(isBookmarked = bookmarks.contains(surah.id))
            }
        }
    }

    override fun getSurahById(id: Int): Surah? {
        val surah = liveSurahList.value.find { it.id == id } ?: fallbackSurahs.find { it.id == id } ?: return null
        return surah.copy(isBookmarked = bookmarkedSurahIds.value.contains(id))
    }

    override fun getAyahsBySurahId(surahId: Int): Flow<List<Ayah>> {
        if (!liveAyahsMap.containsKey(surahId)) {
            val cachedAyahs = cacheManager?.getSurahAyahs(surahId)
            val initialList = cachedAyahs ?: emptyList()
            liveAyahsMap[surahId] = MutableStateFlow(initialList)

            // Fetch complete verses from API
            fetchSurahDetailFromApi(surahId)
        }

        val flow = liveAyahsMap[surahId]!!
        return flow.map { list ->
            val bookmarks = bookmarkedAyahIds.value
            list.map { ayah ->
                ayah.copy(isBookmarked = bookmarks.contains(ayah.id))
            }
        }
    }

    override fun getBookmarkedAyahs(): Flow<List<Ayah>> {
        return bookmarkedAyahIds.map { ayahIds ->
            val result = mutableListOf<Ayah>()
            ayahIds.forEach { ayahId ->
                val surahId = ayahId / 1000
                val verseNum = ayahId % 1000
                val cachedAyahs = cacheManager?.getSurahAyahs(surahId)
                val ayah = cachedAyahs?.find { it.verseNumber == verseNum }
                if (ayah != null) {
                    result.add(ayah.copy(isBookmarked = true))
                } else {
                    // Fallback item
                    val surahName = getSurahById(surahId)?.nameLatin ?: "Surah $surahId"
                    result.add(
                        Ayah(
                            id = ayahId,
                            surahId = surahId,
                            verseNumber = verseNum,
                            arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً",
                            latinText = "$surahName: Ayat $verseNum",
                            indonesianTranslation = "Ayat tersimpan dari Surah $surahName",
                            isBookmarked = true
                        )
                    )
                }
            }
            result
        }
    }

    private fun fetchSurahDetailFromApi(surahId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Fetching complete verses for Surah $surahId from EQuran API...")
                val response = ApiClient.eQuranApiService.getSurahDetail(surahId)

                if (response.isSuccessful && response.body()?.code == 200) {
                    val detail = response.body()?.data
                    val ayatList = detail?.ayat ?: emptyList()

                    if (ayatList.isNotEmpty()) {
                        Log.d(TAG, "Successfully fetched ${ayatList.size} complete verses for Surah $surahId!")

                        val mappedAyahs = ayatList.map { dto ->
                            Ayah(
                                id = surahId * 1000 + dto.nomorAyat,
                                surahId = surahId,
                                verseNumber = dto.nomorAyat,
                                arabicText = sanitizeQuranicArabicText(dto.teksArab),
                                latinText = dto.teksLatin,
                                indonesianTranslation = dto.teksIndonesia
                            )
                        }

                        cacheManager?.saveSurahAyahs(surahId, mappedAyahs)
                        liveAyahsMap[surahId]?.value = mappedAyahs
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching verses for Surah $surahId: ${e.message}")
            }
        }
    }

    override fun getFeaturedAyah(): FeaturedAyah {
        return FeaturedAyah(
            arabicText = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            latinText = "Fazkuruni azkurkum wasykuru li wa la takfurun",
            translation = "Maka ingatlah kepada-Ku, Aku pun akan ingat kepadamu. Bersyukurlah kepada-Ku, dan janganlah kamu ingkar kepada-Ku.",
            surahName = "Al-Baqarah",
            verseRef = "QS. Al-Baqarah: 152"
        )
    }

    override fun toggleBookmarkSurah(surahId: Int) {
        val current = bookmarkedSurahIds.value.toMutableSet()
        if (current.contains(surahId)) {
            current.remove(surahId)
        } else {
            current.add(surahId)
        }
        bookmarkedSurahIds.value = current

        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_BOOKMARKED_SURAHS] = gson.toJson(current)
                }
            }
        }
    }

    override fun toggleBookmarkAyah(ayahId: Int) {
        val current = bookmarkedAyahIds.value.toMutableSet()
        if (current.contains(ayahId)) {
            current.remove(ayahId)
        } else {
            current.add(ayahId)
        }
        bookmarkedAyahIds.value = current

        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.edit { prefs ->
                    prefs[KEY_BOOKMARKED_AYAHS] = gson.toJson(current)
                }
            }
        }
    }
}
