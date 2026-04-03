package com.simplenetworking.sdk.result

/**
 * Represents the normalized result of a network operation.
 */
sealed class ApiResult<out T> {
    /**
     * A successful response containing mapped payload data.
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * A failed response containing a normalized throwable model.
     */
    data class Error(val exception: Throwable) : ApiResult<Nothing>()

    /**
     * A loading state for Flow-driven API requests.
     */
    data object Loading : ApiResult<Nothing>()

    /**
     * An optional state when a successful request returns no content.
     */
    data object Empty : ApiResult<Nothing>()
}
