package com.example.xmatenotes.logic.manager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.model.Page.OldXueCheng;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.R;
import com.example.xmatenotes.logic.model.instruction.Instruction;
import com.tqltech.tqlpencomm.bean.Dot;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p><strong>Page管理类</strong></p>
 * <p>管理所有Page。包括Page对象的生成与获取、获取附加在目标坐标点上的媒体信息、程序启动时对本地笔迹数据文件的加载与解析、新产生的普通书写笔迹写入对应Page对象和本地笔迹数据文件中等等</p>
 * @see OldXueCheng
 * @author xiaofei
 * @since 21/10/22
 */
public class OldPageManager {
    //单例
//    private volatile static PageManager pageManager;
    private final static OldPageManager OLD_PAGE_MANAGER = new OldPageManager();

    /***********************重要常量**********************/

    private static final String TAG = "PageManager";

    /**
     * 本地笔迹数据存储文件名
     */
    private final static String FILE_NAME = "XmateNotes";

    /**
     * 允许内存中存在的最大页面数量
     */
    private static final int MAX_PAGE_NUMBER = 80;

    /***********************重要常量**********************/

    /***********************数据结构**********************/

    /**
     * 记录PageID到有序数组的映射关系，值用来索引三维数组
     */
    private static Map<Long, Integer> pageIDToArray;

    /**
     * 记录PageID到真实页码信息的映射关系，值用于版面响应控制
     */
    private static Map<Integer, SimplePageInf> pageIDToPageInf;

    /**
     * 存在于内存中的Page构成的线性表
     */
    private static ArrayList<OldXueCheng> oldXueChengList;

    /***********************数据结构**********************/

    /***********************动态成员**********************/

    /**
     * 当前pageID
     */
    public static long currentPageID;

    /**
     * 上一个点
     */
    private static MediaDot lastMediaDot = null;

    /***********************动态成员**********************/

    /************************工具对象*********************/
    //用于写文件
    private FileOutputStream out = null;
    private BufferedWriter writer = null;
    private ExcelReader excelReader = null;
    private PenMacManager penMacManager = null;
    private VideoManager videoManager = null;
    private AudioManager audioManager = null;
    /************************工具对象*********************/

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

    private OldPageManager(){
        oldXueChengList = new ArrayList<>();
        pageIDToArray = new HashMap<>();
        pageIDToPageInf = new HashMap<>();

        //预置的学程样例纸张Page
        pageIDToPageInf.put(0, new SimplePageInf(1, R.drawable.x1));
        pageIDToPageInf.put(2, new SimplePageInf(2, R.drawable.x2));
        pageIDToPageInf.put(4, new SimplePageInf(3, R.drawable.x3));
        pageIDToPageInf.put(6, new SimplePageInf(4, R.drawable.x4));
        pageIDToPageInf.put(8, new SimplePageInf(5, R.drawable.x5));
        pageIDToPageInf.put(10, new SimplePageInf(6, R.drawable.x6));
        pageIDToPageInf.put(12, new SimplePageInf(7, R.drawable.x7));
        pageIDToPageInf.put(14, new SimplePageInf(8, R.drawable.x8));
        pageIDToPageInf.put(32, new SimplePageInf(9, R.drawable.x9));
        pageIDToPageInf.put(34, new SimplePageInf(10, R.drawable.x10));
        pageIDToPageInf.put(36, new SimplePageInf(11, R.drawable.x11));
        pageIDToPageInf.put(38, new SimplePageInf(12, R.drawable.x12));
        pageIDToPageInf.put(40, new SimplePageInf(13, R.drawable.x13));

        excelReader = ExcelReader.getInstance();
        penMacManager = PenMacManager.getInstance();
        videoManager = VideoManager.getInstance();
        audioManager = AudioManager.getInstance();
        loadData();
        Log.e(TAG,"loadData(): "+Thread.currentThread().getName());
    }

//    public static PageManager getInstance(){
//        if(pageManager == null){
//            synchronized (PageManager.class){
//                if(pageManager == null){
//                    pageManager = new PageManager();
//                }
//            }
//        }
//        return pageManager;
//    }
    public static OldPageManager getInstance(){
        return OLD_PAGE_MANAGER;
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
    public OldXueCheng savePage(long pageID){
        if(!isSavePageByPageID(pageID)){
            if (oldXueChengList.size() >= MAX_PAGE_NUMBER) {
                Toast.makeText(XmateNotesApplication.context, "Page存储空间已满", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Page存储空间已满");
                return null;
            }
            pageIDToArray.put(pageID, pageIDToArray.size());
            if (pageIDToPageInf.containsKey(pageID)){
                oldXueChengList.add(new OldXueCheng(pageID, pageIDToPageInf.get(pageID).getPageNumber()));
            }else {
                oldXueChengList.add(new OldXueCheng(pageID));
            }
            return oldXueChengList.get(oldXueChengList.size()-1);
        }
        return getPageByPageID(pageID);
    }

    /**
     * 通过页码ID获取对应页对象，前提是对应页对象已经存在
     * @param pageID 页码ID
     * @return 如果对应页对象已经存在，返回页码ID对应的页对象；否则返回null
     */
    public static OldXueCheng getPageByPageID(long pageID){
        if(isSavePageByPageID(pageID)){
            return oldXueChengList.get(pageIDToArray.get(pageID));
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
     * 通过pageID、x坐标和y坐标，获取对应页码里MediaDot类型的点，即获取附加在目标坐标笔迹点上的视频或音频信息等
     * @param pageID 页码ID
     * @param x 笔迹点横坐标整数部分
     * @param y 笔迹点纵坐标整数部分
     * @return 返回对应的MediaDot类型的点
     */
    public MediaDot getDotMedia(long pageID, int x, int y){
        return getPageByPageID(pageID).getDotMedia(x,y);
    }

    /**
     * 判断目标笔迹点是否已经存储
     * @param pageID 页码ID
     * @param x 笔迹点横坐标整数部分
     * @param y 笔迹点纵坐标整数部分
     * @return 如果目标笔迹点已经存储，返回true
     */
    public boolean containsDot(int pageID, int x, int y){
        return getPageByPageID(pageID).containsDot(x,y);
    }

    /**
     * 将组成笔划的笔迹点写入内存和相应文件中
     * @param mDots 笔迹点List
     */
    public void writeDots(ArrayList<MediaDot> mDots){

        //深拷贝，避免多线程操作同一个对象
        ArrayList<MediaDot> mediaDots = new ArrayList<>(mDots);
        try {
            out = XmateNotesApplication.context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            StringBuilder data;

//            if(audioManager.isRATimerStart()){
//                currentAudio = audioManager.getCurrentAudioNumber();
//            }

            if(AudioManager.isRATimerStart()){
                if(!OldXueCheng.isAudioRangeBeginTrue){//确保一次录音期间只执行一次
                    MediaDot mD = new MediaDot(mediaDots.get(0));//标志音频笔迹的开始
                    mD.setX(-1);
                    mD.setY(-1);
                    if(AudioManager.recordStartTime <= mD.timelong){
                        mD.timelong = AudioManager.recordStartTime;//可能大于PEN_DOEN点的timelong
                    }
                    mediaDots.add(1,mD);
                    Log.e(TAG, "writeDots: 插入录音起始空点");
                    OldXueCheng.isAudioRangeBeginTrue = true;
                }
            }

//            if(!Page.addLocalHwsMapBegin){
//                MediaDot mD = new MediaDot(mediaDots.get(0));//标志单次笔迹的开始
//                mD.setCx(-3);
//                mD.setCy(-3);
//                mediaDots.add(0,mD);
//                Page.addLocalHwsMapBegin = true;
//            }

            boolean isSEMediaDot = false;//必须存储的点
            for (MediaDot d : mediaDots) {
                if (d == mediaDots.get(0) || d.type == Dot.DotType.PEN_DOWN || d == mediaDots.get(mediaDots.size() - 1)) {
                    lastMediaDot = d;
                    isSEMediaDot = true;
                }

                if (MediaDot.computeDistance(lastMediaDot, d) > OldXueCheng.RECORD_MIN_DISTANCE || isSEMediaDot == true) {
                    //写入内存
                    OldXueCheng oldXueCheng = oldXueChengList.get(pageIDToArray.get(d.pageId));
                    oldXueCheng.writeDot(d);

                    //写入文件
                    writer.write(d.storageFormat());
                    writer.newLine();
                    writer.flush();

                    lastMediaDot = d;//保存前一个点
                    isSEMediaDot = false;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = null;
            out = null;
        }
    }

    /**
     * 将单个笔迹点写入内存和相应文件中
     * @param mediaDot 笔迹点
     */
    public void writeDot(MediaDot mediaDot){
        try {
            out = XmateNotesApplication.context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            StringBuilder data;

            OldXueCheng oldXueCheng = oldXueChengList.get(pageIDToArray.get(mediaDot.pageId));
            oldXueCheng.writeDot(mediaDot);

            //写入文件
            writer.write(mediaDot.storageFormat());
            writer.newLine();
            writer.flush();

        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = null;
            out = null;
        }
    }

    //读取excel表特定位置信息
    public void readPageExcel(int pageID, int x, int y){
        if(containsPageNumberByPageID(pageID)){
            excelReader.isZiYuanKa(pageIDToPageInf.get(pageID).getPageNumber(), x, y);
        }

    }

    /**
     * 程序启动时加载本地笔迹数据
     */
    public void loadData() {

        File file = new File(XmateNotesApplication.context.getFilesDir().getAbsolutePath(), FILE_NAME);
        Log.e(TAG, XmateNotesApplication.context.getFilesDir().getAbsolutePath()+FILE_NAME);

        if(!file.exists()) {
//            createFile();
            Log.e(TAG, FILE_NAME+"文件不存在！");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            FileInputStream in = null;
            BufferedReader reader = null;
            try {
                in = XmateNotesApplication.context.openFileInput(FILE_NAME);
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    processMediaDot(MediaDot.parse(line));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                reader = null;
                in = null;
            }

        }

//        Page.lockSaveBmpNumber = false;//关锁
        Toast.makeText(XmateNotesApplication.context, "笔迹数据加载完毕",Toast.LENGTH_SHORT).show();
    }

    /**
     * 创建本地笔迹数据存储文件
     */
    private void createFile(){
        try {
            out = XmateNotesApplication.context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            String data = "9";
            writer.write(data);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = null;
            out = null;
        }
    }

    private int lastAudioID = 0;
    private OldXueCheng lastOldXueCheng = null;
    private LocalRect lastLocalRect = null;
//    private boolean isShwStart = false;//记录单次笔迹是否开始
//    private LocalRect shwLocalRect = null;

    /**
     * 将解析出的MediaDot类型对象进行后续处理
     * @param mediaDot 待处理MediaDot类型对象
     */
    public void processMediaDot(MediaDot mediaDot){
        OldXueCheng oldXueCheng = savePage(mediaDot.pageId);//将新页加入页链

        //标志录音结束
        if(mediaDot.getIntX() == -2 && mediaDot.getIntY() == -2){
            oldXueCheng.writeDot(mediaDot);
            oldXueCheng.addCurrentSaveBmpNumber();//存储局域底图序号+1
            return;
        }

        if(mediaDot.isEmptyDot()){
            oldXueCheng.writeDot(mediaDot);
            return;
        }

        //标志录音开始后的第一个普通书写笔迹点
//        if(mediaDot.x == -1 && mediaDot.y == -1){
//            page.writeDot(mediaDot);//将解析后的mediaDot写入内存
//            return;
//        }

        //标志录音结束
//        if(mediaDot.x == -2 && mediaDot.y == -2){
//            Log.e(TAG,"mediaDot.x == -2 && mediaDot.y == -2");
//            page.writeDot(mediaDot);//将解析后的mediaDot写入内存
//            page.addCurrentSaveBmpNumber();//存储局域底图序号+1
//            audioManager.audioLastDot = page.getPageDotsBufferSize()-1;
//            audioManager.audioRangeMap.put(String.valueOf(mediaDot.audioID),new AudioManager.AudioDotsMap(audioManager.audioFirstDot,audioManager.audioLastDot));
//            Log.e(TAG,"audioRangeMap.size(): "+audioManager.audioRangeMap.size());
//            return;
//        }

        //标志单次笔迹开始
//        if(mediaDot.x == -3 && mediaDot.y == -3){
//            isShwStart = true;
//            page.addLocalHwsMapBegin();
//            page.writeDot(mediaDot);
//            return;
//        }

        //标志单次笔迹结束
//        if(mediaDot.x == -4 && mediaDot.y == -4 && shwLocalRect != null){
//        if(mediaDot.x == -4 && mediaDot.y == -4){
//            page.writeDot(mediaDot);
//            String localCode = shwLocalRect.firstLocalCode+"-"+ shwLocalRect.secondLocalCode;
//            page.addLocalHwsMapEnd(localCode);
//            return;
//        }

        //一次普通书写笔迹结束
//        if(mediaDot.x == -5 && mediaDot.y == -5){
//            page.writeDot(mediaDot);
//            return;
//        }

//        if(isShwStart){
//            shwLocalRect = excelReader.getLocalRectByXY(page.getPageNumber(), mediaDot.x, mediaDot.y);
//            isShwStart = false;
//        }

        if(mediaDot.audioID != audioManager.getCurrentRecordAudioNumber()){
            audioManager.setCurrentRecordAudioNumber(mediaDot.audioID);
        }

        byte penID = penMacManager.putMac(mediaDot.penMac);//存储笔mac

        if(videoManager.contains(mediaDot.videoId)){
            videoManager.getVideoByID(mediaDot.videoId).addMate(penID);
            videoManager.getVideoByID(mediaDot.videoId).addPage(mediaDot.pageId);
        }

        if(mediaDot.type == Dot.DotType.PEN_DOWN){
            Instruction.setStrokesID(Instruction.getStrokesID()+1);//给全局笔划ID++
            if(mediaDot.isAudioDot() && mediaDot.audioID != lastAudioID){
                lastAudioID = mediaDot.audioID;
                oldXueCheng.addAudio(mediaDot.getIntX(), mediaDot.getIntY(), OldXueCheng.createRecordAudioName(mediaDot.audioID));
            }
            Log.e(TAG,"if(page != lastPage)");

            LocalRect lR = excelReader.getLocalRectByXY(oldXueCheng.getPageNumber(), mediaDot.getIntX(), mediaDot.getIntY());
            if(oldXueCheng != lastOldXueCheng){
                //跨页当前页已存储底图数量自动+1
                oldXueCheng.addCurrentSaveBmpNumber();
                lastOldXueCheng = oldXueCheng;
                lastLocalRect = lR;
            }else {
                //跨区域当前页已存储底图数量自动+1
                if(lR != null) {
                    if(lastLocalRect != null){
                        if (lR.firstLocalCode != lastLocalRect.firstLocalCode || lR.secondLocalCode != lastLocalRect.secondLocalCode) {
                            oldXueCheng.addCurrentSaveBmpNumber();
                            lastLocalRect = lR;
                        }
                    }
                }
            }
        }
        mediaDot.strokesID = Instruction.getStrokesID();//单独赋值strokesID

        oldXueCheng.writeDot(mediaDot);//将解析后的mediaDot写入内存
    }

    /**
     * 清空内存和文件中的数据
     */
    public void clear(){

        //清空内存
        pageIDToArray.clear();
        oldXueChengList.clear();
        Instruction.setStrokesID(0);
        videoManager.clear();
        videoManager = VideoManager.getInstance();

        audioManager.clear();

        //清空文件内容
        createFile();

        Log.e(TAG, "数据清除");
        Toast.makeText(XmateNotesApplication.context, "数据清除完毕",Toast.LENGTH_SHORT).show();
    }

    /**
     * 通过点来更新状态
     * @param mediaDot
     */
    public void update(MediaDot mediaDot){
        savePage(mediaDot.pageId);
        currentPageID = mediaDot.pageId;
    }
}
