package com.sunnyweather.android

import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.Intent
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket
import java.net.InetSocketAddress


class DeviceAdapter(
    private val context: Context,
    private var deviceList: MutableList<Device>,
    private val onDeviceDeleted: (Int) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val powerSwitch: Switch = itemView.findViewById(R.id.power_switch)
        val brightnessSeekbar: SeekBar = itemView.findViewById(R.id.brightness_seekbar)
        val brightnessValue: TextView = itemView.findViewById(R.id.brightness_value)
        val connectButton: Button = itemView.findViewById(R.id.connect_button)
        val disconnectButton: Button = itemView.findViewById(R.id.disconnect_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]

        // 绑定设备数据
        holder.deviceName.text = device.name
        holder.powerSwitch.isChecked = device.isPowerOn
        holder.brightnessSeekbar.progress = device.brightness
        holder.brightnessValue.text = "${device.brightness}%"

        // 根据连接状态更新UI
        updateConnectionUI(holder, device.isConnected)

        // 电源开关状态监听
        holder.powerSwitch.setOnCheckedChangeListener { _, isChecked ->
            device.isPowerOn = isChecked
            if (device.isConnected) {
                sendCommandToDevice(device)
            }
        }

        // 亮度调节监听
        holder.brightnessSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    device.brightness = progress
                    holder.brightnessValue.text = "$progress%"
                    if (device.isConnected) {
                        sendCommandToDevice(device)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        holder.connectButton.setOnClickListener {
            connectToDevice(device, holder)
        }

        holder.disconnectButton.setOnClickListener {
            disconnectFromDevice(device, holder)
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmDialog(holder.adapterPosition)
        }
        // 设备项空白处点击事件
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DeviceLightActivity::class.java).apply {
                putExtra("deviceName", device.name)
                putExtra("ipAddress", device.ipAddress)
                putExtra("port", device.port)
                putExtra("isPowerOn", device.isPowerOn)
                putExtra("brightness", device.brightness)
            }
            context.startActivity(intent)
        }

// 设备名称点击事件
        holder.deviceName.setOnClickListener {
            val intent = Intent(context, DeviceLightActivity::class.java).apply {
                putExtra("deviceName", device.name)
                putExtra("ipAddress", device.ipAddress)
                putExtra("port", device.port)
                putExtra("isPowerOn", device.isPowerOn)
                putExtra("brightness", device.brightness)
            }
            context.startActivity(intent)
        }
    }

    private fun updateConnectionUI(holder: DeviceViewHolder, isConnected: Boolean) {
        holder.connectButton.isEnabled = !isConnected
        holder.disconnectButton.isEnabled = isConnected
        holder.powerSwitch.isEnabled = isConnected
        holder.brightnessSeekbar.isEnabled = isConnected
    }

    private fun connectToDevice(device: Device, holder: DeviceViewHolder) {
        Toast.makeText(context, "正在连接设备...", Toast.LENGTH_SHORT).show()

        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (!device.isConnected) {
                device.socket?.close()
                device.socket = null
                device.isConnected = false
                handler.post { updateConnectionUI(holder, false) }
            }
        }

        handler.postDelayed(timeoutRunnable, 8000)

        Thread {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(device.ipAddress, device.port.toInt()), 8000)

                device.socket = socket
                device.isConnected = true

                handler.removeCallbacks(timeoutRunnable)
                handler.post {
                    updateConnectionUI(holder, true)
                    Toast.makeText(context, "已连接设备", Toast.LENGTH_SHORT).show()
                    sendCommandToDevice(device)
                }

                // 启动数据接收线程
                startDataReceiver(device, holder, handler)

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    if (!device.isConnected) {
                        Toast.makeText(
                            context,
                            "连接失败: ${device.ipAddress}:${device.port}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }.start()
    }

    // 数据接收方法
    private fun startDataReceiver(device: Device, holder: DeviceViewHolder, handler: Handler) {
        Thread {
            try {
                val inputStream = device.socket?.getInputStream()
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

                var line: String? = null
                while (device.isConnected && reader.readLine().also { line = it } != null) {
                    line?.let { json ->
                        // 解析ESP32发送的JSON数据
                        val jsonObject = JSONObject(json)
                        val brightness = jsonObject.getInt("brightness")
                        val power = jsonObject.getBoolean("power")

                        // 回调主线程更新UI
                        handler.post {
                            device.brightness = brightness
                            device.isPowerOn = power
                            holder.brightnessSeekbar.progress = brightness
                            holder.brightnessValue.text = "$brightness%"
                            holder.powerSwitch.isChecked = power
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (device.isConnected) {
                    handler.post {
                        Toast.makeText(context, "数据接收异常", Toast.LENGTH_SHORT).show()
                        disconnectFromDevice(device, holder)
                    }
                }
            }
        }.start()
    }

    // 断开设备连接
    private fun disconnectFromDevice(device: Device, holder: DeviceViewHolder) {
        try {
            device.isConnected = false // 先标记为断开状态
            device.socket?.shutdownInput() // 关闭输入流
            device.socket?.close() // 关闭socket
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            device.socket = null
            Handler(Looper.getMainLooper()).post {
                updateConnectionUI(holder, false)
                Toast.makeText(context, "已断开连接", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 发送命令到设备
    private fun sendCommandToDevice(device: Device) {
        Thread {
            try {
                val command = """
                    {
                        "deviceName": "${device.name}",
                        "power": ${device.isPowerOn},
                        "brightness": ${device.brightness}
                    }
                """.trimIndent()

                // 通过已建立的Socket发送数据
                device.socket?.let { socket ->
                    if (socket.isConnected && !socket.isClosed) {
                        val outputStream: OutputStream = socket.getOutputStream()
                        outputStream.write(command.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "发送命令失败，请检查连接",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    // 显示删除确认弹窗
    private fun showDeleteConfirmDialog(position: Int) {
        AlertDialog.Builder(context)
            .setTitle("删除设备")
            .setMessage("确定要删除 ${deviceList[position].name} 吗？")
            .setPositiveButton("确定") { _, _ ->
                deviceList[position].socket?.close()
                onDeviceDeleted(position)  // 触发删除回调
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()  // 取消删除
            }
            .setCancelable(false)  // 不允许点击外部取消
            .show()
    }

    override fun getItemCount() = deviceList.size

    fun addDevice(device: Device) {
        deviceList.add(device)
        notifyItemInserted(deviceList.size - 1)
    }

    fun removeDevice(position: Int) {
        deviceList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, deviceList.size - position)  // 刷新后续项位置
    }

    fun setAllDevicesPowerState(powerOn: Boolean) {
        deviceList.forEachIndexed { index, device ->
            if (device.isConnected) {  // 只控制已连接的设备
                device.isPowerOn = powerOn
                sendCommandToDevice(device)
                notifyItemChanged(index)
            }
        }
    }
}