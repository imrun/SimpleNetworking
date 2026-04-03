package com.simplenetworking.sdk.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Normalizes low-level HTTP metadata so consumers can inspect response status consistently.
 */
class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return response.newBuilder()
            .header("X-Network-Code", response.code.toString())
            .build()
    }
}
