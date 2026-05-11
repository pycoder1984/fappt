package com.vidking.firetv.opensubtitles

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vidking.firetv.BuildConfig
import com.vidking.firetv.febbox.SubtitleTrack
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Fetches English subtitles from OpenSubtitles and returns them as a list of
 * [SubtitleTrack] ready to be wired into ExoPlayer.
 *
 * Flow per call:
 *   1. `GET /api/v1/subtitles` — top-N candidates by download count.
 *   2. Pick the best file (highest download_count, prefer non-AI/non-MT,
 *      prefer SRT, drop hearing-impaired unless nothing else exists).
 *   3. `POST /api/v1/download` to exchange the `file_id` for a temporary URL.
 *
 * Anonymous mode: 5 downloads per IP per 24h. To get higher quota the
 * repository would need to call `/api/v1/login` first and pass the JWT as
 * `Authorization: Bearer …` — not implemented yet because credentials are
 * not configured. The repository fails closed (empty list) on quota errors;
 * playback proceeds without subtitles.
 */
object OpenSubtitlesRepository {

    private const val TAG = "OpenSubtitles"
    private const val BASE_URL = "https://api.opensubtitles.com/api/v1/"

    // OpenSubtitles requires a custom User-Agent. Generic UAs get blocked.
    private const val USER_AGENT = "Vidking FireTV v1.0"

    private val api: OpenSubtitlesApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
            })
            .build()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenSubtitlesApi::class.java)
    }

    /**
     * Best-effort subtitle fetch. Returns an empty list on any failure
     * (network, quota, no match) so the caller can play the video
     * without subs.
     */
    suspend fun fetch(
        tmdbId: Int,
        mediaType: String,
        season: Int = 0,
        episode: Int = 0,
        language: String = "en"
    ): List<SubtitleTrack> {
        val key = BuildConfig.OPENSUBTITLES_API_KEY
        if (key.isBlank()) {
            Log.w(TAG, "no API key configured; skipping subtitle fetch")
            return emptyList()
        }

        val isTv = mediaType == "tv" && season > 0 && episode > 0

        return try {
            val search = if (isTv) {
                api.search(
                    apiKey = key,
                    userAgent = USER_AGENT,
                    parentTmdbId = tmdbId,
                    seasonNumber = season,
                    episodeNumber = episode,
                    languages = language,
                    type = "episode"
                )
            } else {
                api.search(
                    apiKey = key,
                    userAgent = USER_AGENT,
                    tmdbId = tmdbId,
                    languages = language,
                    type = "movie"
                )
            }

            val entries = search.data.orEmpty()
            if (entries.isEmpty()) {
                Log.d(TAG, "no subtitles found for tmdb=$tmdbId")
                return emptyList()
            }

            // Pick the top candidate. Scoring rules:
            //   +download_count (popular = usually correctly synced)
            //   - hearing-impaired (most users don't want [door creaks])
            //   - ai/machine-translated (typically lower quality)
            //   + from_trusted
            //   + has at least one downloadable file
            val best = entries
                .filter { !it.attributes?.files.isNullOrEmpty() }
                .maxByOrNull { e ->
                    val a = e.attributes ?: return@maxByOrNull 0
                    val base = a.downloadCount ?: 0
                    val penalty = (if (a.hearingImpaired == true) 50_000 else 0) +
                        (if (a.aiTranslated == true) 30_000 else 0) +
                        (if (a.machineTranslated == true) 30_000 else 0)
                    val bonus = (if (a.fromTrusted == true) 10_000 else 0)
                    base + bonus - penalty
                }
                ?: return emptyList()

            val fileId = best.attributes?.files?.firstOrNull()?.fileId
                ?: return emptyList()

            val download = api.download(
                apiKey = key,
                userAgent = USER_AGENT,
                body = OpenSubtitlesDownloadRequest(fileId = fileId)
            )

            val link = download.link.orEmpty()
            if (link.isBlank()) {
                Log.w(TAG, "download returned no link (remaining=${download.remaining}): ${download.message}")
                return emptyList()
            }

            Log.d(
                TAG,
                "got subtitle $fileId (remaining=${download.remaining}/24h, file=${download.fileName})"
            )
            listOf(
                SubtitleTrack(
                    url = link,
                    label = best.attributes?.release?.takeIf { it.isNotBlank() }
                        ?: download.fileName?.takeIf { it.isNotBlank() }
                        ?: "English",
                    language = language,
                    mimeType = SubtitleTrack.mimeFromUrlOrFormat(
                        url = link,
                        format = download.fileName
                    )
                )
            )
        } catch (t: Throwable) {
            Log.w(TAG, "fetch failed: ${t.javaClass.simpleName} ${t.message}")
            emptyList()
        }
    }
}
