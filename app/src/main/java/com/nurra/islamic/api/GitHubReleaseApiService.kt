package com.nurra.islamic.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

data class GitHubReleaseDto(
    @SerializedName("tag_name") val tagName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("html_url") val htmlUrl: String? = null,
    @SerializedName("assets") val assets: List<GitHubAssetDto>? = null
)

data class GitHubAssetDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("browser_download_url") val browserDownloadUrl: String? = null
)

interface GitHubReleaseApiService {
    @GET("repos/Ubayzaid/Nurra/releases/latest")
    suspend fun getLatestRelease(): Response<GitHubReleaseDto>
}
