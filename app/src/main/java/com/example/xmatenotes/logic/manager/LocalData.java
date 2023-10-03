package com.example.xmatenotes.logic.manager;

import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.util.LogUtil;

import java.util.Map;

/**
 * 微版面预置excel数据
 */
public class LocalData {
    private static final String TAG = "LocalData";

    private static final String COMMAND = "指令";

    private MediaDot localDot;

    private Map<String, Object> data;

    public int getX(){
        if(this.localDot != null){
            return this.localDot.getIntX();
        }
        LogUtil.e(TAG, "getX(): 不存在目标点");
        return -1;
    }

    public int getY(){

        return -1;
    }

    public int getPageId(){

        return -1;
    }

    public LocalData addField(String fieldName, String fieldValue){
        this.data.put(fieldName, fieldValue);
        return this;
    }

    public Object getFieldValue(String fieldName){
        if(this.data.containsKey(fieldName)){
            return this.data.get(fieldName);
        }
        LogUtil.e(TAG,"getFieldValue(): 未找到目标字段！");
        return null;
    }

    public MediaDot getLocalDot() {
        return localDot;
    }

    public void setLocalDot(MediaDot localDot) {
        this.localDot = localDot;
    }

    @Override
    public String toString() {
        return "LocalData{" +
                "localDot=" + localDot +
                ", data=" + data +
                '}';
    }
}
