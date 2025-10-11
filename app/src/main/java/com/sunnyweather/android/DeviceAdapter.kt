package com.sunnyweather.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.widget.Toast

class DeviceAdapter(
    private val context: Context,
    private var deviceList: MutableList<Device>,
    private val onConfirmClickListener: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    // 视图持有者，缓存控件引用
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val powerSwitch: Switch = itemView.findViewById(R.id.power_switch)
        val brightnessSeekbar: SeekBar = itemView.findViewById(R.id.brightness_seekbar)
        val brightnessValue: TextView = itemView.findViewById(R.id.brightness_value)
        val confirmButton: Button = itemView.findViewById(R.id.confirm_button)
    }

    // 创建视图持有者
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    // 绑定数据到视图
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]

        // 初始化设备信息
        holder.deviceName.text = device.name
        holder.powerSwitch.isChecked = device.isPowerOn
        holder.brightnessSeekbar.progress = device.brightness
        holder.brightnessValue.text = "${device.brightness}%"

        // 电源开关状态变化监听
        holder.powerSwitch.setOnCheckedChangeListener { _, isChecked ->
            device.isPowerOn = isChecked
        }

        // 亮度调节监听
        holder.brightnessSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                device.brightness = progress
                holder.brightnessValue.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 确认按钮点击事件
        holder.confirmButton.setOnClickListener {
            onConfirmClickListener(device)
        }
    }

    // 获取列表项数量
    override fun getItemCount() = deviceList.size

    // 添加新设备
    fun addDevice(device: Device) {
        deviceList.add(device)
        notifyItemInserted(deviceList.size - 1)  // 通知列表插入新项
    }
}
