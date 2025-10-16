package com.sunnyweather.android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DeviceLightActivity : AppCompatActivity() {
    private lateinit var currentDevice: Device
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_light)
        handler = Handler(Looper.getMainLooper())

        // 获取传递的设备信息
        val deviceName = intent.getStringExtra("deviceName") ?: ""
        val ipAddress = intent.getStringExtra("ipAddress") ?: ""
        val port = intent.getStringExtra("port") ?: ""
        val isPowerOn = intent.getBooleanExtra("isPowerOn", false)
        val brightness = intent.getIntExtra("brightness", 70)

        // 初始化设备对象
        currentDevice = Device(deviceName, ipAddress, port).apply {
            this.isPowerOn = isPowerOn
            this.brightness = brightness
            // 查找原有socket连接（如果存在）
            DeviceStorage.loadDevices(this@DeviceLightActivity).forEach {
                if (it.ipAddress == ipAddress && it.port == port && it.isConnected) {
                    this.socket = it.socket
                    this.isConnected = true
                }
            }
        }

        initView()
    }

    private fun initView() {
        findViewById<TextView>(R.id.tv_device_name).text = currentDevice.name

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // 电源开关
        val powerIcon = findViewById<ImageView>(R.id.iv_power)
        updatePowerIcon(powerIcon)
        powerIcon.setOnClickListener {
            currentDevice.isPowerOn = !currentDevice.isPowerOn
            updatePowerIcon(powerIcon)
            if (currentDevice.isConnected) {
                sendCommandToDevice()
            } else {
                Toast.makeText(this, "设备未连接", Toast.LENGTH_SHORT).show()
            }
        }

        // 定时功能
        findViewById<LinearLayout>(R.id.ll_timer).setOnClickListener {
            if (!currentDevice.isConnected) {
                Toast.makeText(this, "请先连接设备", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, TimerActivity::class.java).apply {
                putExtra("deviceName", currentDevice.name)
                putExtra("ipAddress", currentDevice.ipAddress)
                putExtra("port", currentDevice.port)
                putExtra("isPowerOn", currentDevice.isPowerOn)
                putExtra("brightness", currentDevice.brightness)
            }
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.ll_scene).setOnClickListener {
            Toast.makeText(this, "智能场景页面", Toast.LENGTH_SHORT).show()
           //待开发
        }

        findViewById<LinearLayout>(R.id.ll_voice).setOnClickListener {
            Toast.makeText(this, "语音定制页面", Toast.LENGTH_SHORT).show()
            // 待开发
        }
    }

    private fun updatePowerIcon(icon: ImageView) {
        icon.setImageResource(
            if (currentDevice.isPowerOn) R.mipmap.icon_power_on
            else R.mipmap.icon_power
        )
    }

    // 发送控制命令
    private fun sendCommandToDevice() {
        Thread {
            try {
                val command = """
                    {
                        "deviceName": "${currentDevice.name}",
                        "power": ${currentDevice.isPowerOn},
                        "brightness": ${currentDevice.brightness}
                    }
                """.trimIndent()

                currentDevice.socket?.let { socket ->
                    if (socket.isConnected && !socket.isClosed) {
                        socket.getOutputStream().apply {
                            write(command.toByteArray(Charsets.UTF_8))
                            flush()
                        }
                        handler.post {
                            Toast.makeText(this@DeviceLightActivity,
                                "已${if (currentDevice.isPowerOn) "开启" else "关闭"}设备",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    Toast.makeText(this@DeviceLightActivity, "发送命令失败", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // 返回时更新设备列表状态
    override fun finish() {
        // 保存设备状态到本地
        val devices = DeviceStorage.loadDevices(this)
        devices.forEachIndexed { index, device ->
            if (device.ipAddress == currentDevice.ipAddress && device.port == currentDevice.port) {
                devices[index] = currentDevice
                return@forEachIndexed
            }
        }
        DeviceStorage.saveDevices(this, devices)
        super.finish()
    }
}