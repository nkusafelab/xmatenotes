package com.example.xmatenotes.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.app.ax.A3
import com.example.xmatenotes.logic.dao.RoleDao
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.ExcelManager
import com.example.xmatenotes.logic.manager.VideoManager
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.Page.XueCheng
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.ui.ckplayer.VideoNoteActivity
import com.example.xmatenotes.util.LogUtil
import com.example.xmatenotes.ui.ckplayer.XueChengVideoNoteActivity
import com.example.xmatenotes.ui.qrcode.CardProcessActivity
import com.example.xmatenotes.ui.qrcode.WeChatQRCodeActivity
import com.king.wechat.qrcode.WeChatQRCodeDetector
import org.opencv.OpenCV

/**
 * 学程版面使用的活动
 */
class XueChengViewActivity : PageViewActivity() {

    companion object {
        private const val TAG = "XueChengViewActivity"
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
            var rectF = it.pageBounds
            var leftTopDot = SimpleDot(rectF.left, rectF.top)
            var rightBottomDot = SimpleDot(rectF.right, rectF.bottom)
            leftTopDot = cConverter.convertOut(leftTopDot)
            rightBottomDot = cConverter.convertOut(rightBottomDot)
            pageManager.save(subPage, generatePageBmp(subPage, Bitmap.createBitmap(bmp, leftTopDot.intX, leftTopDot.intY, rightBottomDot.intX-leftTopDot.intX, rightBottomDot.intY-leftTopDot.intY)))
        }
    }

    override fun getResponser(): Responser {
        return XueChengResponser()
    }

    open inner class XueChengResponser: Responser() {
        override fun onLongPress(command: Command?): Boolean {
            if(!super.onLongPress(command)){
                return false
            }

            showToast("长压命令")
            command?.handWriting?.firstDot?.let {coordinate->
                bitmap?.let { commit(coordinate, page, it) }
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

            showToast("单击")

            return false
        }

        override fun onDoubleClick(command: Command?): Boolean {
            if(!super.onDoubleClick(command)){
                return false
            }

            command?.handWriting?.firstDot?.let {coordinate->
                var mediaDot = coordinateConverter?.convertOut(coordinate) as MediaDot
                    ?: coordinate as MediaDot
                page.getHandWritingByCoordinate(mediaDot)?.let {
                    if(it.penMac.equals(XmateNotesApplication.mBTMac)){
                        if(it.hasVideo()){
                            //跳转视频播放
                            if(baseActivity !is VideoNoteActivity){
                                LogUtil.e(TAG, "onDoubleClick: 跳转视频播放: videoId: "+it.videoId+" videoTime: "+it.videoTime)
                                VideoManager.startVideoNoteActivity(this@XueChengViewActivity, XueChengVideoNoteActivity::class.java, it.videoId, it.videoTime)
                                return false
                            }
                        } else {
                            //跳转笔迹动态复现
                            page.getAudioNameByCoordinate(mediaDot)?.let { audioName ->
                                LogUtil.e(CardProcessActivity.TAG, "播放AudioName为：$audioName")
                                audioManager.comPlayAudio(pageManager.getAudioAbsolutePath(page, audioName))
                            }
                            return false
                        }
                    }
                }
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

            showToast("双击命令")
            return true
        }

        override fun onCalligraphy(command: Command?): Boolean {
            //绘制笔迹
//            pageView.post { pageView.drawDot(page.lastDot as MediaDot, page.coordinateCropper) }
            //绘制笔迹
            updatePageViewDots()

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
            updatePageViewDots()

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