package com.simplenetworking.sdk.model

import com.squareup.moshi.Json

/**
 * Generic backend error payload model.
 */
data class ApiErrorResponse(
    @Json(name = "code") val code: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "details") val details: Map<String, Any?>? = null
)
