package com.sunnyweather.android

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DeviceStorage {
    private const val SP_NAME = "device_storage"
    private const val KEY_DEVICES = "devices"
    private val gson = Gson()

    fun saveDevices(context: Context, devices: List<Device>) {
        // 过滤掉临时状态字段
        val devicesToSave = devices.map {
            it.copy(socket = null, isConnected = false)
        }
        val json = gson.toJson(devicesToSave)
        getSharedPreferences(context).edit()
            .putString(KEY_DEVICES, json)
            .apply()
    }

    // 读取设备列表
    fun loadDevices(context: Context): MutableList<Device> {
        val json = getSharedPreferences(context).getString(KEY_DEVICES, "")
        return if (json.isNullOrEmpty()) {
            mutableListOf()
        } else {
            val type = object : TypeToken<MutableList<Device>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        }
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }
}