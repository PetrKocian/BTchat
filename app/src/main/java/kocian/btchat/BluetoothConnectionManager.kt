package kocian.btchat

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.io.IOException
import java.util.*


//class to take care of all things related to bluetooth connection

class BluetoothConnectionManager() {
    private var mBluetoothManager : BluetoothManager ?= null
    private var mBluetoothAdapter: BluetoothAdapter  ?= null
    private var mContext : Context ?= null
    private var mActivity : Activity ?= null
    private var connectThread : ConnectThread ?= null
    private var connectedThread : MyBluetoothService.ConnectedThread ?= null
    private var recyclerView : RecyclerView ?= null
    private var messageAdapter : MessageAdapter ?= null
    private val handler = Handler(Handler.Callback { message ->
        // Read the message here
        if(message.what == MESSAGE_READ)
        {
            val messageText = message.obj as ByteArray
            val msg = "< " + messageText.toString(Charsets.UTF_8)
            messageAdapter?.addMessage(msg)
        }
        true

    })
    constructor(activity: Activity, context: Context?, recyclerViewIn: RecyclerView, messageAdapterIn: MessageAdapter) : this() {
        if (context != null) {
            mContext = context
        }
        if (activity != null) {
            mActivity = activity
        }
        mBluetoothManager = mContext?.getSystemService(BluetoothManager::class.java)!!
        mBluetoothAdapter = mBluetoothManager?.getAdapter()
        messageAdapter = messageAdapterIn
        recyclerView = recyclerViewIn
    }

    constructor(activity: Activity, context: Context?) : this() {
        if (context != null) {
            mContext = context
        }
        if (activity != null) {
            mActivity = activity
        }
        mBluetoothManager = mContext?.getSystemService(BluetoothManager::class.java)!!
        mBluetoothAdapter = mBluetoothManager?.getAdapter()
    }

    @SuppressLint("MissingPermission")
    public fun getBluetoothDevicesNames(): MutableSet<String>? {
        val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
        val pairedDevicesNames = mutableSetOf<String>()
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            pairedDevicesNames.add(deviceName)
        }

        return pairedDevicesNames
    }

    public fun manageMyConnectedSocket(socket: BluetoothSocket) {
        val bluetoothService = MyBluetoothService(handler)
        connectedThread = bluetoothService.ConnectedThread(socket)
        connectedThread?.start()
        mActivity?.runOnUiThread(Runnable {
            Toast.makeText(
                mActivity,
                "CONNECTED",
                Toast.LENGTH_SHORT
            ).show()
        })
        mActivity?.runOnUiThread(Runnable {
            val textField = mActivity?.findViewById<EditText>(R.id.messageBox)
            val button = mActivity?.findViewById<Button>(R.id.sendMessage)
            val connecting = mActivity?.findViewById<TextView>(R.id.ConnectingText)
            button?.visibility = View.VISIBLE
            textField?.visibility = View.VISIBLE
            connecting?.visibility = View.INVISIBLE
        })
    }

    public fun sendMessage(message: String) {
        connectedThread?.write(message.toByteArray())
        if(connectedThread == null || !connectedThread!!.isAlive()){
            Toast.makeText(mContext, "DEVICE DISCONNECTED", Toast.LENGTH_LONG).show()
            mActivity?.finish()
        }
    }

    public fun disconnect(){
        if(connectedThread != null && connectedThread!!.isAlive)
        {
            connectedThread!!.cancel()
            Toast.makeText(mContext, "DISCONNECTED", Toast.LENGTH_LONG).show()
        }
        if(connectThread != null && connectThread!!.isAlive)
        {
            connectThread!!.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    public fun startConnection(deviceNameIn: String){
        val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
        var deviceToConnect: BluetoothDevice? = null
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            if(deviceName==deviceNameIn)
            {
                deviceToConnect = device
            }
        }
        if(deviceToConnect != null)
        {
            val aconnectThread = ConnectThread(deviceToConnect!!)
            aconnectThread?.start()
        }
        else
        {
            Toast.makeText(mContext, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show()
            mActivity?.finish()
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"))
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter?.cancelDiscovery()
            var connectionSuccessful = true
            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    socket.connect()
                }
                catch (e: IOException) {
                    if(mActivity?.isFinishing == false) {
                        mActivity?.runOnUiThread(Runnable {
                            Toast.makeText(
                                mActivity,
                                "COULDN'T CONNECT",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                    mActivity?.finish()
                    connectionSuccessful = false
                }
                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                if(connectionSuccessful)
                {
                    Looper.prepare()
                    manageMyConnectedSocket(socket)
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
}