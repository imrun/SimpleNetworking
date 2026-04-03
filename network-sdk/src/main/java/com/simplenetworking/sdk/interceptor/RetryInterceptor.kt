package com.simplenetworking.sdk.interceptor

import com.simplenetworking.sdk.core.NetworkConfig
import java.io.IOException
import kotlin.math.pow
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Retries transient I/O failures with exponential backoff.
 */
class RetryInterceptor(
    private val policy: NetworkConfig.RetryPolicy
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null

        while (attempt <= policy.maxRetries) {
            try {
                return chain.proceed(chain.request())
            } catch (ioException: IOException) {
                lastException = ioException
                if (attempt == policy.maxRetries) break
                val delay = (policy.initialDelayMillis * policy.backoffMultiplier.pow(attempt.toDouble())).toLong()
                Thread.sleep(delay)
                attempt++
            }
        }

        throw lastException ?: IOException("Retry interceptor exhausted without exception")
    }
}
