package com.simplenetworking.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplenetworking.sample.data.PostDto
import com.simplenetworking.sample.data.SampleRepository
import com.simplenetworking.sdk.result.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel that loads a single sample payload through the SDK.
 */
class MainViewModel(
    private val repository: SampleRepository = SampleRepository()
) : ViewModel() {
    private val uiState = MutableStateFlow<UiState>(UiState.Idle)
    private var fetchJob: Job? = null

    fun uiState(): StateFlow<UiState> = uiState.asStateFlow()

    fun fetchPost() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            repository.loadPost(id = 1).collect { result ->
                uiState.value = when (result) {
                    is ApiResult.Loading -> UiState.Loading
                    is ApiResult.Empty -> UiState.Empty
                    is ApiResult.Success -> UiState.Success(result.data)
                    is ApiResult.Error -> UiState.Error(result.exception.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * View state for the sample screen.
     */
    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data object Empty : UiState()
        data class Success(val post: PostDto) : UiState()
        data class Error(val message: String) : UiState()
    }
}
