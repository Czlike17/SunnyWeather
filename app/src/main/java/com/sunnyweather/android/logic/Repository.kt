package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers

//作为仓库层的统一封装入口
object Repository {
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {//LiveData（）提供一个挂起函数的上下文
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {//判断服务器状态
                val places = placeResponse.places
                Result.success(places)//获取城市数据列表
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))//包装异常信息
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
}

