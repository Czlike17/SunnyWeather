package com.sunnyweather.android.logic.dao

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

object PlaceDao {
    private const val KEY_PLACE = "place"
    private const val SP_NAME = "sunny_weather"

    // 保存地点信息
    fun savePlace(place: Place) {
        sharedPreferences().edit().apply {
            putString(KEY_PLACE, Gson().toJson(place))
            apply() // 异步提交
        }
    }

    // 获取已保存的地点
    fun getSavedPlace(): Place? {
        val placeJson = sharedPreferences().getString(KEY_PLACE, "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    // 检查是否有保存的地点
    fun isPlaceSaved(): Boolean {
        return sharedPreferences().contains(KEY_PLACE)
    }

    // 私有方法：获取 SharedPreferences
    private fun sharedPreferences(): SharedPreferences {
        return SunnyWeatherApplication.context.getSharedPreferences(
            SP_NAME,
            Context.MODE_PRIVATE
        )
    }
}