package com.example.new_dopamind.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.new_dopamind.R
import com.example.new_dopamind.data.model.NewsItem
import com.example.new_dopamind.databinding.ItemNewsBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {
    private val items = mutableListOf<NewsItem>()
    private var onItemClickListener: ((NewsItem) -> Unit)? = null

    fun setItems(newItems: List<NewsItem>) {
        Log.d("NewsAdapter", "Received ${newItems.size} items")
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (NewsItem) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewsBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(items[adapterPosition])
            }
        }

        fun bind(item: NewsItem) {
            binding.apply {
                // Log for debugging
                Log.d("NewsAdapter", "Binding item: ${item.title}")

                // Set title
                newsTitle.text = item.title

                // Set content snippet
                contentSnippet.text = item.contentSnippet

                // Format and set date
                newsDate.text = formatDate(item.isoDate)

                // Load image using Glide
                Glide.with(itemView.context)
                    .load(item.image.large)
                    .error(R.drawable.ic_backarrowios)
                    .into(newsImage)
            }
        }

        private fun formatDate(isoDate: String): String {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
                outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

                val date = inputFormat.parse(isoDate)
                return outputFormat.format(date!!)
            } catch (e: Exception) {
                Log.e("NewsAdapter", "Error formatting date: ${e.message}")
                return isoDate
            }
        }
    }
}