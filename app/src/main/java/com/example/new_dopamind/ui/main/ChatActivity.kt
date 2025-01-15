package com.example.new_dopamind.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.new_dopamind.data.api.ApiClientBearer
import com.example.new_dopamind.data.datastore.DataStoreInstance
import com.example.new_dopamind.data.datastore.UserPreference
import com.example.new_dopamind.data.model.ChatMessage
import com.example.new_dopamind.data.model.Message
import com.example.new_dopamind.databinding.ActivityChatBinding
import com.example.new_dopamind.ui.adapter.MessageAdapter
import com.example.new_dopamind.ui.auth.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.text.Html
import android.text.Spanned

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val userPreference by lazy {
        UserPreference(DataStoreInstance.getInstance(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        getLatestMoodAndSendWelcome()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                navigateToHome()
            }

            sendButton.setOnClickListener {
                val message = messageInput.text.toString().trim()
                if (message.isNotEmpty()) {
                    sendMessage(message)
                    messageInput.text.clear()
                }
            }
        }
    }

    private fun navigateToHome() {
        Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
        finish()
    }

    private fun getLatestMoodAndSendWelcome() {
        lifecycleScope.launch {
            try {
                val token = userPreference.userToken.first()
                val username = userPreference.username.first()
                val apiService = ApiClientBearer.create(token)

                val response = apiService.getMoodHistory()
                if (response.isSuccessful && response.body()?.data?.isNotEmpty() == true) {
                    val latestMood = response.body()?.data?.last()?.predictions
                    val welcomeMessage = createWelcomeMessage(username, latestMood)
                    messageAdapter.addMessage(Message(content = welcomeMessage, isFromBot = true))
                } else {
                    sendDefaultWelcome(username)
                }
            } catch (e: Exception) {
                val username = try {
                    userPreference.username.first()
                } catch (e: Exception) {
                    "teman"
                }
                sendDefaultWelcome(username)
            }
        }
    }

    private fun sendDefaultWelcome(username: String) {
        messageAdapter.addMessage(
            Message(
                content = "Hai $username, apa yang ingin kamu ceritakan hari ini?",
                isFromBot = true
            )
        )
    }

    private fun createWelcomeMessage(username: String, mood: String?): String {
        return when (mood?.lowercase()) {
            "kegembiraan" -> "Hai $username! Kayaknya kamu lagi senang nih. Bisa ceritakan sesuatu yang membuatmu bahagia?"
            "kesedihan" -> "Hai $username, aku lihat kamu sedang sedih. Mau berbagi cerita? Aku siap mendengarkan"
            "kemarahan" -> "Hai $username, sepertinya ada yang mengganggu pikiranmu. Yuk, cerita sama aku"
            "ketakutan" -> "Hai $username, jangan khawatir. Aku ada di sini untuk mendengarkan ceritamu"
            else -> "Hai $username, apa yang ingin kamu ceritakan hari ini?"
        }
    }

    private fun sendMessage(content: String) {
        messageAdapter.addMessage(Message(content = content, isFromBot = false))
        binding.chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)

        lifecycleScope.launch {
            try {
                showLoading(true)
                val token = userPreference.userToken.first()
                val apiService = ApiClientBearer.create(token)

                val response = apiService.sendMessage(ChatMessage(content))
                if (response.isSuccessful) {
                    response.body()?.let { chatResponse ->
                        val formattedMessage = formatText(chatResponse.data)

                        messageAdapter.addMessage(
                            Message(
                                content = formattedMessage.toString(),
                                isFromBot = true
                            )
                        )
                        binding.chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                } else {
                    if (response.code() == 401) {
                        handleSessionExpired()
                    } else {
                        showError("Gagal mengirim pesan")
                    }
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            sendButton.isEnabled = !isLoading
            messageInput.isEnabled = !isLoading
        }
    }

    private fun handleSessionExpired() {
        lifecycleScope.launch {
            userPreference.updateUserLoginStatusAndToken(false, "")
            showError("Sesi telah berakhir. Silakan login kembali")
            startActivity(Intent(this@ChatActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun formatText(text: String): Spanned {
        val formattedText = text
            .replace(Regex("""\\(.*?)\\*"""), "<b>$1</b>")
            .replace(Regex("""\*(.*?)\*"""), "<i>$1</i>")
            .replace(Regex("""(?:^|\n)- (.*?)(?:\n|$)"""), "<li>$1</li>")
            .replace(Regex("""(?:<li>.*?</li>)+"""), "<ul>$&</ul>")
            .replace("\n", "<br>")

        return Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
    }

}
