package com.example.new_dopamind.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.new_dopamind.databinding.ActivityDiaryDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DiaryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiaryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mood = intent.getStringExtra(EXTRA_MOOD) ?: ""
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val date = intent.getStringExtra(EXTRA_DATE) ?: ""

        setupUI(mood, content, date)
        setupBackButton()
    }

    private fun setupUI(mood: String, content: String, date: String) {
        binding.apply {
            tvTitle.text = "Diary Detail"
            tvDate.text = formatDate(date)
            tvMoodTitle.text = mood
            tvDiaryContent.text = content
        }
    }

    private fun formatDate(dateString: String): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH.mm", Locale.US)

            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

            val date = inputFormat.parse(dateString)
            return outputFormat.format(date)
        } catch (e: Exception) {
            return dateString
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_MOOD = "extra_mood"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_DATE = "extra_date"
    }
}