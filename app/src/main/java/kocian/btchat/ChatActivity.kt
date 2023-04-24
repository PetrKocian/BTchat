package kocian.btchat

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {
    var bMgr : BluetoothConnectionManager ?= null
    val messageAdapter = MessageAdapter()
    var test = 0
    var rand = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //set message window and send button invisible while connecting
        val textField = findViewById<EditText>(R.id.messageBox)
        val button = findViewById<Button>(R.id.sendMessage)
        button?.visibility = View.INVISIBLE
        textField?.visibility = View.INVISIBLE

        //set up message adapter for recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.messageRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter
        messageAdapter.provideRecyclerView(recyclerView)

        //start connecting to the provided device
        bMgr = BluetoothConnectionManager(this, this, recyclerView, messageAdapter)
        val device = intent.getStringExtra("device") as String
        title = device
        if(device == "test")
        {
            val connectingText = findViewById<TextView>(R.id.ConnectingText)
            connectingText?.visibility = View.INVISIBLE
            button?.visibility = View.VISIBLE
            textField?.visibility = View.VISIBLE

            test = 1
        }
        else
        {
            bMgr!!.startConnection(device)
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
            //get message from text field, send the message, display it in the recycler view and erase the text field
            val textField = findViewById<EditText>(R.id.messageBox)
            var text = textField.text.toString()
            bMgr!!.sendMessage(text)
            text = "> " + text
            messageAdapter.addMessage(text)
            textField.setText("")

        }
    }

    override fun onBackPressed() {
        // disconnect the device and close the activity
        disconnect()
        super.onBackPressed()
    }

    private fun disconnect(){
        //call BT manager disconnect
        if(test == 0 && bMgr != null)
        {
            bMgr!!.disconnect()
        }
    }
}