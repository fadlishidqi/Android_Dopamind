package com.example.new_dopamind.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.new_dopamind.R
import com.example.new_dopamind.data.api.ApiClientBearer
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.data.model.HospitalData
import com.example.new_dopamind.data.model.LocationRequest
import com.example.new_dopamind.databinding.ActivitySosDetailBinding
import com.example.new_dopamind.ui.auth.LoginActivity
import com.example.new_dopamind.ui.main.HomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SosDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivitySosDetailBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }

    private var countdownTimer: CountDownTimer? = null
    private var isCountdownRunning = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        private const val COUNTDOWN_DURATION = 10000L // 10 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupMapFragment()
        initLocationClient()
        fetchLocationAndDirections()
    }

    private fun setupViews() {
        binding.progressBar.visibility = View.GONE

        // Set up slider
        binding.slider.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startSlideAnimation()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopSlideAnimation()
            }
            true
        }

        startCountdown()
    }

    private fun fetchLocationAndDirections() {
        lifecycleScope.launch {
            try {
                val currentLocation = getCurrentUserLocation()
                val nearestHospital = getNearestHospital(currentLocation)
                val duration = getDirectionsToHospital(currentLocation, nearestHospital)

                updateLocationInfo(currentLocation, nearestHospital, duration)
            } catch (e: Exception) {
                showError("Error fetching location and directions: ${e.message}")
            }
        }
    }

    private suspend fun getCurrentUserLocation(): Location {
        return suspendCoroutine { continuation ->
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@suspendCoroutine
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                continuation.resume(location)
            }
        }
    }

    private suspend fun getNearestHospital(currentLocation: Location): HospitalData {
        val token = userPreference.userToken.first()
        val apiService = ApiClientBearer.create(token)

        val locationRequest = LocationRequest(
            location = "${currentLocation.latitude},${currentLocation.longitude}",
            radius = 3500
        )

        val response = apiService.getNearbyHospitals(locationRequest)
        if (response.isSuccessful) {
            return response.body()?.data?.firstOrNull() ?: throw Exception("No nearby hospitals found")
        } else {
            throw Exception("Failed to fetch nearby hospitals")
        }
    }

    private suspend fun getDirectionsToHospital(currentLocation: Location, nearestHospital: HospitalData): String {
        val token = userPreference.userToken.first()
        val apiService = ApiClientBearer.create(token)

        val locationRequest = LocationRequest(
            location = "${nearestHospital.location.lat},${nearestHospital.location.lng}",
            radius = 5500,
            origin = "${currentLocation.latitude},${currentLocation.longitude}"
        )

        val response = apiService.getDirections(locationRequest)
        return if (response.isSuccessful) {
            response.body()?.let { directionsResponse ->
                val directionData = directionsResponse.data

                // Draw polyline on map
                drawDirectionsPolyline(directionData.polyline)

                // Show distance and duration information
                showDirectionInfo(
                    directionData.distance,
                    directionData.duration,
                    nearestHospital.name
                )

                directionData.duration
            } ?: "Unknown"
        } else {
            showError("Failed to get directions")
            "Unknown"
        }
    }

    private fun updateLocationInfo(currentLocation: Location, nearestHospital: HospitalData, duration: String) {
        binding.apply {
            tvFromLocation.text = getCurrentLocationAddress(currentLocation)
            tvToLocation.text = nearestHospital.address
            tvDuration.text = duration
        }
    }

    private fun getCurrentLocationAddress(location: Location): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        return addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
    }

    private fun startSlideAnimation() {
        binding.sliderIcon.animate()
            .translationX(200f)
            .setDuration(300)
            .start()
    }

    private fun stopSlideAnimation() {
        binding.sliderIcon.animate()
            .translationX(0f)
            .setDuration(300)
            .withEndAction {
                // User canceled the emergency
                stopCountdown()
                finish()
            }
            .start()
    }

    private fun startCountdown() {
        isCountdownRunning = true
        binding.countdownText.visibility = View.VISIBLE

        countdownTimer = object : CountDownTimer(COUNTDOWN_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                binding.countdownText.text = "Confirming in $secondsLeft.."
            }

            override fun onFinish() {
                isCountdownRunning = false
                binding.countdownText.visibility = View.GONE
                navigateToHome()
                showAmbulanceNotification()
            }
        }.start()
    }

    private fun stopCountdown() {
        isCountdownRunning = false
        countdownTimer?.cancel()
        binding.countdownText.visibility = View.GONE
    }

    private fun navigateToHome() {
        Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
        finish()
    }

    private fun showAmbulanceNotification() {
        // Show a notification to indicate that an ambulance is on the way
        Toast.makeText(this, "An ambulance is on the way.", Toast.LENGTH_LONG).show()
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkAndRequestLocationPermission()
    }

    private fun checkAndRequestLocationPermission() {
        when {
            checkLocationPermission() -> {
                setupLocationUpdates()
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

    private fun setupLocationUpdates() {
        if (checkLocationPermission()) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        updateMapWithCurrentLocation(it)
                    }
                }
            } catch (e: SecurityException) {
                Toast.makeText(this, "Error accessing location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateMapWithCurrentLocation(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)
        if (::mMap.isInitialized) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
        }
    }

    private fun drawDirectionsPolyline(encodedPolyline: String) {
        if (::mMap.isInitialized) {
            val decodedPolyline = PolyUtil.decode(encodedPolyline)
            mMap.addPolyline(PolylineOptions().addAll(decodedPolyline).color(Color.BLUE))
        }
    }

    private fun showDirectionInfo(distance: String, duration: String, hospitalName: String) {
        val infoText = "Nearest Hospital: $hospitalName\nDistance: $distance\nDuration: $duration"

        AlertDialog.Builder(this)
            .setTitle("Route Information")
            .setMessage(infoText)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (checkLocationPermission()) {
            try {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.apply {
                    isMyLocationButtonEnabled = true
                    isZoomControlsEnabled = true
                    isCompassEnabled = true
                }
            } catch (e: SecurityException) {
                showError("Error setting up map")
            }
        }
    }

    private fun addMarkerToMap(latLng: LatLng, title: String, snippet: String) {
        if (::mMap.isInitialized) {
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .snippet(snippet)
            )
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

    private fun showLocationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.location_permission_title)
            .setMessage(R.string.location_permission_message)
            .setPositiveButton(R.string.settings) { _, _ ->
                openLocationSettings()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun openLocationSettings() {
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).also {
            startActivity(it)
        }
    }

    private fun handleUnauthorized() {
        lifecycleScope.launch {
            userPreference.updateUserLoginStatusAndToken(false, "")
            showError("Session expired. Please login again")
            startActivity(Intent(this@SosDetailActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                    setupLocationUpdates()
                } else {
                    showError("Location permission is required")
                }
            }
        }
    }
}