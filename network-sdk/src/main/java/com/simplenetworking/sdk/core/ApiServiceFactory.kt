package com.simplenetworking.sdk.core

import retrofit2.Retrofit

/**
 * Creates strongly typed Retrofit service interfaces from the initialized client.
 */
class ApiServiceFactory internal constructor(
    private val retrofit: Retrofit
) {
    /**
     * Creates a Retrofit service implementation for [service].
     */
    fun <T> create(service: Class<T>): T = retrofit.create(service)

    /**
     * Creates a Retrofit service implementation using a reified type.
     */
    inline fun <reified T> create(): T = create(T::class.java)
}
