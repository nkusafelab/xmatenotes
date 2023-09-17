package com.example.xmatenotes.logic.model.Page;

import android.graphics.Bitmap;
import android.os.Environment;

import com.example.xmatenotes.logic.manager.Storager;
import com.example.xmatenotes.logic.network.BitableManager;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 负责卡片数据的本地存储、上传、下载、解析
 */
public class CardManager {

    private static final String TAG = "CardManager";

    private static final CardManager cardManager = new CardManager();

    private static final String rootPath = "XmateNotesPages";
    private static  String absoluteRootPath;

    private static Storager storager = Storager.getInstance();
    private static BitableManager bitableManager = BitableManager.getInstance();

    private CardManager(){
        absoluteRootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+rootPath;
    }

    public static CardManager getInstance(){
        return cardManager;
    }
    private String getPageAbsolutePath(Card card){
        return absoluteRootPath+"/"+card.getCardName();
    }

    private String getDataAbsolutePath(Card card){
        return getPageAbsolutePath(card)+"/"+"data";
    }

    private String getPicAbsolutePath(Card card){
        return getPageAbsolutePath(card)+"/"+"pic"+".png";
    }

    private String getAudioAbsolutePath(Card card){
        return getPageAbsolutePath(card);
    }

    public void save(Card card, Bitmap bitmap){

        card.create();
        bitableManager.initial(Card.cardsTableId);

        String dataPath = getDataAbsolutePath(card);
        String picPath = getPicAbsolutePath(card);

        //存储图片
        storager.saveBmpWithSuffix(picPath, bitmap);

        //序列化存储CardData对象
        try {
            storager.serializeSaveObject(dataPath, card);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //存储音频

        //上传飞书
        bitableManager.createAppTableRecord(Card.cardsTableId, card.toMap(), new BitableManager.BitableResp() {
            @Override
            public void onFinish(AppTableRecord appTableRecord) {
                super.onFinish(appTableRecord);
                bitableManager.coverAttachmentCell(Card.cardsTableId, appTableRecord.getRecordId(), "卡片数据",new ArrayList<String>(){
                    {
                        add(dataPath);
                    }
                });
                bitableManager.coverAttachmentCell(Card.cardsTableId, appTableRecord.getRecordId(), "卡片图片",new ArrayList<String>(){
                    {
                        add(picPath);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
            }

        });

    }

}
