package com.nurra.islamic.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MyQuranApiService {

    // 1. City / Region Endpoints
    @GET("sholat/kota/semua")
    suspend fun getAllCities(): Response<MyQuranResponse<List<CityDto>>>

    @GET("sholat/kota/cari/{query}")
    suspend fun searchCity(@Path("query") query: String): Response<MyQuranResponse<List<CityDto>>>

    // 2. Prayer Schedule Endpoints by City ID & Date (YYYY/MM/DD)
    @GET("sholat/jadwal/{cityId}/{year}/{month}/{day}")
    suspend fun getPrayerSchedule(
        @Path("cityId") cityId: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): Response<MyQuranResponse<PrayerScheduleDetailDto>>

    // 3. Quran Endpoints
    @GET("quran/surah/semua")
    suspend fun getAllSurahs(): Response<List<SurahDto>>

    @GET("quran/surah/{number}")
    suspend fun getSurahDetail(@Path("number") number: Int): Response<MyQuranResponse<SurahDto>>

    // 4. Doa & Dzikir Endpoints
    @GET("doa/semua")
    suspend fun getAllDoas(): Response<MyQuranResponse<List<DoaDto>>>

    // 5. Hadis Endpoints
    @GET("hadis/enc/explore")
    suspend fun getHadiths(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<MyQuranResponse<List<HadithDto>>>

    @GET("hadis/enc/random")
    suspend fun getRandomHadith(): Response<MyQuranResponse<HadithDto>>

    // 6. Calendar Endpoints
    @GET("cal/today")
    suspend fun getTodayCalendar(): Response<MyQuranResponse<HijriDateDto>>

    // 7. Qibla Bearing Endpoint
    @GET("qibla/{coordinate}")
    suspend fun getQiblaBearing(@Path("coordinate") coordinate: String): Response<MyQuranResponse<QiblaBearingDto>>
}
