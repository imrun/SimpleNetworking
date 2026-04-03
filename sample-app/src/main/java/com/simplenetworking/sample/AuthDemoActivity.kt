package com.simplenetworking.sample

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.simplenetworking.sample.auth.AuthDemoViewModel
import com.simplenetworking.sample.auth.AuthDemoViewModelFactory
import com.simplenetworking.sample.auth.AuthUiState
import com.simplenetworking.sample.databinding.ActivityAuthDemoBinding
import kotlinx.coroutines.launch

/**
 * Demonstrates bearer token configuration and manual refresh in the sample app.
 */
class AuthDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthDemoBinding
    private val viewModel: AuthDemoViewModel by viewModels {
        val sessionManager = (application as SampleApplication).sessionManager
        AuthDemoViewModelFactory(sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets()

        binding.fetchAuthenticatedButton.setOnClickListener {
            viewModel.fetchAuthenticatedPost()
        }
        binding.expireTokenButton.setOnClickListener {
            viewModel.expireToken()
        }
        binding.refreshTokenButton.setOnClickListener {
            viewModel.refreshToken()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState().collect(::render)
            }
        }
    }

    private fun applySystemBarInsets() {
        val root = binding.root
        val initialLeft = root.paddingLeft
        val initialTop = root.paddingTop
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(
                initialLeft + insets.left,
                initialTop + insets.top,
                initialRight + insets.right,
                initialBottom + insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun render(state: AuthUiState) {
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.tokenValueText.text = state.token
        binding.statusText.text = state.title
        binding.bodyText.text = state.error ?: state.body
    }
}
