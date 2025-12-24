package com.portal.app.network

import android.util.Log
import com.google.gson.Gson
import com.portal.app.model.PortalMessage
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * WebSocket client for Portal real-time communication
 */
class WebSocketClient(
    private val serverUrl: String,
    private val onMessageReceived: (PortalMessage) -> Unit,
    private val onConnectionStateChanged: (ConnectionState) -> Unit
) {
    private val TAG = "WebSocketClient"
    private val gson = Gson()
    
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var reconnectJob: Job? = null
    private var isManualDisconnect = false
    
    @Volatile
    private var currentState: ConnectionState = ConnectionState.DISCONNECTED
    
    enum class ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        RECONNECTING
    }
    
    /**
     * Connect to WebSocket server
     */
    fun connect() {
        if (currentState == ConnectionState.CONNECTED || currentState == ConnectionState.CONNECTING) {
            Log.d(TAG, "Already connected or connecting")
            return
        }
        
        isManualDisconnect = false
        updateState(ConnectionState.CONNECTING)
        
        val request = Request.Builder()
            .url(serverUrl)
            .build()
        
        webSocket = client.newWebSocket(request, createWebSocketListener())
    }
    
    /**
     * Disconnect from WebSocket server
     */
    fun disconnect() {
        isManualDisconnect = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        updateState(ConnectionState.DISCONNECTED)
    }
    
    /**
     * Send a message to the server
     */
    fun sendMessage(message: PortalMessage) {
        val json = gson.toJson(message)
        webSocket?.send(json)?.let { success ->
            if (success) {
                Log.d(TAG, "Message sent: ${message.type}")
            } else {
                Log.e(TAG, "Failed to send message")
            }
        }
    }
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean = currentState == ConnectionState.CONNECTED
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        disconnect()
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
    
    private fun createWebSocketListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "WebSocket opened")
            updateState(ConnectionState.CONNECTED)
            reconnectJob?.cancel()
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Message received: $text")
            try {
                val message = gson.fromJson(text, PortalMessage::class.java)
                onMessageReceived(message)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse message: ${e.message}")
            }
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "WebSocket closing: $code - $reason")
            webSocket.close(1000, null)
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "WebSocket closed: $code - $reason")
            updateState(ConnectionState.DISCONNECTED)
            
            if (!isManualDisconnect) {
                scheduleReconnect()
            }
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}")
            updateState(ConnectionState.DISCONNECTED)
            
            if (!isManualDisconnect) {
                scheduleReconnect()
            }
        }
    }
    
    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            updateState(ConnectionState.RECONNECTING)
            delay(3000) // Wait 3 seconds before reconnecting
            
            if (!isManualDisconnect) {
                Log.i(TAG, "Attempting to reconnect...")
                connect()
            }
        }
    }
    
    private fun updateState(newState: ConnectionState) {
        currentState = newState
        onConnectionStateChanged(newState)
    }
}