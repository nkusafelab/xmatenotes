package com.example.xmatenotes.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.xmatenotes.MLScanningAnalyzer
import com.example.xmatenotes.OpenCVQRCodeActivity
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.app.ax.A3
import com.example.xmatenotes.logic.dao.RoleDao
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.VideoManager
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.Page.QRObject
import com.example.xmatenotes.logic.model.Page.XueCheng
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.ui.ckplayer.VideoNoteActivity
import com.example.xmatenotes.util.LogUtil
import com.example.xmatenotes.ui.ckplayer.XueChengVideoNoteActivity
import com.example.xmatenotes.ui.qrcode.BitmapCacheManager
import com.example.xmatenotes.ui.qrcode.CardProcessActivity
import com.example.xmatenotes.ui.qrcode.QRResultListener
import com.example.xmatenotes.ui.qrcode.WeChatQRCodeActivity
import com.example.xmatenotes.util.BitmapUtil
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.king.mlkit.vision.camera.AnalyzeResult
import com.king.wechat.qrcode.WeChatQRCodeDetector
import org.opencv.OpenCV
import org.opencv.core.Mat
import java.lang.Exception
import kotlin.concurrent.thread

/**
 * 学程版面使用的活动
 */
class XueChengViewActivity : PageViewActivity() {

    companion object {
        private const val TAG = "XueChengViewActivity"
        private const val MARGIN = 0 //“余光”宽度，默认10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_xue_cheng)
        excelManager.init(this, "A3学程样例·纸分区坐标.xlsx")
        val dataSheet = excelManager.dataSheetMap["Video"]
        if(dataSheet != null){
            VideoManager.getInstance().init(dataSheet)
        }
    }

    override fun initCoordinateConverter() {

        //配置坐标转换器,maxX，maxY,maxrealX,maxrealY
        this.coordinateConverter = CoordinateConverter(
            A3.ABSCISSA_RANGE.toFloat(),
            A3.ORDINATE_RANGE.toFloat(),
            page.realWidth,
            page.realHeight
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when (id) {
            android.R.id.home -> finish()
            R.id.video_Notes -> {
                val videoNoteIntent = Intent(this@XueChengViewActivity, XueChengVideoNoteActivity::class.java)
                LogUtil.e(TAG, "action_ckplayer")
                videoNoteIntent.putExtra("time", 0.0f)

                LogUtil.e(TAG, "ckplayer跳转至videoID: " + 1.toString())
                videoNoteIntent.putExtra("videoID", 1)
                startActivity(videoNoteIntent)
            }

            R.id.photo_notes -> {
                LogUtil.e(TAG, "QR_scan")
                // 初始化OpenCV
                OpenCV.initAsync(this)
                // 初始化WeChatQRCodeDetector
                WeChatQRCodeDetector.init(this)
                startActivityForResult(WeChatQRCodeActivity::class.java)
            }

            else -> {}
        }

        return true
    }

    /**
     * 提交版本
     */
    override fun commit(point: SimpleDot, page: Page, bmp: Bitmap) {
        val cConverter = CoordinateConverter(
            bmp.width.toFloat(),
            bmp.height.toFloat(),
            page.realWidth,
            page.realHeight
        )
        var subPage = (page as XueCheng).getSubPageByCoordinate(point)
        subPage?.let {
            subPage.createTime = page.createTime
            var rectF = it.pageBounds
            LogUtil.e(TAG, "commit: subPage.pageBounds: $rectF")
            var leftTopDot = SimpleDot(rectF.left, rectF.top)
            var rightBottomDot = SimpleDot(rectF.right, rectF.bottom)
            leftTopDot = cConverter.convertOut(leftTopDot)
            rightBottomDot = cConverter.convertOut(rightBottomDot)
            var subRectF = RectF(leftTopDot.floatX, leftTopDot.floatY, rightBottomDot.floatX, rightBottomDot.floatY)
            //迭代新二维码
            MLScanningAnalyzer(true).analyze(bmp, object : QRResultListener {
                override fun onSuccess(result: AnalyzeResult<MutableList<String>>?) {
                    if (result != null) {
                        LogUtil.e(TAG, result.result.toString())
                        if (result is MLScanningAnalyzer.MLQRCodeAnalyzeResult) {
                            var isQRPToPagePList = ArrayList<Boolean>()
                            var qrBitMapPoints = ArrayList<org.opencv.core.Point>() //二维码四角坐标
                            var qrObjectList = ArrayList<QRObject>()
                            val gson = Gson()
                            var isQRPToPageP = true //是否将二维码四角点坐标转换为版面四角点坐标
                            result.result.forEach {
                                //将二维码字符串解析为数据对象
                                var qrO : QRObject? = null
                                try {
                                    qrO = gson.fromJson(it, QRObject::class.java)
                                    qrObjectList.add(qrO)
                                    isQRPToPagePList.add(true)
                                } catch (e: JsonSyntaxException) {
                                    LogUtil.e(WeChatQRCodeActivity.TAG, "JSON解析失败: ${e.message}")
                                    isQRPToPagePList.add(false)
                                }
                            }

                            var i = -1
                            var j = 0
                            var con = true
                            var qrRectF: RectF? = null
                            result.points?.forEach { mat ->
                                if(con){
                                    var MLPoints = MLMatToPoints(mat)
                                    qrRectF = pointsToRectF(MLPoints)
                                    i++
                                    if(subRectF.contains(qrRectF!!) && isQRPToPagePList[i]){
                                        con = false
                                    }
                                }
                            }
                            if(qrRectF != null){
                                var qrObject = qrObjectList[i]
                                page.setQrObject(qrObject)
                                page.updateRole(RoleDao.getRole())
                                page.addIteration()
                                var canvas = Canvas(bmp)
                                var paint = Paint()
                                page.toQRObject().toQRCodeBitmap(qrRectF!!)?.let {
                                    canvas.drawBitmap(it, qrRectF!!.left, qrRectF!!.top, paint)
                                }
                                pageView.postInvalidate()
                                pageManager.save(subPage, generatePageBmp(subPage, Bitmap.createBitmap(bmp, leftTopDot.intX, leftTopDot.intY, rightBottomDot.intX-leftTopDot.intX, rightBottomDot.intY-leftTopDot.intY)))

                            }

                        }

                    }
                }

                override fun onFailure(e: Exception?) {
                    LogUtil.e(TAG, "onFailure: 未识别到二维码！")
                    pageManager.save(subPage, generatePageBmp(subPage, Bitmap.createBitmap(bmp, leftTopDot.intX, leftTopDot.intY, rightBottomDot.intX-leftTopDot.intX, rightBottomDot.intY-leftTopDot.intY)))

                }
            })
        }
    }

    fun pointsToRectF(points: ArrayList<org.opencv.core.Point>):RectF{
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

    override fun getResponser(): Responser {
        return XueChengResponser()
    }

    open inner class XueChengResponser: CommandResponser() {
        override fun onLongPress(command: Command?): Boolean {
            if(!super.onLongPress(command)){
                return false
            }

//            showToast("长压命令")
            command?.handWriting?.firstDot?.let {coordinate->
                bitmap?.let {
                    thread {
                        commit(coordinate, page, it)
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

//            showToast("单击")

            return false
        }

        override fun onDoubleClick(command: Command?): Boolean {
            if(!super.onDoubleClick(command)){
                return false
            }

            command?.handWriting?.firstDot?.let {coordinate->
                var mediaDot = coordinateConverter?.convertOut(coordinate) as MediaDot
                    ?: coordinate as MediaDot
                page.getHandWritingByCoordinate(mediaDot)?.let {handWriting ->
                    if(handWriting.penMac.equals(XmateNotesApplication.mBTMac)){
                        if(handWriting.hasVideo()){
                            //跳转视频播放
                            if(baseActivity !is VideoNoteActivity){
                                LogUtil.e(TAG, "onDoubleClick: 跳转视频播放: videoId: "+handWriting.videoId+" videoTime: "+handWriting.videoTime)
                                VideoManager.startVideoNoteActivity(this@XueChengViewActivity, XueChengVideoNoteActivity::class.java, handWriting.videoId, handWriting.videoTime)
                                return false
                            }
                        } else {
                            //跳转笔迹动态复现
                            Log.e(TAG, "onDoubleClick: 跳转笔迹动态复现准备开始")
                            var localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
                                mediaDot.pageId.toInt(), command.name, RoleDao.getRole()!!.roleName)

                            localData?.let {
                                var subRect = localData.localBound
                                var superRect = Rect(0, 0, A3.ABSCISSA_RANGE, A3.ORDINATE_RANGE)
                                subRect.left = Math.max(subRect.left - MARGIN, superRect.left)
                                subRect.top = Math.max(subRect.top - MARGIN, superRect.top)
                                subRect.right = Math.min(subRect.right+ MARGIN, superRect.right)
                                subRect.bottom = Math.min(subRect.bottom + MARGIN, superRect.bottom)
                                var newSuperRect = Rect(0, 0, bitmap.width, bitmap.height)
                                var newSubRect = BitmapUtil.mapRect(subRect, superRect, newSuperRect)
                                LogUtil.e(TAG, "onDoubleClick: newSubRect: $newSubRect")
                                var subBitmap = Bitmap.createBitmap(bitmap, newSubRect.left, newSubRect.top, newSubRect.width(), newSubRect.height())
//                                LogUtil.e(TAG, "onDoubleClick: ", )
                                BitmapCacheManager.putBitmap("subBitmap", subBitmap)
                                Log.e(TAG, "onDoubleClick: 跳转笔迹动态复现准备结束")
                                HWReplayActivity.startHWReplayActivity(this@XueChengViewActivity, subRect, "subBitmap", page, it.areaCode, page.getSingleHandWritingByCoordinate(mediaDot))
                            }
//                            page.getAudioNameByCoordinate(mediaDot)?.let { audioName ->
//                                LogUtil.e(CardProcessActivity.TAG, "播放AudioName为：$audioName")
//                                audioManager.comPlayAudio(pageManager.getAudioAbsolutePath(page, audioName))
//                            }
                            return false
                        }
                    }
                }

                command?.let {
                    //资源卡跳转播放
                    var localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
                        mediaDot.pageId.toInt(), command.name, RoleDao.getRole()!!.roleName)

                    localData?.let {
                        if("资源卡" == localData.areaIdentification){
                            LogUtil.e(TAG, "双击资源卡")
                            var v = VideoManager.getInstance().getVideoByName(localData.addInformation)
                            VideoManager.getInstance().addVideo(v.videoID, v.videoName)
                            LogUtil.e(TAG, "onDoubleClick: 跳转视频播放: videoId: "+v.videoID+" videoTime: "+5.0f)
                            VideoManager.startVideoNoteActivity(this@XueChengViewActivity, XueChengVideoNoteActivity::class.java, v.videoID, 5.0f)
                            LogUtil.e(TAG, "视频跳转至videoID: $v.videoID")
                            LogUtil.e(TAG, "视频跳转至videoName: $v.videoName")
                            return false
                        }
                    }
                }

            }

//            showToast("双击命令")
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
                audioManager.startRATimer(pageManager.getNewAudioAbsolutePath(coordinate, page))
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