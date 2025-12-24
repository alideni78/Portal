package com.portal.app

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.portal.app.databinding.ActivityMainBinding
import com.portal.app.model.ChatMessage
import com.portal.app.model.PortalMessage
import com.portal.app.model.Stroke
import com.portal.app.network.MessageBuilder
import com.portal.app.network.WebSocketClient
import com.portal.app.ui.chat.ChatAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main activity with whiteboard and chat
 */
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var chatAdapter: ChatAdapter
    
    private var chatCleanupJob: Job? = null
    private val CHAT_MESSAGE_LIFETIME_MS = 15_000L // 15 seconds
    private val CHAT_CLEANUP_INTERVAL_MS = 1_000L // 1 second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWhiteboard()
        setupChat()
        setupWebSocket()
        startChatCleanupTimer()
    }

    private fun setupWhiteboard() {
        // Set callback for when user draws a stroke
        binding.whiteboardView.onStrokeDrawn = { points ->
            if (webSocketClient.isConnected()) {
                val message = MessageBuilder.createDrawMessage(points)
                webSocketClient.sendMessage(message)
                Log.d(TAG, "Sent draw message with ${points.size} points")
            }
        }
    }

    private fun setupChat() {
        // Setup RecyclerView
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }

        // Send button click
        binding.sendButton.setOnClickListener {
            sendChatMessage()
        }

        // Send on keyboard action
        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendChatMessage()
                true
            } else {
                false
            }
        }
    }

    private fun setupWebSocket() {
        val serverUrl = getString(R.string.websocket_url)
        
        webSocketClient = WebSocketClient(
            serverUrl = serverUrl,
            onMessageReceived = { message ->
                handleIncomingMessage(message)
            },
            onConnectionStateChanged = { state ->
                updateConnectionStatus(state)
            }
        )

        webSocketClient.connect()
    }

    private fun handleIncomingMessage(message: PortalMessage) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (message.type) {
                "draw" -> handleDrawMessage(message)
                "message" -> handleChatMessage(message)
                else -> Log.w(TAG, "Unknown message type: ${message.type}")
            }
        }
    }

    private fun handleDrawMessage(message: PortalMessage) {
        val drawPayload = MessageBuilder.parseDrawPayload(message) ?: return
        
        val stroke = Stroke(
            points = drawPayload.points,
            color = drawPayload.color,
            strokeWidth = drawPayload.strokeWidth,
            timestamp = message.timestamp
        )
        
        binding.whiteboardView.addStroke(stroke)
        Log.d(TAG, "Added remote stroke with ${drawPayload.points.size} points")
    }

    private fun handleChatMessage(message: PortalMessage) {
        val messagePayload = MessageBuilder.parseMessagePayload(message) ?: return
        
        val chatMessage = ChatMessage(
            text = messagePayload.text,
            timestamp = message.timestamp,
            isFromMe = false // All messages appear as from others in MVP
        )
        
        chatAdapter.addMessage(chatMessage)
        binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        Log.d(TAG, "Added chat message: ${messagePayload.text}")
    }

    private fun sendChatMessage() {
        val text = binding.messageInput.text.toString().trim()
        if (text.isEmpty()) return

        if (!webSocketClient.isConnected()) {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show()
            return
        }

        val message = MessageBuilder.createTextMessage(text)
        webSocketClient.sendMessage(message)
        
        // Add to local chat
        val chatMessage = ChatMessage(
            text = text,
            timestamp = message.timestamp,
            isFromMe = true
        )
        chatAdapter.addMessage(chatMessage)
        binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
        
        // Clear input
        binding.messageInput.text.clear()
        Log.d(TAG, "Sent chat message: $text")
    }

    private fun updateConnectionStatus(state: WebSocketClient.ConnectionState) {
        lifecycleScope.launch(Dispatchers.Main) {
            val statusText = when (state) {
                WebSocketClient.ConnectionState.CONNECTING -> getString(R.string.connecting)
                WebSocketClient.ConnectionState.CONNECTED -> getString(R.string.connected)
                WebSocketClient.ConnectionState.DISCONNECTED -> getString(R.string.disconnected)
                WebSocketClient.ConnectionState.RECONNECTING -> getString(R.string.reconnecting)
            }
            binding.connectionStatus.text = statusText
            Log.i(TAG, "Connection state: $statusText")
        }
    }

    private fun startChatCleanupTimer() {
        chatCleanupJob = lifecycleScope.launch {
            while (isActive) {
                delay(CHAT_CLEANUP_INTERVAL_MS)
                withContext(Dispatchers.Main) {
                    chatAdapter.removeOldMessages(CHAT_MESSAGE_LIFETIME_MS)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatCleanupJob?.cancel()
        webSocketClient.cleanup()
    }
}