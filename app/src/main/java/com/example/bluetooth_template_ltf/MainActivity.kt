package com.example.bluetooth_template_ltf

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.ui.AppBarConfiguration
import com.example.bluetooth_template_ltf.databinding.ActivityMainBinding
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*


private const val REQUEST_ENABLE_BT = 1
private const val REQUEST_ACCESS_FINE_LOCATION = 2

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_PRIVILEGED
    )

    private val PERMISSIONS_LOCATION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_PRIVILEGED
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bluetoothManager = BluetoothManager.getInstance()
        if (bluetoothManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(this, "Bluetooth not available.", Toast.LENGTH_LONG)
                .show() // Replace context with your context instance.
            finish()
        }

        val pairedDevices: Collection<BluetoothDevice> = bluetoothManager.pairedDevicesList

        binding.button.setOnClickListener {
            Log.d("BLUETOOTH", "CLICK!")
            for (device in pairedDevices) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Bluetooth not available.", Toast.LENGTH_LONG)
                        .show() // Replace context with your context instance.
                    return@setOnClickListener
                }
                if (device.name.contains("ESP")) {
                    if (deviceInterface == null) {
                        Toast.makeText(this, "Connecting!", Toast.LENGTH_LONG)
                            .show() // Replace context with your context instance.
                        Log.d("BLUETOOTH", "CONNECTING")
                        connectDevice(bluetoothManager, device.address)
                    } else {
                        Toast.makeText(this, "Connected!!", Toast.LENGTH_LONG)
                            .show() // Replace context with your context instance.
                        Log.d("BLUETOOTH", "CONNECTED ALREADY!")
                        deviceInterface?.sendMessage("Hello world!\n")
                    }
                }
            }
        }
    }
    private var deviceInterface: SimpleBluetoothDeviceInterface? = null

    private fun connectDevice(bluetoothManager: BluetoothManager, mac: String) {
        Log.d("BLUETOOTH", "TRYING!")
        val subscribe = bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ connectedDevice: BluetoothSerialDevice -> onConnected(connectedDevice) }) { error: Throwable ->
                Log.d("BLUETOOTH", "ERROR!!!!! ", error)
                onError(
                    error
                )
            }
        Log.d("BLUETOOTH", "DONE!")
    }

    private fun onConnected(connectedDevice: BluetoothSerialDevice) {
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface()

        Log.d("BLUETOOTH", "CONNECTED!!!!!!!!!!! " + connectedDevice.mac)
        // Listen to bluetooth events
        deviceInterface?.setListeners(
            { message: String -> onMessageReceived(message) },
            { message: String -> onMessageSent(message) }) { error: Throwable ->
            onError(
                error
            )
        }
    }

    private fun onMessageSent(message: String) {
        // We sent a message! Handle it here.
        Toast.makeText(this, "Sent a message! Message was: $message", Toast.LENGTH_LONG)
            .show() // Replace context with your context instance.
    }

    private fun onMessageReceived(message: String) {
        // We received a message! Handle it here.
        Toast.makeText(this, "Received a message! Message was: $message", Toast.LENGTH_LONG)
            .show() // Replace context with your context instance.
    }

    private fun onError(error: Throwable) {
        Log.d("BLUETOOTH", "ERROR!!!! $error")
        // Handle the error
    }
}