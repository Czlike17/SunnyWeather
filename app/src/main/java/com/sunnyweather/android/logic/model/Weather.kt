package com.sunnyweather.android.logic.model

//用于封装Realtime和Daily
data class Weather(val realtime: RealtimeResponse.Realtime,val daily: DailyResponse.Daily)