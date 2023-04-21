package kocian.btchat

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {
    var bMgr : BluetoothConnectionManager ?= null
    val messageAdapter = MessageAdapter(mutableListOf("foo", "bar", "baz")
    )
    var test = 0
    var rand = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val messagebox = findViewById<TextView>(R.id.chatbox)
        val recyclerView = findViewById<RecyclerView>(R.id.messageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter
        messageAdapter.provideRecyclerView(recyclerView)

        bMgr = BluetoothConnectionManager(this,messagebox, recyclerView, messageAdapter)

        val device = intent.getStringExtra("device") as String

        if(device == "test")
        {
            test = 1
        }
        else
        {
            bMgr!!.startconnection(device)
        }

    }

    public fun sendMessage(v: View){
        if(test == 1)
        {
            val textField = findViewById<EditText>(R.id.messageBox)
            var text = textField.text.toString()
            val recyclerView = findViewById<RecyclerView>(R.id.messageRecyclerView)
            if(rand == 0){
                text = "> " + text
                messageAdapter.addMessage(text)
                rand = 1
            }
            else
            {
                rand = 0
                text = "< " + text
                messageAdapter.addMessage(text)
            }
            textField.setText("")
        }
        else
        {
            val textField = findViewById<EditText>(R.id.messageBox)
            val text = "> " + textField.text.toString()
            bMgr!!.sendMessage(text)
            messageAdapter.addMessage(text)
            textField.setText("")
        }
    }

    override fun onBackPressed() {
        // Call your function here
        disconnect()
        super.onBackPressed()
    }

    private fun disconnect(){
        if(test == 0 && bMgr != null)
        {
            bMgr!!.disconnect()
        }
    }
}