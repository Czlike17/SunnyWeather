package com.sunnyweather.android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
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
    private val TAG = "DeviceControlActivity"  // 日志标签

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        // 初始化视图
        initViews()
        // 加载示例设备
        initSampleDevices()
        // 初始化适配器
        initAdapter()
    }

    // 初始化视图组件
    private fun initViews() {
        recyclerView = findViewById(R.id.device_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 跳转添加设备页面
        findViewById<FloatingActionButton>(R.id.next_button).setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            startActivityForResult(intent, ADD_DEVICE_REQUEST)
        }

        // 跳转天气预报页面
        findViewById<Button>(R.id.btn_weather)?.setOnClickListener {
            startActivity(Intent(this, WeatherActivity::class.java))
        }
    }

    // 初始化适配器
    private fun initAdapter() {
        deviceAdapter = DeviceAdapter(
            this,
            deviceList,
            onConfirmClickListener = { device ->
                sendCommandToDevice(device)  // 处理确认按钮点击
            },
            onDeviceDeleted = { position ->
                deviceAdapter.removeDevice(position)  // 处理设备删除
                Toast.makeText(this, "设备已删除", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = deviceAdapter
    }

    // 初始化示例设备
    private fun initSampleDevices() {
        deviceList.add(Device("卧室智能灯", "192.168.1.101", "8080"))
        deviceList.add(Device("客厅智能灯", "192.168.1.102", "8080"))
    }

    // 发送命令到设备
    private fun sendCommandToDevice(device: Device) {
        Toast.makeText(this, "正在发送命令到 ${device.name}...", Toast.LENGTH_SHORT).show()

        // 开启新线程进行网络通信，避免阻塞UI线程
        Thread {
            try {
                // 构建JSON命令
                val command = """
                    {
                        "deviceName": "${device.name}",
                        "power": ${device.isPowerOn},
                        "brightness": ${device.brightness}
                    }
                """.trimIndent()

                // 建立Socket连接并发送命令
                Socket(device.ipAddress, device.port.toInt()).use { socket ->
                    val outputStream: OutputStream = socket.getOutputStream()
                    outputStream.write(command.toByteArray(Charsets.UTF_8))
                    outputStream.flush()

                    // 主线程显示成功提示
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this,
                            "${device.name} 已更新: 电源=${if(device.isPowerOn) "开启" else "关闭"}, 亮度=${device.brightness}%",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.d(TAG, "命令发送成功: $command")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 主线程显示错误提示
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        this,
                        "发送失败: 无法连接到 ${device.ipAddress}:${device.port}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e(TAG, "命令发送失败: ${e.message}")
            }
        }.start()
    }

    // 接收从AddDeviceActivity返回的结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_DEVICE_REQUEST && resultCode == RESULT_OK) {
            data?.let { intent ->
                // 获取新设备信息
                val deviceName = intent.getStringExtra("deviceName") ?: return@let
                val ipAddress = intent.getStringExtra("ipAddress") ?: return@let
                val port = intent.getStringExtra("port") ?: return@let

                // 添加新设备到列表
                val newDevice = Device(deviceName, ipAddress, port)
                deviceAdapter.addDevice(newDevice)
                Toast.makeText(this, "已添加新设备: $deviceName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 避免内存泄漏，清除Handler引用
    override fun onDestroy() {
        super.onDestroy()
        // 若使用Handler子类，需在此处移除所有回调和消息
    }
}
