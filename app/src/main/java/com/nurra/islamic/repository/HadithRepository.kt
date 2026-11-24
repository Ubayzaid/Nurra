package com.nurra.islamic.repository

import com.nurra.islamic.model.Hadith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface HadithRepository {
    fun getDailyHadith(): Hadith
    fun getAllHadiths(): Flow<List<Hadith>>
}

class LocalHadithRepository : HadithRepository {

    private val authenticHadiths = listOf(
        Hadith(
            id = "hadith-1",
            title = "Mencintai Sesama Muslim",
            arabicText = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            indonesianTranslation = "Tidak sempurna iman salah seorang di antara kalian hingga ia mencintai untuk saudaranya apa yang ia cintai untuk dirinya sendiri.",
            source = "HR. Bukhari & Muslim",
            bookName = "Shahih Bukhari",
            hadithNumber = 13
        ),
        Hadith(
            id = "hadith-2",
            title = "Niat Dalam Setiap Amal",
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            indonesianTranslation = "Sesungguhnya setiap amalan tergantung pada niatnya, dan sesungguhnya setiap orang akan mendapatkan sesuai dengan apa yang ia niatkan.",
            source = "HR. Bukhari",
            bookName = "Shahih Bukhari",
            hadithNumber = 1
        ),
        Hadith(
            id = "hadith-3",
            title = "Sebaik-baik Manusia Meringankan Orang Lain",
            arabicText = "خَيْرُ النَّاسِ أَنْفَعُهُمْ لِلنَّاسِ",
            indonesianTranslation = "Sebaik-baik manusia adalah yang paling bermanfaat bagi manusia lainnya.",
            source = "HR. Ath-Thabrani",
            bookName = "Al-Mu'jam al-Awsat",
            hadithNumber = 5787
        )
    )

    override fun getDailyHadith(): Hadith {
        return authenticHadiths.first()
    }

    override fun getAllHadiths(): Flow<List<Hadith>> {
        return MutableStateFlow(authenticHadiths).asStateFlow()
    }
}
