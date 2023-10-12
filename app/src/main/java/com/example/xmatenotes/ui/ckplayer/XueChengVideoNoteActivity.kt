package com.example.xmatenotes.ui.ckplayer

import android.os.Bundle
import com.example.xmatenotes.app.ax.A3
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
                var mediaDot = coordinateConverter?.convertOut(coordinate) as MediaDot
                val pN = PageManager.getPageNumberByPageID(mediaDot.pageID)
                val lR = excelReader.getLocalRectByXY(pN, mediaDot.intX, mediaDot.intY)
                if (lR != null) {
                    LogUtil.e(TAG, "局域编码: " + lR.localCode)
                    if ("资源卡" == lR.localName) {
                        LogUtil.e(TAG, "双击资源卡")

                        val videoID = lR.videoIDByAddInf
                        val videoName = lR.videoNameByAddInf
                        videoManager.addVideo(videoID, videoName)
                        seekTime(5.0f, videoID)
                        LogUtil.e(TAG, "ckplayer跳转至videoID: $videoID")
                        LogUtil.e(TAG, "ckplayer跳转至videoName: $videoName")
                    }
                }
            }

            return true
        }
    }
}