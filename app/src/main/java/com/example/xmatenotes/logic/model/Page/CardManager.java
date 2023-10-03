package com.example.xmatenotes.logic.model.Page;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.manager.Storager;
import com.example.xmatenotes.logic.network.BitableManager;
import com.example.xmatenotes.util.LogUtil;
import com.lark.oapi.service.bitable.v1.model.AppTableField;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
//        absoluteRootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+rootPath;在MUUI Pad 14.0.06上没有创建文件或文件夹的权限
        absoluteRootPath = Objects.requireNonNull(XmateNotesApplication.context.getExternalFilesDir(null)).getAbsolutePath()+"/"+rootPath;
    }

    public static CardManager getInstance(){
        return cardManager;
    }
    private String getPageAbsolutePath(Card card){
        return getPageAbsolutePath(card.getCardName());
    }

    /**
     * 通过卡片数据文件名，获得完整存储绝对路径
     * @param cardName
     * @return
     */
    public String getPageAbsolutePath(String cardName){
        return absoluteRootPath+"/"+cardName;
    }

    private String getDataAbsolutePath(Card card){
        return getPageAbsolutePath(card)+"/"+"data";
    }

    private String getDataAbsolutePath(String pageAbsolutePath){
        return pageAbsolutePath+"/"+"data";
    }

    private String getPicAbsolutePath(Card card){
        return getPageAbsolutePath(card)+"/"+"pic"+".png";
    }

    public String getNewAudioAbsolutePath(Card card){
        return getPageAbsolutePath(card)+"/"+card.getNewAudioName()+".mp4";
    }

    public String getAudioAbsolutePath(Card card, String audioName){
        return getPageAbsolutePath(card)+"/"+audioName+".mp4";
    }

    public List<String> getAudioAbsolutePathList(Card card){
        List<String> audioNameList = card.getAudioNameList();
        List<String> audioAbsolutePathList = new ArrayList<>();
        for (String audioName: audioNameList) {
            audioAbsolutePathList.add(getAudioAbsolutePath(card, audioName));
        }
        return audioAbsolutePathList;
    }

    /**
     * 创建卡片存储目录
     * @param card
     */
    public String mkdirs(Card card){
        String cardAbsolutePath = getPageAbsolutePath(card);
        mkdirs(cardAbsolutePath);
        return cardAbsolutePath;
    }

    private void mkdirs(String cardAbsolutePath){
        File file = new File(cardAbsolutePath);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    public void save(Card card, Bitmap bitmap){

        File oldFile = new File(getPageAbsolutePath(card));
        card.create();
        File newFile = new File(getPageAbsolutePath(card));

        //重命名
        if (oldFile.renameTo(newFile)) {
            LogUtil.e(TAG,"Directory " + oldFile.getName() + " renamed to " + newFile.getName());
        } else {
            LogUtil.e(TAG,"Could not rename directory " + newFile.getName());
        }

        bitableManager.initial(Card.cardsTableId);

        String dataPath = getDataAbsolutePath(card);
        String picPath = getPicAbsolutePath(card);

        //存储图片
        storager.saveBmpWithSuffix(picPath, bitmap);
        LogUtil.e(TAG, "卡片图片存储完成");

        //序列化存储CardData对象
        try {
            storager.serializeSaveObject(dataPath, card);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogUtil.e(TAG, "卡片data序列化完成");

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
                LogUtil.e(TAG, "上传卡片数据文件");
                bitableManager.coverAttachmentCell(Card.cardsTableId, appTableRecord.getRecordId(), "卡片图片",new ArrayList<String>(){
                    {
                        add(picPath);
                    }
                });
                LogUtil.e(TAG, "上传卡片图片");
                bitableManager.coverAttachmentCell(Card.cardsTableId, appTableRecord.getRecordId(), "音频",getAudioAbsolutePathList(card));
                LogUtil.e(TAG, "上传卡片音频文件");
            }

            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
            }

        });

    }

    public void downLoad(String code, String cardAbsolutePath, ObjectInputResp objectInputResp){
        File file = new File(cardAbsolutePath);
        if(!file.exists()){
            LogUtil.e(TAG, "目标文件夹不存在！");
            return;
        }

        //配置BitTableManager
        bitableManager.initial(Card.cardsTableId);

        //查飞书多维表格
        bitableManager.searchAppTableRecords(Card.cardsTableId, null, "CurrentValue.[Code] = " + code, new BitableManager.BitableResp() {
            @Override
            public void onFinish(AppTableRecord[] appTableRecords) {
                super.onFinish(appTableRecords);
                for(AppTableRecord appTableRecord: appTableRecords){
                    LogUtil.e(TAG, "onFinish: "+appTableRecord.getRecordId());

                    //创建对应卡片文件夹
//                    String cardAbsolutePath = getPageAbsolutePath(appTableRecord.getFields().get("卡片名称").toString());
//                    mkdirs(cardAbsolutePath);

                    //下载
//                    List<String> picFileTokens = bitableManager.getfileTokensByFieldName(appTableRecord, "卡片图片");
//                    bitableManager.downloadFile(appTableRecord.getRecordId(), "卡片图片", cardAbsolutePath, picFileTokens);

                    if(appTableRecord.getFields().get("卡片数据") != null){
//                        LogUtil.e(TAG, "appTableRecord.getFields().get(\"卡片数据\"): "+ appTableRecord.getFields().get("卡片数据"));
                        List<String> dataFileTokens = bitableManager.getfileTokensByFieldName(appTableRecord, "卡片数据");
                        bitableManager.downloadFile(appTableRecord.getRecordId(), "卡片数据", cardAbsolutePath, dataFileTokens, new BitableManager.BitableResp() {
                            @Override
                            public void onFinish(String string) {
                                super.onFinish(string);
                                //解析Card对象
                                try {
                                    Card card = (Card) storager.serializeParseObject(getDataAbsolutePath(cardAbsolutePath));
                                    if(objectInputResp != null){
                                        objectInputResp.onFinish(card);
                                    }
                                } catch (Exception e) {
                                    LogUtil.e(TAG, "card解析失败");
                                    e.printStackTrace();
                                } finally {

                                }
                            }

                            @Override
                            public void onError(String errorMsg) {
                                super.onError(errorMsg);
                            }
                        });
                    } else {
                        LogUtil.e(TAG, "目标卡片数据为空！");
                    }
                    if(appTableRecord.getFields().get("音频") != null){
                        List<String> audioFileTokens = bitableManager.getfileTokensByFieldName(appTableRecord, "音频");
                        bitableManager.downloadFile(appTableRecord.getRecordId(), "音频", cardAbsolutePath, audioFileTokens, new BitableManager.BitableResp() {
                            @Override
                            public void onFinish(String string) {
                                super.onFinish(string);
                            }

                            @Override
                            public void onError(String errorMsg) {
                                super.onError(errorMsg);
                            }
                        });
                    } else {
                        LogUtil.e(TAG, "目标卡片音频列表为空！");
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
                if(objectInputResp != null){
                    objectInputResp.onError(errorMsg);
                }
            }
        });
    }

    /**
     * Card对象解析返回接口
     */
    public interface ObjectInputResp {
        public void onFinish(Card card);

        public void onError(String errorMsg);
    }

}
