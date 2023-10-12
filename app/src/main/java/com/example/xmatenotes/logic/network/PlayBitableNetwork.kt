package com.example.xmatenotes.logic.network

import com.example.xmatenotes.logic.manager.LocalData
import com.example.xmatenotes.util.LogUtil

/**
 * 解析飞书多维表格请求并获取网络数据
 */
object PlayBitableNetwork {

    private const val SREACH = "读取"
    private const val UPDATE = "写入"
    private const val CREATE = "新增"

    /**
     * 从LocalData中解析出BitableReq
     */
    fun parseLocalData(localData: LocalData){

    }

    fun operateBitable(bitableReq: BitableReq){

    }

    private fun search(){

    }

    private fun update(){

    }

    private fun create(){

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
            private set

        /**
         * 请求类型: 读取、写入、新增
         */
        var type: String? = null
            private set

        /**
         * 写入数据
         */
        var data: String? = null
            private set

        /**
         * 目标字段列表
         */
        var targetFieldList: List<String> = ArrayList()
            private set

        /**
         * 飞书筛选表达式
         */
        var filter: String? = null
            private set
        val map: Map<String, String>?
            /**
             * 获取写入字段和数据
             * @return
             */
            get() {
                val map: MutableMap<String, String> = HashMap()
                if (data != null && !targetFieldList.isEmpty()) {
                    for (targetField in targetFieldList) {
                        map[targetField] = data!!
                    }
                    return map
                }
                LogUtil.e(TAG, "getMap(): 目标字段或写入数据为空！")
                return null
            }

        /**
         * 从LocalData中解析出BitableReq
         */
        fun parseLocalData(localData: LocalData): BitableReq{
            this.tableId = localData.getFieldValue(TABLE_ID).toString()
            this.type = localData.getFieldValue(TYPE).toString()
            this.filter = localData.getFieldValue(FILTER).toString()
            this.data = localData.getFieldValue(DATA).toString()
            this.targetFieldList = localData.getFieldValue(OBJECT_FIELD) as List<String>

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

        fun setTargetFieldList(targetFieldList: List<String>): BitableReq {
            this.targetFieldList = targetFieldList
            return this
        }

        fun setFilter(filter: String?): BitableReq {
            this.filter = filter
            return this
        }




    }

}