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


//class to take care of all things related to BT connection

class BluetoothConnectionManager() {
    private var mBluetoothManager : BluetoothManager ?= null
    private var mBluetoothAdapter: BluetoothAdapter  ?= null
    private var mContext : Context ?= null
    private var mActivity : Activity ?= null
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

    //constructor to use for connecting to a device -> recyclerview to display messages
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

    //constructor to access paired devices to choose which one to connect to
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

    //get paired devices names as string list
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

    //called from thread which establishes connection - starts connected thread
    public fun manageMyConnectedSocket(socket: BluetoothSocket) {
        //start connected thread
        val bluetoothService = MyBluetoothService(handler)
        connectedThread = bluetoothService.ConnectedThread(socket)
        connectedThread?.start()
        //display connected toast
        mActivity?.runOnUiThread(Runnable {
            Toast.makeText(
                mActivity,
                "CONNECTED",
                Toast.LENGTH_SHORT
            ).show()
        })
        //hide "connecting" text and display chat recyclerview and send textfield/button
        mActivity?.runOnUiThread(Runnable {
            val textField = mActivity?.findViewById<EditText>(R.id.messageBox)
            val button = mActivity?.findViewById<Button>(R.id.sendMessage)
            val connecting = mActivity?.findViewById<TextView>(R.id.ConnectingText)
            button?.visibility = View.VISIBLE
            textField?.visibility = View.VISIBLE
            connecting?.visibility = View.INVISIBLE
        })
    }

    //sends message to connected device, if a device is not connected -> finish chat activity
    public fun sendMessage(message: String) {
        connectedThread?.write(message.toByteArray())
        if(connectedThread == null || !connectedThread!!.isAlive()){
            Toast.makeText(mContext, "DEVICE DISCONNECTED", Toast.LENGTH_LONG).show()
            mActivity?.finish()
        }
    }

    //disconnect function to cancel all connecting/connected threads
    public fun disconnect(){
        if(connectedThread != null && connectedThread!!.isAlive)
        {
            //if device was connected -> display toast
            connectedThread!!.cancel()
            Toast.makeText(mContext, "DISCONNECTED", Toast.LENGTH_LONG).show()
        }
    }

    //start connection to device with specified name
    @SuppressLint("MissingPermission")
    public fun startConnection(deviceNameIn: String){
        //get paired devices and search for selected device
        val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
        var deviceToConnect: BluetoothDevice? = null
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            if(deviceName==deviceNameIn)
            {
                deviceToConnect = device
            }
        }
        //if wanted device is found, start thread which tries to establish BT connection
        if(deviceToConnect != null)
        {
            val connectThread = ConnectThread(deviceToConnect!!)
            connectThread?.start()
        }
        else
        {
            //closes chat activity and returns to main activity
            Toast.makeText(mContext, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show()
            mActivity?.finish()
        }
    }

    //thread to establish connection to a BT device
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
                // Connect to the remote device through the socket
                try {
                    socket.connect()
                }
                catch (e: IOException) {
                    //display message and close activity
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
                //start connected thread to manage the BT connection
                if(connectionSuccessful)
                {
                    Looper.prepare()
                    manageMyConnectedSocket(socket)
                }
            }
        }
    }
}