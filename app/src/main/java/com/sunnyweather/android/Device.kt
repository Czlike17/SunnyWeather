package com.sunnyweather.android

import java.net.Socket

data class Device(
    val name: String,
    val ipAddress: String,
    val port: String,
    var isPowerOn: Boolean = true,  // 电源状态
    var brightness: Int = 70,       // 亮度值(0-100)
    var isConnected: Boolean = false,
    var socket: Socket? = null
)