package com.example.xmatenotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/**
 * 图片处理的活动，包括二维码识别、降噪、纠偏、抠图等
 */
class ImgProcessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_process)
    }
}