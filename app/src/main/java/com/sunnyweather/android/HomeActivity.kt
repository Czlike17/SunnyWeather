package com.sunnyweather.android

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sunnyweather.android.ui.weather.WeatherActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 初始化各个功能区域的点击事件
        initClickListeners()
    }

    private fun initClickListeners() {
        // 天气预报区域点击事件（迁移后的天气跳转逻辑）
        findViewById<LinearLayout>(R.id.ll_weather).setOnClickListener {
            // 跳转到天气预报页面（使用项目中已有的WeatherActivity）
            val intent = Intent(this, WeatherActivity::class.java)
            // 如果需要传递数据，可在此处添加extra
            // intent.putExtra("key", value)
            startActivity(intent)
        }

        // 灯光控制区域点击事件（跳转到已有的设备控制界面）
        findViewById<LinearLayout>(R.id.ll_light).setOnClickListener {
            val intent = Intent(this, DeviceControlActivity::class.java)
            startActivity(intent)
        }

        // 空调控制区域点击事件
        findViewById<LinearLayout>(R.id.ll_air_conditioner).setOnClickListener {
            Toast.makeText(this, "打开空调控制", Toast.LENGTH_SHORT).show()
            // 实际应用中替换为: startActivity(Intent(this, AirConditionerActivity::class.java))
        }

        // 窗帘控制区域点击事件
        findViewById<LinearLayout>(R.id.ll_curtain).setOnClickListener {
            Toast.makeText(this, "打开窗帘控制", Toast.LENGTH_SHORT).show()
            // 实际应用中替换为: startActivity(Intent(this, CurtainActivity::class.java))
        }

        // 定时环境区域点击事件
        findViewById<LinearLayout>(R.id.ll_timer_environment).setOnClickListener {
            Toast.makeText(this, "打开定时环境设置", Toast.LENGTH_SHORT).show()
            // 实际应用中替换为: startActivity(Intent(this, TimerEnvironmentActivity::class.java))
        }

        // 智能语音区域点击事件
        findViewById<LinearLayout>(R.id.ll_voice_control).setOnClickListener {
            Toast.makeText(this, "打开智能语音控制", Toast.LENGTH_SHORT).show()
            // 实际应用中替换为: startActivity(Intent(this, VoiceControlActivity::class.java))
        }

        // 风扇控制区域点击事件
        findViewById<LinearLayout>(R.id.ll_fan).setOnClickListener {
            Toast.makeText(this, "打开风扇控制", Toast.LENGTH_SHORT).show()
            // 实际应用中替换为: startActivity(Intent(this, FanActivity::class.java))
        }
    }
}
