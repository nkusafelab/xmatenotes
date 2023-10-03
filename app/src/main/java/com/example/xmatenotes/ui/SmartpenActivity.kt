package com.example.xmatenotes.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.example.xmatenotes.BluetoothLEService
import com.example.xmatenotes.BluetoothLEService.LocalBinder
import com.example.xmatenotes.BluetoothLEService.OnDataReceiveListener
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.manager.PenMacManager
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.presetable.LogUtil
import com.tqltech.tqlpencomm.bean.Dot

/**
 * 支持点阵纸笔书写的活动
 */
open class SmartpenActivity : PageActivity() {

    companion object {
        private const val TAG = "SmartpenActivity"
    }

    private var mService: BluetoothLEService? = null //蓝牙服务

    private var dotsListener: OnDataReceiveListener? = null

    protected val penMacManager = PenMacManager.getInstance()

    protected var currentPageId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_smartpen)

        val gattServiceIntent = Intent(this, BluetoothLEService::class.java)
        val bBind = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onStart() {
        super.onStart()

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


    override fun initPage(){
        switchPage(0)
    }

    fun processEachDot(dot: Dot?){
        LogUtil.e(TAG, dot.toString())
        if(dot != null){
            LogUtil.e(TAG, dot.toString())
            var mediaDot = createMediaDot(dot)
            pageManager.update(mediaDot)
            if (currentPageId != -1){
                if(currentPageId != dot.PageID){
                    switchPage(dot.PageID)
                }
            }
            processEachDot(mediaDot)
        }

    }

    fun createMediaDot(dot: Dot?): MediaDot {
        val mediaDot = MediaDot(dot)
        mediaDot.timelong = System.currentTimeMillis() //原始timelong太早，容易早于录音开始，也可能是原始timelong不准的缘故
        mediaDot.videoTime = XmateNotesApplication.DEFAULT_FLOAT
        mediaDot.videoID = XmateNotesApplication.DEFAULT_INT
        mediaDot.audioID = XmateNotesApplication.DEFAULT_INT
        //如果正在录音，再次长压结束录音
        if (audioRecorder) {
            mediaDot.audioID = Integer.parseInt(page.lastAudioName)
            mediaDot.color = MediaDot.DEEP_GREEN
        }
        mediaDot.penMac = XmateNotesApplication.mBTMac
        return mediaDot
    }

    fun switchPage(pageId: Int) {
        var pageBuffer = PageManager.getPageByPageID(pageId)
        if (pageBuffer != null){
            currentPageId = pageId
            PageManager.currentPageID = pageId
            bitmap = getViewBitmap(pageId)
            pageView.setImageBitmap(bitmap)
            page = pageBuffer
            this.writer.bindPage(page)
            pageView.post {
                pageView.drawDots(page.dotList, page.coordinateCropper)
            }
            if(!pageManager.pagePathexists(page)){
                page.create()
                pageManager.mkdirs(page)
            }
        } else {
            LogUtil.e(TAG, "switchPage(): 尚未存储该页")
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