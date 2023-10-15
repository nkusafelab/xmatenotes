package com.example.xmatenotes.logic.network;

import android.util.Log;

import com.example.xmatenotes.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lark.oapi.Client;
import com.lark.oapi.core.request.RequestOptions;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.bitable.v1.model.AppTableField;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;
import com.lark.oapi.service.bitable.v1.model.Attachment;
import com.lark.oapi.service.bitable.v1.model.CreateAppTableRecordReq;
import com.lark.oapi.service.bitable.v1.model.CreateAppTableRecordResp;
import com.lark.oapi.service.bitable.v1.model.ListAppTableFieldReq;
import com.lark.oapi.service.bitable.v1.model.ListAppTableFieldResp;
import com.lark.oapi.service.bitable.v1.model.ListAppTableRecordReq;
import com.lark.oapi.service.bitable.v1.model.ListAppTableRecordResp;
import com.lark.oapi.service.bitable.v1.model.UpdateAppTableRecordReq;
import com.lark.oapi.service.bitable.v1.model.UpdateAppTableRecordResp;
import com.lark.oapi.service.drive.v1.model.DownloadMediaReq;
import com.lark.oapi.service.drive.v1.model.DownloadMediaResp;
import com.lark.oapi.service.drive.v1.model.UploadAllMediaReq;
import com.lark.oapi.service.drive.v1.model.UploadAllMediaReqBody;
import com.lark.oapi.service.drive.v1.model.UploadAllMediaResp;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class BitableManager {

    private static final String TAG = "BitableManager";
    public static String str1[] = new String[1000];//创建一个字符串数组，用来存储record_id;

    public static String NowRecordId1; //这个参数是填写个人评价时，根据筛选得出的写入使用的recordid。

    private String appId = "cli_a4ac1c99553b9013";

    private String appSecret = "dJnppJxBQQKd4QGmXSrP3fwdvcT5iNZ6";

    private String appToken = "bascn3zrUMtRbKme8rlcyRKfDSc";

    /**
     * 当前tableId
     */
    private String curTableId;

    /**
     * 所有tableId
     */
    private List<String> tableIds = new ArrayList<>();

    private int pageSize = 500;

    private Client client;

    /**
     * 存储所有tableId及其所包含的所有字段对象
     */
    private Map<String, List<AppTableField>> fieldMap = new HashMap<>();

    private static BitableManager bitableManager;

    public static BitableManager getInstance() {
        if (bitableManager == null) {
            synchronized (BitableManager.class) {
                if (bitableManager == null) {
                    bitableManager = new BitableManager();
                }
            }
        }
        return bitableManager;
    }

    private BitableManager() {
//        // 私有构造函数，防止外部实例化
//        initial();
    }

    public abstract static class BitableResp {

        public void onFinish(String string) {
        }

        public void onFinish(AppTableRecord appTableRecord) {
        }

        public void onFinish(AppTableRecord[] appTableRecords) {
        }

        public void onFinish(AppTableField[] appTableFields) {
        }

        public void onError(String errorMsg) {
            Log.e(TAG, errorMsg);
        }
    }

    /**
     * 初始化
     */
    public BitableManager initial(String appId, String appSecret, String appToken){
        if(appSecret != null){
            this.appSecret = appSecret;
        } else if(appId != null){
            this.appId = appId;
        }

       this.appToken = appToken;
        LogUtil.e(TAG, "initial: appId: "+appId+" appSecret: "+appSecret+" appToken: "+appToken);
       this.client = getClient();
       return this;
    }

    /**
     * 初始化
     */
    public BitableManager initialTable(String tableId){
        if(!tableId.equals(curTableId)){
            curTableId = tableId;
            getAppTableFields(curTableId, new BitableResp() {
                @Override
                public void onFinish(AppTableField[] appTableFields) {
                    super.onFinish(appTableFields);
                    fieldMap.put(curTableId, Arrays.asList(appTableFields));
                    fieldMapInitLatch.countDown();
                }
            });
        }
        return this;
    }

    /**
     * 连接创建的自建应用
     * @return
     */
    public Client getClient(){
        return Client.newBuilder(appId, appSecret)
                .build();
    }

    /**
     * 获取目标table的所有字段信息并保存
     */
    public void getAppTableFields(String tableId, BitableResp callBack){
        //连接一个自建应用
        ListAppTableFieldReq req = ListAppTableFieldReq.newBuilder()
                .appToken(appToken)
                .tableId(tableId)
                .pageSize(pageSize)
                .build();

        new Thread(new Runnable() {   //网络问题需要线程，避免拥堵
            @Override
            public void run() {
                try {
                    //获取多维表格记录
                    ListAppTableFieldResp resp = client.bitable().appTableField().list(req, RequestOptions.newBuilder()
                            .build());

                    // 处理服务端错误
                    if(!resp.success()) {
                        callBack.onError("getAppTableFields: "+String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId()));
                        return;
                    }

                    if(callBack != null){
                        callBack.onFinish(resp.getData().getItems());
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private final CountDownLatch fieldMapInitLatch = new CountDownLatch(1);;

    /**
     *
     * @param tableId
     * @param fieldName
     * @return
     */
    public String getFieldIdByName(String tableId, String fieldName){
        if(fieldName == null){
            Log.e(TAG, "searchFiledId: fieldName == null");
            return null;
        }

        try {
            fieldMapInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(fieldMap != null){
            if(fieldMap.containsKey(tableId)){
                List<AppTableField> appTableFields = fieldMap.get(tableId);
                for (AppTableField appTableField:appTableFields) {
                    if(fieldName.equals(appTableField.getFieldName())){
                        return appTableField.getFieldId();
                    }
                }
                Log.e(TAG, "getFieldIdByName: 未找到目标fieldId");
                return null;
            }else {
                Log.e(TAG, "getFieldIdByName: 未找到目标tableId");
                return null;
            }
        }else {
            Log.e(TAG, "getFieldIdByName: fieldMap为空");
            return null;
        }
    }

//    /**
//     * 构造创建新纪录所需字段及值;必须至少包含pageId、页号、数据创建人员和更新时间
//     * @param pageID 版面唯一标识符
//     * @param pageNumber 页号
//     * @param dataCreatorName 数据创建人员姓名
//     * @param updateTime 更新时间
//     * @return
//     */
//    public Map<String, Object> mapFields(int pageID, int pageNumber, String dataCreatorName, String updateTime){
//
//    }

    /**
     * 在多维表格中检索是否存在目标pageId的记录,查找成功则返回所找到的所有记录
     * @param pageId 版面唯一标识符
     * @param fields
     * @param callBack
     * @see #searchAppTableRecords(Map, String, BitableResp)
     */
    public void searchAppTableRecords(int pageId, Map<String, Object> fields, final BitableResp callBack){
        searchAppTableRecords(fields, "CurrentValue.[pageId] = " + pageId, callBack);
    }

    /**
     * 在多维表格目标table中检索是否满足目标筛选条件的记录,查找成功则返回所找到的所有记录
     * @param filter 筛选条件为OR(...)
     * @param callBack 回调接口
     * @see #searchAppTableRecords(String, Map, String, BitableResp)
     */
    public void searchAppTableRecordsWithORFilter(Map<String, Object> filter, final BitableResp callBack){
        //构造OR(...)筛选条件

        //查找记录
        //searchAppTableRecords(String filter, final BitableResp callBack)
    }

    /**
     * 在多维表格目标table中检索是否满足目标筛选条件的记录,查找成功则返回所找到的所有记录
     * @param filter 查找记录筛选条件
     * @param callBack 回调接口
     * @see #searchAppTableRecords(String, Map, String, BitableResp)
     */
    public void searchAppTableRecords(String filter, final BitableResp callBack){
        searchAppTableRecords(curTableId, null, filter, callBack);
    }

    /**
     * 在多维表格当前table中检索是否满足目标筛选条件的记录,查找成功则返回所找到的所有记录;若查找失败可能创建新纪录
     * @param fields
     * @param filter
     * @param callBack
     * @see #searchAppTableRecords(String, Map, String, BitableResp)
     */
    public void searchAppTableRecords(Map<String, Object> fields, String filter, final BitableResp callBack){
        searchAppTableRecords(curTableId, fields, filter, callBack);
    }

    /**
     * 在多维表格目标table中检索是否满足目标筛选条件的记录,查找成功则返回所找到的所有记录
     * @param filter 查找记录筛选条件
     * @param callBack 回调接口
     * @see #searchAppTableRecords(String, Map, String, BitableResp)
     */
    public void searchAppTableRecords(String tableId, String filter, final BitableResp callBack){
        searchAppTableRecords(tableId, null, filter, callBack);
    }

    /**
     * 在多维表格目标table中检索是否满足目标筛选条件的记录,查找成功则返回所找到的所有记录;若查找失败可能创建新纪录
     * @param filter 查找记录筛选条件;详细请参考[筛选条件支持的公式](https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/bitable-v1/filter)
     * @param fields 创建记录所需字段及值，参照{@link #createAppTableRecord(String, Map, BitableResp)}。如果为null，查找失败不创建新纪录，否则创建新纪录
     * @param callBack 回调接口，涉及onFinish(AppTableRecord[] appTableRecords)(找到的所有记录);onFinish(AppTableRecord appTableRecord)(创建的新纪录);onError(String errorMsg)(查找失败);
     */
    public void searchAppTableRecords(String tableId, Map<String, Object> fields, String filter, final BitableResp callBack){
        ListAppTableRecordReq req = ListAppTableRecordReq.newBuilder()
                .appToken(appToken)
                .tableId(tableId)
                .filter(filter)//"AND(CurrentValue.[pageId] = " + pageId +" ,CurrentValue.[人员] = \"" + name +"\")"
                .pageSize(pageSize)
                .build();

        new Thread(new Runnable() {   //网络问题需要线程，避免拥堵
            @Override
            public void run() {
                try {
                    // 发起请求
                    // 如开启了Sdk的token管理功能，就无需调用 RequestOptions.newBuilder().tenantAccessToken("t-xxx").build()来设置租户token了
                    ListAppTableRecordResp resp = client.bitable().appTableRecord().list(req, RequestOptions.newBuilder()
                            .build());

                    // 处理服务端错误
                    if(!resp.success()) {
                        callBack.onError("searchRecordId: "+String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId()));
                        return;
                    }
                    // 业务数据处理

                    Log.e(TAG, "searchRecordId: resp.getData(): "+Jsons.DEFAULT.toJson(resp.getData()));
                    if(resp.getData().getTotal() == 0){
                        if (callBack != null){
                            callBack.onError("searchRecordId: 未查找到目标记录");
                        }
                        if(fields != null){
                            //失败之后，直接尝试创建新的记录
                            createAppTableRecord(tableId, fields, callBack);
                        }
                    } else {
                        AppTableRecord[] s = resp.getData().getItems();
                        if (callBack != null){
                            callBack.onFinish(s);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * 在目标table中创建记录
     * @param fields 新纪录中的字段和值
     * @param callBack 回调接口，至少实现onFinish(AppTableRecord appTableRecord)和onError(String errorMsg)
     */
    public void createAppTableRecord(String tableId, Map<String, Object> fields, BitableResp callBack){
//        CreateAppTableRecordReq reqc = CreateAppTableRecordReq.newBuilder()
//                .appToken(appToken)
//                .tableId(curTableId)
//                .appTableRecord(AppTableRecord.newBuilder()
//                        .fields(new HashMap < String,Object > () {
//                            {
//                                put("pageId",number);
//                                put(number1,number);
//                                put(filed_name, name);
//                            }
//                        })
//                        .build())
//                .build();
//        if(!fields.containsKey("pageId") || !fields.containsKey("页号") || !fields.containsKey("数据创建人员") || !fields.containsKey("更新时间")){
//            Log.e(TAG, "createRecord: 参数fields不符合规范");
//            return;
//        }
        CreateAppTableRecordReq req = CreateAppTableRecordReq.newBuilder()
                .appToken(appToken)
                .tableId(tableId)
                .appTableRecord(AppTableRecord.newBuilder()
                        .fields(fields)
                        .build())
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                CreateAppTableRecordResp resp = null;
                try {
                    resp = client.bitable().appTableRecord().create(req, RequestOptions.newBuilder()
                            .build());

                    // 处理服务端错误
                    if(!resp.success()) {
                        callBack.onError("createAppTableRecord: "+String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId()));
                        return;
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                // 业务数据处理
                LogUtil.e(TAG, "createRecord: resp.getData(): "+Jsons.DEFAULT.toJson(resp.getData()));
//                NowRecordId1 = resp.getData().getRecord().getRecordId();

                if (callBack != null){
                    callBack.onFinish(resp.getData().getRecord());
                }
            }
        }).start();
    }

    /**
     * 更新记录
     * @param tableId
     * @param fields
     * @param filter
     * @param callBack 回调接口，至少实现onFinish(AppTableRecord appTableRecord)和onError(String errorMsg)
     */
    public void updateAppTableRecord(String tableId, Map<String, Object> fields, String filter, BitableResp callBack){
        searchAppTableRecords(tableId, null, filter, new BitableResp() {
            @Override
            public void onFinish(AppTableRecord[] appTableRecords) {
                super.onFinish(appTableRecords);
                for(AppTableRecord appTableRecord : appTableRecords){
                    updateAppTableRecord(tableId, appTableRecord.getRecordId(), fields, callBack);
                }
            }

            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
            }
        });
    }

    /**
     * 更新记录
     * @param tableId
     * @param recordId
     * @param fields
     * @param callBack 回调接口，至少实现onFinish(AppTableRecord appTableRecord)和onError(String errorMsg)
     */
    public void updateAppTableRecord(String tableId, String recordId, Map<String, Object> fields, BitableResp callBack){
        UpdateAppTableRecordReq req = UpdateAppTableRecordReq.newBuilder()
                .appToken(appToken)
                .tableId(tableId)
                .recordId(recordId)
                .appTableRecord(AppTableRecord.newBuilder()
                        .fields(fields)
                        .build())
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 发起请求
                try {
                    UpdateAppTableRecordResp resp = client.bitable().appTableRecord().update(req, RequestOptions.newBuilder()
                            .build());

                    // 处理服务端错误
                    if(!resp.success()) {
                        callBack.onError("updateAppTableRecord: "+String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId()));
                        return;
                    }

                    // 业务数据处理
                    LogUtil.e(TAG, "updateRecord: resp.getData(): "+Jsons.DEFAULT.toJson(resp.getData()));

                    if (callBack != null){
                        callBack.onFinish(resp.getData().getRecord());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * 查找并更新筛选出的记录
     * @param filter 筛选条件
     * @param fields 待更新的字段
     * @param callBack 回调接口，至少实现onFinish(AppTableRecord appTableRecord)和onError(String errorMsg)
     */
    public void sUpdateAppTableRecord(String filter, Map<String, Object> fields, BitableResp callBack){
        searchAppTableRecords(filter, new BitableResp() {

            @Override
            public void onFinish(AppTableRecord[] appTableRecords) {
                super.onFinish(appTableRecords);

                //更新记录
            }

            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
                if(callBack != null){
                    callBack.onError(errorMsg);
                }
            }
        });
    }

    /**
     * 查找并更新筛选出的记录
     * @param filter 筛选条件为AND(...)
     * @param fields 待更新的字段
     * @param callBack 回调接口，至少实现onFinish(AppTableRecord appTableRecord)和onError(String errorMsg)
     */
    public void sUpdateAppTableRecordWithANDFilter(Map<String, Object> filter, Map<String, Object> fields, BitableResp callBack){
        //构造AND(...)

        //查找并更新记录
        //sUpdateAppTableRecord(String filter, Map<String, Object> fields, BitableResp callBack)
    }

//    public void coverAttachmentCell(String tableId, String recordId, String fieldName,List<String> pathList){
//
//    }

    /**
     * 获取指定字段中的所有附件fileToken
     * @param appTableRecord
     * @param fieldName
     * @return
     */
    public static List<String> getfileTokensByFieldName(AppTableRecord appTableRecord, String fieldName){
        if(!appTableRecord.getFields().containsKey(fieldName)){
            Log.e(TAG, "getfileTokensByFieldName: 未找到目标字段: "+fieldName);
            return null;
        }
        //先把linkedTreeMap对象转成json字符串，然后再转成目标list
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String jsonString = gson.toJson(appTableRecord.getFields().get(fieldName));
        List<Attachment> attachments = gson.fromJson(jsonString, new TypeToken<List<Attachment>>(){}.getType());
        List<String> fileTokens = new ArrayList<>();
        for (Attachment attachment:attachments) {
            fileTokens.add(attachment.getFileToken());
        }
        return fileTokens;
    }

    public void addAttachmentCell(int pageId, String fieldName,List<String> pathList){
        addAttachmentCell(curTableId, fieldName, pathList, "CurrentValue.[pageId] = " + pageId);
    }

    public void addAttachmentCell(String fieldName,List<String> pathList, String filter){
        addAttachmentCell(curTableId, fieldName, pathList, filter);
    }

    public void addAttachmentCell(String tableId, String fieldName, List<String> pathList, String filter){

        searchAppTableRecords(tableId, null, filter, new BitableResp(){
            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
            }

            @Override
            public void onFinish(AppTableRecord[] appTableRecords) {
                super.onFinish(appTableRecords);
                for (AppTableRecord appTableRecord:appTableRecords) {
                    addAttachmentCell(tableId, appTableRecord.getRecordId(), fieldName, pathList, getfileTokensByFieldName(appTableRecord, fieldName));
                }
            }
        });
    }

    /**
     * 向目标单元格添加附件
     * @param tableId
     * @param recordId
     * @param fieldName 待添加新附件的字段名
     * @param pathList 待添加附件的绝对路径列表
     * @param fileTokens 目标字段已经上传的附件fileToken
     */
    public void addAttachmentCell(String tableId, String recordId, String fieldName,List<String> pathList, List<String> fileTokens){
        //上传素材
        CountDownLatch cACLatch = new CountDownLatch(pathList.size());
        List<String> fileTokenList = new ArrayList<>();

        //上传素材
        uploadFile(pathList, new BitableResp() {
            @Override
            public void onFinish(String file_token) {
                fileTokenList.add(file_token);
                cACLatch.countDown();
            }
        });

        try {
            cACLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fileTokenList.addAll(fileTokens);
        //更新记录
        updateAttachmentCell(tableId, recordId, fieldName, fileTokenList, new BitableResp() {
            @Override
            public void onFinish(AppTableRecord appTableRecord) {
                super.onFinish(appTableRecord);
            }
        });

    }

    /**
     * 覆盖式更新多维表格当前table单元格中的附件
     * @param recordId
     * @param fieldName
     * @param pathList
     */
    public void coverAttachmentCell(String recordId, String fieldName,List<String> pathList){
        coverAttachmentCell(curTableId, recordId, fieldName, pathList);
    }

    /**
     * 覆盖式更新多维表格目标table单元格中的附件
     * @param recordId 记录id
     * @param fieldName 附件类型的字段名
     * @param pathList 文件绝对路径
     */
    public void coverAttachmentCell(String tableId, String recordId, String fieldName,List<String> pathList){
        Log.e(TAG, "coverAttachmentCell: record_id: "+recordId);
        CountDownLatch cACLatch = new CountDownLatch(pathList.size());
        List<String> fileTokenList = new ArrayList<>();

        //上传素材
        uploadFile(pathList, new BitableResp() {
            @Override
            public void onFinish(String file_token) {
                fileTokenList.add(file_token);
                cACLatch.countDown();
            }
        });

        try {
            cACLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //更新记录
        updateAttachmentCell(tableId, recordId, fieldName, fileTokenList, new BitableResp() {
            @Override
            public void onFinish(AppTableRecord appTableRecord) {
                super.onFinish(appTableRecord);
            }
        });
    }

    /**
     * 更新多维表格目标单元格中的所有附件
     * @param tableId
     * @param recordId
     * @param fieldName 附件类型的字段名
     * @param fileTokenList
     */
    private void updateAttachmentCell(String tableId, String recordId, String fieldName, List<String> fileTokenList, BitableResp callBack) {
        List<Map<String, String>> listMap = new ArrayList<>();
        for(int i=0; i < fileTokenList.size(); i++) {
            Map<String, String> map = new HashMap<>();
            map.put("file_token", fileTokenList.get(i));
            LogUtil.e(TAG, "updateAttachmentCell: file_token: "+fileTokenList.get(i));
            listMap.add(map);
        }

        UpdateAppTableRecordReq req = UpdateAppTableRecordReq.newBuilder()
                .appToken(appToken)
                .tableId(tableId)
                .recordId(recordId)
                .appTableRecord(AppTableRecord.newBuilder()
                        .fields(new HashMap < String,Object > () {
                            {
                                put(fieldName, listMap);
                            }
                        })
                        .build())
                .build();

        new Thread(new Runnable() {   //网络问题需要线程，避免拥堵
            @Override
            public void run() {

                //更新记录（行）
                UpdateAppTableRecordResp resp = null;
                try {
                    resp = client.bitable().appTableRecord().update(req, RequestOptions.newBuilder()
                            .build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                            Log.e(TAG, "xmateUpdate:resp.getData(): "+Jsons.DEFAULT.toJson(resp.getData()));
//                            Log.e(TAG, "xmateUpdate:resp.getMsg(): "+resp.getMsg());
//                            Log.e(TAG, "xmateUpdate:resp.getError(): "+resp.getError());
//                            Log.e(TAG, "xmateUpdate:resp.getCode(): "+resp.getCode());
                // 处理服务端错误
                if(!resp.success()) {
                    if(callBack != null){
                        callBack.onError("updateAttachMentCell: "+String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId()));
                    }
                    return;
                }

                if(callBack != null){
                    callBack.onFinish(resp.getData().getRecord());
                }
            }
        }).start();
    }

    /**
     * 上传文件获取文件的file_token;文件大小不要超过或接近20MB
     * @param path  文件绝对路径列表
     * @param callBack
     */
    public void uploadFile(List<String> path, BitableResp callBack){

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < path.size(); i++) {
                    // 创建请求对象
                    File file = new File(path.get(i));
                    if (!file.exists()) {
                        Log.e(TAG, "uploadFile: 上传文件不存在！");
                        return;
                    }

                    String parentType = "bitable_file";
                    if(isPic(file)){
                        parentType = "bitable_image";
                    }

                    Log.e(TAG, "uploadFile: filename: " + file.getName());
                    UploadAllMediaReq req = UploadAllMediaReq.newBuilder()
                            .uploadAllMediaReqBody(UploadAllMediaReqBody.newBuilder()
                                    .fileName(file.getName())
                                    .parentType(parentType)
                                    .parentNode(appToken)
                                    .size((int) file.length())
                                    .file(file)
                                    .build())
                            .build();

                    try {
                        //获取多维表格记录
                        UploadAllMediaResp resp = client.drive().media().uploadAll(req, RequestOptions.newBuilder()
                                .build());

                        // 处理服务端错误
                        if (!resp.success()) {
                            if(callBack != null){
                                callBack.onError("uploadFile: "+String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId()));
                            }
                            return;
                        }

                        // 业务数据处理
                        Log.e(TAG, "uploadFile: resp.getData(): " + Jsons.DEFAULT.toJson(resp.getData()));
                        if(callBack != null){
                            callBack.onFinish(resp.getData().getFileToken());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    public class Extra {
        private BitablePerm bitablePerm = new BitablePerm();
        class BitablePerm {

            private String tableId;
            private Map<String, Map<String, String[]>> attachments = new HashMap<>();

            public BitablePerm setTableId(String s){
                tableId = s;
                return this;
            }

            public BitablePerm setAttachments(String fieldId, String recordId, String fileToken){
                String[] array = {fileToken};
                Map<String, String[]> map = new HashMap<>();
                map.put(recordId, array);
                attachments.put(fieldId, map);
                return this;
            }

            public String getTableId() {
                return tableId;
            }

            public Map<String, Map<String, String[]>> getAttachments() {
                return attachments;
            }

        }

        class ExtraTypeAdapter extends TypeAdapter<Extra> {

            @Override
            public void write(JsonWriter out, Extra value) throws IOException {
                //按自定义顺序输出字段信息
                out.beginObject();
                out.name("bitablePerm");
                out.beginObject();
                out.name("tableId").value(value.getBitablePerm().getTableId());
                out.name("attachments");
                out.beginObject();
                Set<Map.Entry<String, Map<String, String[]>>> set = value.getBitablePerm().getAttachments().entrySet();
                for (Map.Entry<String, Map<String, String[]>> node: set) {
                    out.name(node.getKey());
                    out.beginObject();
                    Set<Map.Entry<String, String[]>> set1 = node.getValue().entrySet();
                    for (Map.Entry<String, String[]> no: set1) {
                        out.name(no.getKey());
                        out.beginArray();
                        for (String s:
                             no.getValue()) {
                            out.value(s);
                        }
                        out.endArray();
                    }
                    out.endObject();
                }
                out.endObject();
                out.endObject();
                out.endObject();
            }

            @Override
            public Extra read(JsonReader in) throws IOException {
                return null;
            }
        }

        public Extra setTableId(String s){
            bitablePerm.setTableId(s);
            return this;
        }

        public Extra setAttachments(String fieldId, String recordId, String fileToken){
            bitablePerm.setAttachments(fieldId, recordId, fileToken);
            return this;
        }

        public String toJson(){
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Extra.class, new ExtraTypeAdapter())
                    //registerTypeAdapter可以重复使用
                    .create();
            return gson.toJson(this);
        }

        /**
         * URL编码是将特殊字符转换为URL安全的形式，以便在URL中传输。这并不是将整个JSON字符串转换为URL编码的常见做法，通常只对特定的参数进行编码
         * @return
         */
        public String encodeURL(){

            String encode = null;
            String s = toJson();
            Log.e(TAG, "encodeURL: "+s);
            try {
                encode = URLEncoder.encode(s, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return encode;
        }

        public BitablePerm getBitablePerm() {
            return bitablePerm;
        }
    }

    /**
     *
     * @param recordId
     * @param fieldName
     * @param path
     * @param fileTokens
     * @param callBack 回调接口，至少实现onFinish(String string)和onError(String errorMsg)
     */
    public void downloadFile(String recordId, String fieldName, String path, List<String> fileTokens, BitableResp callBack){
        downloadFile(curTableId, recordId, getFieldIdByName(curTableId, fieldName), path, fileTokens, callBack);
    }


    /**
     * 附件素材下载
     * @param tableId
     * @param recordId
     * @param fieldId
     * @param path
     * @param fileTokens
     * @param callBack 回调接口，至少实现onFinish(String string)和onError(String errorMsg)
     */
    public void downloadFile(String tableId, String recordId, String fieldId, String path, List<String> fileTokens, BitableResp callBack){

        if(fileTokens == null){
            LogUtil.e(TAG, "待下载附件列表为空！");
            return;
        } else if(fileTokens.isEmpty()){
            LogUtil.e(TAG, "待下载附件列表为空！");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String fileToken:fileTokens) {
                    String s = new Extra().setTableId(tableId).setAttachments(fieldId, recordId, fileToken).toJson();
                    Log.e(TAG, "downloadFile: "+s);
                    DownloadMediaReq req = DownloadMediaReq.newBuilder()
                            .fileToken(fileToken)//待修改，filetoken需要动态获取
                            .extra(s)
                            .build();

                    try {

                        DownloadMediaResp resp = client.drive().media().download(req, RequestOptions.newBuilder()
                                .build());
//                        Log.e(TAG, "onCreate123: "+Jsons.DEFAULT.toJson(resp.getData()));

                        // 处理服务端错误
                        if(!resp.success()) {
                            Log.e(TAG, "downloadFile: "+String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId()));
                            if(callBack != null){
                                callBack.onError(String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId()));
                            }
                            return;
                        }

                        Log.e(TAG, "downloadFile: path: "+path+"/"+resp.getFileName());
                        resp.writeFile(path+"/"+resp.getFileName());
                        if(callBack != null){
                            callBack.onFinish(path+"/"+resp.getFileName());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
        //.extra("%7B%22bitablePerm%22%3A%7B%22tableId%22%3A%22tblVhiA6VqzIOsXz%22%2C%22attachments%22%3A%7B%22fldw2r5C7d%22%3A%7B%22recebkaWYh%22%3A%5B%22JS3sbQLBfoWK1Sx9GW7cyniGnjh%22%5D%7D%7D%7D%7D")

    }

    /**
     * 判断文件是否为图片
     * @param file
     * @return
     */
    public boolean isPic(File file){
        String fileName = file.getName();
        if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith("bmp") || fileName.endsWith(".gif")){
            return true;
        }
        return false;
    }

    public String getCurTableId() {
        return curTableId;
    }
}

