package com.simplenetworking.sample.data

import com.simplenetworking.sdk.core.NetworkClient
import com.simplenetworking.sdk.result.ApiResult
import kotlinx.coroutines.flow.Flow

/**
 * Demonstrates how app code can hide transport details behind a repository boundary.
 */
class SampleRepository(
    private val api: SampleApi = NetworkClient.createService()
) {
    fun loadPost(id: Int): Flow<ApiResult<PostDto>> {
        return NetworkClient.flowApiCall {
            api.getPost(id)
        }
    }

    fun loadAuthenticatedPost(id: Int): Flow<ApiResult<PostDto>> {
        return NetworkClient.flowApiCall {
            api.getAuthenticatedPost(id)
        }
    }
}
