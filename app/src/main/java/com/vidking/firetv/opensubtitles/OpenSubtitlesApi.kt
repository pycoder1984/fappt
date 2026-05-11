package com.vidking.firetv.opensubtitles

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for the OpenSubtitles REST API v1
 * (https://opensubtitles.stoplight.io/docs/opensubtitles-api).
 *
 * Auth model: every request needs `Api-Key` + `User-Agent`. The User-Agent
 * MUST be a custom descriptive string — using the default Retrofit/okhttp
 * UA gets the key rate-limited or blocked. For higher download quota
 * (10–1000 per day vs. 5 anonymous), include `Authorization: Bearer <jwt>`
 * obtained from `POST /api/v1/login` — currently optional here since the
 * repository runs in anonymous mode.
 */
interface OpenSubtitlesApi {

    /**
     * Search subtitles by TMDB id. For movies, pass `tmdbId` only. For TV
     * episodes, pass `parentTmdbId` (series id) plus `seasonNumber` +
     * `episodeNumber`. Either form works; mixing them produces nonsense.
     */
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("subtitles")
    suspend fun search(
        @Header("Api-Key") apiKey: String,
        @Header("User-Agent") userAgent: String,
        @Query("tmdb_id") tmdbId: Int? = null,
        @Query("parent_tmdb_id") parentTmdbId: Int? = null,
        @Query("season_number") seasonNumber: Int? = null,
        @Query("episode_number") episodeNumber: Int? = null,
        @Query("languages") languages: String = "en",
        @Query("type") type: String? = null,
        @Query("order_by") orderBy: String = "download_count",
        @Query("order_direction") orderDirection: String = "desc"
    ): OpenSubtitlesSearchResponse

    /**
     * Exchange a `file_id` for a temporary download URL. The returned `link`
     * is short-lived; if it expires, call download() again to get a new one.
     */
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("download")
    suspend fun download(
        @Header("Api-Key") apiKey: String,
        @Header("User-Agent") userAgent: String,
        @Body body: OpenSubtitlesDownloadRequest
    ): OpenSubtitlesDownloadResponse
}
