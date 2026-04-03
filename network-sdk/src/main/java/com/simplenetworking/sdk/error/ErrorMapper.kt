package com.simplenetworking.sdk.error

import com.squareup.moshi.JsonDataException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

/**
 * Maps Retrofit, OkHttp, and serialization failures into stable SDK exceptions.
 */
class ErrorMapper(
    private val debug: Boolean
) {
    /**
     * Converts a throwable into the SDK's normalized error model.
     */
    fun map(throwable: Throwable): NetworkException {
        return when (throwable) {
            is NetworkException -> throwable
            is HttpException -> mapHttpException(throwable)
            is IllegalStateException ->
                if (throwable.message == "No Internet Connection") {
                    NetworkException.NoInternet(
                        errorBody = rawError(debug, throwable.message),
                        origin = if (debug) throwable else null
                    )
                } else {
                    NetworkException.Unexpected(
                        userMessage = if (debug) throwable.localizedMessage ?: "Unexpected Error" else "Unexpected Error",
                        errorBody = rawError(debug, throwable.message),
                        origin = if (debug) throwable else null
                    )
                }
            is UnknownHostException -> NetworkException.NoInternet(
                errorBody = rawError(debug, throwable.message),
                origin = if (debug) throwable else null
            )
            is SocketTimeoutException -> NetworkException.Timeout(
                errorBody = rawError(debug, throwable.message),
                origin = if (debug) throwable else null
            )
            is JsonDataException,
            is com.squareup.moshi.JsonEncodingException -> NetworkException.Serialization(
                errorBody = rawError(debug, throwable.message),
                origin = if (debug) throwable else null
            )
            is IOException -> NetworkException.Unexpected(
                userMessage = if (debug) throwable.localizedMessage ?: "I/O Error" else "Unexpected Error",
                errorBody = rawError(debug, throwable.message),
                origin = if (debug) throwable else null
            )
            else -> NetworkException.Unexpected(
                userMessage = if (debug) throwable.localizedMessage ?: "Unexpected Error" else "Unexpected Error",
                errorBody = rawError(debug, throwable.message),
                origin = if (debug) throwable else null
            )
        }
    }

    private fun mapHttpException(throwable: HttpException): NetworkException {
        val code = throwable.code()
        val errorBody = throwable.response()?.errorBody()?.string()
        return if (debug) {
            when (code) {
                401 -> NetworkException.Unauthorized(code = code, errorBody = errorBody, origin = throwable)
                in 500..599 -> NetworkException.Server(code = code, errorBody = errorBody, origin = throwable)
                else -> NetworkException.Unexpected(
                    userMessage = throwable.message(),
                    code = code,
                    errorBody = errorBody,
                    origin = throwable
                )
            }
        } else {
            when (code) {
                401 -> NetworkException.Unauthorized(code = code)
                in 500..599 -> NetworkException.Server(code = code)
                else -> NetworkException.Unexpected(
                    userMessage = throwable.message().takeIf { it.isNotBlank() } ?: "Unexpected Error",
                    code = code
                )
            }
        }
    }

    private fun rawError(debug: Boolean, body: String?): String? = body?.takeIf { debug }
}
