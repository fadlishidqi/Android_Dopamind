package com.example.new_dopamind.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.new_dopamind.data.api.ApiClientBearer
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.databinding.ActivityBookingBinding
import com.example.new_dopamind.ui.adapter.DoctorAdapter
import com.example.new_dopamind.ui.auth.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding
    private val doctorAdapter = DoctorAdapter()

    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeRefresh()
        setupBackButton()
        setupSearch()
        loadDoctors()
    }

    private fun setupRecyclerView() {
        binding.rvDoctors.apply {
            layoutManager = LinearLayoutManager(this@BookingActivity)
            adapter = doctorAdapter
            setHasFixedSize(true)
        }

        doctorAdapter.setOnBookClickListener { doctor ->
            Intent(this, PaymentActivity::class.java).apply {
                putExtra(PaymentActivity.EXTRA_DOCTOR_NAME, doctor.name)
                putExtra(PaymentActivity.EXTRA_DOCTOR_PRICE, doctor.price)
                startActivity(this)
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadDoctors()
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Implement search functionality here
            }
        })
    }

    private fun loadDoctors() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val token = userPreference.userToken.first()
                val apiService = ApiClientBearer.create(token)

                val response = apiService.getDoctors()
                if (response.isSuccessful) {
                    response.body()?.let { doctorResponse ->
                        doctorAdapter.setDoctors(doctorResponse.data)
                    }
                } else {
                    if (response.code() == 401) {
                        handleUnauthorized()
                    } else {
                        showError("Failed to load doctors")
                    }
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleUnauthorized() {
        lifecycleScope.launch {
            userPreference.updateUserLoginStatusAndToken(false, "")
            showError("Session expired. Please login again")
            startActivity(Intent(this@BookingActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}