package com.example.xmatenotes.ui.play

import androidx.lifecycle.ViewModel
import com.example.xmatenotes.logic.model.Play

class PlayShowViewModel : ViewModel() {

    var playTitle = ""
    var enumData = ""
    var play: Play? = null

    fun setPlay(play: Play){
        this.play = play
        this.playTitle = getPlayTitle(this.play!!)
        this.enumData = getEnumText(this.play!!)
    }

    private fun getPlayTitle(play: Play): String{
        return " "
    }

    /**
     * 组合枚举字符串
     */
    private fun getEnumText(play: Play): String{

        return " "
    }
}