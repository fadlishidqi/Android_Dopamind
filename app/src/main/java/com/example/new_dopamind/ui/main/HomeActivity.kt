package com.example.new_dopamind.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.new_dopamind.R
import com.example.new_dopamind.databinding.ActivityHomeBinding
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.ui.auth.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.new_dopamind.data.api.ApiClientBearer
import java.util.Calendar

class HomeActivity : AppCompatActivity(R.layout.activity_home) {
    private val binding by viewBinding(ActivityHomeBinding::bind)
    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBottomNavigation()
        setupFeatureCards()
        setupUserInfo()
        updateGreeting()
        checkUserSession()
        setupProfileImage()
        setupDarkOverlayClick()
    }

    private fun setupDarkOverlayClick() {
        binding.darkOverlay.setOnClickListener {
            toggleSidebar()
        }
    }

    private fun setupProfileImage() {
        binding.profileImage.setOnClickListener {
            toggleSidebar()
        }

        binding.sidebarContainer.root.apply {
            findViewById<ImageView>(R.id.btn_edit_profile).setOnClickListener {
                Log.d(TAG, "Edit Profile clicked")
                toggleSidebar()
            }

            findViewById<ImageView>(R.id.btn_settings).setOnClickListener {
                Log.d(TAG, "Settings clicked")
                toggleSidebar()
            }

            findViewById<ImageView>(R.id.btn_logout).setOnClickListener {
                Log.d(TAG, "Logout clicked")
                handleLogout()
                toggleSidebar()
            }

            findViewById<ImageView>(R.id.btn_close_sidebar).setOnClickListener {
                toggleSidebar()
            }
        }
    }

    private fun toggleSidebar() {
        val sidebar = binding.sidebarContainer.root
        val darkOverlay = binding.darkOverlay

        if (sidebar.visibility == View.GONE) {
            sidebar.visibility = View.VISIBLE
            darkOverlay.visibility = View.VISIBLE
            sidebar.translationX = 300f
            sidebar.animate()
                .translationX(0f)
                .setDuration(300)
                .start()
            darkOverlay.animate()
                .alpha(0.7f)
                .setDuration(300)
                .start()
        } else {
            sidebar.animate()
                .translationX(300f)
                .setDuration(300)
                .withEndAction {
                    sidebar.visibility = View.GONE
                }
                .start()
            darkOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    darkOverlay.visibility = View.GONE
                }
                .start()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.apply {
            findViewById<ImageButton>(R.id.btn_home).setOnClickListener {
                Log.d(TAG, "Already on home screen")
            }

            findViewById<ImageButton>(R.id.btn_history).setOnClickListener {
                startActivity(Intent(this@HomeActivity, HistoryActivity::class.java))
            }

            findViewById<ImageButton>(R.id.btn_sos).setOnClickListener {
                startActivity(Intent(this@HomeActivity, SosActivity::class.java))
            }

            findViewById<ImageButton>(R.id.btn_booking).setOnClickListener {
                startActivity(Intent(this@HomeActivity, BookingActivity::class.java))
            }

            findViewById<ImageButton>(R.id.btn_chatbot).setOnClickListener {
                startActivity(Intent(this@HomeActivity, ChatActivity::class.java))
            }
        }
    }

    private fun setupFeatureCards() {
        binding.apply {
            featureCards.root.apply {
                findViewById<CardView>(R.id.red_day_card).setOnClickListener {
                    Log.d(TAG, "Red Day Card clicked")
                    startActivity(Intent(this@HomeActivity, DiaryActivity::class.java))
                }

                findViewById<CardView>(R.id.educare_card).setOnClickListener {
                    Log.d(TAG, "EduCare Card clicked")
                    startActivity(Intent(this@HomeActivity, EducareActivity::class.java))
                }

                findViewById<CardView>(R.id.diary_card).setOnClickListener {
                    Log.d(TAG, "Dopi Chatbot Card clicked")
                    startActivity(Intent(this@HomeActivity, ChatActivity::class.java))
                }
            }
        }
    }

    private fun navigateToHistory() {
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    private fun updateGreeting() {
        val calendar = Calendar.getInstance()
        val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning!"
            in 12..15 -> "Good Afternoon!"
            in 16..20 -> "Good Evening!"
            else -> "Good Night!"
        }
        binding.greetingSubtext.text = greeting
    }

    private fun setupUserInfo() {
        lifecycleScope.launch {
            try {
                val username = userPreference.username.first()
                binding.apply {
                    greetingText.text = "Hi, $username"
                    sidebarContainer.root.findViewById<TextView>(R.id.profile_text).text =
                        username.ifEmpty { "User" }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up user info", e)
                binding.apply {
                    greetingText.text = "Hi, User"
                    sidebarContainer.root.findViewById<TextView>(R.id.profile_text).text = "User"
                }
            }
        }
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            userPreference.apply {
                updateUserLoginStatusAndToken(false, "")
                updateUsernameAndEmail("", "")
            }
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    private fun checkUserSession() {
        lifecycleScope.launch {
            try {
                val token = userPreference.userToken.first()
                if (token.isEmpty()) {
                    handleSessionExpired("Invalid session. Please login again.")
                    return@launch
                }

                try {
                    val apiService = ApiClientBearer.create(token)
                    val response = apiService.getMoodHistory()

                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string()
                        when {
                            errorBody?.contains("Token telah kadaluarsa") == true -> {
                                handleSessionExpired("Session has expired. Please login again.")
                            }
                            errorBody?.contains("Token tidak valid") == true -> {
                                handleSessionExpired("Invalid token. Please login again.")
                            }
                            response.code() == 401 -> {
                                handleSessionExpired("Authentication required. Please login again.")
                            }
                            else -> {
                                Log.e(TAG, "Error response: $errorBody")
                                handleSessionExpired("An error occurred. Please login again.")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error verifying token: ${e.message}", e)
                    handleSessionExpired("Connection error. Please try again.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking user session: ${e.message}", e)
                handleSessionExpired("An error occurred. Please login again.")
            }
        }
    }

    private fun handleSessionExpired(message: String) {
        lifecycleScope.launch {
            try {
                userPreference.apply {
                    updateUserLoginStatusAndToken(false, "")
                    updateUsernameAndEmail("", "")
                }
                Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
                navigateToLogin()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling expired session: ${e.message}", e)
                Toast.makeText(this@HomeActivity,
                    "Error logging out. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkUserSession()
    }
}