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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.new_dopamind.R
import com.example.new_dopamind.data.api.ApiClient
import com.example.new_dopamind.data.model.RegisterBody
import com.example.new_dopamind.data.repository.UserRepository
import com.example.new_dopamind.databinding.ActivityRegisterBinding
import com.example.new_dopamind.ui.viewmodel.ViewModelFactory

@SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
class RegisterActivity : AppCompatActivity(R.layout.activity_register) {
    private val binding by viewBinding(ActivityRegisterBinding::bind)
    private val registerViewModel by lazy {
        val factory = ViewModelFactory(
            UserRepository(ApiClient.apiClient)
        )
        ViewModelProvider(this, factory)[RegisterViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOrientation()
        setupRegisterButton()
        setupLoginText()
        setupObservers()
    }

    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setupLoginText() {
        binding.loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startScaleDownAnimation(view)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> startScaleUpAnimation(view)
            }
            false
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val username = binding.edRegisterUsername.text.toString()
            val phone = binding.edRegisterPhone.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (listOf(name, username, phone, email, password).any { it.isEmpty() }) {
                showToast(resources.getString(R.string.empty_input))
            } else {
                registerViewModel.userRegister(
                    RegisterBody(
                        name = name,
                        email = email,
                        password = password,
                        username = username,
                        phone = phone
                    )
                )
            }
        }
    }

    private fun setupObservers() {
        with(registerViewModel) {
            // Handle registration response
            registerResponse.observe(this@RegisterActivity) {
                Log.d(TAG, "Register Response received")
                navigateToLogin()
            }

            // Handle loading state
            isLoading.observe(this@RegisterActivity) { isLoading ->
                updateLoadingState(isLoading)
            }

            // Handle error messages
            errorMessage.observe(this@RegisterActivity) { message ->
                showToast(message)
            }

            // Handle exceptions
            exception.observe(this@RegisterActivity) { hasException ->
                if (hasException) {
                    handleException()
                }
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            tvRegister.visibility = if (isLoading) View.GONE else View.VISIBLE
            pbRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun handleException() {
        showToast(resources.getString(R.string.cannot_connect_to_server))
        registerViewModel.resetExceptionValue()
    }

    companion object {
        private const val TAG = "RegisterActivity"
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