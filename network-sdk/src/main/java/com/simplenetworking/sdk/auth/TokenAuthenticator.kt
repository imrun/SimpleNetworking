package com.simplenetworking.sdk.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Refreshes an expired bearer token and retries the original request once.
 */
class TokenAuthenticator(
    private val tokenProvider: (() -> String?)?,
    private val refreshTokenProvider: (suspend () -> String?)?
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val currentToken = tokenProvider?.invoke()
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()
        if (!requestToken.isNullOrBlank() && requestToken != currentToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        val refreshedToken = runBlocking { refreshTokenProvider?.invoke() } ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $refreshedToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
