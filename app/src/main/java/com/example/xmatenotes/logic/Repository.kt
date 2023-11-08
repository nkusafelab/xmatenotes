package com.example.xmatenotes.logic

import com.example.xmatenotes.logic.dao.PlayDao
import com.example.xmatenotes.logic.model.Play

object Repository {

    fun savePlayList(playList: List<Play>) = PlayDao.savePlayList(playList)

    fun getPlayList() = PlayDao.getPlayList()

    fun isPlayListSaved() = PlayDao.isPlayListSaved()
}