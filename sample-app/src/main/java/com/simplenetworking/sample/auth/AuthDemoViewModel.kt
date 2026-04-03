package com.simplenetworking.sample.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplenetworking.sample.data.PostDto
import com.simplenetworking.sample.data.SampleRepository
import com.simplenetworking.sdk.result.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the auth demonstration screen.
 */
class AuthDemoViewModel(
    private val repository: SampleRepository = SampleRepository(),
    private val sessionManager: SampleSessionManager
) : ViewModel() {
    private val uiState = MutableStateFlow(AuthUiState(token = sessionManager.accessToken()))

    init {
        viewModelScope.launch {
            sessionManager.observeToken().collect { token ->
                uiState.update { it.copy(token = token) }
            }
        }
    }

    fun uiState(): StateFlow<AuthUiState> = uiState.asStateFlow()

    fun fetchAuthenticatedPost() {
        viewModelScope.launch {
            repository.loadAuthenticatedPost(id = 2).collect { result ->
                uiState.value = when (result) {
                    is ApiResult.Loading -> uiState.value.copy(
                        isLoading = true,
                        title = "Calling API with bearer token",
                        body = "Authorization: Bearer ${sessionManager.accessToken()}",
                        error = null
                    )
                    is ApiResult.Empty -> uiState.value.copy(
                        isLoading = false,
                        title = "Authenticated request returned no content",
                        body = "",
                        error = null
                    )
                    is ApiResult.Success -> uiState.value.copy(
                        isLoading = false,
                        title = "Authenticated request completed",
                        body = formatPost(result.data),
                        error = null
                    )
                    is ApiResult.Error -> uiState.value.copy(
                        isLoading = false,
                        title = "Authenticated request failed",
                        body = "",
                        error = result.exception.message
                    )
                }
            }
        }
    }

    fun expireToken() {
        sessionManager.expireToken()
        uiState.update {
            it.copy(
                title = "Token expired locally",
                body = "The next real 401 response would trigger TokenAuthenticator.",
                error = null
            )
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    title = "Refreshing token",
                    body = "Calling refreshTokenProvider...",
                    error = null
                )
            }
            val token = sessionManager.refreshAccessToken()
            uiState.update {
                it.copy(
                    isLoading = false,
                    title = "Token refreshed",
                    body = "New token: $token",
                    error = null
                )
            }
        }
    }

    private fun formatPost(post: PostDto): String = buildString {
        appendLine("Authorization header is injected by TokenInterceptor.")
        appendLine()
        appendLine("id: ${post.id}")
        appendLine("userId: ${post.userId}")
        appendLine()
        appendLine(post.title)
        appendLine()
        append(post.body)
    }
}

/**
 * UI state for the auth demo screen.
 */
data class AuthUiState(
    val token: String,
    val isLoading: Boolean = false,
    val title: String = "Bearer authentication example",
    val body: String = "Requests use tokenProvider and automatic refresh is wired through refreshTokenProvider.",
    val error: String? = null
)
