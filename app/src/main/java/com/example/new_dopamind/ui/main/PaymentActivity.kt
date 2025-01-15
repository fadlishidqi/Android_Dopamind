package com.example.new_dopamind.ui.main

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.new_dopamind.R
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.databinding.ActivityPaymentBinding
import com.example.new_dopamind.databinding.DialogPromoCodeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private var originalPrice: Double = 0.0
    private var promoDiscount: Double = 0.0

    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }

    companion object {
        const val EXTRA_DOCTOR_NAME = "extra_doctor_name"
        const val EXTRA_DOCTOR_PRICE = "extra_doctor_price"
        private const val TAG = "PaymentActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupPaymentDetails()
        setupOfferSection()
        setupCheckbox()
        setupPaymentButton()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupPaymentDetails() {
        val doctorName = intent.getStringExtra(EXTRA_DOCTOR_NAME) ?: "Unknown Doctor"
        val priceString = intent.getStringExtra(EXTRA_DOCTOR_PRICE) ?: "0"
        originalPrice = priceString.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy / hh:mm a", Locale.getDefault())
        val appointmentDateTime = dateFormat.format(calendar.time)

        binding.orderSummary.apply {
            findViewById<TextView>(R.id.tvDoctorName).text = "Doctor: $doctorName"
            findViewById<TextView>(R.id.tvDateTime).text = "Date: $appointmentDateTime"
            updateTotalPrice()
        }
    }

    private fun setupOfferSection() {
        binding.offerSection.setOnClickListener {
            showPromoDialog()
        }
    }

    private fun setupCheckbox() {
        binding.cbReceipt.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lifecycleScope.launch {
                    val userEmail = userPreference.userEmail.first()
                    if (userEmail.isEmpty()) {
                        showEmailDialog()
                    }
                }
            }
        }
    }

    private fun setupPaymentButton() {
        binding.btnConfirmPayment.setOnClickListener {
            confirmPayment()
        }
    }

    private fun confirmPayment() {
        binding.btnConfirmPayment.isEnabled = false

        binding.btnConfirmPayment.postDelayed({
            val intent = Intent(this, PaymentConfirmationActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)
    }


    private fun showPromoDialog() {
        val dialog = Dialog(this)
        val dialogBinding = DialogPromoCodeBinding.inflate(layoutInflater)

        dialog.setContentView(dialogBinding.root)
        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogBinding.apply {
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnApply.setOnClickListener {
                val promoCode = etPromoCode.text.toString().uppercase()
                applyPromoCode(promoCode)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showEmailDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_email, null)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Email Address")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { _, _ ->
                binding.cbReceipt.isChecked = false
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val email = etEmail.text.toString()
                if (email.isValidEmail()) {
                    lifecycleScope.launch {
                        userPreference.updateUsernameAndEmail("", email)
                    }
                    dialog.dismiss()
                } else {
                    etEmail.error = "Please enter a valid email address"
                }
            }
        }

        dialog.show()
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage("Are you sure you want to proceed with the payment?")
            .setPositiveButton("Yes") { _, _ ->
                processPayment()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun processPayment() {
        // Simulate payment process
        binding.btnConfirmPayment.isEnabled = false
        // Show loading if needed

        // Simulating API call delay
        binding.btnConfirmPayment.postDelayed({
            showSuccessDialog()
        }, 1500)
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Payment Successful")
            .setMessage("Your appointment has been successfully booked!")
            .setPositiveButton("OK") { _, _ ->
                navigateToHome()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToHome() {
        Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
        finish()
    }

    private fun applyPromoCode(code: String) {
        // Simple promo code logic - you can expand this
        promoDiscount = when (code) {
            "DOPAMIND10" -> originalPrice * 0.10
            "DOPAMIND20" -> originalPrice * 0.20
            else -> {
                Toast.makeText(this, "Invalid promo code", Toast.LENGTH_SHORT).show()
                0.0
            }
        }
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val finalPrice = originalPrice - promoDiscount
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(finalPrice)
        binding.orderSummary.findViewById<TextView>(R.id.tvTotal).text = "Total: $formattedPrice"
    }

    private fun String.isValidEmail(): Boolean {
        return isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}