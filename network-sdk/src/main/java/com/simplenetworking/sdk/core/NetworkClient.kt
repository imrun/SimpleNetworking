package com.simplenetworking.sdk.core

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.simplenetworking.sdk.auth.TokenAuthenticator
import com.simplenetworking.sdk.error.ErrorMapper
import com.simplenetworking.sdk.interceptor.ErrorInterceptor
import com.simplenetworking.sdk.interceptor.HeaderInterceptor
import com.simplenetworking.sdk.interceptor.LoggingInterceptorFactory
import com.simplenetworking.sdk.interceptor.RateLimitInterceptor
import com.simplenetworking.sdk.interceptor.RetryInterceptor
import com.simplenetworking.sdk.interceptor.TokenInterceptor
import com.simplenetworking.sdk.monitor.NetworkMonitor
import com.simplenetworking.sdk.result.ApiResult
import com.simplenetworking.sdk.util.safeApiCall
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central SDK entry point that hides Retrofit and OkHttp setup from consumers.
 */
object NetworkClient {
    @Volatile
    private var retrofit: Retrofit? = null
    @Volatile
    private var okHttpClient: OkHttpClient? = null
    @Volatile
    private var config: NetworkConfig? = null
    @Volatile
    private var errorMapper: ErrorMapper? = null
    @Volatile
    private var networkMonitor: NetworkMonitor? = null

    /**
     * Initializes the SDK with the standard configuration expected by app teams.
     */
    @JvmStatic
    fun initialize(
        baseUrl: String,
        debug: Boolean,
        connectTimeout: Long = 30,
        readTimeout: Long = 30,
        headersProvider: (() -> Map<String, String>)? = null,
        tokenProvider: (() -> String?)? = null,
        refreshTokenProvider: (suspend () -> String?)? = null,
        retryCount: Int = 3,
        rateLimitRequests: Int = 10,
        rateLimitWindowSeconds: Long = 1
    ) {
        initialize(
            NetworkConfig(
                debug = debug,
                baseUrl = normalizeBaseUrl(baseUrl),
                connectTimeoutSeconds = connectTimeout,
                readTimeoutSeconds = readTimeout,
                writeTimeoutSeconds = readTimeout,
                retryPolicy = NetworkConfig.RetryPolicy(maxRetries = retryCount),
                rateLimitPolicy = NetworkConfig.RateLimitPolicy(
                    maxRequests = rateLimitRequests,
                    perSeconds = rateLimitWindowSeconds
                ),
                headersProvider = headersProvider,
                tokenProvider = tokenProvider,
                refreshTokenProvider = refreshTokenProvider
            )
        )
    }

    /**
     * Initializes the SDK using a fully specified [NetworkConfig].
     */
    @JvmStatic
    fun initialize(networkConfig: NetworkConfig) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(networkConfig.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(networkConfig.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(networkConfig.writeTimeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor(networkConfig.headersProvider))
            .addInterceptor(TokenInterceptor(networkConfig.tokenProvider))
            .addInterceptor(RetryInterceptor(networkConfig.retryPolicy))
            .addInterceptor(RateLimitInterceptor(networkConfig.rateLimitPolicy))
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(LoggingInterceptorFactory.create(networkConfig.debug))
            .authenticator(
                TokenAuthenticator(
                    tokenProvider = networkConfig.tokenProvider,
                    refreshTokenProvider = networkConfig.refreshTokenProvider
                )
            )
            .build()

        val retrofitInstance = Retrofit.Builder()
            .baseUrl(networkConfig.baseUrl)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        okHttpClient = okHttp
        retrofit = retrofitInstance
        config = networkConfig
        errorMapper = ErrorMapper(networkConfig.debug)
    }

    /**
     * Optionally attaches a platform connectivity monitor for offline checks and Flow updates.
     */
    @JvmStatic
    fun attachNetworkMonitor(context: Context) {
        networkMonitor = NetworkMonitor(context)
    }

    /**
     * Returns the current [NetworkMonitor] when one has been attached.
     */
    fun networkMonitor(): NetworkMonitor? = networkMonitor

    /**
     * Builds a Retrofit-backed API service.
     */
    fun <T> createService(service: Class<T>): T = apiServiceFactory().create(service)

    /**
     * Builds a Retrofit-backed API service using a reified type.
     */
    inline fun <reified T> createService(): T = apiServiceFactory().create()

    /**
     * Exposes a shared [ApiServiceFactory] for service creation.
     */
    fun apiServiceFactory(): ApiServiceFactory = ApiServiceFactory(requireRetrofit())

    /**
     * Executes a request safely with standard error mapping.
     */
    suspend fun <T> safeApiCall(request: suspend () -> Response<T>): ApiResult<T> {
        return try {
            checkConnectivity()
            safeApiCall(requireErrorMapper(), request)
        } catch (throwable: Throwable) {
            ApiResult.Error(requireErrorMapper().map(throwable))
        }
    }

    /**
     * Executes a request as a Flow that first emits loading and then the final result.
     */
    fun <T> flowApiCall(request: suspend () -> Response<T>): Flow<ApiResult<T>> {
        return flow {
            emit(ApiResult.Loading)
            try {
                checkConnectivity()
                emit(safeApiCall(requireErrorMapper(), request))
            } catch (throwable: Throwable) {
                emit(ApiResult.Error(requireErrorMapper().map(throwable)))
            }
        }
    }

    /**
     * Exposes the current immutable SDK configuration.
     */
    fun config(): NetworkConfig = requireNotNull(config) {
        "NetworkClient has not been initialized. Call initialize() first."
    }

    @VisibleForTesting
    internal fun reset() {
        retrofit = null
        okHttpClient = null
        config = null
        errorMapper = null
        networkMonitor = null
    }

    private fun checkConnectivity() {
        val monitor = networkMonitor ?: return
        check(monitor.isOnline()) { "No Internet Connection" }
    }

    private fun requireRetrofit(): Retrofit = requireNotNull(retrofit) {
        "NetworkClient has not been initialized. Call initialize() first."
    }

    private fun requireErrorMapper(): ErrorMapper = requireNotNull(errorMapper) {
        "NetworkClient has not been initialized. Call initialize() first."
    }

    private fun normalizeBaseUrl(baseUrl: String): String =
        if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
}
