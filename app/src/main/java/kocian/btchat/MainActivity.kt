package kocian.btchat

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

private const val REQUEST_ENABLE_BT = 1


class MainActivity : AppCompatActivity() {
    var bMgr : BluetoothConnectionManager ?= null
    var myWebView :WebView ?= null
    var webViewOn = false
    var spinner : Spinner ?= null
    var connectButton : Button ?= null
    var infoButton : ImageButton?= null
    var text : TextView ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myWebView = findViewById(R.id.webview)
        spinner = findViewById(R.id.spinner)
        connectButton = findViewById(R.id.connectButton)
        text = findViewById(R.id.mainText)
        infoButton = findViewById(R.id.infoButton)

        //turn BT on, create an instance of BT manager to get paired devices and populate the spinner with them
        turnBluetoothOn()
        bMgr = BluetoothConnectionManager(this,this)
        populateSpinner()
    }

    //handles turning on BT
    val regiterForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //populate spinner after BT was turned on
            populateSpinner()
        }
        else
        {
            //close the application
            Toast.makeText(this@MainActivity, "Bluetooth has to be turned ON", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    public fun turnBluetoothOn() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        //check if phone has BT adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(this@MainActivity, "Bluetooth is required for this app to work", Toast.LENGTH_LONG).show()
            finish()
        }
        //ask to enable BT if not on
        else if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            regiterForResult.launch(enableBtIntent)
        }
    }

    public fun populateSpinner(){
        //get names of BT devices and spinner reference
        var devices = bMgr?.getBluetoothDevicesNames()
        devices?.add("TEST")
        if(devices != null)
        {
            //if the device list is not null, fill the spinner
            val adapter = ArrayAdapter(spinner!!.context, android.R.layout.simple_spinner_item, devices!!.toList())
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner!!.adapter = adapter
        }
    }

    public fun connectToDevice(v: View){
        //enable BT in case it was disabled in the meantime
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if(bluetoothAdapter?.isEnabled == false)
        {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            regiterForResult.launch(enableBtIntent)
        }
        //start new activity to chat with the device
        val intent = Intent(this, ChatActivity::class.java)
        //pass the device name to the activity
        var spinner = findViewById<Spinner>(R.id.spinner)
        val device = spinner.selectedItem as String
        intent.putExtra("device", device)
        startActivity(intent)
    }

    public fun about(v: View){
        myWebView = findViewById<WebView>(R.id.webview)
        myWebView?.loadUrl("https://github.com/PetrKocian/BTchat")
        myWebView?.visibility = View.VISIBLE
        spinner!!.visibility = View.INVISIBLE
        connectButton!!.visibility = View.INVISIBLE
        infoButton!!.visibility = View.INVISIBLE
        text!!.visibility = View.INVISIBLE

        webViewOn = true
    }

    override fun onBackPressed() {
        if(webViewOn == true){
            myWebView?.visibility = View.INVISIBLE
            spinner!!.visibility = View.VISIBLE
            connectButton!!.visibility = View.VISIBLE
            infoButton!!.visibility = View.VISIBLE
            text!!.visibility = View.VISIBLE
            webViewOn = false
        } else {
            // If the WebView cannot go back, close the app or perform other actions
            super.onBackPressed()
        }
    }

}