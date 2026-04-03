package com.simplenetworking.sample.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit contract for the example screen.
 */
interface SampleApi {
    @GET("posts/{id}")
    suspend fun getPost(
        @Path("id") id: Int
    ): Response<PostDto>

    @GET("posts/{id}")
    suspend fun getAuthenticatedPost(
        @Path("id") id: Int
    ): Response<PostDto>
}
