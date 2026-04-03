package com.simplenetworking.sdk.pagination

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Coordinates page-based and cursor-based pagination in a transport-agnostic way.
 */
class Paginator<T>(
    private val onLoadPage: suspend (page: Int?, cursor: String?) -> PaginationResult<T>
) {
    private val state = MutableStateFlow(PagingState<T>())

    /**
     * Exposes the current pagination state.
     */
    fun state(): Flow<PagingState<T>> = state.asStateFlow()

    /**
     * Resets the paginator and loads the first batch.
     */
    suspend fun refresh() {
        state.value = PagingState(isLoading = true)
        loadNext()
    }

    /**
     * Loads the next page or cursor batch if available.
     */
    suspend fun loadNext() {
        val current = state.value
        if (current.isLoading || current.endReached) return

        state.value = current.copy(isLoading = true, error = null)
        runCatching {
            onLoadPage(current.nextPage, current.nextCursor)
        }.onSuccess { result ->
            val merged = current.items + result.items
            state.value = current.copy(
                items = merged,
                isLoading = false,
                endReached = result.endReached,
                nextPage = result.nextPage,
                nextCursor = result.nextCursor,
                error = null
            )
        }.onFailure { throwable ->
            state.value = current.copy(isLoading = false, error = throwable)
        }
    }

    /**
     * Result contract used by both page- and cursor-based APIs.
     */
    data class PaginationResult<T>(
        val items: List<T>,
        val endReached: Boolean,
        val nextPage: Int? = null,
        val nextCursor: String? = null
    )
}
