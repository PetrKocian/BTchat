package kocian.btchat

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.*
import kocian.btchat.MyBluetoothService
import java.util.concurrent.TimeoutException


//class to take care of all things related to bluetooth connection

class BluetoothConnectionManager() {
    var mBluetoothManager : BluetoothManager ?= null
    var mBluetoothAdapter: BluetoothAdapter  ?= null
    var mContext : Context ?= null
    var pairedDevices: Set<BluetoothDevice>  ?= null
    var connectedthread : MyBluetoothService.ConnectedThread ?= null
    var messagebox : TextView ?= null
    var recyclerView : RecyclerView ?= null
    var messageAdapter : MessageAdapter ?= null
    val handler = Handler(Handler.Callback { message ->
        // Read the message here
        if(message.what == MESSAGE_READ)
        {
            val messageText = message.obj as ByteArray
            // Do something with the message text
            val msg = "< " + messageText.toString(Charsets.UTF_8)
            //Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show()
            //messagebox!!.setText(msg)
            messageAdapter?.addMessage(msg)
        }
        true

    })
    constructor(context: Context?, messageboxin: TextView, recyclerViewIn: RecyclerView, messageAdapterIn: MessageAdapter) : this() {
        if (context != null) {
            mContext = context
        }
        messagebox = messageboxin
        mBluetoothManager = mContext?.getSystemService(BluetoothManager::class.java)!!
        mBluetoothAdapter = mBluetoothManager!!.getAdapter()
        messageAdapter = messageAdapterIn
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "NEMAM BT", Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(context, "MAM BT", Toast.LENGTH_SHORT).show()
        }
    }

    constructor(context: Context?, messageboxin: TextView) : this() {
        if (context != null) {
            mContext = context
        }
        messagebox = messageboxin
        mBluetoothManager = mContext?.getSystemService(BluetoothManager::class.java)!!
        mBluetoothAdapter = mBluetoothManager!!.getAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "NEMAM BT", Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(context, "MAM BT", Toast.LENGTH_SHORT).show()
        }
    }

    public fun mam_bt_mrg(){
        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, "NEMAM BT", Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(mContext, "MAM BT", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    public fun ukaz_dev_mrg(){
        val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Toast.makeText(mContext, deviceName, Toast.LENGTH_LONG).show()
            Thread.sleep(1000)
        }
    }

    @SuppressLint("MissingPermission")
    public fun gimme_dev_mrg(): MutableSet<String>? {
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
        connectedthread = bluetoothService.ConnectedThread(socket)
        connectedthread!!.start()
    }

    public fun sendMessage(message: String){
        connectedthread!!.write(message.toByteArray())
    }

    public fun disconnect(){
        if(connectedthread != null && connectedthread!!.isAlive)
        {
            connectedthread!!.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    public fun startconnection(deviceNameIn: String){
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
            val connectthread = ConnectThread(deviceToConnect!!)
            try { //TODO: DOESN'T WORK
                connectthread.start()
            }
            catch (e: IOException) {
                Toast.makeText(mContext, "COULDN'T CONNECT", Toast.LENGTH_LONG).show()
            }
        }
        else
        {
            Toast.makeText(mContext, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show()
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

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                Looper.prepare()
                manageMyConnectedSocket(socket)
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