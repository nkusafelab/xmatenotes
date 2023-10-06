package com.example.xmatenotes.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.OldPageManager
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.logic.presetable.LogUtil
import com.example.xmatenotes.ui.ckplayer.CkplayerActivity

class XueChengVideoNoteActivity : VideoNoteActivity() {

    companion object {
        private const val TAG = "XueChengVideoNoteActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_xue_cheng_video_note)
    }

    override fun getResponser(): Responser {
        return super.getResponser()
    }

    open inner class XueChengVideoNoteResponser : VideoNoteResponser() {
        override fun onDoubleClick(command: Command?): Boolean {
            if(!super.onDoubleClick(command)){
                return false
            }

            command?.handWriting?.firstDot?.let {coordinate->
                val pN = PageManager.getPageNumberByPageID((coordinate as MediaDot).pageID)
                val lR = excelReader.getLocalRectByXY(pN, coordinate.intX, coordinate.intY)
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