package com.example.xmatenotes.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.xmatenotes.MLScanningAnalyzer
import com.example.xmatenotes.OpenCVQRCodeActivity
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.app.ax.A3
import com.example.xmatenotes.logic.dao.RoleDao
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.manager.Storager
import com.example.xmatenotes.logic.manager.VideoManager
import com.example.xmatenotes.logic.model.Page.Card
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.Page.QRObject
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.ui.ckplayer.VideoNoteActivity
import com.example.xmatenotes.ui.ckplayer.XueChengVideoNoteActivity
import com.example.xmatenotes.ui.qrcode.BitmapCacheManager
import com.example.xmatenotes.ui.qrcode.CardProcessActivity
import com.example.xmatenotes.ui.qrcode.QRResultListener
import com.example.xmatenotes.ui.qrcode.WeChatQRCodeActivity
import com.example.xmatenotes.util.BitmapUtil
import com.example.xmatenotes.util.LogUtil
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.king.mlkit.vision.camera.AnalyzeResult
import org.opencv.core.Mat
import java.io.File
import java.lang.Exception
import kotlin.concurrent.thread

class CardViewActivity : PageViewActivity() {

    companion object {
        private const val TAG = "CardViewActivity"
    }

    private lateinit var card: Card
    private var pageBmpBounds =  RectF()
    private var bmpBounds = RectF()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_card_view)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun initUI() {
        super.initUI()
        supportActionBar?.hide()
        bitmap = BitmapUtil.getBitmap("WeChatQRCodeBitmap")!!
        LogUtil.e(TAG, "Storager.cardCache == null: "+(Storager.cardCache == null))
        card = Storager.cardCache
        this.page = card
        Storager.cardCache = null
        var cardAbsolutePath = pageManager.mkdirs(card)
        bitmap?.let {
            pageView.setImageBitmap(it)
        }
        LogUtil.e(TAG, "initPage: 下载目标卡片资源: "+card.preCode)
        pageManager.downLoad(card.preCode, cardAbsolutePath, object : PageManager.ObjectInputResp {
            override fun onFinish(page: Page?) {
                LogUtil.e(TAG, "onCreate: page == null: "+(page == null))
                if(page != null){
                    pageBmpBounds.left = intent.getFloatExtra("intrnleft", 0f)
                    pageBmpBounds.right = intent.getFloatExtra("intrnright", 0f)
                    pageBmpBounds.top = intent.getFloatExtra("intrntop", 0f)
                    pageBmpBounds.bottom = intent.getFloatExtra("intrnbottom", 0f)
                    bmpBounds.left = intent.getFloatExtra("superleft", 0f)
                    bmpBounds.top = intent.getFloatExtra("supertop", 0f)
                    bmpBounds.right = intent.getFloatExtra("superright", 0f)
                    bmpBounds.bottom = intent.getFloatExtra("superbottom", 0f)
                    var handWritingNumber = 0
                    for (singleHandWriting in page.dotList){
                        singleHandWriting.isNew = false
                        handWritingNumber += singleHandWriting.size()
                    }
                    card.setPosition(page.realLeft, page.realTop)
                    card.setRealDimensions(page.realWidth, page.realHeight)
                    //融合笔迹点集合
                    card.addDotsList(0, page.dotList)
                    LogUtil.e(TAG, "onCreate: 融合旧的singleHandWriting数量为: "+card.dotList.size+" HandWriting数量为: "+handWritingNumber)
                    //融合音频文件名集合
                    card.addAudioNameList(0, page.audioNameList)
                    //融合笔迹范围，暂不处理

                    //主动设置pageView的
                    pageView.setCoordinateConverter(card.realWidth, card.realHeight, pageBmpBounds, bmpBounds)
                }
            }

            override fun onError(errorMsg: String?) {
                LogUtil.e(TAG, "onCreate: 获取前代卡片数据失败!")
            }
        })
    }

    override fun initPage() {

        this.writer.bindPage(card)
    }

    override fun getCoordinateConverter(left: Float, top: Float, viewWidth: Float, viewHeight: Float): CoordinateConverter {
        var viewRectF = RectF()
        viewRectF.left = left
        viewRectF.top = top
        viewRectF.right = viewRectF.left + viewWidth
        viewRectF.bottom = viewRectF.top + viewHeight
        viewRectF = BitmapUtil.mapRect(pageBmpBounds, bmpBounds, viewRectF)
        return CoordinateConverter(viewRectF.left, viewRectF.top, viewRectF.width(),
            viewRectF.height(), card.realWidth, card.realHeight
        )
    }

    override fun createMediaDot(simpleDot: SimpleDot): MediaDot {
        val mediaDot = MediaDot(simpleDot)
        mediaDot.pageId = this.card.code.toLong()
        mediaDot.penMac = XmateNotesApplication.mBTMac
        LogUtil.e(TAG, "封装MediaDot: $mediaDot")
        return mediaDot
    }

    override fun generatePageBmp(page: Page, mBitmap: Bitmap): Bitmap {
        val bitmap: Bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        var paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        val coordinateConverter = CoordinateConverter(
            pageBmpBounds.left,
            pageBmpBounds.top,
            pageBmpBounds.width(),
            pageBmpBounds.height(),
            page.realWidth,
            page.realHeight
        )
        var path = pageView.drawDots(page.dotList, coordinateConverter, page.coordinateCropper)
        canvas.drawPath(path, paint)
        return bitmap
    }

    fun pointsToRectF(points: ArrayList<org.opencv.core.Point>): RectF {
        var rectF = RectF()
        rectF.left = points.get(0).x.toFloat()
        rectF.top = points.get(0).y.toFloat()
        rectF.right = points.get(2).x.toFloat()
        rectF.bottom = points.get(2).y.toFloat()
        return rectF
    }

    fun MLMatToPoints(mat: Mat): ArrayList<org.opencv.core.Point> {
        var points = ArrayList<org.opencv.core.Point>()
        for( i in 0..3){
            points.add(org.opencv.core.Point(mat[i, i][0], mat[i, i][1]))
            Log.d(OpenCVQRCodeActivity.TAG, "point$i: ${mat[i, i][0]}, ${mat[i, i][1]}")
        }
        return points
    }

    /**
     * 提交版本
     */
    override fun commit(point: SimpleDot, page: Page, bmp: Bitmap) {
        MLScanningAnalyzer(true).analyze(bmp, object : QRResultListener {
            override fun onSuccess(result: AnalyzeResult<MutableList<String>>?) {
                if (result != null) {
                    LogUtil.e(TAG, result.result.toString())
                    if (result is MLScanningAnalyzer.MLQRCodeAnalyzeResult) {
                        var qrObject: QRObject? = null
                        val gson = Gson()
                        result.result.forEach {
                            try {
                                qrObject = gson.fromJson(it, QRObject::class.java)
                            } catch (e: JsonSyntaxException) {
                                LogUtil.e(WeChatQRCodeActivity.TAG, "JSON解析失败: ${e.message}")
                            }
                        }
                        if(qrObject != null){
                            var qrRectF: RectF? = null
                            result.points?.forEach { mat ->
                                var MLPoints = MLMatToPoints(mat)
                                qrRectF = pointsToRectF(MLPoints)
                            }
                            if(qrRectF != null){
                                page.qrObject = qrObject
                                page.updateRole(RoleDao.getRole())
                                page.addIteration()
                                val oldFile = File(pageManager.getPageAbsolutePath(page))
                                pageManager.iterateVersion(page)
                                var newFile = File(pageManager.getPageAbsolutePath(page))
                                var pageResAbsolutePath = newFile.absolutePath
                                if (oldFile.renameTo(newFile)) {
                                    LogUtil.e(
                                        TAG,
                                        "save: Directory " + oldFile.name + " renamed to " + newFile.name
                                    )
                                } else {
                                    LogUtil.e(
                                        TAG,
                                        "save: Could not rename directory " + oldFile.name + " to " + newFile.name
                                    )
                                }
                                var canvas = Canvas(bmp)
                                var paint = Paint()
                                page.toQRObject().toQRCodeBitmap(qrRectF!!)?.let {
                                    canvas.drawBitmap(it, qrRectF!!.left, qrRectF!!.top, paint)
                                }
                                pageView.postInvalidate()
                                pageManager.save(page, bitmap, pageResAbsolutePath)
//                                pageManager.save(page, bitmap?.let { generatePageBmp(page, it) }, pageResAbsolutePath)
                            }

                        }
                    }

                }
            }

            override fun onFailure(e: Exception?) {
                LogUtil.e(TAG, "onFailure: 未识别到二维码！")
                page.updateRole(RoleDao.getRole())
                val oldFile = File(pageManager.getPageAbsolutePath(page))
                pageManager.iterateVersion(page)
                var newFile = File(pageManager.getPageAbsolutePath(page))
                var pageResAbsolutePath = newFile.absolutePath
                if (oldFile.renameTo(newFile)) {
                    LogUtil.e(
                        TAG,
                        "save: Directory " + oldFile.name + " renamed to " + newFile.name
                    )
                } else {
                    LogUtil.e(
                        TAG,
                        "save: Could not rename directory " + oldFile.name + " to " + newFile.name
                    )
                }
                pageManager.save(page, bitmap, pageResAbsolutePath)
//                pageManager.save(page, bitmap?.let { generatePageBmp(page, it) }, pageResAbsolutePath)
            }
        })
    }

    override fun getResponser(): Responser {
        return CardResponser()
    }

    open inner class CardResponser: CommandResponser() {
        override fun onLongPress(command: Command?): Boolean {
            if(!super.onLongPress(command)){
                return false
            }

//            showToast("长压命令")
            command?.handWriting?.firstDot?.let {coordinate->
                bitmap?.let {
                    thread {
                        commit(coordinate, card, it)
                    }
                }
            }

            return true
        }

        override fun onSingleClick(command: Command?): Boolean {
            if(!super.onSingleClick(command)){
                return false
            }

            if (audioRecorder) {
                audioRecorder = false
                audioManager.stopRATimer()
            }

            return false
        }

        override fun onDoubleClick(command: Command?): Boolean {
            if(!super.onDoubleClick(command)){
                return false
            }

            command?.handWriting?.firstDot?.let { coordinate ->
                var mediaDot = coordinate as MediaDot
                card.getHandWritingByCoordinate(mediaDot)?.let { handWriting ->
//                    if (handWriting.penMac.equals(XmateNotesApplication.mBTMac)) {
                        if (handWriting.hasVideo()) {
                            //跳转视频播放
                            if (baseActivity !is VideoNoteActivity) {
                                LogUtil.e(
                                    TAG,
                                    "onDoubleClick: 跳转视频播放: videoId: " + handWriting.videoId + " videoTime: " + handWriting.videoTime
                                )
                                VideoManager.startVideoNoteActivity(
                                    this@CardViewActivity,
                                    XueChengVideoNoteActivity::class.java,
                                    handWriting.videoId,
                                    handWriting.videoTime
                                )
                                return false
                            }
                        } else {
                            //跳转笔迹动态复现

                            bitmap?.let {
                                var subRect = Rect(
                                    card.realLeft.toInt(), card.realTop.toInt(),
                                    (card.realLeft + card.realWidth).toInt(), (card.realTop+card.realHeight).toInt()
                                )
                                BitmapCacheManager.putBitmap("cardBitmap", it.copy(Bitmap.Config.ARGB_8888, true))
                                HWReplayActivity.startHWReplayActivity(
                                    this@CardViewActivity,
                                    subRect,
                                    "cardBitmap",
                                    card,
                                    card.code,
                                    card.getSingleHandWritingByCoordinate(mediaDot)
                                )
                            }

//                            page.getAudioNameByCoordinate(mediaDot)?.let { audioName ->
//                                LogUtil.e(CardProcessActivity.TAG, "播放AudioName为：$audioName")
//                                audioManager.comPlayAudio(pageManager.getAudioAbsolutePath(page, audioName))
//                            }
                            return false
                        }
//                    }
                }
            }

            return true
        }

        override fun onCalligraphy(command: Command?): Boolean {
            //绘制笔迹
//            pageView.post { pageView.drawDot(page.lastDot as MediaDot, page.coordinateCropper) }
//            //绘制笔迹
//            updatePageViewDots()
            //绘制笔迹
            command?.handWriting?.let { handWriting ->
                if(!handWriting.isDrawed){
                    updatePageViewDots(handWriting)
                    handWriting.isDrawed = true
                } else {
                    handWriting.lastDot?.let {
                        updatePageViewDot(it)
                    }
                }
            }

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

            command?.handWriting?.firstDot?.let {coordinate->
                audioManager.startRATimer(pageManager.getNewAudioAbsolutePath(coordinate, card))
                audioRecorder = true
            }

//            runOnUiThread { Toast.makeText(XmateNotesApplication.context, "指令控制符命令", Toast.LENGTH_SHORT).show() }

            return false
        }

        override fun onSymbolicCommand(command: Command?):Boolean {
            if(!super.onSymbolicCommand(command)){
                return false
            }

//            //绘制笔迹
//            updatePageViewDots()
            //绘制笔迹
//            command?.handWriting?.lastDot?.let {
//                updatePageViewDot(it)
//            }

            return false
        }

        override fun onDui(command: Command?): Boolean {
//            showToast("对勾命令")
            return super.onDui(command)
        }

        override fun onCha(command: Command?): Boolean {
//            showToast("叉命令")
            return super.onCha(command)
        }

        override fun onBanDui(command: Command?): Boolean {
//            showToast("半对命令")
            return super.onBanDui(command)
        }

    }
}