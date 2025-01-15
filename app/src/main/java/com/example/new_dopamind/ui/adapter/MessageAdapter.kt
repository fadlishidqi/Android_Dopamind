package com.example.new_dopamind.ui.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.new_dopamind.R
import com.example.new_dopamind.data.model.Message
import com.example.new_dopamind.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<Message>()

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.lastIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                // Setup message container
                val containerParams = messageContainer.layoutParams as FrameLayout.LayoutParams
                containerParams.gravity = if (message.isFromBot) Gravity.START else Gravity.END
                messageContainer.layoutParams = containerParams

                // Setup message
                messageText.apply {
                    text = message.content
                    setBackgroundResource(
                        if (message.isFromBot) R.drawable.bg_message_bot
                        else R.drawable.bg_message_user
                    )
                    setTextColor(
                        if (message.isFromBot) Color.WHITE
                        else Color.BLACK
                    )
                }

                // Setup time
                timeText.apply {
                    text = formatTime(message.timestamp)
                    gravity = if (message.isFromBot) Gravity.START else Gravity.END
                    setTextColor(Color.parseColor("#888888"))
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }
}