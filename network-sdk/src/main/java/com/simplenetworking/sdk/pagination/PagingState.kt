package com.simplenetworking.sdk.pagination

/**
 * Represents the state of incremental pagination operations.
 */
data class PagingState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
    val nextPage: Int? = 1,
    val nextCursor: String? = null,
    val error: Throwable? = null
)
