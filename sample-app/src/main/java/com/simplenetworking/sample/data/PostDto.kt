package com.simplenetworking.sample.data

/**
 * DTO returned by the sample JSONPlaceholder endpoint.
 */
data class PostDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
