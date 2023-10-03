package com.example.xmatenotes.logic.presetable;

import android.graphics.Rect;

import com.example.xmatenotes.logic.model.handwriting.MediaDot;

import java.util.HashMap;
import java.util.Map;

/**
 * 微版面预置excel数据
 */
public class LocalData {
    private static final String TAG = "LocalData";

    private static final String COMMAND = "指令";
    private static final String ROLE = "角色";
    private static final String PAGEID = "pageId";

    private int x;

    private int y;

    private Rect localBound = new Rect();

    private Map<String, Object> data = new HashMap<>();

    public LocalData(int x, int y, int pageId, String command, String roleName) {
        this.x = x;
        this.y = y;
        this.data.put(PAGEID, pageId);
        this.data.put(COMMAND, command);
        this.data.put(ROLE, roleName);

    }

    public LocalData setLeft(int left){
        this.localBound.left = left;
        return this;
    }

    public LocalData setLTop(int top){
        this.localBound.top = top;
        return this;
    }

    public LocalData setRight(int right){
        this.localBound.right = right;
        return this;
    }

    public LocalData setBottom(int bottom){
        this.localBound.bottom = bottom;
        return this;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public int getPageId(){
        return (int) this.data.get(PAGEID);
    }

    public String getRole(){
        return (String) this.data.get(ROLE);
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

    @Override
    public String toString() {
        return "LocalData{" +
                "x=" + x +
                ", y=" + y +
                ", localBound=" + localBound +
                ", data=" + data +
                '}';
    }
}
