package com.example.xmatenotes.logic.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简化构造字段的方式
 */
public class FieldsMap {

    private static final String TAG = "FieldsMap";

    private Map<String, Object> fieldsMap = new HashMap<>();

    public FieldsMap() {
    }

    public FieldsMap(String fieldName, Object fieldValue) {
        addField(fieldName, fieldValue);
    }

    public Map<String, Object> getFieldsMap(){
        return this.fieldsMap;
    }


    public FieldsMap addFieldsMap(FieldsMap fieldsMap){
        this.fieldsMap.putAll(fieldsMap.getFieldsMap());
        return this;
    }

    /**
     * 添加文本类型字段及值
     * @return
     */
    public FieldsMap addTextField(String fieldName, String textValue){

        addField(fieldName, textValue);
        return this;
    }

    /**
     * 添加数字类型字段及值
     * @param fieldName
     * @param numberValue
     * @return
     */
    public FieldsMap addNumberField(String fieldName, float numberValue){

        addField(fieldName, numberValue);
        return this;
    }

    /**
     * 添加日期类型字段及值
     * @param fieldName
     * @param timeValue
     * @return
     */
    public FieldsMap addTimeField(String fieldName, long timeValue){

        addField(fieldName, timeValue);
        return this;
    }

    /**
     * 添加附件类型字段及值
     * @param fieldName
     * @param fileTokenList
     * @return
     */
    public FieldsMap addAttachmentField(String fieldName, List<String> fileTokenList){

        List<Map<String, String>> listMap = new ArrayList<>();
        for(int i=0; i < fileTokenList.size(); i++) {
            Map<String, String> map = new HashMap<>();
            map.put("file_token", fileTokenList.get(i));
            listMap.add(map);
        }
        addField(fieldName, listMap);
        return this;
    }

    private FieldsMap addField(String fieldName, Object fieldValue){
        this.fieldsMap.put(fieldName, fieldValue);
        return this;
    }
}
