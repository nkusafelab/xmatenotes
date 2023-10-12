package com.example.xmatenotes.logic.model

import com.example.xmatenotes.logic.manager.PlayShowReq
import com.lark.oapi.service.bitable.v1.model.AppTableRecord
import java.io.Serializable

/**
 * 活动数据
 */
data class Play(val title: String, val initialTime: Long, val role: String, val lifeDuration: Long = LIFE_DURATION, var remainingTime: Long, var recordData: Array<AppTableRecord>, val playShowReq: PlayShowReq): Serializable {

    companion object {
        private const val serialVersionUID: Long = 2017967446111988058L
        private const val LIFE_DURATION = 7*24*60*60*10L
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Play

        if (title != other.title) return false
        if (initialTime != other.initialTime) return false
        if (role != other.role) return false
        if (lifeDuration != other.lifeDuration) return false
        if (remainingTime != other.remainingTime) return false
        if (!recordData.contentEquals(other.recordData)) return false
        if (playShowReq != other.playShowReq) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + initialTime.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + lifeDuration.hashCode()
        result = 31 * result + remainingTime.hashCode()
        result = 31 * result + recordData.contentHashCode()
        result = 31 * result + playShowReq.hashCode()
        return result
    }
}
