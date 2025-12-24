package com.portal.app.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.portal.app.R
import com.portal.app.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for chat messages
 */
class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
        val timestampText: TextView = view.findViewById(R.id.timestampText)
        val messageContainer: FrameLayout = view.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text
        holder.timestampText.text = dateFormat.format(Date(message.timestamp))
        
        // Align message based on sender
        val layoutParams = holder.messageContainer.layoutParams as FrameLayout.LayoutParams
        if (message.isFromMe) {
            layoutParams.gravity = Gravity.END
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message_me)
        } else {
            layoutParams.gravity = Gravity.START
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message_other)
        }
        holder.messageContainer.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Add a new message
     */
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    /**
     * Remove old messages (older than 15 seconds)
     */
    fun removeOldMessages(maxAge: Long = 15_000L) {
        val currentTime = System.currentTimeMillis()
        val initialSize = messages.size
        
        messages.removeAll { message ->
            currentTime - message.timestamp > maxAge
        }
        
        val removedCount = initialSize - messages.size
        if (removedCount > 0) {
            notifyDataSetChanged()
        }
    }

    /**
     * Clear all messages
     */
    fun clear() {
        messages.clear()
        notifyDataSetChanged()
    }
}