package com.simplenetworking.sdk.interceptor

import okhttp3.logging.HttpLoggingInterceptor

/**
 * Produces a body-level logger in debug builds and a no-op logger otherwise.
 */
object LoggingInterceptorFactory {
    fun create(debug: Boolean): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (debug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
}
