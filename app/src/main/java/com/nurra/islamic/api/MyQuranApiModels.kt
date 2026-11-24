package com.nurra.islamic.api

import com.google.gson.annotations.SerializedName

// Generic API Response Wrapper
data class MyQuranResponse<T>(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null
)

// City Item DTO
data class CityDto(
    @SerializedName("id") val id: String,
    @SerializedName("lokasi") val lokasi: String
)

// Prayer Schedule Detail DTO
data class PrayerScheduleDetailDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("lokasi") val lokasi: String? = null,
    @SerializedName("daerah") val daerah: String? = null,
    @SerializedName("jadwal") val jadwal: PrayerTimesDto? = null
)

data class PrayerTimesDto(
    @SerializedName("tanggal") val tanggal: String? = null,
    @SerializedName("imsak") val imsak: String? = null,
    @SerializedName("subuh") val subuh: String? = null,
    @SerializedName("terbit") val terbit: String? = null,
    @SerializedName("dhuha") val dhuha: String? = null,
    @SerializedName("dzuhur") val dzuhur: String? = null,
    @SerializedName("ashar") val ashar: String? = null,
    @SerializedName("maghrib") val maghrib: String? = null,
    @SerializedName("isya") val isya: String? = null,
    @SerializedName("date") val date: String? = null
)

// Surah List & Detail DTO
data class SurahDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nomor") val nomor: Int? = null,
    @SerializedName("nama") val nama: String? = null,
    @SerializedName("nama_latin") val namaLatin: String? = null,
    @SerializedName("jumlah_ayat") val jumlahAyat: Int? = null,
    @SerializedName("tempat_turun") val tempatTurun: String? = null,
    @SerializedName("arti") val arti: String? = null,
    @SerializedName("audio") val audio: String? = null,
    @SerializedName("ayat") val ayatList: List<AyahDto>? = null
)

data class AyahDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("surah") val surah: Int? = null,
    @SerializedName("nomor") val nomor: Int? = null,
    @SerializedName("ar") val ar: String? = null,
    @SerializedName("tr") val tr: String? = null,
    @SerializedName("idn") val idn: String? = null
)

// Official EQuran.id DTOs
data class EQuranListResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: List<EQuranSurahDto> = emptyList()
)

data class EQuranSurahDto(
    @SerializedName("nomor") val nomor: Int = 0,
    @SerializedName("nama") val nama: String = "",
    @SerializedName("namaLatin") val namaLatin: String = "",
    @SerializedName("jumlahAyat") val jumlahAyat: Int = 0,
    @SerializedName("tempatTurun") val tempatTurun: String = "",
    @SerializedName("arti") val arti: String = ""
)

data class EQuranDetailResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: EQuranSurahDetailDto? = null
)

data class EQuranSurahDetailDto(
    @SerializedName("nomor") val nomor: Int = 0,
    @SerializedName("nama") val nama: String = "",
    @SerializedName("namaLatin") val namaLatin: String = "",
    @SerializedName("jumlahAyat") val jumlahAyat: Int = 0,
    @SerializedName("tempatTurun") val tempatTurun: String = "",
    @SerializedName("arti") val arti: String = "",
    @SerializedName("ayat") val ayat: List<EQuranAyatDto> = emptyList()
)

data class EQuranAyatDto(
    @SerializedName("nomorAyat") val nomorAyat: Int = 0,
    @SerializedName("teksArab") val teksArab: String = "",
    @SerializedName("teksLatin") val teksLatin: String = "",
    @SerializedName("teksIndonesia") val teksIndonesia: String = ""
)

// Hadith DTO
data class HadithDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("arabic") val arabic: String? = null,
    @SerializedName("terjemah") val terjemah: String? = null,
    @SerializedName("sumber") val sumber: String? = null,
    @SerializedName("no") val no: Int? = null
)

// Doa DTO
data class DoaDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("judul") val judul: String? = null,
    @SerializedName("doa") val doa: String? = null,
    @SerializedName("ayat") val ayat: String? = null,
    @SerializedName("latin") val latin: String? = null,
    @SerializedName("artinya") val artinya: String? = null,
    @SerializedName("source") val source: String? = null
)

// Hijri Calendar DTO
data class HijriDateDto(
    @SerializedName("date") val dateMasehi: String? = null,
    @SerializedName("hijri") val hijriDateFormatted: String? = null,
    @SerializedName("day") val dayName: String? = null
)

// Qibla Bearing DTO
data class QiblaBearingDto(
    @SerializedName("degree") val degree: Float? = null,
    @SerializedName("direction") val direction: String? = null
)
