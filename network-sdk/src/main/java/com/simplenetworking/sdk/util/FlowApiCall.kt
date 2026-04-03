package com.simplenetworking.sdk.util

import com.simplenetworking.sdk.error.ErrorMapper
import com.simplenetworking.sdk.result.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * Bridges safe Retrofit execution into a Flow-based API surface.
 */
fun <T> flowApiCall(
    errorMapper: ErrorMapper,
    request: suspend () -> Response<T>
): Flow<ApiResult<T>> = flow {
    emit(ApiResult.Loading)
    emit(safeApiCall(errorMapper, request))
}
