package com.sunnyweather.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sunnyweather.android.ui.weather.WeatherActivity

class DeviceControlActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private val deviceList by lazy { DeviceStorage.loadDevices(this) }
    private val ADD_DEVICE_REQUEST = 1001  // 请求码
    private lateinit var emptyHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        emptyHint = findViewById(R.id.empty_hint)

        recyclerView = findViewById(R.id.device_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(
            this,
            deviceList,
            onDeviceDeleted = { position ->
                deviceAdapter.removeDevice(position)
                updateEmptyState()
                // 删除后保存
                DeviceStorage.saveDevices(this, deviceList)
                Toast.makeText(this, "设备已删除", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = deviceAdapter

        updateEmptyState()

        findViewById<FloatingActionButton>(R.id.next_button).setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            startActivityForResult(intent, ADD_DEVICE_REQUEST)
        }

        findViewById<Button>(R.id.btn_weather)?.setOnClickListener {
            startActivity(Intent(this, WeatherActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_DEVICE_REQUEST && resultCode == RESULT_OK) {
            data?.let { intent ->
                val deviceName = intent.getStringExtra("deviceName") ?: ""
                val ipAddress = intent.getStringExtra("ipAddress") ?: ""
                val port = intent.getStringExtra("port") ?: ""

                val newDevice = Device(deviceName, ipAddress, port)
                deviceAdapter.addDevice(newDevice)
                updateEmptyState()
                // 添加后保存
                DeviceStorage.saveDevices(this, deviceList)
                Toast.makeText(this, "已添加新设备: $deviceName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState() {
        if (deviceList.isEmpty()) {
            emptyHint.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyHint.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deviceList.forEach { device ->
            try {
                device.socket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}