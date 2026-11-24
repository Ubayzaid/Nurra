package com.nurra.islamic.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface EQuranApiService {

    @GET("surat")
    suspend fun getAllSurahs(): Response<EQuranListResponse>

    @GET("surat/{nomor}")
    suspend fun getSurahDetail(@Path("nomor") nomor: Int): Response<EQuranDetailResponse>
}
