package com.example.xmatenotes.logic.model.Page;

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

import com.example.xmatenotes.app.ax.A3;
import com.example.xmatenotes.logic.manager.AudioManager;
import com.example.xmatenotes.logic.manager.ExcelReader;
import com.example.xmatenotes.logic.manager.LocalRect;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.instruction.HandWriting;
import com.tqltech.tqlpencomm.bean.Dot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p><strong>学程版面类</strong></p>
 * @see Page
 */
public class OldXueCheng implements Serializable {

    /***********************重要常量**********************/

    private static final String TAG = "Page";

    private static final long serialVersionUID = -7015148013025862034L;

    /**
     * 笔迹存储 1/3 宽度
     */
    private final static int DOT_WIDTH = 1;

    /***********************重要常量**********************/

    /***********************属性**********************/
    /**
     * 本页PageID属性
     */
    private long pageID;

    /**
     * 页号。初始值为-1
     */
    private int pageNumber = -1;

    /***********************属性**********************/

    /***********************数据结构**********************/

    /**
     * 存储坐标对应普通书写笔迹ID信息的二维页面矩阵
     */
    private final int[][] handWritingIDS = new int[A3.ABSCISSA_RANGE][A3.ORDINATE_RANGE];

    /**
     * 本页普通书写笔迹链，其中普通书写笔迹的下标即为其ID
     */
    private ArrayList<HandWriting> handWritingsList;

    /**
     * 局域编码到附加在该局域上的所有音频文件名的映射
     */
    private Map<String, ArrayList<String>> localAudio = new HashMap<>();

    /**
     * 局域编码到本局域各次笔迹的映射
     */
    private Map<String, ArrayList<LocalHandwritingsMap>> localHandwritings = new HashMap<>();

    /**
     * 笔迹点存储缓冲区
     * 按时间戳顺序存储该页笔迹点;若开始录音并普通书写，则先存入一个空点(横纵坐标均为-1)作为录音起始书写点，只记录录音开始时间戳
     */
    private ArrayList<MediaDot> dotsbuffer = new ArrayList<>();

    /***********************数据结构**********************/

    //以下数据结构中存储的有效数据不能同时为0
//    private float[][] timeS = new float[A3_ABSCISSA_RANGE][A3_ORDINATE_RANGE];//存储坐标对应视频碎片时刻信息
//    private int[][] videoIDS = new int[A3_ABSCISSA_RANGE][A3_ORDINATE_RANGE];//存储坐标对应视频碎片ID信息
//    private int[][] audioIDS = new int[A3_ABSCISSA_RANGE][A3_ORDINATE_RANGE];//存储坐标对应音频碎片ID信息
//    private byte[][] penIDS = new byte[A3_ABSCISSA_RANGE][A3_ORDINATE_RANGE];//存储坐标对应笔Mac地址信息

    /***********************动态成员**********************/

    private LocalHandwritingsMap localHwsMapBuffer = null;

    /**
     * 刻画当前笔划的矩形范围
     */
    private static RectF rectF = null;

    /**
     * 当前HandWriting
     */
    private static HandWriting handWriting = null;

    /**
     * 记录单次笔迹是否开始
     */
    private boolean isSingleHwStart = false;

    /**
     * 记录单次笔迹起始点
     */
    private static MediaDot shwMDotFirst = null;

    /**
     * 上一个采样的笔迹点
     */
    private MediaDot lastMediaDot = null;

    /***********************动态成员**********************/

    /**
     * 记录录音开始后的第一个空点是否已经设置过了
     */
    public static boolean isAudioRangeBeginTrue = false;

    /**
     * 存储在dotsbuffer中的笔迹点的最小采样距离
     */
    public final static double RECORD_MIN_DISTANCE = 0.1;

    /**
     * 该页当前页码底图已存储的数量
     */
    private int currentSaveBmpNumber = 0;
//    public static boolean lockSaveBmpNumber = false;

    /**
     * 当前局域，单次笔迹在同一局域内
     */
    private String currentLocalCode = null;

    /**
     * 当前页面是否包含有效笔迹
     */
    public boolean hasNote = false;

    /************************工具对象*********************/

    private ExcelReader excelReader = null;

    /************************工具对象*********************/

    /**
     * 根据局域编码获取该局域上的笔迹人数
     * @param localCode
     * @return
     */
    public int getPeopleNum(String localCode){
        Set<String> set = new HashSet<>();
        if(!localHandwritings.containsKey(localCode)){
            return 0;
        }
        for (LocalHandwritingsMap lhm :localHandwritings.get(localCode)) {
            set.add(dotsbuffer.get(lhm.getBegin()+5).penMac);
        }
        return set.size();
    }

    /**
     * 根据局域编码获取该局域上的笔迹次数
     * @param localCode
     * @return
     */
    public int getHandWritingsNum(String localCode){
        if(!localHandwritings.containsKey(localCode)){
            return 0;
        }
        return localHandwritings.get(localCode).size();
    }

    /**
     * <p>单次笔迹类: 描述单次笔迹特征的数据结构</p>
     * <p><strong>单次笔迹结构：</strong>单次笔迹由至少一个普通书写笔迹按照时间顺序构成，一个普通书写笔迹由至少一个普通书写笔划构成，一个普通书写笔划由多个笔迹点构成</p>
     * <p>本类主要记录各次笔迹的笔迹点在{@link #dotsbuffer}中的起始与结束位置</p>
     */
    public class LocalHandwritingsMap {

        /**
         * 单次笔迹在dotsbuffer中的起始位置，左闭
         */
        private int begin;

        /**
         * 单次笔迹在dotsbuffer中的结束位置，右闭
         */
        private int end;//右闭

        /**
         * 使用下面的ArrayList型变量按照时间顺序依次存储下组成该单次笔迹的所有普通书写笔迹的索引，即普通书写笔迹索引链
         */
        private ArrayList<Integer> localHWsList;

        /**
         * 该单次笔迹对应的平面区域范围
         */
        private Region region;

        public LocalHandwritingsMap(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        public LocalHandwritingsMap(int begin) {
            this.begin = begin;
        }

        public int getBegin() {
            return begin;
        }

        public void setBegin(int begin) {
            this.begin = begin;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        /**
         * 添加单个普通书写笔迹
         * @param handWriting 待添加普通书写笔迹对象
         */
        public void addHandWriting(HandWriting handWriting){
            if(localHWsList == null){
                localHWsList = new ArrayList<>();
            }
            if(region == null){
                region = new Region();
            }
            region.op(handWriting.getRegion(), Region.Op.UNION);
            localHWsList.add(handWritingsList.size()-1);
            setEnd(dotsbuffer.size()-1);
        }

        /**
         * 判断该单次笔迹是否包含目标点
         * @param x 目标点横坐标整数部分
         * @param y 目标点纵坐标整数部分
         * @return 如果包含，返回true; 否则返回false
         */
        public boolean contains(int x, int y){
            return region.contains(x, y);
        }

        public Rect getBounds(){
            return region.getBounds();
        }
    }

    public OldXueCheng() {
        this(-1);
    }

    public OldXueCheng(long pageID) {
        this(pageID, -1);
    }

    public OldXueCheng(long pageID, int pageNumber) {
        this.pageID = pageID;
        this.pageNumber = pageNumber;
        excelReader = ExcelReader.getInstance();
        if(handWritingsList == null){
            handWritingsList = new ArrayList<>();
            handWritingsList.add(new HandWriting());//加入空HandWriting，使得普通书写笔迹ID从1开始
        }
    }

    /**
     * 根据目标点坐标获取包含附加媒体信息的MediaDot类型的目标点。
     * 调用此方法前应先通过containsDot()判断该页已存储的普通书写笔迹中是否存在该点
     * @param x 目标点横坐标整数部分
     * @param y 目标点纵坐标整数部分
     * @return 如果存在目标点，则返回MediaDot类型的目标点; 否则返回null
     */
    public MediaDot getDotMedia(int x, int y) {
        if(!containsDot(x, y)){
            return null;
        }
        MediaDot dotMedia = new MediaDot(x, y);
        HandWriting hw = handWritingsList.get(handWritingIDS[x][y]);
        Log.e(TAG, "getDotMedia: hw: "+hw.toString());
//        dotMedia.time = timeS[x][y];
//        dotMedia.videoID = videoIDS[x][y];
//        dotMedia.audioID = audioIDS[x][y];
//        dotMedia.penMac = PenMacManager.getPenMacByID(penIDS[x][y]);
        dotMedia.videoTime = hw.getTime();
        dotMedia.videoID = hw.getVideoID();
        dotMedia.audioID = hw.getAudioID();
        dotMedia.penMac = hw.getPenMac();
        dotMedia.pageID = pageID;
        dotMedia.strokesID = hw.getLocalHWsMapID();//携带指向所在的单次笔迹的索引，并不是原有含义
        return dotMedia;
    }

    /**
     * 判断当前页面是否存在该点
     * @param dot
     * @return
     */
    public boolean containsDot(Dot dot) {
        return containsDot(dot.x, dot.y);
    }

    /**
     * 判断当前页面是否存在该点
     * @param x 目标点横坐标整数部分
     * @param y 目标点纵坐标整数部分
     * @return 如果目标点尚未存储或不合法，则返回false; 否则返回true
     */
    public boolean containsDot(int x, int y) {
//        if (timeS[x - 1][y - 1] != 0 || videoIDS[x - 1][y - 1] != 0 || audioIDS[x - 1][y - 1] != 0 || penIDS[x - 1][y - 1] != 0) {
//            return true;
//        }
        if(x<0 || x>=A3.ABSCISSA_RANGE || y<0 || y>=A3.ORDINATE_RANGE){
            Log.e(TAG,"要获取的目标点不存在");
            return false;
        }
        if(handWritingIDS[x][y] != 0){
            return true;
        }
        Log.e(TAG,"要获取的目标点不存在");
        return false;
    }

    private boolean once = false;//控制某个操作在一段流程中只执行一次

    /**
     * 将单个MediaDot类型的点写入内存中的该page对象
     * @param mediaDot 待写入目标MediaDot类型的点
     */
    public void writeDot(MediaDot mediaDot) {

        int x = mediaDot.getIntX();
        int y = mediaDot.getIntY();
        float cx = mediaDot.getFloatX();
        float cy = mediaDot.getFloatY();

        //一次普通书写笔迹结束
        if(x == -5 && y == -5){
            dotsbuffer.add(mediaDot);
            addHandwriting(handWriting);
            handWriting = null;
            return;
        }

        //标志录音开始后的第一个普通书写笔迹点
        if (x == -1 && y == -1) {
            dotsbuffer.add(mediaDot);//插入空点
            AudioManager.audioFirstDot = dotsbuffer.size() - 1;//记录录音开始标记空点在dotsbuffer中的索引
            return;
        }

        //标志录音结束
        if (x == -2 && y == -2) {
            dotsbuffer.add(mediaDot);//录音结束前插入一个空点
            Log.e(TAG, mediaDot.toString());

            AudioManager.setCurrentRecordAudioName(createRecordAudioName(mediaDot.audioID));
            AudioManager.audioLastDot = dotsbuffer.size() - 1;//记录录音结束标记空点在dotsbuffer中的索引
            AudioManager.addAudio();
//            addCurrentSaveBmpNumber();
//            lockSaveBmpNumber = true;//开锁
//            Log.e(TAG, "lockSaveBmpNumber = true;开锁");
            return;
        }

        //标志单次笔迹开始
        if(x == -3 && y == -3){
            addLocalHwsMapBegin();
            once = true;
            dotsbuffer.add(mediaDot);
            return;
        }

        //标志单次笔迹结束
        if(x == -4 && y == -4){
            dotsbuffer.add(mediaDot);
            addLocalHwsMapEnd();
//            if(pageNumber != -1){
//                LocalRect lR = excelReader.getLocalRectByXY(pageNumber, shwMDotFirst.x, shwMDotFirst.y);
//                if(lR != null){
//                    addLocalHwsMapEnd(lR.firstLocalCode+"-"+ lR.secondLocalCode);
//                }
//            }else {
//                addLocalHwsMapEnd(String.valueOf(pageID));
//            }
            return;
        }

        if(isSingleHwStart && once){
            shwMDotFirst = mediaDot;//记录单次笔迹开始后第一个有效点
            
            if(pageNumber != -1){
                LocalRect lR = excelReader.getLocalRectByXY(pageNumber, shwMDotFirst.getIntX(), shwMDotFirst.getIntY());
                Log.e(TAG, "writeDot: lR: "+lR);
                if(lR != null){
                    currentLocalCode = lR.getLocalCode();
                }
            }else {
                currentLocalCode = String.valueOf(pageID);
            }
            if(localHandwritings.containsKey(currentLocalCode)){
                localHandwritings.get(currentLocalCode).add(localHwsMapBuffer);
            }else{
                ArrayList<LocalHandwritingsMap> a = new ArrayList<>();
                a.add(localHwsMapBuffer);
                localHandwritings.put(currentLocalCode, a);
                Log.e(TAG, "writeDot:put(currentLocalCode, a) currentLocalCode:" +currentLocalCode);
            }
            once = false;
        }

        int xMin = Math.max(x - DOT_WIDTH, 0);
        int yMin = Math.max(y - DOT_WIDTH, 0);
        int xMax = Math.min(x + DOT_WIDTH, A3.ABSCISSA_RANGE - 1);
        int yMax = Math.min(y + DOT_WIDTH, A3.ORDINATE_RANGE - 1);

//        float time = mediaDot.time;
//        int videoID = mediaDot.videoID;
//        int audioID = mediaDot.audioID;
//        byte penID = PenMacManager.getPenIDByMac(mediaDot.penMac);

        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
//                timeS[i][j] = time;
//                videoIDS[i][j] = videoID;
//                audioIDS[i][j] = audioID;
//                penIDS[i][j] = penID;
                handWritingIDS[i][j] = handWritingsList.size();//确保在此之前已经调用了addHandwriting()
            }
        }

        hasNote = true;

        switch (mediaDot.type) {
            case PEN_DOWN:
                if(rectF == null){
                    rectF = new RectF();
                }
                rectF.setEmpty();
                rectF.left = cx;
                rectF.right = cx;
                rectF.top = cy;
                rectF.bottom = cy;
                lastMediaDot = mediaDot;
                dotsbuffer.add(lastMediaDot);
                break;
            case PEN_UP:
                rectF.union(cx, cy);
                lastMediaDot = mediaDot;
                dotsbuffer.add(lastMediaDot);
                if(handWriting == null){
                    handWriting = new HandWriting();
                }
                handWriting.addRect(new Rect((int)Math.floor(rectF.left), (int)Math.floor(rectF.top), (int)Math.ceil(rectF.right), (int)Math.ceil(rectF.bottom)));
                handWriting.addAttribute(mediaDot);
                Log.e(TAG, "writeDot: localHandwritings.get(currentLocalCode).size()-1 currentLocalCode:" + currentLocalCode);
                handWriting.setLocalHWsMapID(localHandwritings.get(currentLocalCode).size()-1);
                Log.e(TAG, "writeDot: handWriting: "+handWriting.toString());
                break;
            case PEN_MOVE:
//                if (SimpleDot.computeDistance(lastTimelongDot.x, lastTimelongDot.y, mediaDot.getCx(), mediaDot.getCy()) < RECORD_MIN_DISTANCE) {
//                    break;
//                }
                rectF.union(cx, cy);
                lastMediaDot = mediaDot;
                dotsbuffer.add(lastMediaDot);
                break;
            default:
        }

    }

    public ArrayList<MediaDot> getPageDotsBuffer() {
        return dotsbuffer;
    }

    public int getPageDotsBufferSize() {
        return dotsbuffer.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OldXueCheng oldXueCheng = (OldXueCheng) o;
        return pageID == oldXueCheng.pageID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageID, pageNumber);
    }

    @Override
    public String toString() {
        return "Page{" +
                "pageID=" + pageID +
                ", pageNumber=" + pageNumber +
                '}';
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public long getPageID() {
        return pageID;
    }

    public void setPageID(int pageID) {
        this.pageID = pageID;
    }

    public int getCurrentSaveBmpNumber() {
        return currentSaveBmpNumber;
    }

    public void setCurrentSaveBmpNumber(int currentSaveBmpNumber) {
        this.currentSaveBmpNumber = currentSaveBmpNumber;
    }

    public int addCurrentSaveBmpNumber() {
        Log.e(TAG, "addCurrentSaveBmpNumber(): currentSaveBmpNumber: " + (currentSaveBmpNumber + 1));
        return currentSaveBmpNumber++;
    }

    /**
     * 在本页添加目标音频信息，音频信息添加在目标点所确定的目标局域中
     * @param x 目标横坐标整数部分
     * @param y 目标纵坐标整数部分
     * @param audioName 待附加音频文件名
     * <p>如果该页为学程样例纸张页码(即pageNumber > 0)，先获取目标局域对象，再通过本方法的重载方法{@link #addAudio(LocalRect, String)}添加；</p>
     * <p>如果该页是普通点阵纸张(pageNumber == -1)，没有划分局域，则不获取目标局域对象</p>
     */
    public void addAudio(int x, int y, String audioName) {
        if (pageNumber > 0) {
            LocalRect lR = excelReader.getLocalRectByXY(pageNumber, x, y);
            addAudio(lR, audioName);
        } else {
            addAudio(null, audioName);//非学程样例页
        }

    }

    /**
     * 在本页目标局域上添加音频信息
     * @param lR 目标局域
     * @param audioName 待添加音频文件名
     * 主要将待添加音频文件名加入{@link #localAudio}中。需要注意的是，当方法参数lR为null时，意味着目标页码为未划分局域的普通点阵纸张页码，
     * 这种情况下，待添加音频文件名在{@link #localAudio}中对应的键应为目标页码的PageID，而不是局域编码
     */
    public void addAudio(LocalRect lR, String audioName) {
        String localCode;
        if (lR != null) {
            localCode = lR.getLocalCode();
        } else {
            localCode = String.valueOf(pageID);
        }
        if (localAudio.containsKey(localCode)) {
            if (!localAudio.get(localCode).contains(audioName)) {
                localAudio.get(localCode).add(audioName);
            }
        } else {
            ArrayList<String> a = new ArrayList<>();
            a.add(audioName);
            localAudio.put(localCode, a);
        }
    }

    /**
     * 获取整个局域所有音频文件名
     * @param localCode 目标局域编码
     * @return 由目标局域所有音频文件名构成的ArrayList对象
     * 需要注意的是，如果是获取未划分局域的普通点阵纸张上的音频文件列表，则参数“localCode”应为点阵纸张PageID，而不是局域编码
     */
    public ArrayList<String> getAudioList(String localCode) {
        if (!localAudio.containsKey(localCode)) {
            return null;
        }
        return localAudio.get(localCode);
    }

    /**
     * 获取局域中按时间顺序从audioName开始的所有音频文件名
     * @param localCode 局域编码
     * @param audioName 起始音频文件名
     * @return 如果目标局域中没有录制的音频，返回null;
     * 如果目标局域中没有名为audioName的音频文件，则返回空的ArrayList<String>型对象;
     * 否则返回由目标局域符合要求的所有音频文件名构成的ArrayList对象
     * <p>需要注意的是，如果是获取未划分局域的普通点阵纸张上的音频文件列表，则参数“localCode”应为点阵纸张PageID，而不是局域编码</p>
     */
    public ArrayList<String> getAudioList(String localCode, String audioName) {
        if (!localAudio.containsKey(localCode)) {
            return null;
        }
        ArrayList<String> lA = new ArrayList<>();
        ArrayList<String> oLA = localAudio.get(localCode);
        boolean b = false;
        for (String aN : oLA) {
            if (aN == audioName) {
                b = true;
            }
            if (b) {
                lA.add(aN);
            }
        }
        return lA;
    }

    //记录是否已经在单次笔迹开始时在dotsbuffer中插入坐标为(-3,-3)的空点
//    public static boolean addLocalHwsMapBegin = false;

    /**
     * 记录单次笔迹在dotsbuffer中的起始位置,需在插入单次笔迹起始标志空点前调用
     */
    private void addLocalHwsMapBegin() {
        isSingleHwStart = true;
        localHwsMapBuffer = new LocalHandwritingsMap(dotsbuffer.size(), dotsbuffer.size());
    }

    /**
     * 在本页中添加目标普通书写笔迹
     * @param handWriting 目标普通书写笔迹
     * <p>总共有两个步骤: </p>
     * <p>1、在本页的{@link #handWritingsList}中添加</p>
     * <p>2、在本页当前的{@link #localHwsMapBuffer}中添加</p>
     */
    public void addHandwriting(HandWriting handWriting){
        handWritingsList.add(handWriting);

        if(isSingleHwStart && (localHwsMapBuffer != null)){
            localHwsMapBuffer.addHandWriting(handWriting);
        }
    }

    /**
     * 记录单次笔迹在dotsbuffer中的结束位置,需在插入单次笔迹结束标志空点后调用
     */
    private void addLocalHwsMapEnd() {
        if(isSingleHwStart && (localHwsMapBuffer != null)){
            localHwsMapBuffer.setEnd(dotsbuffer.size()-1);
//            if(localHandwritings.containsKey(localCode)){
//                localHandwritings.get(localCode).add(localHwsMapBuffer);
//            }else{
//                ArrayList<LocalHandwritingsMap> a = new ArrayList<>();
//                a.add(localHwsMapBuffer);
//                localHandwritings.put(localCode, a);
//            }
            localHwsMapBuffer = null;
            isSingleHwStart = false;
//            Page.addLocalHwsMapBegin = false;
        }
    }

    /**
     * 获取整个目标局域中的各次笔迹
     * @param localCode 目标局域编码
     * @return ArrayList类型的各次笔迹
     * 需要注意的是，如果是获取未划分局域的普通点阵纸张上的各次笔迹，则参数“localCode”应为点阵纸张PageID，而不是局域编码
     */
    public ArrayList<LocalHandwritingsMap> getLocalHandwritings(String localCode){
        if(!localHandwritings.containsKey(localCode)){
            return null;
        }
        return localHandwritings.get(localCode);
    }

    /**
     * 通过audioID生成完整录音文件名，定义录音文件名构造格式(格式待迭代)
     * @param audioID 录音文件全局ID
     * @return 完整录音文件名
     */
    public static String createRecordAudioName(int audioID){
        return String.valueOf(audioID);
    }
}
