package com.example.new_dopamind.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.new_dopamind.R
import com.example.new_dopamind.data.model.MoodData
import com.example.new_dopamind.databinding.ItemDiaryHistoryBinding
import com.example.new_dopamind.ui.main.DiaryDetailActivity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MoodHistoryAdapter : RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder>() {
    private val items = mutableListOf<MoodData>()

    fun setItems(newItems: List<MoodData>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val binding: ItemDiaryHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                val context = itemView.context
                val intent = Intent(context, DiaryDetailActivity::class.java).apply {
                    putExtra(DiaryDetailActivity.EXTRA_MOOD, getMoodText(item.predictions))
                    putExtra(DiaryDetailActivity.EXTRA_CONTENT, item.texts)
                    putExtra(DiaryDetailActivity.EXTRA_DATE, formatDate(item.created_at))
                }
                context.startActivity(intent)
            }
        }

        fun bind(item: MoodData) {
            binding.apply {
                tvDate.text = formatDate(item.created_at)
                tvDiaryContent.text = item.texts

                val icon = when (item.predictions.lowercase()) {
                    "kegembiraan" -> R.drawable.foto_emotehappy
                    "kesedihan" -> R.drawable.foto_emotesad
                    "kemarahan" -> R.drawable.foto_emoteanggry
                    "ketakutan" -> R.drawable.foto_emotefear
                    else -> R.drawable.foto_emotenetral
                }
                ivMoodEmote.setImageResource(icon)
            }
        }

        private fun getMoodText(prediction: String): String {
            return when (prediction.lowercase()) {
                "kegembiraan" -> "Senang"
                "kesedihan" -> "Sedih"
                "kemarahan" -> "Marah"
                "ketakutan" -> "Takut"
                else -> "Netral"
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
    }
}