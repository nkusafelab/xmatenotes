package com.example.xmatenotes.ui.play

import android.os.Bundle
import android.util.Log
import com.example.xmatenotes.R
import com.example.xmatenotes.logic.dao.RoleDao
import com.example.xmatenotes.logic.manager.ExcelManager
import com.example.xmatenotes.logic.manager.LocalData
import com.example.xmatenotes.logic.manager.PenMacManager
import com.example.xmatenotes.logic.model.Play
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.logic.network.BitableManager
import com.example.xmatenotes.logic.network.PlayBitableNetwork
import com.example.xmatenotes.ui.CommandActivity
import com.example.xmatenotes.util.LogUtil
import com.lark.oapi.service.bitable.v1.model.AppTableRecord

/**
 * “活动”管理活动
 */
open class PlayActivity : CommandActivity() {

    companion object {
        private const val TAG = "PlayActivity"
    }

    protected var bitableManager = BitableManager.getInstance()
    protected val penMacManager = PenMacManager.getInstance()
    protected val excelManager = ExcelManager.getInstance()
    private lateinit var fragment: PlayFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragment = supportFragmentManager.findFragmentById(R.id.playFragment) as PlayFragment
        excelManager.init(this, "A0填写索引表.xlsx")
    }

    override fun initCoordinateConverter() {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_play
    }

    fun response(command: Command?){
        command?.handWriting?.firstDot?.let { coordinate ->
            if (coordinate is MediaDot) {
                Log.e(TAG, "response: coordinate: $coordinate")
                var mediaDot = coordinate as MediaDot
                var localData: LocalData? = null
                if(RoleDao.getRole() != null){
                    Log.e(TAG, "response: RoleDao.getRole()!!.roleName: "+RoleDao.getRole()!!.roleName)
                    localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
                        mediaDot.pageID.toInt(), command.name, RoleDao.getRole()!!.roleName)
                } else {
                    Log.e(TAG, "response: roleName: null")
                    localData = excelManager.getLocalData(
                        mediaDot.intX, mediaDot.intY,
                        mediaDot.pageID.toInt(), command.name, null
                    )
                }
                localData?.let { localData ->
                    Log.e(TAG, "response: localData: $localData")
                    var btR = PlayBitableNetwork.parseLocalData(localData)
                    btR?.let {
                        Log.e(TAG, "response: BitableReq: $btR")
                        PlayBitableNetwork.operateBitable(btR, object :
                            BitableManager.BitableResp() {
                            override fun onFinish(appTableRecord: AppTableRecord?) {
                                super.onFinish(appTableRecord)
                                Log.e(TAG, "onFinish: 写入数据")
                            }

                            override fun onFinish(appTableRecords: Array<out AppTableRecord>?) {
                                super.onFinish(appTableRecords)
                                Log.e(TAG, "onFinish: 生成活动")
                                //生成活动
                                runOnUiThread {
                                    var play = Play.create(appTableRecords, localData, it)
                                    fragment.addPlay(play)
                                }

                            }

                            override fun onError(errorMsg: String?) {
                                super.onError(errorMsg)
                            }
                        })
                    }
                }
            }
        }
    }

    override fun getResponser(): Responser {
        return PlayResponser()
    }
    open inner class PlayResponser: CommandResponser() {

        override fun onLongPress(command: Command?): Boolean {
            if(!super.onLongPress(command)){
                return false
            }

            LogUtil.e(TAG, "onLongPress: ")
            //查表
            response(command)
//            command?.handWriting?.firstDot?.let {coordinate->
//                Log.e(TAG, "onLongPress: A")
//                if (coordinate is MediaDot){
//                    Log.e(TAG, "onLongPress: B")
//                    var mediaDot = coordinate as MediaDot
//                    var localData: LocalData? = null
//                    if(RoleDao.getRole() != null){
//                        Log.e(TAG, "onLongPress: C")
//                        localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
//                            mediaDot.pageID.toInt(), command.name, RoleDao.getRole()!!.roleName)
//                    } else {
//                        Log.e(TAG, "onLongPress: D")
//                        localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
//                            mediaDot.pageID.toInt(), command.name, null)
//                    }
//                    localData?.let {localData ->
//                        Log.e(TAG, "onLongPress: E")
//                        var btR = PlayBitableNetwork.parseLocalData(localData)
//                        btR?.let {
//                            Log.e(TAG, "onLongPress: F")
//                            PlayBitableNetwork.operateBitable(btR, object :
//                                BitableManager.BitableResp() {
//                                override fun onFinish(appTableRecord: AppTableRecord?) {
//                                    super.onFinish(appTableRecord)
//                                }
//
//                                override fun onFinish(appTableRecords: Array<out AppTableRecord>?) {
//                                    super.onFinish(appTableRecords)
//                                    Log.e(TAG, "onLongPress: G")
//                                     //生成活动
//                                    runOnUiThread {
//                                        var play = Play.create(appTableRecords, localData, it)
//                                        fragment.addPlay(play)
//                                    }
//
//                                }
//
//                                override fun onError(errorMsg: String?) {
//                                    super.onError(errorMsg)
//                                }
//                            })
//                        }
//                    }
//                }
//
//            }
            //请求飞书

            //生成活动

            //活动加入列表更新


            return true
        }

        override fun onDui(command: Command?): Boolean {
            response(command)
            return super.onDui(command)
        }

        override fun onBanDui(command: Command?): Boolean {
            response(command)
            return super.onBanDui(command)
        }

        override fun onBanBanDui(command: Command?): Boolean {
            response(command)
            return super.onBanBanDui(command)
        }

        override fun onBanBanBanDui(command: Command?): Boolean {
            response(command)
            return super.onBanBanBanDui(command)
        }

        override fun onCha(command: Command?): Boolean {
            response(command)
            return super.onCha(command)
        }
    }

}