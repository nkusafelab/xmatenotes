package com.example.xmatenotes.logic.manager;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.example.xmatenotes.R;
import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.model.Page.Card;
import com.example.xmatenotes.logic.model.Page.Page;
import com.example.xmatenotes.logic.model.Page.XueCheng;
import com.example.xmatenotes.logic.model.Page.XueChengCard;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.network.BitableManager;
import com.example.xmatenotes.util.LogUtil;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 负责Page数据的本地存储、上传、下载、解析
 */
public class PageManager {

    private static final String TAG = "PageManager";

    private static final PageManager pageManager = new PageManager();
    private static final String rootPath = "XmateNotesPages";
    private static  String absoluteRootPath;

    private static Storager storager = Storager.getInstance();
    private static BitableManager bitableManager = BitableManager.getInstance();


    /**
     * 允许内存中存在的最大页面数量
     */
    private static final int MAX_PAGE_NUMBER = 80;

    /**
     * 记录PageID到有序数组的映射关系，值用来索引三维数组
     */
    private static Map<Long, Integer> pageIDToArray;

    /**
     * 记录PageID到真实页码信息的映射关系，值用于版面响应控制
     */
    private static Map<Long, PageManager.SimplePageInf> pageIDToPageInf;

    /**
     * 存在于内存中的Page构成的线性表
     */
    private static ArrayList<Page> XueChengList;

    /**
     * 当前pageID
     */
    public static long currentPageID = 0L;

    /**
     * 简单页码信息对象类，存储页号和对应页码图片资源ID的映射关系，以便在需要时将目标页码对应的相关资源呈现在界面上
     */
    class SimplePageInf{
        private int pageNumber;//页号
        private int resId;//资源id

        public SimplePageInf(int pageNumber, int resId) {
            this.pageNumber = pageNumber;
            this.resId = resId;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getResId() {
            return resId;
        }
    }

    private PageManager(){
        absoluteRootPath = Objects.requireNonNull(XmateNotesApplication.context.getExternalFilesDir(null)).getAbsolutePath()+"/"+rootPath;
    }

    public static PageManager getInstance(){
        return pageManager;
    }

    public PageManager init(){
        XueChengList = new ArrayList<>();
        pageIDToArray = new HashMap<>();
        pageIDToPageInf = new HashMap<>();

        //预置的学程样例纸张Page
        pageIDToPageInf.put(0L, new PageManager.SimplePageInf(1, R.drawable.x1));
        pageIDToPageInf.put(2L, new PageManager.SimplePageInf(2, R.drawable.x2));
        pageIDToPageInf.put(4L, new PageManager.SimplePageInf(3, R.drawable.x3));
        pageIDToPageInf.put(6L, new PageManager.SimplePageInf(4, R.drawable.x4));
        pageIDToPageInf.put(8L, new PageManager.SimplePageInf(5, R.drawable.x5));
        pageIDToPageInf.put(10L, new PageManager.SimplePageInf(6, R.drawable.x6));
        pageIDToPageInf.put(12L, new PageManager.SimplePageInf(7, R.drawable.x7));
        pageIDToPageInf.put(14L, new PageManager.SimplePageInf(8, R.drawable.x8));
        pageIDToPageInf.put(32L, new PageManager.SimplePageInf(9, R.drawable.x9));
        pageIDToPageInf.put(34L, new PageManager.SimplePageInf(10, R.drawable.x10));
        pageIDToPageInf.put(36L, new PageManager.SimplePageInf(11, R.drawable.x11));
        pageIDToPageInf.put(38L, new PageManager.SimplePageInf(12, R.drawable.x12));
        pageIDToPageInf.put(40L, new PageManager.SimplePageInf(13, R.drawable.x13));

        savePage(0);
        savePage(2);
        savePage(4);
        savePage(6);
        savePage(8);
        savePage(10);
        savePage(12);
        savePage(14);
        savePage(32);
        savePage(34);
        savePage(36);
        savePage(38);
        savePage(40);

        return this;
    }

    /**
     * 通过pageID判断是否已经存储某页
     * @param pageID 页码ID
     * @return 如果目标页已存储，则返回true
     */
    public static boolean isSavePageByPageID(long pageID){
        return pageIDToArray.containsKey(pageID);
    }

    /**
     * 通过pageID判断该页是否属于学程样例
     * @param pageID 页码ID
     * @return 如果目标页属于学程样例，则返回true
     */
    public static boolean containsPageNumberByPageID(long pageID){
        return pageIDToPageInf.containsKey(pageID);
    }

    /**
     * 如果目标页尚未存储，且Page数量存储上限未满，则存储目标页为新的Page对象，并返回该Page对象；如果目标页已经存储过，则直接返回对应Page对象。
     * 涉及的具体操作包括pageID存入pageIDToArray、新建Page对象存入pageList等
     * @param pageID 页码ID
     * @return 返回目标页对应的Page对象
     */
    public Page savePage(long pageID){
        if(!isSavePageByPageID(pageID)){
            if (XueChengList.size() >= MAX_PAGE_NUMBER) {
                Toast.makeText(XmateNotesApplication.context, "Page存储空间已满", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Page存储空间已满");
                return null;
            }
            pageIDToArray.put(pageID, pageIDToArray.size());
            if (pageIDToPageInf.containsKey(pageID)){
                XueChengList.add(new XueCheng(pageID, pageIDToPageInf.get(pageID).getPageNumber()));
            }else {
                XueChengList.add(new XueCheng(pageID, -1));
            }
            return XueChengList.get(XueChengList.size()-1);
        }
        return getPageByPageID(pageID);
    }

    /**
     * 判断pageId是否合法
     * @param pageId
     * @return
     */
    private boolean isPageIdLegal(long pageId){
        if(0 <= pageId && pageId <= 255){
            return true;
        }
        return false;
    }

    /**
     * 通过页码ID获取对应页对象，前提是对应页对象已经存在
     * @param pageID 页码ID
     * @return 如果对应页对象已经存在，返回页码ID对应的页对象；否则返回null
     */
    public static Page getPageByPageID(long pageID){
        if(isSavePageByPageID(pageID)){
            return XueChengList.get(pageIDToArray.get(pageID));
        }
        return null;
    }

    /**
     * 通过页码ID获取对应页码图片资源ID，前提是对应页码图片资源存在
     * @param pageID 页码ID
     * @return 如果页码图片资源存在，返回资源ID；否则返回-1
     */
    public static int getResIDByPageID(long pageID){
        if(containsPageNumberByPageID(pageID)){
            return pageIDToPageInf.get(pageID).getResId();
        }
        return -1;
    }

    /**
     * 通过页码ID获取对应页号
     * @param pageID 页码ID
     * @return 如果对应页号存在，返回页号；否则返回-1
     */
    public static int getPageNumberByPageID(long pageID){
        if(containsPageNumberByPageID(pageID)){
            return pageIDToPageInf.get(pageID).getPageNumber();
        }
        return -1;
    }

    /**
     * 清空内存和文件中的数据
     */
    public void clear(){

        //清空内存
        pageIDToArray.clear();
        XueChengList.clear();

        Log.e(TAG, "数据清除");
        Toast.makeText(XmateNotesApplication.context, "数据清除完毕",Toast.LENGTH_SHORT).show();
    }

    /**
     * 通过点来更新状态
     * @param mediaDot
     */
    public void update(MediaDot mediaDot){
        long pageId = mediaDot.pageId;
        LogUtil.e(TAG, "update: pageId: "+pageId);
        if(isPageIdLegal(pageId)){
            //只存储并生成pageId在0~255之间的page对象
            savePage((int) pageId);
        }
        currentPageID = pageId;
    }

    public static long getCurrentPageID() {
        return currentPageID;
    }

    public String getPageAbsolutePath(Page page){
        return getPageAbsolutePath(page.getPageName());
    }

    public boolean pagePathexists(Page page){
        File file = new File(getPageAbsolutePath(page));
        return file.exists();
    }

    /**
     * 通过卡片数据文件名，获得完整存储绝对路径
     * @param pageName
     * @return
     */
    public String getPageAbsolutePath(String pageName){
        return absoluteRootPath+"/"+pageName;
    }

    private String getDataAbsolutePath(Page page){
        return getPageAbsolutePath(page)+"/"+"data";
    }

    private String getDataAbsolutePath(String pageAbsolutePath){
        return pageAbsolutePath+"/"+"data";
    }

    private String getPicAbsolutePath(Page page){
        return getPageAbsolutePath(page)+"/"+"pic"+".png";
    }

    public String getNewAudioAbsolutePath(SimpleDot simpleDot, Page page){
        return getPageAbsolutePath(page)+"/"+page.getNewAudioName(simpleDot)+".mp4";
    }

    public String getAudioAbsolutePath(Page page, String audioName){
        return getPageAbsolutePath(page)+"/"+audioName+".mp4";
    }

    public String getAudioAbsolutePath(String pageAbsolutePath, String audioName){
        return pageAbsolutePath+"/"+audioName+".mp4";
    }

    public List<String> getAudioAbsolutePathList(Page page){
        List<String> audioNameList = page.getAudioNameList();
        List<String> audioAbsolutePathList = new ArrayList<>();
        for (String audioName: audioNameList) {
            audioAbsolutePathList.add(getAudioAbsolutePath(page, audioName));
        }
        return audioAbsolutePathList;
    }

    public List<String> getAudioAbsolutePathList(String pageAbsolutePath, Page page){
        List<String> audioNameList = page.getAudioNameList();
        List<String> audioAbsolutePathList = new ArrayList<>();
        for (String audioName: audioNameList) {
            audioAbsolutePathList.add(getAudioAbsolutePath(pageAbsolutePath, audioName));
        }
        LogUtil.e(TAG, "getAudioAbsolutePathList: audioAbsolutePathList: "+audioAbsolutePathList);
        return audioAbsolutePathList;
    }

    /**
     * 创建卡片存储目录
     * @param page
     */
    public String mkdirs(Page page){
        String pageAbsolutePath = getPageAbsolutePath(page);
        mkdirs(pageAbsolutePath);
        return pageAbsolutePath;
    }

    private void mkdirs(String pageAbsolutePath){
        File file = new File(pageAbsolutePath);
        if(!file.exists()){
            file.mkdirs();
            LogUtil.e(TAG, "mkdirs: 创建file: "+file);
        }
    }

    /**
     * 使得目标page生成新版本
     * if(!pagePathexists(page)){
     *      createVersion(Page page);
     * }
     * @param page
     */
    public void createVersion(Page page){
        LogUtil.e(TAG, "createVersion");
        page.create();
        mkdirs(page);
    }

    /**
     * 使得目标page迭代新版本
     * @param page
     */
    public void iterateVersion(Page page){
        if(!pagePathexists(page)){
            LogUtil.e(TAG, "generateVersion: pagePathexists(page): false");
            createVersion(page);
        } else {
            File oldFile = new File(getPageAbsolutePath(page));
            page.create();
            File newFile = new File(getPageAbsolutePath(page));
            if (oldFile.renameTo(newFile)) {
                LogUtil.e(TAG,"save: Directory " + oldFile.getName() + " renamed to " + newFile.getName());
            } else {
                LogUtil.e(TAG,"save: Could not rename directory " + oldFile.getName() + " to " + newFile.getName());
            }
        }
    }

    public void save(Page page, Bitmap bitmap, String pageResAbsolutePath){
        File file = new File(getPageAbsolutePath(page));
        if(!file.exists()){
            file.mkdirs();
        }

//        File oldFile = new File(getPageAbsolutePath(page));
//        LogUtil.e(TAG, "save: oldFile.getAbsolutePath(): "+oldFile.getAbsolutePath());
//        File newFile;
//        String pageAbsolutePath = oldFile.getAbsolutePath();
//        //重命名
//        if(oldFile.exists()){
//            page.create();
//            newFile = new File(getPageAbsolutePath(page));
//            if (oldFile.renameTo(newFile)) {
//                pageAbsolutePath = newFile.getAbsolutePath();
//                LogUtil.e(TAG,"save: Directory " + oldFile.getName() + " renamed to " + newFile.getName());
//            } else {
//                LogUtil.e(TAG,"save: Could not rename directory " + oldFile.getName() + " to " + newFile.getName());
//            }
//        } else {
//            LogUtil.e(TAG, "save: oldFile不存在!");
//            if(page instanceof XueChengCard){
//                LogUtil.e(TAG, "save: page为XueChengCard");
//                XueChengCard xueChengCard = (XueChengCard)page;
//                oldFile = new File(getPageAbsolutePath(xueChengCard.getSuperPageName()));
//                pageAbsolutePath = oldFile.getAbsolutePath();
//                LogUtil.e(TAG, "save: xueChengCard.getSuperPageAbsolutePath: "+pageAbsolutePath);
////                xueChengCard.create();
//                newFile = new File(getPageAbsolutePath(xueChengCard));
//                newFile.mkdirs();
//                LogUtil.e(TAG, "save: XueChengCard: newFile.mkdirs(): "+newFile.getAbsolutePath());
//            } else {
//                LogUtil.e(TAG, "save: 退出save流程");
//                return;
//            }
//        }

        bitableManager.initialTable(Page.pagesTableId);

        String dataPath = getDataAbsolutePath(page);
        String picPath = getPicAbsolutePath(page);
        LogUtil.e(TAG, "save: dataPath: "+dataPath);
        LogUtil.e(TAG, "save: picPath: "+picPath);

        //存储图片
        if(bitmap != null){
            storager.saveBmpWithSuffix(picPath, bitmap);
            LogUtil.e(TAG, "卡片图片存储完成");
        }

        //序列化存储Page对象
        try {
            storager.serializeSaveObject(dataPath, page);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogUtil.e(TAG, "卡片data序列化完成");

        //存储音频

        //上传飞书
        String finalPageAbsolutePath = pageResAbsolutePath;
        LogUtil.e(TAG, "save: finalPageAbsolutePath: "+finalPageAbsolutePath);
        bitableManager.createAppTableRecord(Page.pagesTableId, page.toMap(), new BitableManager.BitableResp() {
            @Override
            public void onFinish(AppTableRecord appTableRecord) {
                super.onFinish(appTableRecord);
                bitableManager.coverAttachmentCell(Page.pagesTableId, appTableRecord.getRecordId(), "卡片数据",new ArrayList<String>(){
                    {
                        add(dataPath);
                    }
                });
                LogUtil.e(TAG, "上传卡片数据文件");
                if(bitmap != null){
                    bitableManager.coverAttachmentCell(Page.pagesTableId, appTableRecord.getRecordId(), "卡片图片",new ArrayList<String>(){
                        {
                            add(picPath);
                        }
                    });
                    LogUtil.e(TAG, "上传卡片图片");
                }

                bitableManager.coverAttachmentCell(Page.pagesTableId, appTableRecord.getRecordId(), "音频",getAudioAbsolutePathList(finalPageAbsolutePath, page));
                LogUtil.e(TAG, "上传卡片音频文件");
            }

            @Override
            public void onError(String errorMsg) {
                super.onError(errorMsg);
            }

        });

    }

    public void downLoad(String code, String pageAbsolutePath, PageManager.ObjectInputResp objectInputResp){
        File file = new File(pageAbsolutePath);
        if(!file.exists()){
            LogUtil.e(TAG, "目标文件夹不存在！");
            return;
        }

        //配置BitTableManager
        bitableManager.initialTable(Page.pagesTableId);

        //查飞书多维表格
        bitableManager.searchAppTableRecords(Page.pagesTableId, null, "CurrentValue.[Code] = \"" + code+"\"", new BitableManager.BitableResp() {
            @Override
            public void onFinish(AppTableRecord[] appTableRecords) {
                super.onFinish(appTableRecords);
                for(AppTableRecord appTableRecord: appTableRecords){
                    LogUtil.e(TAG, "onFinish: "+appTableRecord.getRecordId());

                    //创建对应卡片文件夹
//                    String pageAbsolutePath = getPageAbsolutePath(appTableRecord.getFields().get("卡片名称").toString());
//                    mkdirs(pageAbsolutePath);

                    //下载
//                    List<String> picFileTokens = bitableManager.getfileTokensByFieldName(appTableRecord, "卡片图片");
//                    bitableManager.downloadFile(appTableRecord.getRecordId(), "卡片图片", pageAbsolutePath, picFileTokens);

                    String fieldName = "卡片数据";
                    if(appTableRecord.getFields().get(fieldName) != null){
//                        LogUtil.e(TAG, "appTableRecord.getFields().get(\"卡片数据\"): "+ appTableRecord.getFields().get("卡片数据"));
                        List<String> dataFileTokens = bitableManager.getfileTokensByFieldName(appTableRecord, fieldName);
                        bitableManager.downloadFile(appTableRecord.getRecordId(), fieldName, pageAbsolutePath, dataFileTokens, new BitableManager.BitableResp() {
                            @Override
                            public void onFinish(String string) {
                                super.onFinish(string);
                                //解析Card对象
                                try {
                                    Page page = (Page) storager.serializeParseObject(getDataAbsolutePath(pageAbsolutePath));
                                    if(objectInputResp != null){
                                        objectInputResp.onFinish(page);
                                    }
                                } catch (Exception e) {
                                    LogUtil.e(TAG, "Page解析失败");
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

                    fieldName = "音频";
                    if(appTableRecord.getFields().get(fieldName) != null){
                        List<String> audioFileTokens = bitableManager.getfileTokensByFieldName(appTableRecord, fieldName);
                        bitableManager.downloadFile(appTableRecord.getRecordId(), fieldName, pageAbsolutePath, audioFileTokens, new BitableManager.BitableResp() {
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
        public void onFinish(Page page);

        public void onError(String errorMsg);
    }

}
