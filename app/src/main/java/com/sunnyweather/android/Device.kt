package com.sunnyweather.android

data class Device(
    val name: String,
    val ipAddress: String,
    val port: String,
    var isPowerOn: Boolean = true,  // 电源状态
    var brightness: Int = 70        // 亮度值(0-100)
)