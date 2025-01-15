package com.example.new_dopamind.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.new_dopamind.R
import com.example.new_dopamind.data.api.ApiClientBearer
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.databinding.ActivityHistoryBinding
import com.example.new_dopamind.ui.adapter.MoodHistoryAdapter
import com.example.new_dopamind.ui.auth.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity(R.layout.activity_history) {
    private val binding by viewBinding(ActivityHistoryBinding::bind)
    private val adapter = MoodHistoryAdapter()
    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }
    private val TAG = "HistoryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupBackButton()
        loadMoodHistory()
    }

    private fun setupRecyclerView() {
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadMoodHistory()
        }

        binding.swipeRefresh.setColorSchemeResources(
            R.color.black,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            rvHistory.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun loadMoodHistory() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val token = userPreference.userToken.first()

                if (token.isEmpty()) {
                    handleExpiredToken()
                    return@launch
                }

                Log.d(TAG, "Using token: $token")
                val apiService = ApiClientBearer.create(token)
                val response = apiService.getMoodHistory()

                if (response.isSuccessful) {
                    response.body()?.let { historyResponse ->
                        Log.d(TAG, "Response received: $historyResponse")
                        if (historyResponse.data.isEmpty()) {
                            showEmptyState()
                        } else {
                            hideEmptyState()
                            adapter.setItems(historyResponse.data)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API Error: $errorBody")
                    handleApiError(errorBody)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading history: ${e.message}", e)
                handleError(e)
            } finally {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showEmptyState() {
        binding.apply {
            rvHistory.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyState() {
        binding.apply {
            rvHistory.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
        }
    }

    private fun handleApiError(errorBody: String?) {
        when {
            errorBody?.contains("Token telah kadaluarsa") == true -> handleExpiredToken()
            else -> showToast("Failed to load history")
        }
    }

    private fun handleError(exception: Exception) {
        exception.printStackTrace()
        showToast("Error: ${exception.message}")
    }

    private fun handleExpiredToken() {
        lifecycleScope.launch {
            try {
                userPreference.updateUserLoginStatusAndToken(false, "")
                showToast("Session expired. Please login again")
                navigateToLogin()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling expired token", e)
                showToast("Error handling session expiration")
            }
        }
    }

    private fun navigateToLogin() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadMoodHistory()
    }
}