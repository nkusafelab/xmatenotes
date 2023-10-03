package com.example.xmatenotes.ui

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.AudioManager
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.manager.Writer
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.logic.network.BitableManager
import com.example.xmatenotes.ui.qrcode.CardProcessActivity
import com.example.xmatenotes.ui.view.PageView
import com.example.xmatenotes.util.LogUtil

/**
 * 支持屏幕NK-cola的活动
 */
open class PageActivity : BaseActivity() {

    companion object {
        private const val TAG = "PageActivity"
    }

    protected var pageManager = PageManager.getInstance()
    protected val audioManager = AudioManager.getInstance()
    private var bitableManager = BitableManager.getInstance()
    protected lateinit var writer: Writer
    protected lateinit var pageView: PageView
    protected lateinit var page: Page
    protected var bitmap: Bitmap? = null
    protected var audioRecorder = false //录音开关

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page)
        pageView = findViewById(R.id.pageView)

        supportActionBar?.hide()

        val matrix = Matrix()
        matrix.postRotate(90F) // 顺时针旋转90度

        //测试接口用
        pageView.setPaintSize(40F)
        pageView.setPaintTypeface(Typeface.MONOSPACE)

    }

    override fun onStart() {
        super.onStart()

        this.writer = Writer.getInstance().init().setResponser(getPageResponsor())
        initPage()

    }

    override fun onResume() {
        bitmap?.let {
            if(it.width >= it.height){
                //横屏
                if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                }
            } else {
                //竖屏
                if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                }
            }
        }
        super.onResume()

        //绘制笔迹
        pageView.post {
            pageView.drawDots(page.dotList, page.coordinateCropper)
        }

    }

    override fun onPause() {
        super.onPause()
        if (audioRecorder) {
            audioRecorder = false
            audioManager.stopRATimer()
        }
    }

    protected open fun initPage(){

        bitmap = getViewBitmap()
        bitmap?.let {

            //imageView.setPaintColor(Color.RED)

            pageView.setImageBitmap(it)

        }
        page = getViewPage()
        this.writer.bindPage(page)
        page.create()
        var pageAbsolutePath = pageManager.mkdirs(page)
    }

    fun getPageResponsor(): PageResponsor{
        return PageResponsor()
    }

    fun getViewBitmap(): Bitmap {
        return getViewBitmap(0)
    }

    protected fun getViewBitmap(pageId: Int): Bitmap {
        var resId = PageManager.getResIDByPageID(pageId)
        LogUtil.e(TAG, "resources: $resources resId: $resId")
        return BitmapFactory.decodeResource(resources, resId).copy(Bitmap.Config.ARGB_8888, true)
    }

    fun getViewPage(): Page {
        return PageManager.getPageByPageID(0);
    }

    fun getCoordinateConverter(left: Float, top: Float, viewWidth: Float, viewHeight: Float): CoordinateConverter {
        return CoordinateConverter(left, top, viewWidth,
            viewHeight, page.realWidth, page.realHeight)
//        return page.setRealDimensions(viewWidth.toFloat(), viewHeight.toFloat(), resources.displayMetrics.density * 160)
    }

    /**
     * 生成带有笔迹的卡片图片
     */
    fun generatePageBmp(page: Page, mBitmap: Bitmap): Bitmap{
        val bitmap: Bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        var paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        val coordinateConverter = CoordinateConverter(
            bitmap.width.toFloat(),
            bitmap.height.toFloat(),
            page.realWidth,
            page.realHeight
        )
        var path = pageView.drawDots(page.dotList, coordinateConverter, page.coordinateCropper)
        canvas.drawPath(path, paint)
        return bitmap
    }



    fun processEachDot(simpleDot: SimpleDot) {

        processEachDot(createMediaDot(simpleDot))
    }

    fun processEachDot(mediaDot: MediaDot) {
        writer.let {
            writer.processEachDot(page.coordinateCropper.cropOut(mediaDot) as MediaDot)
        }
    }

    protected fun createMediaDot(simpleDot: SimpleDot): MediaDot {
        val mediaDot = MediaDot(simpleDot)
        mediaDot.pageID = this.page.code.toLong()
        //如果正在录音，再次长压结束录音
        if (audioRecorder) {
            mediaDot.audioID = Integer.parseInt(page.lastAudioName)
            mediaDot.color = MediaDot.DEEP_GREEN
        }

        mediaDot.penMac = XmateNotesApplication.mBTMac
        LogUtil.e(TAG, "封装MediaDot: $mediaDot")
        return mediaDot
    }

    inner class PageResponsor: Responser() {
        override fun onLongPress(command: Command?):Boolean {
            if(!super.onLongPress(command)){
                return false
            }

            showToast("长压命令")
            pageManager.save(page, bitmap?.let { generatePageBmp(page, it) })

            return false
        }

        override fun onSingleClick(command: Command?):Boolean {
            if(!super.onSingleClick(command)){
                return false
            }

            if (audioRecorder) {
                audioRecorder = false
                audioManager.stopRATimer()
            }

            showToast("单击")

            return false
        }

        override fun onDoubleClick(command: Command?):Boolean {
            if(!super.onDoubleClick(command)){
                return false
            }

            command?.handWriting?.firstDot?.let {coordinate->
                if(coordinate is MediaDot)
                page.getAudioNameByCoordinate(coordinate)?.let { audioName ->
                    LogUtil.e(CardProcessActivity.TAG, "播放AudioName为：$audioName")
                    audioManager.comPlayAudio(pageManager.getAudioAbsolutePath(page, audioName))
                }
            }

            showToast("双击命令")

            return false
        }

        override fun onActionCommand(command: Command?):Boolean {
            return super.onActionCommand(command)
        }

        override fun onCalligraphy(command: Command?):Boolean {

            if (command != null) {
                if(command.handWriting.isClosed){

                }
            }

            //绘制笔迹
            pageView.post { pageView.drawDots(page.dotList, page.coordinateCropper) }

            return super.onCalligraphy(command)
        }

        override fun onDelayHandWriting(command: Command?): Boolean {
            showToast("普通书写完毕")
            return super.onDelayHandWriting(command)
        }

        override fun onDelaySingleHandWriting(command: Command?): Boolean {
            showToast("单次笔迹完毕")
            return super.onDelaySingleHandWriting(command)
        }

        override fun onZhiLingKongZhi(command: Command?):Boolean {
            if (!super.onZhiLingKongZhi(command)){
                return false
            }

            audioManager.startRATimer(pageManager.getNewAudioAbsolutePath(page))
            audioRecorder = true

            runOnUiThread { Toast.makeText(XmateNotesApplication.context, "指令控制符命令", Toast.LENGTH_SHORT).show() }

            return false
        }

        override fun onSymbolicCommand(command: Command?):Boolean {
            if(!super.onSymbolicCommand(command)){
                return false
            }

            //绘制笔迹
            pageView.post { pageView.drawDots(page.dotList, page.coordinateCropper) }

            return false
        }

        override fun onDui(command: Command?): Boolean {
            showToast("对勾命令")
            return super.onDui(command)
        }

        override fun onCha(command: Command?): Boolean {
            showToast("叉命令")
            return super.onCha(command)
        }

        override fun onBanDui(command: Command?): Boolean {
            showToast("半对命令")
            return super.onBanDui(command)
        }
    }
}