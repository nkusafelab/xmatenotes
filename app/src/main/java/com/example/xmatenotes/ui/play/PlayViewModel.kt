package com.example.xmatenotes.ui.play

import androidx.lifecycle.ViewModel
import com.example.xmatenotes.logic.Repository
import com.example.xmatenotes.logic.dao.PlayDao
import com.example.xmatenotes.logic.model.Play
import com.example.xmatenotes.logic.network.PlayBitableNetwork

class PlayViewModel : ViewModel() {

    val playList = ArrayList<Play>()

//    fun operateBitable(bitableReq: PlayBitableNetwork.BitableReq){
//        PlayBitableNetwork.operateBitable(bitableReq)
//    }

    fun addPlay(play: Play){
        this.playList.add(play)
    }

    fun savePlayList() = Repository.savePlayList(playList)

    fun getPlayList() = Repository.getPlayList()

    fun isPlayListSaved() = Repository.isPlayListSaved()
}