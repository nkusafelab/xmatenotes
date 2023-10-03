package com.example.xmatenotes.logic.presetable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞书请求类
 */
public class BitableReq {

    private static final String TAG = "BitableReq";

    /**
     * 目标tableId
     */
    private String tableId;

    /**
     * 请求类型: 读取、写入、新增
     */
    private String type;

    /**
     * 写入数据
     */
    private String data;

    /**
     * 目标字段列表
     */
    private List<String> targetFieldList = new ArrayList<>();

    /**
     * 飞书筛选表达式
     */
    private String filter;

    /**
     * 获取写入字段和数据
     * @return
     */
    public Map<String, String> getMap(){
        Map<String, String> map = new HashMap<>();
        if (data != null && !targetFieldList.isEmpty()){
            for (String targetField : targetFieldList){
                map.put(targetField, data);
            }
            return map;
        }
        LogUtil.e(TAG, "getMap(): 目标字段或写入数据为空！");
        return null;
    }

    public BitableReq setTableId(String tableId) {
        this.tableId = tableId;
        return this;
    }

    public BitableReq setType(String type) {
        this.type = type;
        return this;
    }

    public BitableReq setData(String data) {
        this.data = data;
        return this;
    }

    public BitableReq setTargetFieldList(List<String> targetFieldList) {
        this.targetFieldList = targetFieldList;
        return this;
    }

    public BitableReq setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public String getTableId() {
        return tableId;
    }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public List<String> getTargetFieldList() {
        return targetFieldList;
    }

    public String getFilter() {
        return filter;
    }
}
