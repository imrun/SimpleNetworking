package com.simplenetworking.sample.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * In-memory token store used by the sample app to demonstrate bearer auth setup.
 */
class SampleSessionManager {
    private val tokenState = MutableStateFlow("demo-access-token")

    fun accessToken(): String = tokenState.value

    fun observeToken(): StateFlow<String> = tokenState.asStateFlow()

    suspend fun refreshAccessToken(): String {
        delay(500)
        val newToken = "refreshed-${UUID.randomUUID().toString().take(8)}"
        tokenState.value = newToken
        return newToken
    }

    fun expireToken() {
        tokenState.value = "expired-demo-token"
    }
}
