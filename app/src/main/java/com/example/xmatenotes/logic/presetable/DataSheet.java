package com.example.xmatenotes.logic.presetable;

import java.util.HashMap;
import java.util.Map;

public class DataSheet {
    private static final String TAG = "DataSheet";

    private String name;

    private Map<String, Map<String, String>> data = new HashMap<>();

    public DataSheet(String name) {
        this.name = name;
    }

    /**
     *
     * @param primaryKey 主键
     * @param map 一条数据记录
     * @return
     */
    public DataSheet addMap(String primaryKey, Map<String, String> map){
        this.data.put(primaryKey, map);
        return this;
    }

    public String getName() {
        return name;
    }

    /**
     * 通过主键获取目标数据记录
     * @param primaryKey
     * @return
     */
    public Map<String, String> getMap(String primaryKey){
        if(this.data.containsKey(primaryKey)){
            return this.data.get(primaryKey);
        }
        LogUtil.e(TAG, "getMap(): 未找到目标主键！");
        return null;
    }

    @Override
    public String toString() {
        return "DataSheet{" +
                "name='" + name + '\'' +
                ", data=" + data +
                '}';
    }
}
