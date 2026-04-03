package com.simplenetworking.sdk.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds static or dynamic headers to every outgoing request.
 */
class HeaderInterceptor(
    private val headersProvider: (() -> Map<String, String>)?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val updated = original.newBuilder().apply {
            headersProvider?.invoke().orEmpty().forEach { (key, value) ->
                if (value.isNotBlank()) {
                    header(key, value)
                }
            }
        }.build()
        return chain.proceed(updated)
    }
}
