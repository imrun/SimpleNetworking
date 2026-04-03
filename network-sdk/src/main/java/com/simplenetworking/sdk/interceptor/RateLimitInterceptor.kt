package com.simplenetworking.sdk.interceptor

import com.simplenetworking.sdk.core.NetworkConfig
import java.io.IOException
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Applies client-side request throttling within a rolling time window.
 */
class RateLimitInterceptor(
    private val policy: NetworkConfig.RateLimitPolicy
) : Interceptor {
    private val timestamps = ArrayDeque<Long>()
    private val monitor = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(monitor) {
            val now = System.nanoTime()
            val windowNanos = TimeUnit.SECONDS.toNanos(policy.perSeconds)
            while (timestamps.isNotEmpty() && now - timestamps.first() > windowNanos) {
                timestamps.removeFirst()
            }

            if (timestamps.size >= policy.maxRequests) {
                throw IOException("Rate limit exceeded for ${policy.maxRequests} requests in ${policy.perSeconds}s")
            }

            timestamps.addLast(now)
        }

        return chain.proceed(chain.request())
    }
}
