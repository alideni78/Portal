package com.portal.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val connectionManager = ConnectionManager()

    routing {
        webSocket("/ws") {
            val logger = LoggerFactory.getLogger("WebSocket")
            val sessionId = java.util.UUID.randomUUID().toString()
            
            logger.info("Client connected: $sessionId")
            connectionManager.addConnection(sessionId, this)

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val receivedText = frame.readText()
                        logger.info("Received from $sessionId: $receivedText")
                        
                        // Broadcast to all connected clients
                        connectionManager.broadcast(receivedText, sessionId)
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                logger.info("Client disconnected: $sessionId")
            } catch (e: Exception) {
                logger.error("Error in WebSocket: ${e.message}", e)
            } finally {
                connectionManager.removeConnection(sessionId)
                logger.info("Client removed: $sessionId")
            }
        }
        
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }
    }
}

class ConnectionManager {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val logger = LoggerFactory.getLogger(ConnectionManager::class.java)

    fun addConnection(sessionId: String, session: WebSocketSession) {
        connections[sessionId] = session
        logger.info("Active connections: ${connections.size}")
    }

    fun removeConnection(sessionId: String) {
        connections.remove(sessionId)
        logger.info("Active connections: ${connections.size}")
    }

    suspend fun broadcast(message: String, senderId: String) {
        connections.forEach { (id, session) {
            try {
                // Send to all clients including sender
                session.send(Frame.Text(message))
                logger.debug("Broadcasted to $id")
            } catch (e: Exception) {
                logger.error("Failed to send to $id: ${e.message}")
            }
        }
        logger.info("Broadcasted message from $senderId to ${connections.size} clients")
    }
}

// Message models
@Serializable
data class PortalMessage(
    val type: String, // "draw" or "message"
    val roomId: String,
    val payload: String, // JSON string of DrawPayload or MessagePayload
    val timestamp: Long
)

@Serializable
data class DrawPayload(
    val points: List<Point>,
    val color: String,
    val strokeWidth: Float
)

@Serializable
data class Point(
    val x: Float,
    val y: Float
)

@Serializable
data class MessagePayload(
    val text: String
)