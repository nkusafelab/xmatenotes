package com.example.xmatenotes.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.example.xmatenotes.BluetoothLEService
import com.example.xmatenotes.BluetoothLEService.LocalBinder
import com.example.xmatenotes.BluetoothLEService.OnDataReceiveListener
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.AudioManager
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.manager.PenMacManager
import com.example.xmatenotes.logic.manager.VideoManager
import com.example.xmatenotes.logic.manager.Writer
import com.example.xmatenotes.logic.model.Page.Page
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.logic.network.BitableManager
import com.example.xmatenotes.logic.presetable.LogUtil
import com.example.xmatenotes.ui.qrcode.CardProcessActivity
import com.tqltech.tqlpencomm.bean.Dot

/**
 * 支持点阵纸笔书写的活动
 */
open class SmartpenActivity : BaseActivity() {

    companion object {
        private const val TAG = "SmartpenActivity"
    }

    private var mService: BluetoothLEService? = null //蓝牙服务

    private var dotsListener: OnDataReceiveListener? = null

    protected var pageManager = PageManager.getInstance()
    protected val audioManager = AudioManager.getInstance()
    protected var bitableManager = BitableManager.getInstance()
    protected val penMacManager = PenMacManager.getInstance()
    protected lateinit var writer: Writer

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
        setContentView(getLayoutId())

        val gattServiceIntent = Intent(this, BluetoothLEService::class.java)
        val bBind = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)

        initUI()
    }

    override fun onStart() {
        super.onStart()

        this.writer = Writer.getInstance().setResponser(getResponsor())
        initPage()
    }

    override fun onResume() {
        super.onResume()

        if (BluetoothLEService.isPenConnected) {
            supportActionBar!!.setTitle(resources.getString(R.string.app_name) + "（蓝牙已连接）")
        } else {
            supportActionBar!!.setTitle(resources.getString(R.string.app_name) + "（蓝牙未连接）")
        }
        if (mService != null) {
            mService!!.setOnDataReceiveListener(dotsListener) //添加监听器
        }
        LogUtil.e(TAG, "SmartpenActivity.onResume()")
    }

    override fun onPause() {
        super.onPause()
        if (audioRecorder) {
            audioRecorder = false
            audioManager.stopRATimer()
        }
    }

    protected open fun getLayoutId() : Int{
        return R.layout.activity_smartpen
    }

    protected open fun initUI(){

    }

    protected open fun initPage(){
        var mediaDot = MediaDot()
        mediaDot.pageID = 0L
        switchPage(mediaDot)
    }

    protected open fun getResponsor(): Responser {
        return SmartPenResponser()
    }

    fun processEachDot(dot: Dot?){
        if(dot != null){
            LogUtil.e(TAG, dot.toString())
            processEachDot(createMediaDot(dot))
        }
    }

    protected open fun processEachDot(mediaDot: MediaDot) {
        if (currentPageId != -1L){
            if(currentPageId != mediaDot.pageID){
                switchPage(mediaDot)
            }
        }
        //如果正在录音，再次长压结束录音
        if (audioRecorder) {
            mediaDot.audioID = Integer.parseInt(page.lastAudioName)
            mediaDot.color = MediaDot.DEEP_GREEN
        }
        writer.let {
            writer.processEachDot(page.coordinateCropper.cropOut(mediaDot) as MediaDot)
        }
    }

    fun createMediaDot(dot: Dot?): MediaDot {
        val mediaDot = MediaDot(dot)
        mediaDot.timelong = System.currentTimeMillis() //原始timelong太早，容易早于录音开始，也可能是原始timelong不准的缘故
        mediaDot.penMac = XmateNotesApplication.mBTMac
        return mediaDot
    }

    protected open fun switchPage(mediaDot: MediaDot) : Boolean {
        var pageBuffer = PageManager.getPageByPageID(mediaDot.pageID)
        if (pageBuffer != null){
            currentPageId = mediaDot.pageID
            pageManager.update(mediaDot)
            page = pageBuffer
            this.writer.unBindPage()
            this.writer.bindPage(page)

            if(!pageManager.pagePathexists(page)){
                page.create()
                pageManager.mkdirs(page)
            }
            return true
        } else {
            LogUtil.e(TAG, "switchPage(): 尚未存储该页")
            return false
        }
    }

    open inner class SmartPenResponser: Responser() {
        override fun onLongPress(command: Command?):Boolean {
            if(!super.onLongPress(command)){
                return false
            }

            showToast("长压命令")
            pageManager.save(page, null)

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
                        VideoManager.startVideoNoteActivity(this@SmartpenActivity, it.videoId, it.videoTime);
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


    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, rawBinder: IBinder?) {
            mService = (rawBinder as LocalBinder).service
            LogUtil.d(TAG, "onServiceConnected mService= $mService")
            if (!mService!!.initialize()) {
                finish()
            }

            dotsListener = object: BluetoothLEService.OnDataReceiveListener {
                override fun onDataReceive(dot: Dot?) {
                    runOnUiThread {
                        LogUtil.e(TAG, dot.toString())
                        processEachDot(dot)
                    }
                }

                override fun onOfflineDataReceive(dot: Dot?) {
                    runOnUiThread { processEachDot(dot) }
                }

                override fun onFinishedOfflineDown(success: Boolean) {

                }

                override fun onOfflineDataNum(num: Int) {

                }

                override fun onReceiveOIDSize(OIDSize: Int) {

                }

                override fun onReceiveOfflineProgress(i: Int) {

                }

                override fun onDownloadOfflineProgress(i: Int) {

                }

                override fun onReceivePenLED(color: Byte) {

                }

                override fun onOfflineDataNumCmdResult(success: Boolean) {

                }

                override fun onDownOfflineDataCmdResult(success: Boolean) {

                }

                override fun onWriteCmdResult(code: Int) {

                }

                override fun onReceivePenType(type: Int) {

                }

            }
            mService!!.setOnDataReceiveListener(dotsListener)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            LogUtil.d(TAG, "onServiceDisconnected")
        }
    }
}