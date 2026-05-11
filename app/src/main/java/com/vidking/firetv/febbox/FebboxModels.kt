package com.vidking.firetv.febbox

import com.squareup.moshi.JsonClass

/**
 * Request body for the rnrvibe `/api/siteadmin/febbox` POST endpoint.
 * `type` is "movie" or "tv"; `season`/`episode` are required for TV and
 * ignored for movies.
 */
@JsonClass(generateAdapter = false)
data class FebboxResolveRequest(
    val type: String,
    val tmdbId: String,
    val season: Int? = null,
    val episode: Int? = null
)

/**
 * Response shape from `/api/siteadmin/febbox`. The route picks the best
 * stream (highest resolution, H.264 preferred at equal resolution) and
 * returns its url + the full ranked alternatives in `qualities`. On error
 * the route returns a non-2xx status with `{ error: "..." }`.
 */
@JsonClass(generateAdapter = false)
data class FebboxResolveResponse(
    val url: String? = null,
    val quality: String? = null,
    val codec: String? = null,
    val fid: String? = null,
    val mime: String? = null,
    val qualities: List<FebboxQuality>? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = false)
data class FebboxQuality(
    val url: String? = null,
    val quality: String? = null,
    val codec: String? = null,
    val format: String? = null
)
