package com.example.xmatenotes.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.AudioManager
import com.example.xmatenotes.logic.manager.ExcelManager
import com.example.xmatenotes.logic.manager.ExcelReader
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.manager.PenMacManager
import com.example.xmatenotes.logic.manager.VideoManager
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.handwriting.SimpleDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.logic.network.BitableManager
import com.example.xmatenotes.ui.ckplayer.VideoNoteActivity
import com.example.xmatenotes.ui.ckplayer.XueChengVideoNoteActivity
import com.example.xmatenotes.ui.qrcode.CardProcessActivity
import com.example.xmatenotes.ui.qrcode.WeChatQRCodeActivity
import com.example.xmatenotes.util.LogUtil
import com.king.wechat.qrcode.WeChatQRCodeDetector
import org.opencv.OpenCV

/**
 * 支持点阵纸笔版面书写和命令识别的活动
 */
abstract class PageActivity : CommandActivity() {

    companion object {
        private const val TAG = "PageActivity"
        const val PAGES_TABLEID = "tblXcJERkVDkcPki"
        const val APPToken = "bascn3zrUMtRbKme8rlcyRKfDSc"
    }

//    private var mService: BluetoothLEService? = null //蓝牙服务
//
//    private var dotsListener: OnDataReceiveListener? = null

    protected var pageManager: PageManager = PageManager.getInstance()
    protected val audioManager = AudioManager.getInstance()
    protected var bitableManager = BitableManager.getInstance()
    protected val penMacManager = PenMacManager.getInstance()
    protected val excelManager = ExcelManager.getInstance()
    protected val excelReader = ExcelReader.getInstance()
//    protected lateinit var writer: Writer

//    //坐标转换器
//    protected var coordinateConverter: CoordinateConverter? = null

    /**
     * 当前Page
     */
    protected lateinit var page: Page

    /**
     * 当前PageId
     */
    protected var currentPageId: Long = -1L

    protected var audioRecorder = false //录音开关

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(getLayoutId())

//        val gattServiceIntent = Intent(this, BluetoothLEService::class.java)
//        val bBind = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)

//        initUI()
    }

    override fun onStart() {
        bitableManager.initial(null, null, APPToken).initialTable(PAGES_TABLEID)
        if(!this.writer.isBindPage){
            var pageBuffer = PageManager.getPageByPageID(PageManager.currentPageID)
            if (pageBuffer != null){
                this.writer.bindPage(pageBuffer)
            }
        }
        super.onStart()
//        this.writer = Writer.getInstance().setResponser(getResponser())
//        initPage()
//        initCoordinateConverter()
    }

    override fun onResume() {
        super.onResume()

//        if (BluetoothLEService.isPenConnected) {
//            supportActionBar!!.setTitle(resources.getString(R.string.app_name) + "（蓝牙已连接）")
//        } else {
//            supportActionBar!!.setTitle(resources.getString(R.string.app_name) + "（蓝牙未连接）")
//        }
//        if (mService != null) {
//            mService!!.setOnDataReceiveListener(dotsListener) //添加监听器
//        }
        LogUtil.e(TAG, "SmartpenActivity.onResume()")
    }

    override fun onPause() {
        super.onPause()
        if (audioRecorder) {
            audioRecorder = false
            audioManager.stopRATimer()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
//        unbindService(mServiceConnection)
    }

    override fun getLayoutId() : Int{
        return R.layout.activity_smartpen
    }

//    override fun initUI(){
//        super.initUI()
////        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//    }

    override fun initPage(){

        var mediaDot = MediaDot()
        mediaDot.pageId = PageManager.currentPageID
        switchPage(mediaDot)
    }

    override fun getResponser(): Responser {
        return PageResponser()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.smartpenmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when (id) {
            android.R.id.home -> finish()
            R.id.video_Notes -> {
                val videoNoteIntent = Intent(this@PageActivity, VideoNoteActivity::class.java)
                LogUtil.e(TAG, "action_ckplayer")
                videoNoteIntent.putExtra("time", 0.0f)

                LogUtil.e(TAG, "ckplayer跳转至videoID: " + 1.toString())
                videoNoteIntent.putExtra("videoID", 1)
                startActivity(videoNoteIntent)
            }

            R.id.photo_notes -> {
                //                QRIntent = new Intent(this, WeChatQRCodeActivity.class);
//                QRIntent = new Intent(this,ScanActivity.class);
                Log.e(TAG, "QR_scan")
                // 初始化OpenCV
                OpenCV.initAsync(XmateNotesApplication.context)
                // 初始化WeChatQRCodeDetector
                WeChatQRCodeDetector.init(XmateNotesApplication.context)
                startActivityForResult(WeChatQRCodeActivity::class.java)
            }

            else -> {}
        }

        return super.onOptionsItemSelected(item)
    }

    protected fun startActivityForResult(clazz: Class<*>) {
        val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.alpha_in, R.anim.alpha_out)
        val intent = Intent(this, clazz)
        startActivityForResult(intent, WeChatQRCodeActivity.REQUEST_CODE_QRCODE, options.toBundle())
    }

//    fun processEachDot(dot: Dot?){
//        if(dot != null){
//            var mediaDot = createMediaDot(dot)
//            if (this.coordinateConverter != null){
//                mediaDot = this.coordinateConverter!!.convertIn(mediaDot) as MediaDot
//            }
//            processEachDot(mediaDot)
//        }
//    }

    override fun processEachDot(mediaDot: MediaDot) {
        if (currentPageId != -1L){
            if(currentPageId != mediaDot.pageId){
                    switchPage(mediaDot)
                    LogUtil.e(TAG, "processEachDot: 切换PageId: $currentPageId")
            }
        }
        //如果正在录音，再次长压结束录音
        if (audioRecorder) {
            mediaDot.audioID = Integer.parseInt(page.lastAudioName)
            mediaDot.color = MediaDot.DEEP_GREEN
        }
        writer.let {
            //mediaDot 已经是真实物理坐标
            writer.processEachDot(page.coordinateCropper.cropOut(mediaDot) as MediaDot)
        }
    }

//    protected open fun createMediaDot(dot: Dot): MediaDot {
//        val mediaDot = MediaDot(dot)
//        mediaDot.timelong = System.currentTimeMillis() //原始timelong太早，容易早于录音开始，也可能是原始timelong不准的缘故
//        mediaDot.penMac = XmateNotesApplication.mBTMac
//        return mediaDot
//    }

    protected open fun switchPage(mediaDot: MediaDot) : Boolean {
        var pageBuffer = PageManager.getPageByPageID(mediaDot.pageId)
        if (pageBuffer != null){
            currentPageId = mediaDot.pageId
            pageManager.update(mediaDot)
            page = pageBuffer
            this.writer.bindPage(page)

            if(!pageManager.pagePathexists(page)){
                LogUtil.e(TAG, "switchPage: pageManager.pagePathexists(page): false")
                page.create()
                pageManager.mkdirs(page)
            }
            LogUtil.e(TAG, "switchPage: 切换pageId: $currentPageId")
            return true
        } else {
            LogUtil.e(TAG, "switchPage(): 尚未存储该页")
            return false
        }
    }

    protected open fun commit(point: SimpleDot, page: Page, bmp: Bitmap){

    }

    open inner class PageResponser: Responser() {
        override fun onLongPress(command: Command?):Boolean {
            if(!super.onLongPress(command)){
                return false
            }

            showToast("长压命令")
            pageManager.save(page, null, null)

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
                var mediaDot = coordinateConverter?.convertOut(coordinate) as MediaDot ?: coordinate as MediaDot
                page.getHandWritingByCoordinate(mediaDot)?.let {
                    if(it.penMac.equals(XmateNotesApplication.mBTMac)){
                        if(it.hasVideo()){
                            //跳转视频播放
                            if(baseActivity !is VideoNoteActivity){
                                LogUtil.e(TAG, "onDoubleClick: 跳转视频播放: videoId: "+it.videoId+" videoTime: "+it.videoTime)
                                VideoManager.startVideoNoteActivity(this@PageActivity, VideoNoteActivity::class.java, it.videoId, it.videoTime)
                            }
                        } else {
                            //跳转笔迹动态复现

                        }
                    }

                }
                page.getAudioNameByCoordinate(mediaDot)?.let { audioName ->
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

            runOnUiThread { Toast.makeText(XmateNotesApplication.context, "指令控制符命令", Toast.LENGTH_SHORT).show() }

            return false
        }

        override fun onSymbolicCommand(command: Command?):Boolean {
            if(!super.onSymbolicCommand(command)){
                return false
            }

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