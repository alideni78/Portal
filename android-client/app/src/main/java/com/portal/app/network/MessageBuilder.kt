package com.portal.app.network

import com.google.gson.Gson
import com.portal.app.model.*

/**
 * Helper object to build Portal messages
 */
object MessageBuilder {
    private const val ROOM_ID = "demoRoom"
    private val gson = Gson()
    
    /**
     * Create a draw message
     */
    fun createDrawMessage(
        points: List<DrawPoint>,
        color: String = "#000000",
        strokeWidth: Float = 4f
    ): PortalMessage {
        val payload = DrawPayload(points, color, strokeWidth)
        val payloadJson = gson.toJson(payload)
        
        return PortalMessage(
            type = "draw",
            roomId = ROOM_ID,
            payload = payloadJson,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Create a text message
     */
    fun createTextMessage(text: String): PortalMessage {
        val payload = MessagePayload(text)
        val payloadJson = gson.toJson(payload)
        
        return PortalMessage(
            type = "message",
            roomId = ROOM_ID,
            payload = payloadJson,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Parse draw payload from message
     */
    fun parseDrawPayload(message: PortalMessage): DrawPayload? {
        return try {
            gson.fromJson(message.payload, DrawPayload::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse message payload from message
     */
    fun parseMessagePayload(message: PortalMessage): MessagePayload? {
        return try {
            gson.fromJson(message.payload, MessagePayload::class.java)
        } catch (e: Exception) {
            null
        }
    }
}