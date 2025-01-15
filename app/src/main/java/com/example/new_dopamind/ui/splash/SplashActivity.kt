package com.example.new_dopamind.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.new_dopamind.R
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.ui.auth.LoginActivity
import com.example.new_dopamind.ui.main.HomeActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"

    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.d(TAG, "SplashActivity Created")

        // Check location permission first
        checkAndRequestLocationPermission()
    }

    private fun checkAndRequestLocationPermission() {
        when {
            checkLocationPermission() -> {
                // Permission already granted, proceed with normal flow
                startSplashDelay()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Show explanation if needed
                requestLocationPermission()
            }
            else -> {
                // Request permission
                requestLocationPermission()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // Whether permission is granted or not, proceed with the app
                startSplashDelay()
            }
        }
    }

    private fun startSplashDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatusAndNavigate()
        }, 2000)
    }

    private fun checkLoginStatusAndNavigate() {
        lifecycleScope.launch {
            try {
                val isLoggedIn = userPreference.isUserLogin.first()
                val token = userPreference.userToken.first()

                Log.d(TAG, "Login Status: $isLoggedIn")
                Log.d(TAG, "Token: $token")

                if (isLoggedIn && token.isNotEmpty()) {
                    navigateToHome()
                } else {
                    navigateToLogin()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login status: ${e.message}", e)
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this@SplashActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}