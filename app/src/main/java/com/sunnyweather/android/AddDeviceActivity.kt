package com.sunnyweather.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern

class AddDeviceActivity : AppCompatActivity() {
    private lateinit var etDeviceName: EditText
    private lateinit var etIpAddress: EditText
    private lateinit var etPort: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        etDeviceName = findViewById(R.id.et_device_name)
        etIpAddress = findViewById(R.id.et_ip_address)
        etPort = findViewById(R.id.et_port)

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_add).setOnClickListener {
            val deviceName = etDeviceName.text.toString().trim()
            val ipAddress = etIpAddress.text.toString().trim()
            val port = etPort.text.toString().trim()

            // 验证输入合法性
            if (!validateInput(deviceName, ipAddress, port)) {
                return@setOnClickListener
            }

            // 返回设备信息给主页面
            val intent = Intent()
            intent.putExtra("deviceName", deviceName)
            intent.putExtra("ipAddress", ipAddress)
            intent.putExtra("port", port)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun validateInput(deviceName: String, ipAddress: String, port: String): Boolean {
        if (deviceName.isEmpty()) {
            showToast("请输入设备名称")
            return false
        }

        if (ipAddress.isEmpty() || !isValidIpAddress(ipAddress)) {
            showToast("请输入有效的IP地址")
            return false
        }

        if (port.isEmpty()) {
            showToast("请输入端口号")
            return false
        }

        val portNum = port.toIntOrNull()
        if (portNum == null || portNum !in 1..65535) {
            showToast("请输入有效的端口号(1-65535)")
            return false
        }

        return true
    }

    // 验证IP地址格式
    private fun isValidIpAddress(ip: String): Boolean {
        val ipPattern = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )
        return ipPattern.matcher(ip).matches()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
