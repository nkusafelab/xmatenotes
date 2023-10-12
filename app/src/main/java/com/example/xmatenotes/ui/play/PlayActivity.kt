package com.example.xmatenotes.ui.play

import android.os.Bundle
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.ExcelManager
import com.example.xmatenotes.logic.manager.PenMacManager
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.logic.network.BitableManager
import com.example.xmatenotes.ui.CommandActivity

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
                    if(XmateNotesApplication.role != null){
                        var localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
                            mediaDot.pageID.toInt(), command.name, XmateNotesApplication.role.roleName)
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