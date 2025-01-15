package com.example.new_dopamind.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.new_dopamind.R
import com.example.new_dopamind.data.api.ApiClient
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.data.model.PredictRequest
import com.example.new_dopamind.data.repository.UserRepository
import com.example.new_dopamind.databinding.ActivityDiaryBinding
import com.example.new_dopamind.ui.auth.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DiaryActivity : AppCompatActivity(R.layout.activity_diary) {
    private val binding by viewBinding(ActivityDiaryBinding::bind)
    private var wordCount = 0
    private val maxWords = 250
    private val TAG = "DiaryActivity"

    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUserRepository()
        setupBackButton()
        setupSubmitButton()
        setupWordCounter()
    }

    private fun setupUserRepository() {
        lifecycleScope.launch {
            try {
                val token = userPreference.userToken.first()
                userRepository = UserRepository(ApiClient.getPredictApiClient(token))
            } catch (e: Exception) {
                Log.e(TAG, "Error getting token: ${e.message}")
                Toast.makeText(this@DiaryActivity, "Error: Unable to authenticate", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmitContainer.setOnClickListener {
            val diaryContent = binding.diaryInput.text.toString()
            if (diaryContent.isNotEmpty()) {
                if (::userRepository.isInitialized) {
                    submitDiary(diaryContent)
                } else {
                    Toast.makeText(this, "Please wait, initializing...", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please write something in your diary", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            tvSubmit.visibility = if (isLoading) View.GONE else View.VISIBLE
            pbSubmit.visibility = if (isLoading) View.VISIBLE else View.GONE
            diaryInput.isEnabled = !isLoading
            backButton.isEnabled = !isLoading
            btnSubmitContainer.isEnabled = !isLoading
        }
    }

    private fun submitDiary(content: String) {
        lifecycleScope.launch {
            try {
                updateLoadingState(true)
                val predictRequest = PredictRequest(listOf(content))
                val response = userRepository.predictText(predictRequest)

                if (response.isSuccessful) {
                    response.body()?.let { predictResponse ->
                        Log.d(TAG, "Prediction Result:")
                        Log.d(TAG, "Prediction: ${predictResponse.predictions}")
                        Log.d(TAG, "Text: ${predictResponse.texts}")

                        val intent = Intent(this@DiaryActivity, PredictionResultActivity::class.java).apply {
                            putExtra(PredictionResultActivity.EXTRA_PREDICTION, predictResponse.predictions)
                        }
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API Error: $errorBody")

                    if (errorBody?.contains("Token telah kadaluarsa") == true) {
                        handleExpiredToken()
                    } else {
                        Toast.makeText(this@DiaryActivity, "Failed to submit diary", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting diary: ${e.message}", e)
                Toast.makeText(this@DiaryActivity, "Error submitting diary", Toast.LENGTH_SHORT).show()
            } finally {
                updateLoadingState(false)
            }
        }
    }

    private fun handleExpiredToken() {
        lifecycleScope.launch {
            try {
                userPreference.updateUserLoginStatusAndToken(false, "")

                Toast.makeText(this@DiaryActivity, "Session expired. Please login again", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@DiaryActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling expired token: ${e.message}", e)
            }
        }
    }

    private fun setupWordCounter() {
        binding.diaryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                wordCount = if (text.trim().isEmpty()) {
                    0
                } else {
                    text.trim().split("\\s+".toRegex()).size
                }
                updateWordCounter()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateWordCounter() {
        binding.wordCounter.text = "$wordCount/$maxWords words"

        if (wordCount > maxWords) {
            binding.wordCounter.setTextColor(getColor(android.R.color.holo_red_dark))
            binding.btnSubmitContainer.isEnabled = false
        } else {
            binding.wordCounter.setTextColor(getColor(android.R.color.black))
            binding.btnSubmitContainer.isEnabled = true
        }
    }
}