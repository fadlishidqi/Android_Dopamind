package com.example.new_dopamind.ui.main

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.new_dopamind.R
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.databinding.ActivitySosBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class SosActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySosBinding
    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupViews()
        setupFaqItems()
        setupUserInfo()
        setupLocation()
        startSosButtonAnimation()
    }

    private fun startSosButtonAnimation() {
        val pulseAnimator = ValueAnimator.ofFloat(1f, 0.95f, 1f)
        pulseAnimator.duration = 1000
        pulseAnimator.repeatCount = ValueAnimator.INFINITE
        pulseAnimator.repeatMode = ValueAnimator.RESTART

        pulseAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            binding.btnSos.scaleX = value
            binding.btnSos.scaleY = value
        }

        pulseAnimator.start()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            btnSos.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.clearAnimation()
                        view.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start()
                    }
                    MotionEvent.ACTION_UP -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction {
                                startSosButtonAnimation()
                                // Navigate to SosDetailActivity
                                startActivity(Intent(this@SosActivity, SosDetailActivity::class.java))
                            }
                            .start()
                        view.performClick()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction {
                                startSosButtonAnimation()
                            }
                            .start()
                    }
                }
                true
            }
        }
    }

    private fun setupUserInfo() {
        lifecycleScope.launch {
            try {
                val username = userPreference.username.first()
                binding.tvUsername.text = getString(R.string.hello_user, username)
            } catch (e: Exception) {
                binding.tvUsername.text = getString(R.string.hello_user, "User")
            }
        }
    }

    private fun setupFaqItems() {
        binding.apply {
            layoutPanicAttack.setOnClickListener {
                toggleFaqItem(tvPanicContent, imgPanicArrow)
            }

            layoutBreathing.setOnClickListener {
                toggleFaqItem(tvBreathingContent, imgBreathingArrow)
            }

            layoutAnxiety.setOnClickListener {
                toggleFaqItem(tvAnxietyContent, imgAnxietyArrow)
            }
        }
    }

    private fun toggleFaqItem(contentView: View, arrowView: View) {
        if (contentView.visibility == View.VISIBLE) {
            contentView.visibility = View.GONE
            arrowView.animate().rotation(0f).setDuration(200).start()
        } else {
            contentView.visibility = View.VISIBLE
            arrowView.animate().rotation(180f).setDuration(200).start()
        }
    }

    private fun setupLocation() {
        if (checkLocationPermission()) {
            getCurrentLocation()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocation() {
        when {
            checkLocationPermission() -> {
                getCurrentLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showLocationPermissionDialog()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showLocationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.location_permission_title)
            .setMessage(R.string.location_permission_message)
            .setPositiveButton(R.string.settings) { _, _ ->
                openSettings()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun openSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(it.latitude, it.longitude, 1) { addresses ->
                                val address = addresses.firstOrNull()?.getAddressLine(0)
                                    ?: "${it.latitude}, ${it.longitude}"
                                binding.tvLocation.text = address
                                binding.tvLocation.setTextColor(
                                    ContextCompat.getColor(this, R.color.black)
                                )
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            val address = addresses?.firstOrNull()?.getAddressLine(0)
                                ?: "${it.latitude}, ${it.longitude}"
                            binding.tvLocation.text = address
                            binding.tvLocation.setTextColor(
                                ContextCompat.getColor(this, R.color.black)
                            )
                        }
                    } catch (e: Exception) {
                        binding.tvLocation.text = getString(R.string.location_error)
                        binding.tvLocation.setTextColor(
                            ContextCompat.getColor(this, R.color.red)
                        )
                    }
                }
            }.addOnFailureListener {
                binding.tvLocation.text = getString(R.string.location_unavailable)
                binding.tvLocation.setTextColor(
                    ContextCompat.getColor(this, R.color.red)
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    binding.tvLocation.text = getString(R.string.location_permission_denied)
                    binding.tvLocation.setTextColor(
                        ContextCompat.getColor(this, R.color.red)
                    )
                }
            }
        }
    }
}