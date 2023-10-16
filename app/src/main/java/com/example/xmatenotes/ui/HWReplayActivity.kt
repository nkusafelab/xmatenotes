package com.example.xmatenotes.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.xmatenotes.R
import com.example.xmatenotes.logic.manager.AudioManager
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting
import com.example.xmatenotes.ui.qrcode.BitmapCacheManager
import com.example.xmatenotes.ui.view.PageView
import com.example.xmatenotes.util.LogUtil
import kotlin.concurrent.thread

/**
 * 笔迹复现活动
 */
class HWReplayActivity : BaseActivity() {

    companion object {
        private const val TAG = "HWReplayActivity"
        private const val SPEED: Long = 15 //笔迹绘制速度10
        private const val HANDWRITING_PERIOD: Long = 100 //相邻两次笔迹间隔时间30

        @JvmStatic
        fun startHWReplayActivity(context: Context, subRect: Rect, bitmapKey: String, page: Page, localCode: String, singleHandWriting: SingleHandWriting){
            //跳转至笔迹复现页面
            val rpIntent = Intent(context, HWReplayActivity::class.java)
            rpIntent.putExtra("page", page)
            //扩展“余光”
//            rpIntent.putExtra("rect", subRect)
            //扩展“余光”
            rpIntent.putExtra("rectLeft", subRect.left)
            rpIntent.putExtra("rectTop", subRect.top)
            rpIntent.putExtra("rectRight", subRect.right)
            rpIntent.putExtra("rectBottom", subRect.bottom)
            rpIntent.putExtra("bitmapKey", bitmapKey)
            rpIntent.putExtra("localCode", localCode)
            rpIntent.putExtra("localHWsMapId", singleHandWriting)
            LogUtil.e(TAG,
                "startHWReplayActivity: 跳转: page: $page rect: $subRect bitmapKey: $bitmapKey localCode: $localCode localHWsMapId: $singleHandWriting"
            )
            context.startActivity(rpIntent)
        }
    }

    private val pageManager = PageManager.getInstance()
    private val audioManager = AudioManager.getInstance()
    private lateinit var pageView: PageView
    private lateinit var rect: Rect
    private lateinit var page: Page

    private lateinit var singleHandWriting: SingleHandWriting
    private lateinit var localCode: String
    private val penWidth = 2
    private lateinit var bitmap: Bitmap
    /**
     * 索引和对应颜色的映射
     * 当前索引范围0~6
     */
    private val indexToColorMap:Map<Int,Int> = mapOf(
        0 to Color.RED,
        1 to Color.rgb(255,204,0),//橘黄色
        2 to Color.YELLOW,//黄色
        3 to Color.GREEN,//绿色
        4 to Color.rgb(0,255,255),//青色
        5 to Color.BLUE,//蓝色
        6 to Color.rgb(128, 0, 128),//紫色
    )

    private var colorNumber = -1 //颜色索引


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hwreplay)

//        page = intent.getSerializableExtra("page", Page::class.java) as Page
        page = intent.getSerializableExtra("page") as Page
        LogUtil.e(TAG, "onCreate: 收到page: $page")
        rect = Rect()
        rect.left = intent.getIntExtra("rectLeft", 0)
        rect.top = intent.getIntExtra("rectTop", 0)
        rect.right = intent.getIntExtra("rectRight", 0)
        rect.bottom = intent.getIntExtra("rectBottom", 0)
//        rect = intent.getParcelableExtra("rect", Rect::class.java)!!
        LogUtil.e(TAG, "onCreate: 收到rect: $rect")
        localCode = intent.getStringExtra("localCode").toString()
        LogUtil.e(TAG, "onCreate: 收到localCode: $localCode")
        singleHandWriting = intent.getSerializableExtra("localHWsMapId") as SingleHandWriting
//        singleHandWriting = intent.getSerializableExtra("localHWsMapId", SingleHandWriting::class.java) as SingleHandWriting
        LogUtil.e(TAG, "onCreate: 收到localHWsMapId: $singleHandWriting")
        var bitmapKey = intent.getStringExtra("bitmapKey")
        LogUtil.e(TAG, "onCreate: 收到bitmapKey: $bitmapKey")
        bitmapKey?.let {
            bitmap = BitmapCacheManager.getBitmap(bitmapKey)!!
        }

        isSupportActionBarDisplayConnected = false

        if (localCode == "null") {
            localCode = "" + page.code
            supportActionBar!!.title = "页码: $localCode"
            LogUtil.e(TAG, "onCreate: supportActionBar!!.title: "+supportActionBar!!.title)
        } else {
            supportActionBar!!.title = "局域编码: $localCode"
            LogUtil.e(TAG, "onCreate: supportActionBar!!.title: "+supportActionBar!!.title)
        }

        pageView = findViewById(R.id.hwReplayView)

        pageView.post {
            pageView.setImageBitmap(bitmap)
            pageView.setPenWidth(penWidth)
            var singleHandWritingList = page.getSingleHandWritingListByRect(rect)
            LogUtil.e(TAG, "onCreate: singleHandWritingList.size: ${singleHandWritingList.size}")
            LogUtil.e(TAG, "onCreate: singleHandWritingList: $singleHandWritingList")
            var cropper = CoordinateConverter.CoordinateCropper(rect.left.toFloat(),
                rect.top.toFloat()
            )

            //如果不开启子线程，在循环中sleep时系统可能会提醒程序无响应
            thread {
                var quickDraw = true // 是否快速绘制
                var isHwStart = false
                var dotTimelongStart: Long = 0
                var timelongStart: Long = 0
                val audioID = 0 //音频文件标记
                var isPlayingAudio = false //标记当前是否正在播放音频
                val lhmnumber = 0 //lhm下标

                for(shw in singleHandWritingList){
                    pageView.setPenColor(getColor())
                    LogUtil.e(TAG, "onCreate: shw.equals(singleHandWriting): "+shw.equals(singleHandWriting)+" shw: "+shw+" ?= singleHandWriting: "+singleHandWriting)
                    if(quickDraw){
                        if(shw.equals(singleHandWriting)){
                            quickDraw = false
                            LogUtil.e(TAG, "onCreate: quickDraw = false")
                        }
                    }


                    if(quickDraw){
                        pageView.drawLineDots(shw, cropper)
                    } else {
                        for(hw in shw.handWritings){
                            if(hw.hasAudio()){
                                var audioName = page.getAudioNameByAudioId(hw.audioId)
                                audioManager.comPlayAudio(pageManager.getAudioAbsolutePath(page, audioName))
                                LogUtil.e(TAG, "播放AudioName为：$audioName")
                                for(stroke in hw.strokes){
                                    for(simpleDot in stroke.dots){
                                        if(simpleDot is MediaDot){
                                            if(!isHwStart){
                                                dotTimelongStart = simpleDot.timelong
                                                timelongStart = System.currentTimeMillis()
                                                isHwStart = true
                                            }
                                            pageView.drawLineDot(simpleDot, cropper)

                                        }
                                        //控制音频笔迹同步绘制
                                        if (audioManager.isPlaying) {
                                            while (System.currentTimeMillis() - timelongStart < simpleDot.timelong - dotTimelongStart) {
                                            }
                                        }
                                    }
                                }

                                isHwStart = false
                                while (audioManager.isPlaying){
                                }

                            } else {
                                for(stroke in hw.strokes){
                                    for(simpleDot in stroke.dots){
                                        if(simpleDot is MediaDot){
                                            pageView.drawLineDot(simpleDot, cropper)
                                            try {
                                                Thread.sleep(SPEED)
                                            } catch (e: InterruptedException) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }

                            try {
                                Thread.sleep(HANDWRITING_PERIOD)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    //单次笔迹绘制完成


                }

                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                finish()

            }
        }

    }



    fun getCoordinateConverter(left: Float, top: Float, viewWidth: Float, viewHeight: Float): CoordinateConverter {
        return CoordinateConverter(left, top, viewWidth,
            viewHeight, rect.width().toFloat(), rect.height().toFloat()
        )
//        return page.setRealDimensions(viewWidth.toFloat(), viewHeight.toFloat(), resources.displayMetrics.density * 160)
    }

    private fun getColor(): Int {
        colorNumber = (colorNumber + 1) % 7
        return indexToColorMap[colorNumber]!!
    }

    override fun onResume() {
        super.onResume()
        audioManager.startPlayAudio()
    }

    override fun onPause() {
        super.onPause()
        audioManager.pausePlayAudio()
    }

    override fun onStop() {
        super.onStop()
        if (audioManager.isPlaying) {
            audioManager.stopPlayAudio()
        }
    }
}