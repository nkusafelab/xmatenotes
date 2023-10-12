package com.example.xmatenotes.logic.manager;

import java.util.List;

public class PlayShowReq {
    private static final String TAG = "DataShowReq";

    private String roleName;

    /**
     * 目标数据
     */
    private String targetData;

    /**
     * 呈现维度
     */
    private String showDimension;

    /**
     * 目标字段列表
     */
    private List<String> targetFieldList;

    public List<String> getTargetFieldList() {
        return targetFieldList;
    }

    public PlayShowReq setTargetFieldList(List<String> targetFieldList) {
        this.targetFieldList = targetFieldList;
        return this;
    }
}
