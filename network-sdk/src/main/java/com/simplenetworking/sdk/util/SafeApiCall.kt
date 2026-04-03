package com.simplenetworking.sdk.util

import com.simplenetworking.sdk.error.ErrorMapper
import com.simplenetworking.sdk.result.ApiResult
import retrofit2.Response

/**
 * Executes a Retrofit call safely and maps all failures through [ErrorMapper].
 */
suspend fun <T> safeApiCall(
    errorMapper: ErrorMapper,
    request: suspend () -> Response<T>
): ApiResult<T> {
    return try {
        val response = request()
        when {
            response.isSuccessful -> {
                val body = response.body()
                if (body == null) ApiResult.Empty else ApiResult.Success(body)
            }
            else -> {
                val exception = retrofit2.HttpException(response)
                ApiResult.Error(errorMapper.map(exception))
            }
        }
    } catch (throwable: Throwable) {
        ApiResult.Error(errorMapper.map(throwable))
    }
}
