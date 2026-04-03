package com.simplenetworking.sdk.core

/**
 * SDK-wide configuration used to construct Retrofit and OkHttp.
 */
data class NetworkConfig(
    val debug: Boolean,
    val baseUrl: String,
    val connectTimeoutSeconds: Long = 30,
    val readTimeoutSeconds: Long = 30,
    val writeTimeoutSeconds: Long = 30,
    val retryPolicy: RetryPolicy = RetryPolicy(),
    val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(),
    val headersProvider: (() -> Map<String, String>)? = null,
    val tokenProvider: (() -> String?)? = null,
    val refreshTokenProvider: (suspend () -> String?)? = null
) {
    /**
     * Retry behavior for transient failures.
     */
    data class RetryPolicy(
        val maxRetries: Int = 3,
        val initialDelayMillis: Long = 300,
        val backoffMultiplier: Double = 2.0
    )

    /**
     * Request throttling to protect APIs from client-side bursts.
     */
    data class RateLimitPolicy(
        val maxRequests: Int = 10,
        val perSeconds: Long = 1
    )
}
