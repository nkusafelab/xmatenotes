package com.example.xmatenotes.ui.qrcode

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.util.LruCache
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.R
import com.example.xmatenotes.logic.manager.AudioManager
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.manager.Storager
import com.example.xmatenotes.logic.manager.VideoManager
import com.example.xmatenotes.logic.manager.Writer
import com.example.xmatenotes.logic.model.Page.Card
import com.example.xmatenotes.logic.model.Page.CardManager
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.logic.network.BitableManager
import com.example.xmatenotes.ui.BaseActivity
import com.example.xmatenotes.ui.HWReplayActivity
import com.example.xmatenotes.ui.ckplayer.VideoNoteActivity
import com.example.xmatenotes.ui.ckplayer.XueChengVideoNoteActivity
import com.example.xmatenotes.ui.view.DrawingImageView
import com.example.xmatenotes.util.BitmapUtil
import com.example.xmatenotes.util.LogUtil
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.lang.Math.abs
import java.lang.Math.atan2
import java.lang.Math.sqrt
import java.util.concurrent.CountDownLatch
import kotlin.math.pow


/**
 * 图片处理的活动，包括二维码识别、降噪、纠偏、抠图等
 */

class CardProcessActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CardProcessActivity"
        private const val PAGRS_TABLEID = "tblXcJERkVDkcPki"
    }

    private var storager = Storager.getInstance()
    private var cardManager = CardManager.getInstance()
    private var pageManager = PageManager.getInstance()
    private val audioManager = AudioManager.getInstance()
    private var bitableManager = BitableManager.getInstance()
    private lateinit var writer: Writer
    private lateinit var imageView: DrawingImageView

    private lateinit var undoButton:Button
    private lateinit var commitButton:Button
    private lateinit var clearButton:Button
    private lateinit var recameraButton: Button

    private lateinit var card: Card
    private var bitmap: Bitmap? = null
    private var audioRecorder = false //录音开关

    private var pageBmpBounds =  RectF()
    private var bmpBounds = RectF()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_process)
        imageView =findViewById(R.id.imageView)
//        undoButton= findViewById(R.id.undo_button)
//        commitButton= findViewById(R.id.commit_button)
//        clearButton = findViewById(R.id.clear_button)
//        recameraButton = findViewById(R.id.recamera_button)
//        undoButton.setOnClickListener { imageView.undo() }
//        commitButton.setOnClickListener { imageView.commit() }
//        clearButton.setOnClickListener{ imageView.clear() }
//        recameraButton.setOnClickListener{ finish() }


        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
//            actionBar.setDisplayShowTitleEnabled(true)
//            actionBar.title = "拍照迭代"
//            //actionBar.setTitle(ApplicationResources.getLocalVersionName(this));
//            //actionBar.setTitle(ApplicationResources.getLocalVersionName(this));
//            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        bitmap = BitmapUtil.getBitmap("WeChatQRCodeBitmap")
        LogUtil.e(TAG, "Storager.cardCache == null: "+(Storager.cardCache == null))
        card = Storager.cardCache
        Storager.cardCache = null
        var cardAbsolutePath = pageManager.mkdirs(card)
        pageManager.downLoad(card.preCode, cardAbsolutePath, object : PageManager.ObjectInputResp {
            fun onFinish(obj: Any?) {
                LogUtil.e(TAG, "onCreate: card == null: "+(obj == null))
                if (obj != null){
                    if(obj is Card){
                        var card = obj as Card
                        var handWritingNumber = 0
                        //区分新旧笔迹
                        for(singleHandWriting in card.cardResource.dotList){
                            handWritingNumber += singleHandWriting.size()
                            singleHandWriting.isNew = false
                        }

                        //融合笔迹点集合
                        card.addDotsList(0, card.cardResource.dotList)
                        LogUtil.e(TAG, "onCreate: 融合旧的singleHandWriting数量为"+card.cardResource.dotList.size+" HandWriting数量为: "+handWritingNumber)
                        //融合音频文件名集合
                        card.addAudioNameList(0, card.getAudioNameList())
                        //融合笔迹范围，暂不处理
                    }
                    if(obj is Page){
                        var page = obj as Page
                        pageBmpBounds.left = intent.getFloatExtra("left", 0f)
                        pageBmpBounds.right = intent.getFloatExtra("right", 0f)
                        pageBmpBounds.top = intent.getFloatExtra("top", 0f)
                        pageBmpBounds.bottom = intent.getFloatExtra("bottom", 0f)
                        bmpBounds.left = intent.getFloatExtra("superleft", 0f)
                        bmpBounds.top = intent.getFloatExtra("supertop", 0f)
                        bmpBounds.right = intent.getFloatExtra("superright", 0f)
                        bmpBounds.bottom = intent.getFloatExtra("superbottom", 0f)
//                        bmpBounds.left =
//                        var cConverter = CoordinateConverter(pageBmpBounds.left, pageBmpBounds.top, pageBmpBounds.width(), pageBmpBounds.height(), page.realWidth, page.realHeight)
//                        var cCropper = CoordinateCropper(pageBmpBounds.left, pageBmpBounds.top)
//                        var shwList = ArrayList<SingleHandWriting>()
                        var handWritingNumber = 0
                        for (singleHandWriting in page.dotList){
//                            var shw = SingleHandWriting()
//                            for (handWriting in singleHandWriting.handWritings){
//                                var hw = HandWriting(handWriting.prePeriod, handWriting.firsttime)
//                                for (stroke in handWriting.strokes){
//                                    for (dot in stroke.dots){
//                                        hw.addDot(cConverter.convertOut(page.coordinateCropper.cropIn(dot)))
//                                    }
//                                }
//                                shw.addHandWriting(hw)
//                            }
                            singleHandWriting.isNew = false
//                            shw.isNew = false
                            handWritingNumber += singleHandWriting.size()
//                            shwList.add(shw)
                        }
                        //融合笔迹点集合
                        card.addDotsList(0, page.dotList)
                        LogUtil.e(TAG, "onCreate: 融合旧的singleHandWriting数量为: "+card.dotList.size+" HandWriting数量为: "+handWritingNumber)
                        //融合音频文件名集合
                        card.addAudioNameList(0, page.audioNameList)
                        //融合笔迹范围，暂不处理
                    }

                }
            }

            override fun onFinish(page: Page?) {
                LogUtil.e(TAG, "onCreate: page == null: "+(page == null))
                if(page != null){
                    pageBmpBounds.left = intent.getFloatExtra("left", 0f)
                    pageBmpBounds.right = intent.getFloatExtra("right", 0f)
                    pageBmpBounds.top = intent.getFloatExtra("top", 0f)
                    pageBmpBounds.bottom = intent.getFloatExtra("bottom", 0f)
                    bmpBounds.left = intent.getFloatExtra("superleft", 0f)
                    bmpBounds.top = intent.getFloatExtra("supertop", 0f)
                    bmpBounds.right = intent.getFloatExtra("superright", 0f)
                    bmpBounds.bottom = intent.getFloatExtra("superbottom", 0f)
                    var handWritingNumber = 0
                    for (singleHandWriting in page.dotList){
                        singleHandWriting.isNew = false
                        handWritingNumber += singleHandWriting.size()
                    }
                    //融合笔迹点集合
                    card.addDotsList(0, page.dotList)
                    LogUtil.e(TAG, "onCreate: 融合旧的singleHandWriting数量为: "+card.dotList.size+" HandWriting数量为: "+handWritingNumber)
                    //融合音频文件名集合
                    card.addAudioNameList(0, page.audioNameList)
                    //融合笔迹范围，暂不处理
                }
            }

            override fun onError(errorMsg: String?) {
                LogUtil.e(TAG, "onCreate: 获取前代卡片数据失败")
            }

        })

        bitmap?.let {
            val rotatedBitmap: Bitmap

            val matrix = Matrix()
            matrix.postRotate(90F) // 顺时针旋转90度


            rotatedBitmap = Bitmap.createBitmap(
                it,
                0,
                0,
                it.getWidth(),
                it.getHeight(),
                matrix,
                true
            )

            //测试接口用
            imageView.setPaintSize(40F)
            imageView.setPaintTypeface(Typeface.MONOSPACE)

            //imageView.setPaintColor(Color.RED)

            imageView.setImageBitmap(it)

        }

//        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
//        }
//        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission_group.STORAGE), 2)
//        }

//        if(bitmap!=null) {
//            // 检测结果：二维码的位置信息
//            val points = ArrayList<Mat>()
//            //通过WeChatQRCodeDetector识别图片中的二维码并返回二维码的位置信息
//            val results = WeChatQRCodeDetector.detectAndDecode(bitmap, points)
//            points.forEach { mat ->
//                // 扫码结果二维码的四个点（一个矩形）
//                Log.d(TAG, "point0: ${mat[0, 0][0]}, ${mat[0, 1][0]}")
//                Log.d(TAG, "point1: ${mat[1, 0][0]}, ${mat[1, 1][0]}")
//                Log.d(TAG, "point2: ${mat[2, 0][0]}, ${mat[2, 1][0]}")
//                Log.d(TAG, "point3: ${mat[3, 0][0]}, ${mat[3, 1][0]}")
//            }
//
//            // 将 Bitmap 转换为可变的 Mat
//            val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
//            Utils.bitmapToMat(bitmap, mat)
//
//            // 创建一个红色 (BGR 格式，所以红色是 [0, 0, 255])
//            val redColor = org.opencv.core.Scalar(0.0, 0.0, 255.0)
//
//
//            for (i in 0 until points.size) {
//                val point0 = Point(points.get(i).get(0, 0)[0], points.get(i).get(0, 1)[0])
//                val point1 = Point(points.get(i).get(1, 0)[0], points.get(i).get(1, 1)[0])
//                val point2 = Point(points.get(i).get(2, 0)[0], points.get(i).get(2, 1)[0])
//                val point3 = Point(points.get(i).get(3, 0)[0], points.get(i).get(3, 1)[0])
//
//                // 在 mat 上绘制四条线连接这四个点
//                Imgproc.line(mat, point0, point1, redColor, 5)
//                Imgproc.line(mat, point1, point2, redColor, 5)
//                Imgproc.line(mat, point2, point3, redColor, 5)
//                Imgproc.line(mat, point3, point0, redColor, 5)
//            }
//            // 将绘制后的 Mat 转换回 Bitmap
//            Utils.matToBitmap(mat, bitmap)

//            val contourBitmap:Bitmap = findMaxContour(bitmap)
//            imageView.setImageBitmap(contourBitmap)

//            val width = bitmap.width
//            val height = bitmap.height
//            val matrix1 = Matrix()
//            matrix1.postScale(0.8F, 0.8F)
//            val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix1, false)
//

//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "You denied the permission WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show()
                }
            }
            2 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "You denied the permission STORAGE", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

        this.writer = Writer.getInstance().init().bindPage(card).setResponser(object : Responser() {
            override fun onLongPress(command: Command?):Boolean {
                if(!super.onLongPress(command)){
                    return false
                }

                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "长压命令", Toast.LENGTH_SHORT).show() }
                cardManager.save(card, bitmap?.let { generateCardBmp(card, it) })

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

                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "单击", Toast.LENGTH_SHORT).show() }

                return false
            }

            override fun onDoubleClick(command: Command?):Boolean {
                if(!super.onDoubleClick(command)){
                    return false
                }

                command?.handWriting?.firstDot?.let { coordinate ->
                    var mediaDot = coordinate as MediaDot
                    card.getHandWritingByCoordinate(mediaDot)?.let { handWriting ->
                        if (handWriting.penMac.equals(XmateNotesApplication.mBTMac)) {
                            if (handWriting.hasVideo()) {
                                //跳转视频播放
                                if (BaseActivity.baseActivity !is VideoNoteActivity) {
                                    LogUtil.e(
                                        TAG,
                                        "onDoubleClick: 跳转视频播放: videoId: " + handWriting.videoId + " videoTime: " + handWriting.videoTime
                                    )
                                    VideoManager.startVideoNoteActivity(
                                        this@CardProcessActivity,
                                        XueChengVideoNoteActivity::class.java,
                                        handWriting.videoId,
                                        handWriting.videoTime
                                    )
                                    return false
                                }
                            } else {
                                //跳转笔迹动态复现

                                bitmap?.let {
                                    var subRect = Rect(0, 0, it.width, it.height)
                                    BitmapCacheManager.putBitmap("cardBitmap", it)
                                    HWReplayActivity.startHWReplayActivity(
                                        this@CardProcessActivity,
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
                        }
                    }
                }

//                command?.handWriting?.firstDot?.let {coordinate->
//                    cardData.getAudioNameByCoordinate(coordinate)?.let { audioName ->
//                        LogUtil.e(TAG, "播放AudioName为：$audioName")
//                        audioManager.comPlayAudio(cardManager.getAudioAbsolutePath(cardData, audioName))
//                    }
//                }

                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "双击命令", Toast.LENGTH_SHORT).show() }

                return false
            }

            override fun onActionCommand(command: Command?):Boolean {
                return super.onActionCommand(command)
            }

            override fun onCalligraphy(command: Command?):Boolean {

                if (command != null) {
                    if(command.handWriting.isClosed){

//                        //普通书写基本延时响应
//                        writer.addResponseWorker(
//                            HandWriting.DELAY_PERIOD
//                        ) {
//                            runOnUiThread { Toast.makeText(XmateNotesApplication.context, "普通书写完毕", Toast.LENGTH_SHORT).show() }
//                        }
//
//                        writer.addResponseWorker(
//                            SingleHandWriting.SINGLE_HANDWRITING_DELAY_PERIOD
//                        ) {
//                            runOnUiThread { Toast.makeText(XmateNotesApplication.context, "单次笔迹完毕", Toast.LENGTH_SHORT).show() }}
                    }
                }

                //绘制笔迹
                imageView.drawDots(card.dotList)
                return super.onCalligraphy(command)
            }

            override fun onDelayHandWriting(command: Command?): Boolean {
                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "普通书写完毕", Toast.LENGTH_SHORT).show() }
                return super.onDelayHandWriting(command)
            }

            override fun onDelaySingleHandWriting(command: Command?): Boolean {
                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "单次笔迹完毕", Toast.LENGTH_SHORT).show() }
                return super.onDelaySingleHandWriting(command)
            }

            override fun onZhiLingKongZhi(command: Command?):Boolean {
                if (!super.onZhiLingKongZhi(command)){
                    return false
                }

                command?.handWriting?.firstDot?.let {coordinate->
                    audioManager.startRATimer(cardManager.getNewAudioAbsolutePath(coordinate, card))
                    audioRecorder = true
                }

                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "指令控制符命令", Toast.LENGTH_SHORT).show() }

                return false
            }

            override fun onSymbolicCommand(command: Command?):Boolean {
                if(!super.onSymbolicCommand(command)){
                    return false
                }

                //绘制笔迹
                imageView.drawDots(card.dotList)
//                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "手势命令", Toast.LENGTH_SHORT).show() }

                return false
            }

            override fun onDui(command: Command?): Boolean {
                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "对勾命令", Toast.LENGTH_SHORT).show() }
                return super.onDui(command)
            }

            override fun onCha(command: Command?): Boolean {
                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "叉命令", Toast.LENGTH_SHORT).show() }
                return super.onCha(command)
            }

            override fun onBanDui(command: Command?): Boolean {
                runOnUiThread { Toast.makeText(XmateNotesApplication.context, "半对命令", Toast.LENGTH_SHORT).show() }
                return super.onBanDui(command)
            }
        })


//        Thread {
//            Thread.sleep(1000)
//
//            cardManager.save(cardData, bitmap)
//
//        }.start()
    }

    override fun onPause() {
        super.onPause()
        if (audioRecorder) {
            audioRecorder = false
            audioManager.stopRATimer()
        }
    }
    fun getCoordinateConverter(left: Float, top: Float, viewWidth: Float, viewHeight: Float): CoordinateConverter {
//        bitmap?.let {
//            cardData.setRealDimensions(bitmap!!.width.toFloat(), bitmap!!.height.toFloat())
//        }
        var viewRectF = RectF()
        viewRectF.left = left
        viewRectF.top = top
        viewRectF.right = viewRectF.left + viewWidth
        viewRectF.bottom = viewRectF.top + viewHeight
        viewRectF = BitmapUtil.mapRect(pageBmpBounds, bmpBounds, viewRectF)
        return CoordinateConverter(viewRectF.left, viewRectF.top, viewRectF.width(),
            viewRectF.height(), card.realWidth, card.realHeight
        )
//        return page.setRealDimensions(viewWidth.toFloat(), viewHeight.toFloat(), resources.displayMetrics.density * 160)
    }


//    fun getCoordinateConverter(viewWidth: Int, viewHeight: Int): CoordinateConverter {
//        return cardData.setDimensions(viewWidth.toFloat(), viewHeight.toFloat(), bitmap.width.toFloat(), bitmap.height.toFloat())
////        return cardData.setDimensions(viewWidth.toFloat(), viewHeight.toFloat(), resources.displayMetrics.density * 160)
//    }

    /**
     * 生成带有笔迹的卡片图片
     */
    fun generateCardBmp(card: Card, mBitmap: Bitmap): Bitmap{
        val bitmap: Bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        var paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        val coordinateConverter = CoordinateConverter(
            bitmap.width.toFloat(),
            bitmap.height.toFloat(),
            imageView.coordinateConverter.coordinateScaler.realWidth,
            imageView.coordinateConverter.coordinateScaler.realHeight
        )
        var path = imageView.drawDots(this.card.cardResource.dotList, coordinateConverter)
        canvas.drawPath(path, paint)
        return bitmap
    }

    fun processEachDot(simpleDot: SimpleDot){
        val mediaDot = MediaDot(simpleDot)
        mediaDot.pageId = this.card.code.toLong()
        //如果正在录音，再次长压结束录音
        if (audioRecorder) {
            mediaDot.audioID = Integer.parseInt(card.getLastAudioName())
            mediaDot.color = MediaDot.DEEP_GREEN
        }

        mediaDot.penMac = XmateNotesApplication.mBTMac
        LogUtil.e(TAG, "封装MediaDot: $mediaDot")
        writer.let {
            writer.processEachDot(mediaDot)
        }
    }

    fun upload(path: String) : MutableList<String> {
        //上传素材
        val cACLatch = CountDownLatch(1)
        val fileTokenList: MutableList<String> = ArrayList()

        //上传素材
        bitableManager.uploadFile(mutableListOf(path), object : BitableManager.BitableResp() {
            override fun onFinish(file_token: String) {
                fileTokenList.add(file_token)
                cACLatch.countDown()
            }
        })

        try {
            cACLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return fileTokenList
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> {}
        }
        return true
    }

    fun findMaxContour(bitmap: Bitmap): Bitmap{
        // 将 Bitmap 转换为 OpenCV 的 Mat 对象
//        val inputMat = Mat()
//        Utils.bitmapToMat(bitmap, inputMat)
//
//        // 将 Mat 转换为灰度图像
//        val grayMat = Mat()
//        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)
//
//        val blurredMat = Mat()
//        val gausskernelSize = 7
//        Imgproc.GaussianBlur(grayMat, blurredMat, Size(gausskernelSize.toDouble(), gausskernelSize.toDouble()), 7.0)
//
//
//        // 图像增强（例如，直方图均衡化）
//        val enhancedMat = Mat()
//        Imgproc.equalizeHist(blurredMat, enhancedMat)
//
//        // 多尺度边缘检测
//        val edgesMat = Mat()
//        val thresholdLow = 50.0
//        val thresholdHigh = 150.0
//        Imgproc.Canny(enhancedMat, edgesMat, thresholdLow, thresholdHigh)
//
//        // 形态学操作（例如，闭运算）
//        val kernelSize = 3
//        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(kernelSize.toDouble(), kernelSize.toDouble()))
//        val morphedEdgesMat = Mat()
//        Imgproc.morphologyEx(edgesMat, morphedEdgesMat, Imgproc.MORPH_CLOSE, kernel)
//
//        // 自适应阈值
//        val adaptiveThresholdMat = Mat()
//        val blockSize = 11
//        val C = 2.0
//        Imgproc.adaptiveThreshold(morphedEdgesMat, adaptiveThresholdMat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, C)
//
//        //将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(adaptiveThresholdMat.cols(), adaptiveThresholdMat.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(adaptiveThresholdMat, resultBitmap)
//        return resultBitmap


//        // 使用霍夫直线变换来检测直线
//        val lines = Mat()
//        Imgproc.HoughLinesP(edgesMat, lines, 1.0, Math.PI / 180, 100, 200.0, 20.0)

//        // 在原始图像上绘制直线
//        for (i in 0 until lines.rows()) {
//            val points = lines[i, 0]
//            val pt1 = Point(points[0], points[1])
//            val pt2 = Point(points[2], points[3])
//            Imgproc.line(inputMat, pt1, pt2, Scalar(0.0, 255.0, 0.0), 2)
//        }



//        // 合并短直线成长直线
//        val mergedLines = mutableListOf<Line>()
//        for (i in 0 until lines.rows()) {
//            val points = lines[i, 0]
//            val pt1 = Point(points[0], points[1])
//            val pt2 = Point(points[2], points[3])
//            val line = Line(pt1, pt2)
//
//            var merged = false
//            for (existingLine in mergedLines) {
//                if (existingLine.isMergeable(line)) {
//                    existingLine.merge(line)
//                    merged = true
//                    break
//                }
//            }
//
//            if (!merged) {
//                mergedLines.add(line)
//            }
//        }
//
//        // 在原始图像上绘制直线
//        for (line in mergedLines) {
//            Imgproc.line(inputMat, line.pt1, line.pt2, Scalar(0.0, 255.0, 0.0), 2)
//        }
//
//        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(inputMat.cols(), inputMat.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(inputMat, resultBitmap)
//        return resultBitmap

//        // 根据直线的角度和位置来找到四边形
//        val potentialQuadrilaterals = mutableListOf<MatOfPoint>()
//        for (line in mergedLines) {
//            if (line.length() > 200.0 && isLineInRegion(line, inputMat.cols(), inputMat.rows())) {
//                potentialQuadrilaterals.add(MatOfPoint(line.pt1, line.pt2))
//            }
//        }
//
//        // 找到最大的四边形
//        var maxQuadrilateral: MatOfPoint? = null
//        var maxArea = 0.0
//        for (quadrilateral in potentialQuadrilaterals) {
//            val area = Imgproc.contourArea(quadrilateral)
//            if (area > maxArea) {
//                maxArea = area
//                maxQuadrilateral = quadrilateral
//            }
//        }
//
//        // 绘制找到的四边形
//        if (maxQuadrilateral != null) {
//            Imgproc.polylines(inputMat, listOf(maxQuadrilateral), true, Scalar(0.0, 255.0, 0.0), 2)
//        }
//
//        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(inputMat.cols(), inputMat.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(inputMat, resultBitmap)
//        return resultBitmap
        // 将 Bitmap 转换为 OpenCV 的 Mat 对象
        // 将 Bitmap 转换为 OpenCV 的 Mat 对象
        val inputMat = Mat()
        Utils.bitmapToMat(bitmap, inputMat)

        // 将 Mat 转换为灰度图像
        val grayMat = Mat()
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)

//        // 高斯模糊去噪
//        val denoisedMat = Mat()
//        Imgproc.GaussianBlur(grayMat, denoisedMat, Size(5.0, 5.0), 5.0)
//
//        // 进行边缘检测
//        val edgesMat = Mat()
//        Imgproc.Laplacian(denoisedMat, edgesMat, CvType.CV_8U)

        // 自适应阈值化进行图像二值化
        val binaryMat = Mat()
        Imgproc.adaptiveThreshold(
            grayMat,
            binaryMat,
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            11,
            2.0
        )

        // 进行边缘检测
        val edgesMat = Mat()
        Imgproc.Laplacian(binaryMat, edgesMat, CvType.CV_8U)

//        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(edgesMat.cols(), edgesMat.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(edgesMat, resultBitmap)

//        // 膨胀操作填充边缘间的空隙
//        val dilatedMat = Mat()
//        val kernelSize = 3
//        val kernel = Mat.ones(kernelSize, kernelSize, CvType.CV_8U)
//        val anchor = Point(-1.0, -1.0)
//        val iterations = 1 // 增加迭代次数
//        Imgproc.dilate(edgesMat, dilatedMat, kernel, anchor, iterations)
//
//        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(dilatedMat.cols(), dilatedMat.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(dilatedMat, resultBitmap)


        // 开运算去除边缘混叠问题
        val openedMat = Mat()
        val kernelSize = 3
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(kernelSize.toDouble(), kernelSize.toDouble()))
        Imgproc.morphologyEx(edgesMat, openedMat, Imgproc.MORPH_OPEN, kernel)

//        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(openedMat.cols(), openedMat.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(openedMat, resultBitmap)

        // 寻找图像中的轮廓
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edgesMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS)

        // 找出最大的轮廓
        var maxContour: MatOfPoint? = null
        var maxArea = 0.0
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > maxArea) {
                maxArea = area
                maxContour = contour
            }
        }

        // 在新的 Mat 上绘制最大轮廓
        val contourImage = Mat.zeros(edgesMat.size(), CvType.CV_8UC3)
        if (maxContour != null) {
            val contourList = listOf(maxContour)
            Imgproc.drawContours(contourImage, contourList, -1, Scalar(255.0, 0.0, 0.0), 2)
        }

//        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(contourImage.cols(), contourImage.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(contourImage, resultBitmap)

//         求取最小外接矩形，得到矩形的顶点坐标
        val minRect = Imgproc.minAreaRect(MatOfPoint2f(*maxContour?.toArray()))
        val vertices = Array<Point>(4) { Point() }
        minRect.points(vertices)

        // 在新的 Mat 上绘制矩形
        val trapezoidImage = Mat.zeros(edgesMat.size(), CvType.CV_8UC3)
        val trapezoidContourList = listOf(maxContour)
        Imgproc.drawContours(trapezoidImage, trapezoidContourList, -1, Scalar(255.0, 0.0, 0.0), 2)

        // 绘制梯形的顶点
        for (i in 0 until 4) {
            Imgproc.circle(trapezoidImage, vertices[i], 5, Scalar(0.0, 255.0, 0.0), -1)
        }

        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(trapezoidImage.cols(), trapezoidImage.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(trapezoidImage, resultBitmap)


//         定义原图中边框上的四个点
        val srcPoints = MatOfPoint2f(
            vertices[0], // 左上角
            vertices[1], // 右上角
            vertices[2], // 右下角
            vertices[3]  // 左下角
        )

        // 定义目标图中的边框上的四个点
        val dstWidth = bitmap.width
        val dstHeight = bitmap.height
        val dstPoints = MatOfPoint2f(
            Point(0.0, 0.0),                     // 左上角
            Point(dstWidth.toDouble(), 0.0),     // 右上角
            Point(dstWidth.toDouble(), dstHeight.toDouble()), // 右下角
            Point(0.0, dstHeight.toDouble())     // 左下角
        )

        // 计算透视变换矩阵
        val perspectiveTransform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

        // 进行透视变换
        val transformedMat = Mat()
        val inputMatResult = Mat()
        Utils.bitmapToMat(bitmap, inputMatResult)
        Imgproc.warpPerspective(inputMatResult, transformedMat, perspectiveTransform, Size(dstWidth.toDouble(), dstHeight.toDouble()))

//         将 Mat 转换回 Bitmap
        val resultBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(transformedMat, resultBitmap)

        return resultBitmap
        }

    // 判断是否为凸四边形
    private fun isConvex(approxCurve: MatOfPoint2f): Boolean {
        val points = approxCurve.toList()

        val sides = mutableListOf<Double>()
        for (i in 0 until 4) {
            val dx = points[i].x - points[(i + 1) % 4].x
            val dy = points[i].y - points[(i + 1) % 4].y
            val sideLength = sqrt(dx.pow(2) + dy.pow(2))
            sides.add(sideLength)
        }

        // 检查对角线的长度是否接近
        val diagonal1 = sqrt(sides[0].pow(2) + sides[2].pow(2))
        val diagonal2 = sqrt(sides[1].pow(2) + sides[3].pow(2))
        val diagonalDiff = abs(diagonal1 - diagonal2)

        return diagonalDiff < 100.0
    }

    // 辅助函数：对四边形的顶点按照左上、左下、右上、右下的顺序进行排序
    private fun sortQuadrilateralPoints(quadrilateral: MatOfPoint2f): List<Point> {
        // 计算四边形的中心点
        val center = Point(0.0, 0.0)
        for (point in quadrilateral.toList()) {
            center.x += point.x
            center.y += point.y
        }
        center.x /= quadrilateral.rows()
        center.y /= quadrilateral.rows()

        // 计算每个点相对于中心点的角度
        val angles = mutableListOf<Double>()
        for (point in quadrilateral.toList()) {
            val dx = point.x - center.x
            val dy = point.y - center.y
            val angle = atan2(dy, dx)
            angles.add(angle)
        }

        // 根据角度对点进行排序
        val sortedIndices = angles.indices.sortedBy { angles[it] }
        return sortedIndices.map { quadrilateral.toList()[it] }
    }

    private fun isLineInRegion(line: Line, imageWidth: Int, imageHeight: Int): Boolean {
        val regionWidth = imageWidth / 2
        val regionHeight = imageHeight / 2

        val pt1 = line.pt1
        val pt2 = line.pt2

        val pt1RegionX = if (pt1.x < regionWidth) 0 else 1
        val pt1RegionY = if (pt1.y < regionHeight) 0 else 1

        val pt2RegionX = if (pt2.x < regionWidth) 0 else 1
        val pt2RegionY = if (pt2.y < regionHeight) 0 else 1

        return (pt1RegionX != pt2RegionX) && (pt1RegionY != pt2RegionY)
    }
}

data class Line(val pt1: Point, val pt2: Point) {
    // 计算直线的长度
    private fun distance(pt1: Point, pt2: Point): Double {
        return Math.sqrt(Math.pow(pt2.x - pt1.x, 2.0) + Math.pow(pt2.y - pt1.y, 2.0))
    }
    // 计算直线的角度（弧度）
    fun angle(): Double {
        return Math.atan2(pt2.y - pt1.y, pt2.x - pt1.x)
    }
    // 计算直线与另一条直线之间的角度差异（弧度）
    fun angleDiffWith(otherLine: Line): Double {
        val angle1 = angle()
        val angle2 = otherLine.angle()
        val diff = Math.abs(angle1 - angle2)
        return if (diff > Math.PI) 2 * Math.PI - diff else diff
    }

    fun length(): Double {
        return distance(pt1, pt2)
    }
    // 计算直线到指定点的距离
    fun distanceTo(pt: Point): Double {
        val p1 = Point(pt2.x - pt1.x, pt2.y - pt1.y)
        val p2 = Point(pt.x - pt1.x, pt.y - pt1.y)
        val cross = p1.x * p2.y - p1.y * p2.x
        return Math.abs(cross) / length()
    }

    // 计算点到直线的距离
    private fun distanceToLine(point1: Point, point2: Point): Double {
        val a = point2.y - point1.y
        val b = point1.x - point2.x
        val c = point2.x * point1.y - point1.x * point2.y

        return Math.abs(a * pt1.x + b * pt1.y + c) / sqrt(a * a + b * b)
    }
    fun isMergeable(otherLine: Line): Boolean {
        // 判断两条直线的长度是否接近
        val lengthDiffThreshold = 50.0
        val length1 = length()
        val length2 = otherLine.length()
        if (Math.abs(length1 - length2) > lengthDiffThreshold) {
            return false
        }

        // 判断两条直线的角度是否接近
        val angleDiffThreshold = Math.PI / 18  // 约等于 10 度
        val angle1 = angle()
        val angle2 = otherLine.angle()
        val angleDiff = Math.abs(angle1 - angle2)
        if (angleDiff > angleDiffThreshold) {
            return false
        }

        // 判断两条直线之间的距离是否接近
        val distanceThreshold = 20.0
        val distanceToOtherLine1 = distanceToLine(otherLine.pt1, otherLine.pt2)
        val distanceToOtherLine2 = otherLine.distanceToLine(pt1, pt2)
        if (distanceToOtherLine1 > distanceThreshold || distanceToOtherLine2 > distanceThreshold) {
            return false
        }

        return true
    }
    fun merge(otherLine: Line) {
        // 计算两条直线的平均点，得到合并后的直线
        val mergedPt1 = Point((pt1.x + otherLine.pt1.x) / 2, (pt1.y + otherLine.pt1.y) / 2)
        val mergedPt2 = Point((pt2.x + otherLine.pt2.x) / 2, (pt2.y + otherLine.pt2.y) / 2)

        // 更新当前直线的端点
        pt1.x = mergedPt1.x
        pt1.y = mergedPt1.y
        pt2.x = mergedPt2.x
        pt2.y = mergedPt2.y
    }


}

object BitmapCacheManager {
    private val cacheSize: Int = (Runtime.getRuntime().maxMemory() / 8).toInt()
    private val bitmapCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String?, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun getBitmap(key: String): Bitmap? {
        return bitmapCache.get(key)
    }

    fun putBitmap(key: String, bitmap: Bitmap) {
        bitmapCache.put(key, bitmap)
    }

    //清理内存

}