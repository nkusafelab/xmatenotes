package com.example.xmatenotes.ui.ckplayer

import android.os.Bundle
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.app.ax.A3
import com.example.xmatenotes.logic.dao.RoleDao
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.util.LogUtil

class XueChengVideoNoteActivity : VideoNoteActivity() {

    companion object {
        private const val TAG = "XueChengVideoNoteActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_xue_cheng_video_note)

    }

    override fun getResponser(): Responser {
        return XueChengVideoNoteResponser()
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

    open inner class XueChengVideoNoteResponser : VideoNoteResponser() {
        override fun onDoubleClick(command: Command?): Boolean {
            if(!super.onDoubleClick(command)){
                return false
            }

            command?.handWriting?.firstDot?.let {coordinate->
                var mediaDot = coordinateConverter?.convertOut(coordinate) as MediaDot ?: coordinate as MediaDot
                //资源卡播放
                var localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
                    mediaDot.pageId.toInt(), command.name, RoleDao.getRole()!!.roleName)

                localData?.let {
                    if("资源卡" == localData.areaIdentification){
                        LogUtil.e(TAG, "双击资源卡")
                        var v = videoManager.getVideoByName(localData.addInformation)
                        videoManager.addVideo(v.videoID, v.videoName)
                        seekTime(5.0f, v.videoID)
                        LogUtil.e(TAG, "视频跳转至videoID: $v.videoID")
                        LogUtil.e(TAG, "视频跳转至videoName: $v.videoName")
                        return false
                    }
                }
            }

            return true
        }

        override fun onDelayHandWriting(command: Command?): Boolean {
            if(!super.onDelayHandWriting(command)){
                return false
            }

            command?.handWriting?.firstDot?.let {coordinate->
                var mediaDot = coordinateConverter?.convertOut(coordinate) as MediaDot ?: coordinate as MediaDot

                var localData = excelManager.getLocalData(mediaDot.intX, mediaDot.intY,
                    mediaDot.pageId.toInt(), command.name, RoleDao.getRole()!!.roleName)
                //如果正在书写区答题，退出视频笔记
//                localData?.let {
//                    if("书写区" == localData.areaIdentification){
//                        finish();
//                    }
//                }
            }


            return true
        }
    }
}