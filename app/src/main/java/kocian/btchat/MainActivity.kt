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

private const val REQUEST_ENABLE_BT = 1


class MainActivity : AppCompatActivity() {
    var bMgr : BluetoothConnectionManager ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val messagebox = findViewById<TextView>(R.id.messagebox)

        bMgr = BluetoothConnectionManager(this,messagebox)

    }

    public fun mam_bt(v: View){
        bMgr!!.mam_bt_mrg()
    }

    public fun send_message(v: View){
        val textField = findViewById<EditText>(R.id.editMessageText)
        val text = textField.text.toString()
        bMgr!!.sendMessage(text)
    }

    val regiterForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this@MainActivity, "POVEDLO SE ZAPNOUT BT", Toast.LENGTH_LONG).show()
        }
        else
        {
            Toast.makeText(this@MainActivity, "BT MUSI BYT ZAPNUT ABY APPKA BEZELA", Toast.LENGTH_LONG).show()
        }
    }

    public fun zapni_bt(v: View){
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this@MainActivity, "NEMAM BT", Toast.LENGTH_SHORT).show()
        }
        else if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            regiterForResult.launch(enableBtIntent)
        }
        else
        {
            Toast.makeText(this@MainActivity, "BT UZ JE ZAPNUTY", Toast.LENGTH_LONG).show()
        }
    }


    @SuppressLint("MissingPermission")
    public fun ukaz_dev(v: View){
        bMgr!!.ukaz_dev_mrg()
    }

    public fun pripoj_dev(v: View){
        var spinner = findViewById<Spinner>(R.id.spinner)
        val device = spinner.selectedItem as String
        bMgr!!.startconnection(device)
    }

    public fun populate_spinner(v:View){
        var devices = bMgr!!.gimme_dev_mrg()
        var spinner = findViewById<Spinner>(R.id.spinner)
        devices!!.add("test")
        val adapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, devices!!.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }


    public fun connect_to_device(v: View){
        val intent = Intent(this, ChatActivity::class.java)
        var spinner = findViewById<Spinner>(R.id.spinner)
        val device = spinner.selectedItem as String
        intent.putExtra("device", device)
        startActivity(intent)
    }

}