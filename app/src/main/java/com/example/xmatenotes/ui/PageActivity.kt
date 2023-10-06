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
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.manager.VideoManager
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.ui.qrcode.CardProcessActivity
import com.example.xmatenotes.ui.view.PageView
import com.example.xmatenotes.logic.presetable.LogUtil

/**
 * 支持屏幕NK-cola的活动
 */
open class PageActivity : SmartpenActivity() {

    companion object {
        private const val TAG = "PageActivity"
    }

    protected lateinit var pageView: PageView

    protected var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matrix = Matrix()
        matrix.postRotate(90F) // 顺时针旋转90度

    }

    override fun onStart() {
        super.onStart()
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
//        if (audioRecorder) {
//            audioRecorder = false
//            audioManager.stopRATimer()
//        }
    }

    protected override fun getLayoutId() : Int{
        return R.layout.activity_page
    }

    protected override fun initUI(){
        supportActionBar?.hide()
        pageView = findViewById(R.id.pageView)
        //测试接口用
        pageView.setPaintSize(40F)
        pageView.setPaintTypeface(Typeface.MONOSPACE)
    }

//    protected override fun initPage(){
//
//        bitmap = getViewBitmap()
//        bitmap?.let {
//
//            //imageView.setPaintColor(Color.RED)
//
//            pageView.setImageBitmap(it)
//
//        }
//        page = getViewPage()
//        this.writer.bindPage(page)
//        page.create()
//        var pageAbsolutePath = pageManager.mkdirs(page)
//    }

    override fun getResponser(): Responser{
        return PageResponser()
    }

    fun getViewBitmap(): Bitmap {
        return getViewBitmap(0)
    }

    protected fun getViewBitmap(pageId: Long): Bitmap {
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

    override fun switchPage(mediaDot: MediaDot): Boolean {
        if(super.switchPage(mediaDot)){
            bitmap = getViewBitmap(mediaDot.pageID)
            pageView.setImageBitmap(bitmap)
            pageView.post {
                pageView.drawDots(page.dotList, page.coordinateCropper)
            }
            return true
        }
        return false
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

        processEachDot(page.coordinateCropper.cropOut(createMediaDot(simpleDot)) as MediaDot)
    }

    protected fun createMediaDot(simpleDot: SimpleDot): MediaDot {
        val mediaDot = MediaDot(simpleDot)
        mediaDot.pageID = this.currentPageId
        mediaDot.penMac = XmateNotesApplication.mBTMac
        LogUtil.e(TAG, "封装MediaDot: $mediaDot")
        return mediaDot
    }

    open inner class PageResponser: Responser() {
        override fun onLongPress(command: Command?):Boolean {
            if(!super.onLongPress(command)){
                return false
            }
            showToast("长压命令")
            pageManager.save(page, bitmap?.let { generatePageBmp(page, it) })

            return true
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
                page.getHandWritingByCoordinate(coordinate)?.let {
                    if(it.hasVideo()){
                        //跳转视频播放
                        VideoManager.startVideoNoteActivity(this@PageActivity, it.videoId, it.videoTime);
                    } else {
                        //跳转笔迹动态复现
                    }
                }
                page.getAudioNameByCoordinate(coordinate)?.let { audioName ->
                    LogUtil.e(CardProcessActivity.TAG, "播放AudioName为：$audioName")
                    audioManager.comPlayAudio(pageManager.getAudioAbsolutePath(page, audioName))
                }
            }

            showToast("双击命令")

            return true
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