package com.example.new_dopamind.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.new_dopamind.R
import com.example.new_dopamind.databinding.ActivityPredictionResultBinding

class PredictionResultActivity : AppCompatActivity(R.layout.activity_prediction_result) {
    private val binding by viewBinding(ActivityPredictionResultBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prediction = intent.getStringExtra(EXTRA_PREDICTION) ?: "neutral"
        setupPredictionUI(prediction)
        setupButtons()
    }

    private fun setupPredictionUI(prediction: String) {
        val (image, title, description) = when (prediction.lowercase()) {
            "kegembiraan" -> Triple(
                R.drawable.foto_emotehappy,
                "Happy",
                "Now you're in a happy mood, let the joy fill every moment!"
            )
            "kesedihan" -> Triple(
                R.drawable.foto_emotesad,
                "Sad",
                "Sadness lingers, feel it fully. Let it teach you to value joy."
            )
            "kemarahan" -> Triple(
                R.drawable.foto_emoteanggry,
                "Angry",
                "Anger burns, channel it wisely. Use it to spark change."
            )
            "ketakutan" -> Triple(
                R.drawable.foto_emotefear,
                "Fear",
                "Fear looms, face it boldly. Let it build your strength."
            )
            else -> Triple(
                R.drawable.foto_emotenetral,
                "Netral",
                "Neutrality comes, embrace its balance. Let it guide steady steps."
            )
        }

        binding.apply {
            moodImage.setImageResource(image)
            moodTitle.text = title
            moodDescription.text = description
        }
    }

    private fun setupButtons() {
        binding.apply {
            btnBackToDashboard.setOnClickListener {
                startActivity(Intent(this@PredictionResultActivity, HomeActivity::class.java))
                finishAffinity()
            }

            btnChatWithDopi.setOnClickListener {
                startActivity(Intent(this@PredictionResultActivity, ChatActivity::class.java))
                finishAffinity()
            }
        }
    }

    companion object {
        const val EXTRA_PREDICTION = "extra_prediction"
    }
}