package com.portal.app.model

import com.google.gson.annotations.SerializedName

/**
 * Main message wrapper for all WebSocket communications
 */
data class PortalMessage(
    @SerializedName("type")
    val type: String, // "draw" or "message"
    
    @SerializedName("roomId")
    val roomId: String,
    
    @SerializedName("payload")
    val payload: String, // JSON string of DrawPayload or MessagePayload
    
    @SerializedName("timestamp")
    val timestamp: Long
)

/**
 * Payload for draw events
 */
data class DrawPayload(
    @SerializedName("points")
    val points: List<DrawPoint>,
    
    @SerializedName("color")
    val color: String,
    
    @SerializedName("strokeWidth")
    val strokeWidth: Float
)

/**
 * Single point in a drawing stroke
 */
data class DrawPoint(
    @SerializedName("x")
    val x: Float, // Normalized 0.0 to 1.0
    
    @SerializedName("y")
    val y: Float  // Normalized 0.0 to 1.0
)

/**
 * Payload for text messages
 */
data class MessagePayload(
    @SerializedName("text")
    val text: String
)

/**
 * Internal representation of a stroke with timestamp
 */
data class Stroke(
    val points: List<DrawPoint>,
    val color: String,
    val strokeWidth: Float,
    val timestamp: Long
)

/**
 * Chat message with timestamp
 */
data class ChatMessage(
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean = false
)