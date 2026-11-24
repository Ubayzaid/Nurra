package com.nurra.islamic.repository

import com.nurra.islamic.model.Hadith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface HadithRepository {
    fun getDailyHadith(): Hadith
    fun getRandomHadith(): Hadith
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
            title = "Sebaik-baik Manusia",
            arabicText = "خَيْرُ النَّاسِ أَنْفَعُهُمْ لِلنَّاسِ",
            indonesianTranslation = "Sebaik-baik manusia adalah yang paling bermanfaat bagi manusia lainnya.",
            source = "HR. Ath-Thabrani",
            bookName = "Al-Mu'jam al-Awsat",
            hadithNumber = 5787
        ),
        Hadith(
            id = "hadith-4",
            title = "Senyum adalah Sedekah",
            arabicText = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
            indonesianTranslation = "Senyummu di hadapan saudaramu adalah (bernilai) sedekah bagimu.",
            source = "HR. Tirmidzi",
            bookName = "Sunan Tirmidzi",
            hadithNumber = 1956
        ),
        Hadith(
            id = "hadith-5",
            title = "Kebersihan Bagian dari Iman",
            arabicText = "الطُّهُورُ شَطْرُ الإِيمَانِ",
            indonesianTranslation = "Kebersihan dan bersuci itu adalah sebagian dari iman.",
            source = "HR. Muslim",
            bookName = "Shahih Muslim",
            hadithNumber = 223
        ),
        Hadith(
            id = "hadith-6",
            title = "Berkata Baik atau Diam",
            arabicText = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            indonesianTranslation = "Barangsiapa yang beriman kepada Allah dan hari akhirat, hendaklah ia berkata yang baik atau diam.",
            source = "HR. Bukhari & Muslim",
            bookName = "Shahih Bukhari",
            hadithNumber = 6018
        ),
        Hadith(
            id = "hadith-7",
            title = "Agama adalah Nasihat",
            arabicText = "الدِّينُ النَّصِيحَةُ",
            indonesianTranslation = "Agama itu adalah nasihat (ketulusan dan kesetiaan untuk Allah, Rasul-Nya, dan seluruh kaum muslimin).",
            source = "HR. Muslim",
            bookName = "Shahih Muslim",
            hadithNumber = 55
        ),
        Hadith(
            id = "hadith-8",
            title = "Mencegah Kemungkaran",
            arabicText = "مَنْ رَأَى مِنْكُمْ مُنْكَرًا فَلْيُغَيِّرْهُ بِيَدِهِ فَإِنْ لَمْ يَسْتَطِعْ فَبِلِسَانِهِ",
            indonesianTranslation = "Barangsiapa di antara kalian melihat kemungkaran, hendaklah ia mengubahnya dengan tangannya. Jika tidak mampu, maka dengan lisannya.",
            source = "HR. Muslim",
            bookName = "Shahih Muslim",
            hadithNumber = 49
        ),
        Hadith(
            id = "hadith-9",
            title = "Menahan Marah",
            arabicText = "لَيْسَ الشَّدِيدُ بِالصُُّرَعَةِ إِنَّمَا الشَّدِيدُ الَّذِي يَمْلِكُ نَفْسَهُ عِنْدَ الْغَضَبِ",
            indonesianTranslation = "Orang yang kuat itu bukanlah orang yang jago gulat, tetapi orang kuat adalah orang yang dapat menahan dirinya ketika marah.",
            source = "HR. Bukhari",
            bookName = "Shahih Bukhari",
            hadithNumber = 6114
        ),
        Hadith(
            id = "hadith-10",
            title = "Keutamaan Menuntut Ilmu",
            arabicText = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            indonesianTranslation = "Barangsiapa menempuh jalan untuk menuntut ilmu, maka Allah akan memudahkan baginya jalan menuju surga.",
            source = "HR. Muslim",
            bookName = "Shahih Muslim",
            hadithNumber = 2699
        )
    )

    private var currentIndex = 0

    override fun getDailyHadith(): Hadith {
        return authenticHadiths.first()
    }

    override fun getRandomHadith(): Hadith {
        currentIndex = (currentIndex + 1) % authenticHadiths.size
        return authenticHadiths[currentIndex]
    }

    override fun getAllHadiths(): Flow<List<Hadith>> {
        return MutableStateFlow(authenticHadiths).asStateFlow()
    }
}
