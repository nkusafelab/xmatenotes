package com.example.xmatenotes.ui.play

import androidx.lifecycle.ViewModel
import com.example.xmatenotes.logic.model.Play
import com.lark.oapi.service.bitable.v1.model.*

class PlayShowViewModel : ViewModel() {

    companion object {
        @JvmStatic
        fun getPlayTitle(play: Play): String{
            return play.title
        }

        /**
         * 组合枚举字符串
         */
        @JvmStatic
        fun getEnumText(play: Play): String{

//            return "你好"
//            return play.recordData.toString()

            val appTableRecords: Array<out AppTableRecord>? = play.recordData
            var MessageFromTA = "\n"
            var A: String
            var B: String
            var C: String
            appTableRecords?.let {

                val temp1 = play.playShowReq.targetFieldList.get(0); //小组编号

                val temp2 = play.playShowReq.targetFieldList.get(1); //打点时间

                val temp3 = play.playShowReq.targetFieldList.get(2);  //小组组型

                for (appTableRecord in appTableRecords) {
                    A = appTableRecord.fields[temp1].toString()
                    B = appTableRecord.fields[temp2].toString()
                    val newAB = A.replace("text=", "赣")
                    for (i in 0 until newAB.length) {
                        if (newAB[i] == '赣') {
                            var h = i
                            while (newAB[h] != ',') {
                                h++
                            }
                            A = newAB.substring(i + 1, h)
                            break
                        }
                    }
                    C = if (appTableRecord.fields[temp3] == null) {
                        "未填写"
                    } else {
                        appTableRecord.fields[temp3].toString()
                    }

                    MessageFromTA =MessageFromTA+temp1+": "+A +"\n"+ temp2+": "+B+" "+temp3+": "+C + "\n"+"\n";

                }
            }



            return MessageFromTA
        }
    }

    var playTitle = ""
    var enumData = ""
    var play: Play? = null

    fun savePlay(play: Play){
        this.play = play
        this.playTitle = getPlayTitle(this.play!!)
        this.enumData = getEnumText(this.play!!)
    }

}