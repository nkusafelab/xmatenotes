package com.example.xmatenotes.ui.play

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.xmatenotes.R
import com.example.xmatenotes.databinding.ActivityPlayShowBinding
import com.example.xmatenotes.logic.model.Play
import com.example.xmatenotes.ui.BaseActivity
import java.text.SimpleDateFormat
import java.util.Locale

class PlayShowActivity : BaseActivity() {
    val viewModel by lazy { ViewModelProvider(this).get(PlayShowViewModel::class.java) }

    private lateinit var binding: ActivityPlayShowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
        binding = ActivityPlayShowBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_weather)
        setContentView(binding.root)
        if (viewModel.enumData.isEmpty()) {
//            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.playTitle.isEmpty()) {
//            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.play == null) {
//            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }




        binding.navBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })
    }

    fun closeDrawers() {
        binding.drawerLayout.closeDrawers()
    }

    fun refreshPlay(){
        viewModel.play?.let { showPlayInfo(it) }
    }

    private fun showPlayInfo(play: Play) {
        binding.playTitle.text = viewModel.playTitle
        binding.enumData.text = viewModel.enumData
//        val realtime = weather.realtime
//        val daily = weather.daily
//        // 填充now.xml布局中数据
//        val currentTempText = "${realtime.temperature.toInt()} ℃"
//        binding.now.currentTemp.text = currentTempText
//        binding.now.currentSky.text = getSky(realtime.skycon).info
//        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
//        binding.now.currentAQI.text = currentPM25Text
//        binding.now.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
//        // 填充forecast.xml布局中的数据
//        binding.forecast.forecastLayout.removeAllViews()
//        val days = daily.skycon.size
//        for (i in 0 until days) {
//            val skycon = daily.skycon[i]
//            val temperature = daily.temperature[i]
//            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, binding.forecast.forecastLayout, false)
//            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
//            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
//            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
//            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
//            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            dateInfo.text = simpleDateFormat.format(skycon.date)
//            val sky = getSky(skycon.value)
//            skyIcon.setImageResource(sky.icon)
//            skyInfo.text = sky.info
//            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
//            temperatureInfo.text = tempText
//            binding.forecast.forecastLayout.addView(view)
//        }
        // 填充life_index.xml布局中的数据
//        val lifeIndex = daily.lifeIndex
//        binding.lifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
//        binding.lifeIndex.dressingText.text = lifeIndex.dressing[0].desc
//        binding.lifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc
//        binding.lifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        binding.playShowLayout.visibility = View.VISIBLE
    }


}