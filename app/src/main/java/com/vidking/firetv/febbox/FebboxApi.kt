package com.vidking.firetv.febbox

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit interface for the rnrvibe `/api/siteadmin/febbox` route (see
 * `app/api/siteadmin/febbox/route.ts` in the rnrvibe repo). One POST endpoint
 * carrying a JSON body with `{type, tmdbId, season?, episode?}`. Auth is the
 * `rnrvibe_siteadmin` cookie value, sent via the `Cookie` header.
 *
 * The full URL is passed via [Url] so callers can store any endpoint they like
 * in Settings without forcing Retrofit's baseUrl to match the route's prefix.
 */
interface FebboxApi {

    @POST
    suspend fun resolve(
        @Url url: String,
        @Header("Cookie") cookie: String,
        @Body body: FebboxResolveRequest
    ): FebboxResolveResponse
}
