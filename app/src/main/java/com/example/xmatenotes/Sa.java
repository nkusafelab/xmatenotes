//package com.example.xmatenotes;
//
//import com.lark.oapi.Client;
//import com.lark.oapi.core.request.RequestOptions;
//import com.lark.oapi.service.bitable.v1.model.AppTableRecord;
//import com.lark.oapi.service.bitable.v1.model.ListAppTableRecordReq;
//import com.lark.oapi.service.bitable.v1.model.ListAppTableRecordResp;
//
//public class Sa {
//
//    public void fun(){
//        Client client = Client.newBuilder("appId", "appSecret").build();
//
//        // 创建请求对象
//        ListAppTableRecordReq req = ListAppTableRecordReq.newBuilder()
//                .appToken("bascn3zrUMtRbKme8rlcyRKfDSc")
//                .tableId("tblpAZmppl1siFd7")
//                .viewId("vewCJXERGG")
//                .filter("AND(CurrentValue.[起始时间]<NOW(),NOW()<CurrentValue.[结束时间])")
//                .pageSize(20)
//                .build();
//
//        // 发起请求
//        ListAppTableRecordResp resp = client.bitable().appTableRecord().list(req, RequestOptions.newBuilder()
//                .userAccessToken("u-f81nkBLVB3XpkzYGNfsRb4h1mgjh0hzxMa0041c00432")
//                .build());
//
//        resp.getData().getItems()[0].getFields().get("科目");
//    }
//}
