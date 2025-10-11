package com.sunnyweather.android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sunnyweather.android.ui.weather.WeatherActivity
import java.io.OutputStream
import java.net.Socket

class DeviceControlActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private val deviceList = mutableListOf<Device>()
    private val ADD_DEVICE_REQUEST = 1001  // 请求码
    private lateinit var emptyHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        emptyHint = findViewById(R.id.empty_hint)

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.device_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(
            this,
            deviceList,
            onConfirmClickListener = { device ->
                sendCommandToDevice(device)
            },
            onDeviceDeleted = { position ->
                deviceAdapter.removeDevice(position)
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

    private fun sendCommandToDevice(device: Device) {
        Toast.makeText(this, "正在发送命令到 ${device.name}...", Toast.LENGTH_SHORT).show()

        // 开启新线程进行网络通信，避免阻塞UI线程
        Thread {
            try {
                val command = """
                    {
                        "deviceName": "${device.name}",
                        "power": ${device.isPowerOn},
                        "brightness": ${device.brightness}
                    }
                """.trimIndent()

                // 建立Socket连接并发送数据
                Socket(device.ipAddress, device.port.toInt()).use { socket ->
                    val outputStream: OutputStream = socket.getOutputStream()
                    outputStream.write(command.toByteArray(Charsets.UTF_8))
                    outputStream.flush()

                    // 在UI线程显示成功提示
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this,
                            "${device.name} 已更新: 电源=${if(device.isPowerOn) "开启" else "关闭"}, 亮度=${device.brightness}%",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.d("DeviceControl", "命令发送成功: $command")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 在UI线程显示失败提示
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        this,
                        "发送失败: 无法连接到 ${device.ipAddress}:${device.port}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    // 接收从AddDeviceActivity返回的结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_DEVICE_REQUEST && resultCode == RESULT_OK) {
            data?.let { intent ->
                // 获取新设备信息
                val deviceName = intent.getStringExtra("deviceName") ?: ""
                val ipAddress = intent.getStringExtra("ipAddress") ?: ""
                val port = intent.getStringExtra("port") ?: ""

                // 添加新设备到列表
                val newDevice = Device(deviceName, ipAddress, port)
                deviceAdapter.addDevice(newDevice)
                updateEmptyState()
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
}