package kocian.btchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for recyclerview
class MessageAdapter() : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var recyclerView: RecyclerView? = null
    private val messages = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.itemTextView)
    }

    fun provideRecyclerView(recyclerViewIn: RecyclerView)
    {
        recyclerView = recyclerViewIn
    }

    fun addMessage(message: String) {
        messages.add(message)
        // Scroll to bottom to see new message
        notifyItemInserted(messages.size - 1)
        if(recyclerView != null)
        {
            recyclerView!!.scrollToPosition(messages.size - 1)
        }
    }
}