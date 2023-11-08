package com.example.xmatenotes.logic.dao

import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.PageManager
import com.example.xmatenotes.logic.manager.Storager
import com.example.xmatenotes.logic.model.Play
import java.io.File
import java.util.Objects

/**
 * 活动数据的获取与本地存储
 */
object PlayDao {

    private const val FILE_NAME = "PlayList"
    private val storager = Storager.getInstance()

    fun savePlayList(playList: List<Play>){
        storager.serializeSaveObject(absolutePath(), playList)
    }

    fun getPlayList(): List<Play>{
        return storager.serializeParseObject(absolutePath()) as List<Play>
    }

    fun isPlayListSaved(): Boolean {
        var file = File(absolutePath())
        return file.exists()
    }

    private fun absolutePath() =
        "${Objects.requireNonNull(XmateNotesApplication.context.getExternalFilesDir(null))?.absolutePath}/$FILE_NAME"
}