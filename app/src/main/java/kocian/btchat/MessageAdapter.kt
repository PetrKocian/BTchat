package kocian.btchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: MutableList<String>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var recyclerView: RecyclerView? = null

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
        notifyItemInserted(messages.size - 1)
        if(recyclerView != null)
        {
            recyclerView!!.scrollToPosition(messages.size - 1)
        }
    }
}/*
sealed class Message {
    data class SenderMessage(val message: String, val sender: String) : Message()
    data class ReceiverMessage(val message: String, val sender: String) : Message()
}

class MessageAdapter() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<Message>()


    companion object {
        const val MESSAGE_TYPE_SENDER = 0
        const val MESSAGE_TYPE_RECEIVER = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MESSAGE_TYPE_SENDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.sender_message_item, parent, false)
                SenderViewHolder(view)
            }
            MESSAGE_TYPE_RECEIVER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.receiver_message_item, parent, false)
                ReceiverViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder.itemViewType) {
            MESSAGE_TYPE_SENDER -> {
                val senderHolder = holder as SenderViewHolder
                val senderMessage = message as Message.SenderMessage
                senderHolder.bind(senderMessage)
            }
            MESSAGE_TYPE_RECEIVER -> {
                val receiverHolder = holder as ReceiverViewHolder
                val receiverMessage = message as Message.ReceiverMessage
                receiverHolder.bind(receiverMessage)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (val message = messages[position]) {
            is Message.SenderMessage -> MESSAGE_TYPE_SENDER
            is Message.ReceiverMessage -> MESSAGE_TYPE_RECEIVER
            else -> throw IllegalArgumentException("Invalid message type")
        }
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: Message.SenderMessage) {
            senderTextView.text = message.message
            messageTextView.text = message.sender
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: Message.ReceiverMessage) {
            senderTextView.text = message.message
            messageTextView.text = message.sender
        }
    }

    fun addSenderMessage(messageStr: String) {
        val message =  Message.SenderMessage(messageStr, "sender")
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun addReceiverMessage(messageStr: String) {
        val message =  Message.ReceiverMessage(messageStr, "receiver")
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
*/