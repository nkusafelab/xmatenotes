package com.example.xmatenotes.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.xmatenotes.BluetoothLEService
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.Writer
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.util.DateUtil
import com.example.xmatenotes.util.LogUtil
import com.tqltech.tqlpencomm.bean.Dot

/**
 * 支持命令识别的活动
 */
abstract class CommandActivity : BaseActivity() {

    companion object {
        private const val TAG = "CommandActivity"
    }

    private var mService: BluetoothLEService? = null //蓝牙服务

    private var dotsListener: BluetoothLEService.OnDataReceiveListener? = null

    protected lateinit var writer: Writer

    //坐标转换器
    protected var coordinateConverter: CoordinateConverter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_command)
        setContentView(getLayoutId())

        val gattServiceIntent = Intent(this, BluetoothLEService::class.java)
        val bBind = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)

        initUI()

    }

    override fun onStart() {
        super.onStart()

        this.writer = Writer.getInstance().setResponser(getResponser())
        initPage()
        initCoordinateConverter()
    }

    override fun onResume() {
        super.onResume()

        if (BluetoothLEService.isPenConnected) {
            supportActionBar!!.title = resources.getString(R.string.app_name) + "（蓝牙已连接）"
        } else {
            supportActionBar!!.title = resources.getString(R.string.app_name) + "（蓝牙未连接）"
        }
        if (mService != null) {
            mService!!.setOnDataReceiveListener(dotsListener) //添加监听器
        }

        LogUtil.e(TAG, "CommandActivity.onResume()")

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }

    protected open fun getLayoutId() : Int{
        return R.layout.activity_command
    }

    protected open fun initUI(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected open fun initPage(){
        this.writer.bindPage(null)
    }

    /**
     * 初始化点阵输入坐标转换器
     */
    protected abstract fun initCoordinateConverter()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> finish()

            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    protected open fun getResponser(): Responser {
        return CommandResponser()
    }

    protected open fun processEachDot(dot: Dot?){
        if(dot != null){
            var mediaDot = createMediaDot(dot)
            if (this.coordinateConverter != null){
                mediaDot = this.coordinateConverter!!.convertIn(mediaDot) as MediaDot
            }
            processEachDot(mediaDot)
        }
    }

    protected open fun processEachDot(mediaDot: MediaDot) {

        writer.let {
            //mediaDot 已经是真实物理坐标
            writer.processEachDot(mediaDot)
        }
    }

    protected open fun createMediaDot(dot: Dot): MediaDot {
//        LogUtil.e(TAG, "createMediaDot: dot.timelong: "+DateUtil.formatTimelong(MediaDot.reviseTimelong(dot.timelong), "yyyy年MM月dd日-HH时mm分ss秒SSS毫秒"))
        val mediaDot = MediaDot(dot)
//        mediaDot.timelong = System.currentTimeMillis() //原始timelong太早，容易早于录音开始，也可能是原始timelong不准的缘故
        mediaDot.penMac = XmateNotesApplication.mBTMac
        return mediaDot
    }

    open inner class CommandResponser: Responser() {

        override fun onActionCommand(command: Command?): Boolean {
            return super.onActionCommand(command)
        }

        override fun onSingleClick(command: Command?): Boolean {
            if(!super.onSingleClick(command)){
                return false
            }
            showToast("单击")
            return true
        }

        override fun onDoubleClick(command: Command?): Boolean {
            if(!super.onDoubleClick(command)){
                return false
            }
            showToast("双击命令")
            return true
        }

        override fun onLongPress(command: Command?): Boolean {
            if(!super.onLongPress(command)){
                return false
            }

            LogUtil.e(TAG, "onLongPress: ")
            showToast("长压命令")
            return true
        }

        override fun onSymbolicCommand(command: Command?): Boolean {
            return super.onSymbolicCommand(command)
        }

        override fun onZhiLingKongZhi(command: Command?): Boolean {
            if (!super.onZhiLingKongZhi(command)){
                return false
            }
            runOnUiThread { Toast.makeText(XmateNotesApplication.context, "指令控制符命令", Toast.LENGTH_SHORT).show() }
            return true
        }

        override fun onDui(command: Command?): Boolean {
            showToast("对勾命令")
            return super.onDui(command)
        }

        override fun onBanDui(command: Command?): Boolean {
            showToast("半对命令")
            return super.onBanDui(command)
        }

        override fun onBanBanDui(command: Command?): Boolean {
            showToast("半半对命令")
            return super.onBanBanDui(command)
        }

        override fun onBanBanBanDui(command: Command?): Boolean {
            showToast("半半半对命令")
            return super.onBanBanBanDui(command)
        }

        override fun onCha(command: Command?): Boolean {
            showToast("叉命令")
            return super.onCha(command)
        }

        override fun onWen(command: Command?): Boolean {
            showToast("问号命令")
            return super.onWen(command)
        }

        override fun onBanWen(command: Command?): Boolean {
            showToast("半问号命令")
            return super.onBanWen(command)
        }

        override fun onBanBanWen(command: Command?): Boolean {
            showToast("半半问号命令")
            return super.onBanBanWen(command)
        }

        override fun onBanBanBanWen(command: Command?): Boolean {
            showToast("半半半问号命令")
            return super.onBanBanBanWen(command)
        }

        override fun onTan(command: Command?): Boolean {
            showToast("叹号命令")
            return super.onTan(command)
        }

        override fun onBanTan(command: Command?): Boolean {
            showToast("半叹号命令")
            return super.onBanTan(command)
        }

        override fun onBanBanTan(command: Command?): Boolean {
            showToast("半半叹号命令")
            return super.onBanBanTan(command)
        }

        override fun onBanBanBanTan(command: Command?): Boolean {
            showToast("半半半叹号命令")
            return super.onBanBanBanTan(command)
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, rawBinder: IBinder?) {
            mService = (rawBinder as BluetoothLEService.LocalBinder).service
            LogUtil.d(TAG, "onServiceConnected mService= $mService")
            if (!mService!!.initialize()) {
                finish()
            }

            dotsListener = object: BluetoothLEService.OnDataReceiveListener {
                override fun onDataReceive(dot: Dot?) {
                    runOnUiThread {
                        LogUtil.e(TAG, "onDataReceive: $dot")
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