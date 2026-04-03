package com.simplenetworking.sdk.error

/**
 * Base type for all SDK-level network failures.
 */
sealed class NetworkException(
    message: String,
    cause: Throwable? = null,
    open val code: Int? = null,
    open val errorBody: String? = null
) : Exception(message, cause) {

    /**
     * Raised when the device has no internet connectivity.
     */
    data class NoInternet(
        override val errorBody: String? = null,
        private val origin: Throwable? = null
    ) : NetworkException("No Internet Connection", origin, errorBody = errorBody)

    /**
     * Raised when the request exceeds the configured timeout.
     */
    data class Timeout(
        override val errorBody: String? = null,
        private val origin: Throwable? = null
    ) : NetworkException("Network Timeout", origin, errorBody = errorBody)

    /**
     * Raised when backend authentication fails.
     */
    data class Unauthorized(
        override val code: Int? = 401,
        override val errorBody: String? = null,
        private val origin: Throwable? = null
    ) : NetworkException("Unauthorized", origin, code = code, errorBody = errorBody)

    /**
     * Raised for 5xx responses or irrecoverable server issues.
     */
    data class Server(
        override val code: Int? = null,
        override val errorBody: String? = null,
        private val origin: Throwable? = null
    ) : NetworkException("Server Error", origin, code = code, errorBody = errorBody)

    /**
     * Raised for parsing or serialization failures.
     */
    data class Serialization(
        override val errorBody: String? = null,
        private val origin: Throwable? = null
    ) : NetworkException("Unexpected Error", origin, errorBody = errorBody)

    /**
     * Raised for all other unclassified failures.
     */
    data class Unexpected(
        private val userMessage: String = "Unexpected Error",
        override val code: Int? = null,
        override val errorBody: String? = null,
        private val origin: Throwable? = null
    ) : NetworkException(userMessage, origin, code = code, errorBody = errorBody)
}
