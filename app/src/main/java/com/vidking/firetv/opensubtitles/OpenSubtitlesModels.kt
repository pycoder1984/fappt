package com.vidking.firetv.opensubtitles

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from `GET /api/v1/subtitles`. The interesting payload is
 * `data[].attributes.files[].file_id` — that's what `/api/v1/download` needs.
 * Everything else is metadata used to score / sort candidates.
 */
@JsonClass(generateAdapter = false)
data class OpenSubtitlesSearchResponse(
    @Json(name = "total_pages") val totalPages: Int? = null,
    @Json(name = "total_count") val totalCount: Int? = null,
    val page: Int? = null,
    val data: List<OpenSubtitlesEntry>? = null
)

@JsonClass(generateAdapter = false)
data class OpenSubtitlesEntry(
    val id: String? = null,
    val type: String? = null,
    val attributes: OpenSubtitlesAttributes? = null
)

@JsonClass(generateAdapter = false)
data class OpenSubtitlesAttributes(
    @Json(name = "subtitle_id") val subtitleId: String? = null,
    val language: String? = null,
    @Json(name = "download_count") val downloadCount: Int? = null,
    @Json(name = "hearing_impaired") val hearingImpaired: Boolean? = null,
    val hd: Boolean? = null,
    val fps: Double? = null,
    val votes: Int? = null,
    val ratings: Double? = null,
    @Json(name = "from_trusted") val fromTrusted: Boolean? = null,
    val release: String? = null,
    @Json(name = "ai_translated") val aiTranslated: Boolean? = null,
    @Json(name = "machine_translated") val machineTranslated: Boolean? = null,
    val files: List<OpenSubtitlesFile>? = null
)

@JsonClass(generateAdapter = false)
data class OpenSubtitlesFile(
    @Json(name = "file_id") val fileId: Long? = null,
    @Json(name = "cd_number") val cdNumber: Int? = null,
    @Json(name = "file_name") val fileName: String? = null
)

/**
 * Body for `POST /api/v1/download`. Only `file_id` is required; the rest are
 * format hints OpenSubtitles uses to transcode on the fly. We leave them at
 * defaults so we get the original subtitle format the uploader posted.
 */
@JsonClass(generateAdapter = false)
data class OpenSubtitlesDownloadRequest(
    @Json(name = "file_id") val fileId: Long
)

/**
 * Response from `/api/v1/download`. `link` is a temporary URL — must be
 * downloaded before it expires, and a fresh request is needed for retries.
 * `remaining` is the user's leftover daily quota for visibility.
 */
@JsonClass(generateAdapter = false)
data class OpenSubtitlesDownloadResponse(
    val link: String? = null,
    @Json(name = "file_name") val fileName: String? = null,
    val requests: Int? = null,
    val remaining: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "reset_time") val resetTime: String? = null,
    @Json(name = "reset_time_utc") val resetTimeUtc: String? = null
)
