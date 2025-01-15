package com.example.new_dopamind.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.new_dopamind.R
import com.example.new_dopamind.data.api.NewsApiClient
import com.example.new_dopamind.databinding.ActivityEducareBinding
import com.example.new_dopamind.ui.adapter.NewsAdapter
import kotlinx.coroutines.launch
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import kotlinx.coroutines.flow.first

class EducareActivity : AppCompatActivity(R.layout.activity_educare) {
    private val binding by viewBinding(ActivityEducareBinding::bind)
    private val newsAdapter = NewsAdapter()
    private val TAG = "EducareActivity"

    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupBackButton()
        loadNews()
        setupUserInfo()
    }

    private fun setupUserInfo() {
        lifecycleScope.launch {
            try {
                val username = userPreference.username.first()
                binding.greetingText.text = "Hi, $username"
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up user info", e)
                binding.greetingText.text = "Hi, User"
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(this@EducareActivity)
            adapter = newsAdapter
            setHasFixedSize(true)
            Log.d(TAG, "RecyclerView setup complete")
        }

        newsAdapter.setOnItemClickListener { newsItem ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.link))
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening link: ${e.message}")
                showError("Cannot open link")
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setOnRefreshListener {
                loadNews()
            }
            setColorSchemeResources(
                R.color.black,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
            )
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadNews() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val response = NewsApiClient.newsApiService.getLifestyleNews()

                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        Log.d(TAG, "Received ${newsResponse.data.size} news items")
                        newsAdapter.setItems(newsResponse.data)
                    }
                } else {
                    Log.e(TAG, "Error response: ${response.code()}")
                    showError("Failed to load news")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading news", e)
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvNews.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadNews()
        setupUserInfo()
    }
}