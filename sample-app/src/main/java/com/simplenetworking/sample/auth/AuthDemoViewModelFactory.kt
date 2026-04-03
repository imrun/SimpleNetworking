package com.simplenetworking.sample.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory used to pass the sample session manager into the auth demo ViewModel.
 */
class AuthDemoViewModelFactory(
    private val sessionManager: SampleSessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthDemoViewModel::class.java)) {
            return AuthDemoViewModel(sessionManager = sessionManager) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
