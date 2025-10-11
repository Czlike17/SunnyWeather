package com.sunnyweather.android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.OutputStream
import java.net.Socket

class DeviceControlActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private val deviceList = mutableListOf<Device>()
    private val ADD_DEVICE_REQUEST = 1001  // 请求码

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        // 初始化示例设备
        initSampleDevices()

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.device_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(this, deviceList) { device ->
            // 处理确认按钮点击，发送信号到设备
            sendCommandToDevice(device)
        }
        recyclerView.adapter = deviceAdapter

        // 悬浮按钮点击事件，跳转到添加设备页面
        findViewById<FloatingActionButton>(R.id.next_button).setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            startActivityForResult(intent, ADD_DEVICE_REQUEST)
        }
    }

    // 初始化示例设备
    private fun initSampleDevices() {
        deviceList.add(Device("卧室智能灯", "192.168.1.101", "8080"))
        deviceList.add(Device("客厅智能灯", "192.168.1.102", "8080"))
    }

    // 发送命令到设备
    private fun sendCommandToDevice(device: Device) {
        // 显示发送中提示
        Toast.makeText(this, "正在发送命令到 ${device.name}...", Toast.LENGTH_SHORT).show()

        // 开启新线程进行网络通信，避免阻塞UI线程
        Thread {
            try {
                // 创建命令JSON
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

                Toast.makeText(this, "已添加新设备: $deviceName", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
