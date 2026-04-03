package com.simplenetworking.sample

import android.app.Application
import com.simplenetworking.sample.auth.SampleSessionManager
import com.simplenetworking.sdk.core.NetworkClient

/**
 * Initializes the networking SDK for the sample application.
 */
class SampleApplication : Application() {
    val sessionManager: SampleSessionManager by lazy { SampleSessionManager() }

    override fun onCreate() {
        super.onCreate()

        NetworkClient.initialize(
            baseUrl = "https://jsonplaceholder.typicode.com/",
            debug = BuildConfig.DEBUG,
            headersProvider = {
                mapOf(
                    "Accept" to "application/json",
                    "X-Sample-Client" to "simple-networking-sample"
                )
            },
            tokenProvider = { sessionManager.accessToken() },
            refreshTokenProvider = { sessionManager.refreshAccessToken() }
        )
        NetworkClient.attachNetworkMonitor(this)
    }
}
