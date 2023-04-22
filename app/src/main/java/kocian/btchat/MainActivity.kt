package kocian.btchat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.w3c.dom.Text
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

private const val REQUEST_ENABLE_BT = 1


class MainActivity : AppCompatActivity() {
    var bMgr : BluetoothConnectionManager ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        turnBluetoothOn()

        bMgr = BluetoothConnectionManager(this,this)

        populateSpinner()
    }

    val regiterForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            populateSpinner()
        }
        else
        {
            Toast.makeText(this@MainActivity, "Bluetooth has to be turned ON", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    public fun turnBluetoothOn() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this@MainActivity, "Bluetooth is required for this app to work", Toast.LENGTH_LONG).show()
            finish()
        }
        else if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            regiterForResult.launch(enableBtIntent)
        }
    }

    public fun populateSpinner(){
        var devices = bMgr!!.getBluetoothDevicesNames()
        var spinner = findViewById<Spinner>(R.id.spinner)
        devices!!.add("test")
        val adapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, devices!!.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    public fun connectToDevice(v: View){
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if(bluetoothAdapter?.isEnabled == false)
        {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            regiterForResult.launch(enableBtIntent)
        }
        val intent = Intent(this, ChatActivity::class.java)
        var spinner = findViewById<Spinner>(R.id.spinner)
        val device = spinner.selectedItem as String
        intent.putExtra("device", device)
        startActivity(intent)
    }

}