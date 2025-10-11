package com.sunnyweather.android

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context

class DeviceAdapter(
    private val context: Context,
    private var deviceList: MutableList<Device>,
    private val onConfirmClickListener: (Device) -> Unit,
    private val onDeviceDeleted: (Int) -> Unit  // 新增删除回调
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val powerSwitch: Switch = itemView.findViewById(R.id.power_switch)
        val brightnessSeekbar: SeekBar = itemView.findViewById(R.id.brightness_seekbar)
        val brightnessValue: TextView = itemView.findViewById(R.id.brightness_value)
        val confirmButton: Button = itemView.findViewById(R.id.confirm_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)  // 绑定删除按钮
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

        // 电源开关状态监听
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

        // 新增：删除按钮点击事件
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmDialog(holder.adapterPosition)  // 使用adapterPosition确保位置正确
        }
    }

    // 显示删除确认弹窗
    private fun showDeleteConfirmDialog(position: Int) {
        AlertDialog.Builder(context)
            .setTitle("删除设备")
            .setMessage("确定要删除 ${deviceList[position].name} 吗？")
            .setPositiveButton("确定") { _, _ ->
                onDeviceDeleted(position)  // 触发删除回调
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()  // 取消删除
            }
            .setCancelable(false)  // 不允许点击外部取消
            .show()
    }

    override fun getItemCount() = deviceList.size

    // 添加新设备
    fun addDevice(device: Device) {
        deviceList.add(device)
        notifyItemInserted(deviceList.size - 1)
    }

    // 新增：删除设备
    fun removeDevice(position: Int) {
        deviceList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, deviceList.size - position)  // 刷新后续项位置
    }
}
