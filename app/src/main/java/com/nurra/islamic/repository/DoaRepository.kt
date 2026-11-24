package com.nurra.islamic.repository

import android.content.Context
import android.util.Log
import com.nurra.islamic.api.ApiClient
import com.nurra.islamic.model.DhikrPreset
import com.nurra.islamic.model.DoaCategory
import com.nurra.islamic.model.DoaItem
import com.nurra.islamic.util.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface DoaRepository {
    fun getDoas(): Flow<List<DoaItem>>
    fun getDoaById(id: String): DoaItem?
    fun toggleFavoriteDoa(id: String)
    fun getDhikrPresets(): List<DhikrPreset>
}

class LocalDoaRepository(
    private val context: Context? = null
) : DoaRepository {

    private val TAG = "NurraDoaRepo"
    private val cacheManager = context?.let { CacheManager(it) }

    private val favoriteDoaIds = MutableStateFlow<Set<String>>(setOf("doa-1", "doa-5"))

    // Exact Surah & Verse mapping for Qur'anic Doas
    private val quranDoaVerseMap = mapOf(
        "Doa Mohon Kebaikan Dunia dan Akhirat" to "QS. Al-Baqarah: 201",
        "Doa Tabah Menghadapi Lawan" to "QS. Al-Baqarah: 250",
        "Doa Diberi Keteguhan Iman" to "QS. Ali 'Imran: 8",
        "Doa Mohon Ampunan Dosa" to "QS. Ali 'Imran: 16",
        "Doa Mohon Rahmat & Ampunan" to "QS. Al-A'raf: 23",
        "Doa Husnul Khatimah" to "QS. Ali 'Imran: 193",
        "Doa Mohon Kebaikan Keluarga & Keturunan" to "QS. Al-Furqan: 74",
        "Doa Mohon Kelapangan Dada & Kemudahan" to "QS. Taha: 25-28",
        "Doa Mohon Ampunan Orang Tua" to "QS. Ibrahim: 41",
        "Doa Mohon Bertambah Ilmu" to "QS. Taha: 114",
        "Doa Selamat dari Orang Zalim" to "QS. Al-Qasas: 21",
        "Doa Nabi Yunus dalam Perut Ikan" to "QS. Al-Anbiya': 87",
        "Doa Nabi Ayyub Mohon Kesembuhan" to "QS. Al-Anbiya': 83",
        "Doa Nabi Zakariya Mohon Keturunan" to "QS. Ali 'Imran: 38",
        "Doa Keturunan Yang Sholeh" to "QS. As-Saffat: 100",
        "Doa Bangun Bangunan & Rumah Berkah" to "QS. Al-Mu'minun: 29",
        "Doa Berlindung dari Bisikan Setan" to "QS. Al-Mu'minun: 97-98",
        "Doa Diberi Hikmah & Kebijaksanaan" to "QS. Asy-Syu'ara': 83-85"
    )

    private val fallbackDoas = listOf(
        DoaItem(
            id = "doa-1",
            title = "Doa Sebelum Makan",
            arabicText = "اللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ",
            latinText = "Allahumma barik lana fi ma razaqtana wa qina 'adzaban-nar",
            indonesianTranslation = "Ya Allah, berkahilah rezeki yang telah Engkau berikan kepada kami dan peliharalah kami dari siksa api neraka.",
            category = DoaCategory.HARIAN,
            source = "HR. Ibny As-Sunni"
        ),
        DoaItem(
            id = "doa-2",
            title = "Doa Sesudah Makan",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
            latinText = "Alhamdu lillahilladzi at'amana wa saqana wa ja'alana muslimin",
            indonesianTranslation = "Segala puji bagi Allah yang telah memberi kami makan dan minum serta menjadikan kami orang-orang muslim.",
            category = DoaCategory.HARIAN,
            source = "HR. Abu Daud"
        ),
        DoaItem(
            id = "doa-3",
            title = "Doa Sebelum Tidur",
            arabicText = "بِاسْمِكَ اللَّهُمَّ أَحْيَا وَأَمُوتُ",
            latinText = "Bismikallahumma ahya wa amut",
            indonesianTranslation = "Dengan nama-Mu ya Allah, aku hidup dan aku mati.",
            category = DoaCategory.HARIAN,
            source = "HR. Bukhari & Muslim"
        ),
        DoaItem(
            id = "doa-4",
            title = "Doa Bangun Tidur",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            latinText = "Alhamdu lillahilladzi ahyana ba'da ma amatana wa ilaihin-nusyur",
            indonesianTranslation = "Segala puji bagi Allah yang telah menghidupkan kami setelah mematikan kami dan hanya kepada-Nya kami dikembalikan.",
            category = DoaCategory.HARIAN,
            source = "HR. Bukhari"
        ),
        DoaItem(
            id = "doa-5",
            title = "Sayyidul Istighfar",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
            latinText = "Allahumma anta rabbi la ilaha illa anta khalaqtani wa ana 'abduka wa ana 'ala 'ahdika wa wa'dika mastata'tu",
            indonesianTranslation = "Ya Allah, Engkau adalah Tuhanku, tidak ada tuhan selain Engkau. Engkau yang menciptakanku dan aku adalah hamba-Mu.",
            category = DoaCategory.DZIKIR_PAGI,
            source = "HR. Bukhari No. 6306"
        )
    )

    private val liveDoaList = MutableStateFlow<List<DoaItem>>(fallbackDoas)

    init {
        fetchDoasFromApi()
    }

    private fun fetchDoasFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Fetching Doa & Dzikir collection from myQuran API v2...")
                val response = ApiClient.apiService.getAllDoas()

                if (response.isSuccessful && response.body()?.status == true) {
                    val dtos = response.body()?.data ?: emptyList()
                    if (dtos.isNotEmpty()) {
                        Log.d(TAG, "Successfully fetched ${dtos.size} doas from myQuran API v2!")

                        val mappedDoas = dtos.mapIndexed { index, dto ->
                            val title = dto.judul ?: "Doa Islami"
                            val arabic = dto.doa ?: "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً"
                            val latin = dto.latin ?: ""
                            val translation = dto.artinya ?: ""

                            val rawSource = dto.source?.lowercase()?.trim() ?: ""
                            val rawAyat = dto.ayat?.trim() ?: ""

                            // Exact Surah & Verse lookup or clean source string
                            val mappedVerse = quranDoaVerseMap[title]
                            val cleanSource = when {
                                !mappedVerse.isNullOrBlank() -> mappedVerse
                                rawAyat.isNotBlank() -> rawAyat
                                !dto.source.isNullOrBlank() && !dto.source.equals("quran", ignoreCase = true) -> dto.source.replace("Sumber:", "").replace("sumber:", "").trim()
                                rawSource.contains("quran") -> "Ayat Al-Qur'anul Karim"
                                rawSource.contains("hadit") || rawSource.contains("hadi") -> "Hadits Shahih Rasulullah SAW"
                                else -> "Al-Qur'an & Sunnah"
                            }

                            val category = when {
                                title.contains("Pagi", ignoreCase = true) -> DoaCategory.DZIKIR_PAGI
                                title.contains("Petang", ignoreCase = true) || title.contains("Sore", ignoreCase = true) -> DoaCategory.DZIKIR_PETANG
                                title.contains("Salat", ignoreCase = true) || title.contains("Shalat", ignoreCase = true) || title.contains("Wudhu", ignoreCase = true) -> DoaCategory.SALAT
                                rawSource.contains("quran") || rawAyat.contains("QS") || mappedVerse != null -> DoaCategory.QURAN
                                else -> DoaCategory.HARIAN
                            }

                            DoaItem(
                                id = "api-doa-$index-${dto.id ?: index}",
                                title = title,
                                arabicText = arabic,
                                latinText = latin,
                                indonesianTranslation = translation,
                                category = category,
                                source = cleanSource
                            )
                        }

                        cacheManager?.saveDoas(mappedDoas)
                        liveDoaList.value = mappedDoas
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching doas from myQuran API: ${e.message}. Using cached/fallback doas.")
                cacheManager?.getDoas()?.let { cached ->
                    liveDoaList.value = cached
                }
            }
        }
    }

    override fun getDoas(): Flow<List<DoaItem>> {
        return liveDoaList.map { list ->
            val favorites = favoriteDoaIds.value
            list.map { item ->
                item.copy(isFavorite = favorites.contains(item.id))
            }
        }
    }

    override fun getDoaById(id: String): DoaItem? {
        val item = liveDoaList.value.find { it.id == id } ?: return null
        return item.copy(isFavorite = favoriteDoaIds.value.contains(id))
    }

    override fun toggleFavoriteDoa(id: String) {
        val current = favoriteDoaIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        favoriteDoaIds.value = current
    }

    override fun getDhikrPresets(): List<DhikrPreset> {
        return listOf(
            DhikrPreset("preset-1", "Subhanallah", "سُبْحَانَ اللَّهِ", "Maha Suci Allah", 10),
            DhikrPreset("preset-2", "Alhamdulillah", "الْحَمْدُ لِلَّهِ", "Segala puji bagi Allah", 10),
            DhikrPreset("preset-3", "Allahu Akbar", "اللَّهُ أَكْبَرُ", "Allah Maha Besar", 10),
            DhikrPreset("preset-4", "Astaghfirullah", "أَسْتَغْفِرُ اللَّهَ", "Aku memohon ampun kepada Allah", 50),
            DhikrPreset("preset-5", "La ilaha illallah", "لَا إِلَٰهَ إِلَّا اللَّهُ", "Tiada tuhan selain Allah", 100)
        )
    }
}
