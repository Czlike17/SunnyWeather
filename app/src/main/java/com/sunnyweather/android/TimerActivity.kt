package com.sunnyweather.android

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class TimerActivity : AppCompatActivity() {
    private lateinit var device: Device
    private var countDownTimer: CountDownTimer? = null
    private var isCounting = false

    private lateinit var npHour: NumberPicker
    private lateinit var npMinute: NumberPicker
    private lateinit var npSecond: NumberPicker
    private lateinit var tvCountdown: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        // 获取传递的设备信息
        device = Device(
            name = intent.getStringExtra("deviceName") ?: "",
            ipAddress = intent.getStringExtra("ipAddress") ?: "",
            port = intent.getStringExtra("port") ?: "",
            isPowerOn = intent.getBooleanExtra("isPowerOn", false),
            brightness = intent.getIntExtra("brightness", 70)
        )

        // 初始化时间选择器
        initNumberPickers()

        // 初始化视图
        tvCountdown = findViewById(R.id.tv_countdown)
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val ivBack = findViewById<ImageView>(R.id.iv_back)

        // 返回按钮
        ivBack.setOnClickListener {
            finish()
        }

        // 开始倒计时
        btnStart.setOnClickListener {
            if (isCounting) {
                stopCountdown()
                btnStart.text = "开始倒计时"
                isCounting = false
            } else {
                val totalSeconds = calculateTotalSeconds()
                if (totalSeconds <= 0) {
                    Toast.makeText(this, "请设置有效时间", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                startCountdown(totalSeconds)
                btnStart.text = "停止倒计时"
                isCounting = true
            }
        }

        // 取消按钮
        btnCancel.setOnClickListener {
            stopCountdown()
            finish()
        }
    }

    private fun initNumberPickers() {
        npHour = findViewById<NumberPicker>(R.id.np_hour).apply {
            minValue = 0
            maxValue = 23
            value = 0
        }
        npMinute = findViewById<NumberPicker>(R.id.np_minute).apply {
            minValue = 0
            maxValue = 59
            value = 0
        }
        npSecond = findViewById<NumberPicker>(R.id.np_second).apply {
            minValue = 0
            maxValue = 59
            value = 30 // 默认30秒
        }
    }

    // 计算总秒数
    private fun calculateTotalSeconds(): Long {
        return (npHour.value * 3600 + npMinute.value * 60 + npSecond.value).toLong()
    }

    // 开始倒计时
    private fun startCountdown(totalSeconds: Long) {
        tvCountdown.visibility = TextView.VISIBLE
        countDownTimer = object : CountDownTimer(totalSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                tvCountdown.text = String.format(
                    "剩余时间: %02d:%02d:%02d",
                    hours, minutes, seconds
                )
            }

            override fun onFinish() {
                tvCountdown.text = "倒计时结束"
                sendTurnOffCommand()
                Toast.makeText(this@TimerActivity, "已自动关闭设备", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.start()
    }

    // 停止倒计时
    private fun stopCountdown() {
        countDownTimer?.cancel()
        tvCountdown.visibility = TextView.GONE
        isCounting = false
    }

    // 发送关闭设备命令
    private fun sendTurnOffCommand() {
        Thread {
            try {
                // 查找设备当前连接状态
                val devices = DeviceStorage.loadDevices(this)
                val targetDevice = devices.find {
                    it.ipAddress == device.ipAddress && it.port == device.port
                }

                targetDevice?.let {
                    if (it.isConnected && it.socket != null) {
                        val command = """
                            {
                                "deviceName": "${it.name}",
                                "power": false,
                                "brightness": ${it.brightness}
                            }
                        """.trimIndent()

                        val outputStream: OutputStream = it.socket!!.getOutputStream()
                        outputStream.write(command.toByteArray(Charsets.UTF_8))
                        outputStream.flush()

                        it.isPowerOn = false
                        DeviceStorage.saveDevices(this, devices)
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this, "设备未连接，无法执行关闭命令", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "发送命令失败", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCountdown()
    }
}