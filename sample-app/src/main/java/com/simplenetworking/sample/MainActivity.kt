package com.simplenetworking.sample

import android.content.Intent
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
import com.simplenetworking.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * Example activity showing how a consumer app can observe SDK-driven result states.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets()

        binding.fetchButton.setOnClickListener {
            viewModel.fetchPost()
        }
        binding.authDemoButton.setOnClickListener {
            startActivity(Intent(this, AuthDemoActivity::class.java))
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

    private fun render(state: MainViewModel.UiState) {
        when (state) {
            MainViewModel.UiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.statusText.text = getString(R.string.status_idle)
                binding.bodyText.text = ""
            }
            MainViewModel.UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.statusText.text = getString(R.string.status_loading)
                binding.bodyText.text = ""
            }
            MainViewModel.UiState.Empty -> {
                binding.progressBar.visibility = View.GONE
                binding.statusText.text = getString(R.string.status_empty)
                binding.bodyText.text = ""
            }
            is MainViewModel.UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.statusText.text = getString(R.string.status_error)
                binding.bodyText.text = state.message
            }
            is MainViewModel.UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.statusText.text = getString(R.string.status_success)
                binding.bodyText.text = buildString {
                    appendLine("id: ${state.post.id}")
                    appendLine("userId: ${state.post.userId}")
                    appendLine()
                    appendLine(state.post.title)
                    appendLine()
                    append(state.post.body)
                }
            }
        }
    }
}
