package com.example.xmatenotes.logic.dao

import com.example.xmatenotes.logic.manager.LocalData

/**
 * 访问预置excel的接口
 */
interface IExcelDao {

    fun getLocalData(x: Int, y: Int, pageId: Int, command: String?, roleName: String?): LocalData?
}