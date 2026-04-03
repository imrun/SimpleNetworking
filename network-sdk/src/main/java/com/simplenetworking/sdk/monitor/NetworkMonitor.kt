package com.simplenetworking.sdk.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Observes device connectivity and exposes it as both pull- and push-based APIs.
 */
class NetworkMonitor(
    context: Context
) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val state = MutableStateFlow(isCurrentlyConnected())

    /**
     * Returns the current connectivity state synchronously.
     */
    fun isOnline(): Boolean = state.value

    /**
     * Emits connectivity updates as a hot StateFlow.
     */
    fun observe(): Flow<Boolean> = state.asStateFlow()

    /**
     * Emits connectivity updates from the platform callback API.
     */
    fun observeCallback(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                state.value = true
                trySend(true)
            }

            override fun onLost(network: Network) {
                val connected = isCurrentlyConnected()
                state.value = connected
                trySend(connected)
            }

            override fun onUnavailable() {
                state.value = false
                trySend(false)
            }
        }

        trySend(state.value)
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback
        )
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    private fun isCurrentlyConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
