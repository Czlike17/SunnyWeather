package com.sunnyweather.android.logic.network

import retrofit2.Call
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.http.GET
import retrofit2.http.Query

//定义网络层接口，用于访问彩云天气
interface PlaceService {
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    fun searchPlaces(@Query("query")query: String):Call<PlaceResponse>//服务器返回的JSON数据自动解析成PlaceResponse对象
}