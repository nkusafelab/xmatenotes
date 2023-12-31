package com.example.xmatenotes.logic.network

import android.util.Log
import com.example.xmatenotes.logic.manager.LocalData
import com.example.xmatenotes.logic.network.BitableManager.BitableResp
import com.example.xmatenotes.util.LogUtil
import java.util.ArrayList

/**
 * 解析飞书多维表格请求并获取网络数据
 */
object PlayBitableNetwork {

    private const val TAG = "PlayBitableNetwork"
    private const val SREACH = "读取"
    private const val UPDATE = "写入"
    private const val CREATE = "新增"
    private var bitableManager = BitableManager.getInstance()

    /**
     * 从LocalData中解析出BitableReq
     */
    fun parseLocalData(localData: LocalData): BitableReq?{
        LogUtil.e(TAG, "parseLocalData")
        return BitableReq().parseLocalData(localData)
//        BitableReq().parseLocalData(localData)?.let { operateBitable(it) }
    }

    /**
     *
     * @param callBack 回调接口，至少实现onFinish(AppTableRecord[] appTableRecords)(找到的所有记录)、onFinish(AppTableRecord appTableRecord)(更新记录)和onError(String errorMsg)
     */
    fun operateBitable(bitableReq: BitableReq, callBack: BitableResp){
//        bitableManager.initial(bitableReq.tableId)
        LogUtil.e(TAG, "operateBitable")
        var type = bitableReq.type
        if(SREACH == type){
            search(bitableReq, callBack)
        } else if(UPDATE == type){
            update(bitableReq, callBack)
        } else if(CREATE == type){
            create(bitableReq)
        }
    }

    private fun search(bitableReq: BitableReq, callBack: BitableManager.BitableResp){
        LogUtil.e(TAG, "search")
        bitableManager.searchAppTableRecords(bitableReq.tableId, null, bitableReq.filter, callBack)
    }

    private fun update(bitableReq: BitableReq, callBack: BitableResp){
        LogUtil.e(TAG, "update")
        var map = HashMap<String, String>()
        for (targetField in bitableReq.targetFieldList){
            map[targetField] = bitableReq.data.toString()
        }
        bitableManager.updateAppTableRecord(bitableReq.tableId,
            map as Map<String, Any>?, bitableReq.filter, callBack)
    }

    private fun create(bitableReq: BitableReq){

    }


    /**
     * 飞书请求类
     */
    class BitableReq {

        companion object {
            private const val TAG = "BitableReq"
            private const val TABLE_ID = "目标tableId"
            private const val FILTER = "筛选条件"
            private const val OBJECT_FIELD = "目标字段"
            private const val DATA = "写入数据"
            private const val TYPE = "操作类型"
        }
        /**
         * 目标tableId
         */
        var tableId: String? = null

        /**
         * 请求类型: 读取、写入、新增
         */
        var type: String? = null

        /**
         * 写入数据
         */
        var data: String? = null

        /**
         * 目标字段列表
         */
        var targetFieldList: MutableList<String> = ArrayList<String>()

        /**
         * 飞书筛选表达式
         */
        var filter: String? = null

//        val map: Map<String, String>?
//
//        /**
//         * 获取写入字段和数据
//         * @return
//         */
//        get() {
//            val map: MutableMap<String, String> = HashMap()
//            if (data != null && !targetFieldList.isEmpty()) {
//                for (targetField in targetFieldList) {
//                    map[targetField] = data!!
//                }
//                return map
//            }
//            LogUtil.e(TAG, "getMap(): 目标字段或写入数据为空！")
//            return null
//        }

        /**
         * 从LocalData中解析出BitableReq
         */
        fun parseLocalData(localData: LocalData): BitableReq?{
            var obj: Any? = null
            obj = localData.getFieldValue(TABLE_ID)
            LogUtil.e(TAG, "parseLocalData: TABLE_ID obj: $obj")
            if(obj != null){
                this.tableId = obj as String
            } else {
                return null
            }

            obj = localData.getFieldValue(TYPE)
            LogUtil.e(TAG, "parseLocalData: TYPE obj: $obj")
            if(obj != null){
                this.type = obj as String
            } else {
                return null
            }

            obj = localData.getFieldValue(FILTER)
            LogUtil.e(TAG, "parseLocalData: FILTER obj: $obj")
            if(obj != null){
                this.filter = obj as String
            } else {
                return null
            }

            obj = localData.getFieldValue(DATA)
            LogUtil.e(TAG, "parseLocalData: DATA obj: $obj")
            if(obj != null){
                this.data = obj as String
            } else {

            }

            obj = localData.getFieldValue(OBJECT_FIELD)
            LogUtil.e(TAG, "parseLocalData: OBJECT_FIELD obj: $obj")
            if(obj != null){
                if(obj is String){
                    this.targetFieldList.add(obj as String)
                } else if(obj is List<*>){
                    this.targetFieldList.addAll(obj as List<String>)
                }

//                this.targetFieldList = (obj as Array<*>).map { it.toString() }
                LogUtil.e(TAG, "parseLocalData: this.targetFieldList: "+this.targetFieldList)
            } else {
                return null
            }

            return this
        }

        fun setTableId(tableId: String?): BitableReq {
            this.tableId = tableId
            return this
        }

        fun setType(type: String?): BitableReq {
            this.type = type
            return this
        }

        fun setData(data: String?): BitableReq {
            this.data = data
            return this
        }

        fun setTargetFieldList(targetFieldList: MutableList<String>): BitableReq {
            this.targetFieldList = targetFieldList
            return this
        }

        fun setFilter(filter: String?): BitableReq {
            this.filter = filter
            return this
        }

        override fun toString(): String {
            return "BitableReq(tableId=$tableId, type=$type, data=$data, targetFieldList=$targetFieldList, filter=$filter)"
        }

    }

}