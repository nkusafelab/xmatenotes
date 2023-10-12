package com.example.xmatenotes.ui.play

import androidx.lifecycle.ViewModel
import com.example.xmatenotes.logic.model.Play

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

            return play.recordData.toString()
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