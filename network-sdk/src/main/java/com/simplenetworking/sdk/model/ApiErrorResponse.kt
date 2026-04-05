package com.simplenetworking.sdk.model

import com.squareup.moshi.Json

/**
 * Generic backend error payload model.
 */
data class ApiErrorResponse(
    @param:Json(name = "code") val code: String? = null,
    @param:Json(name = "message") val message: String? = null,
    @param:Json(name = "details") val details: Map<String, Any?>? = null
)
