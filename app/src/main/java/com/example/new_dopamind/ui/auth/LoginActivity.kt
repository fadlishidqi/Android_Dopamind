package com.example.new_dopamind.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.new_dopamind.R
import com.example.new_dopamind.data.api.ApiClient
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.data.model.LoginBody
import com.example.new_dopamind.data.model.LoginResponse
import com.example.new_dopamind.data.repository.UserRepository
import com.example.new_dopamind.databinding.ActivityLoginBinding
import com.example.new_dopamind.ui.main.HomeActivity
import com.example.new_dopamind.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
class LoginActivity : AppCompatActivity(R.layout.activity_login) {
    private val binding by viewBinding(ActivityLoginBinding::bind)
    private val loginViewModel by lazy {
        val factory = ViewModelFactory(
            UserRepository(ApiClient.apiClient)
        )
        ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }
    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOrientation()
        setupLoginButton()
        setupRegisterText()
        setupObservers()
    }

    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startScaleDownAnimation(view)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> startScaleUpAnimation(view)
            }
            false
        }

        binding.btnLogin.setOnClickListener {
            userEmail = binding.edLoginEmail.text.toString()
            val userPassword = binding.edLoginPassword.text.toString()

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                showToast(resources.getString(R.string.empty_input))
            } else {
                loginViewModel.userLogin(LoginBody(userEmail, userPassword))
            }
        }
    }

    private fun setupObservers() {
        with(loginViewModel) {
            // Handle login response
            loginResponse.observe(this@LoginActivity) { response ->
                if (response.token.isNotEmpty()) {
                    handleSuccessLogin(response)
                }
            }

            // Handle loading state
            isLoading.observe(this@LoginActivity) { isLoading ->
                updateLoadingState(isLoading)
            }

            // Handle error messages
            errorMessage.observe(this@LoginActivity) { message ->
                handleError(message)
            }

            // Handle exceptions
            exception.observe(this@LoginActivity) { hasException ->
                if (hasException) {
                    handleException()
                }
            }
        }
    }

    private fun handleSuccessLogin(response: LoginResponse) {
        lifecycleScope.launch {
            try {
                userPreference.updateUserLoginStatusAndToken(true, response.token)
                userPreference.updateUsernameAndEmail(response.data.username, response.data.email)
                Log.d(TAG, "User data saved to preferences")
                navigateToHome()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user data: ${e.message}", e)
                showToast("Error saving user data")
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            tvLogin.visibility = if (isLoading) View.GONE else View.VISIBLE
            pbLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun handleError(message: String) {
        showToast(message)
        binding.apply {
            edLoginPassword.text?.clear()
            edLoginPassword.requestFocus()
        }
    }

    private fun handleException() {
        showToast(resources.getString(R.string.cannot_connect_to_server))
        loginViewModel.resetExceptionValue()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

    private fun setupRegisterText() {
        binding.daftarsekarangText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun startScaleDownAnimation(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .start()
    }

    private fun startScaleUpAnimation(view: View) {
        view.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(100)
            .start()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}