package com.example.xmatenotes.ui.play

import android.os.Bundle
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
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

    override fun getResponser(): Responser {
        return PlayResponser()
    }
    open inner class PlayResponser: CommandResponser() {

        override fun onLongPress(command: Command?): Boolean {
            if(!super.onLongPress(command)){
                return false
            }

            //查表
            command?.handWriting?.firstDot?.let {coordinate->
                if (coordinate is MediaDot){
                    var mediaDot = coordinate as MediaDot
                    var localData: LocalData? = null
                    if(XmateNotesApplication.role != null){
                        localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
                            mediaDot.pageID.toInt(), command.name, XmateNotesApplication.role.roleName)
                    } else {
                        localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
                            mediaDot.pageID.toInt(), command.name, null)
                    }
                    localData?.let {localData ->
                        var btR = PlayBitableNetwork.parseLocalData(localData)
                        btR?.let {
                            PlayBitableNetwork.operateBitable(btR, object :
                                BitableManager.BitableResp() {
                                override fun onFinish(appTableRecord: AppTableRecord?) {
                                    super.onFinish(appTableRecord)
                                }

                                override fun onFinish(appTableRecords: Array<out AppTableRecord>?) {
                                    super.onFinish(appTableRecords)
                                     //生成活动
                                    var play = Play.create(appTableRecords, localData, it)
                                    fragment.addPlay(play)
                                }

                                override fun onError(errorMsg: String?) {
                                    super.onError(errorMsg)
                                }
                            })
                        }
                    }
                }

            }
            //请求飞书

            //生成活动

            //活动加入列表更新


            return true
        }

        override fun onDui(command: Command?): Boolean {
            return super.onDui(command)
        }

        override fun onBanDui(command: Command?): Boolean {
            return super.onBanDui(command)
        }

        override fun onBanBanDui(command: Command?): Boolean {
            return super.onBanBanDui(command)
        }

        override fun onBanBanBanDui(command: Command?): Boolean {
            return super.onBanBanBanDui(command)
        }

        override fun onCha(command: Command?): Boolean {
            return super.onCha(command)
        }
    }

}