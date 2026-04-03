package com.simplenetworking.sdk.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds a bearer token when authentication is configured.
 */
class TokenInterceptor(
    private val tokenProvider: (() -> String?)?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider?.invoke()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
