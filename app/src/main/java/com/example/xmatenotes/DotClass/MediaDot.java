package com.example.xmatenotes.DotClass;

import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.example.xmatenotes.App.XApp;
import com.example.xmatenotes.Gesture;
import com.tqltech.tqlpencomm.bean.Dot;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 附加媒体信息的点
 */
public class MediaDot {

    private final static String  TAG = "MediaDot";

    /**
     * 点横坐标，整数部分
     */
    public int x;

    /**
     * 点纵坐标，整数部分
     */
    public int y;

    /**
     * 点完整横坐标
     */
    private float cx;

    /**
     * 点完整纵坐标
     */
    private float cy;

    /**
     * 点的类型
     */
    public Dot.DotType type;

    /**
     * 笔划ID，同页内唯一
     */
    public int strokesID;

    /**
     * 页号
     */
    public int pageID;

    /**
     * RTC，起始时间1970-01-01 00:00:00 000，单位为毫秒ms
     */
    public long timelong;

    /**
     * 笔Mac地址
     */
    public String penMac;

    /***************************媒体信息*****************************/
    /**
     * 视频ID
     */
    public int videoID;

    /**
     * 视频进度
     */
    public float time;

    /**
     * 音频ID
     */
    public int audioID;
    /***************************媒体信息*****************************/

    /***************************形态信息*****************************/
    /**
     * 颜色
     */
    public int color;
    private static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEEP_GREEN = Color.rgb(5, 102, 8);
    public static final int DEEP_ORANGE = Color.rgb(243,117,44);

    /**
     * 大小(或宽度)
     */
    public int width;
    public static final int DEFAULT_WIDTH = 1;
    public static final int DEFAULT_BOLD_WIDTH = 3;

    /**
     * 命令Id
     */
    public int ins;
    public static final int DEFAULT_INS = 0;

    /***************************形态信息*****************************/

    public MediaDot() {
    }

    public MediaDot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public MediaDot(SimpleDot simpleDot){
        setCx(simpleDot.x);
        setCy(simpleDot.y);
        this.type = simpleDot.type;
        this.pageID = XApp.DEFAULT_INT;
        this.timelong = System.currentTimeMillis();
        this.time = XApp.DEFAULT_FLOAT;
        this.videoID = XApp.DEFAULT_INT;
        this.audioID = XApp.DEFAULT_INT;
        this.color = DEFAULT_COLOR;
        this.width = DEFAULT_WIDTH;
        this.ins = DEFAULT_INS;
    }

    public MediaDot(Dot dot) throws ParseException {
        this.x = dot.x;
        this.y = dot.y;
        this.cx = SimpleDot.computeCompletedDot(dot.x, dot.fx);
        this.cy = SimpleDot.computeCompletedDot(dot.y, dot.fy);
        this.type = dot.type;
        this.pageID = dot.PageID;
        this.timelong = reviseTimelong(dot.timelong);//修正起始时间，方便处理
        this.color = DEFAULT_COLOR;
        this.width = DEFAULT_WIDTH;
        this.ins = DEFAULT_INS;
    }

    public MediaDot(Dot dot, int strokesID, float timeS, int videoIDS, int audioIDS, String penMac) throws ParseException {
        this(dot);

        this.strokesID = strokesID;
        this.time = timeS;
        this.videoID = videoIDS;
        this.audioID = audioIDS;
        this.penMac = penMac;
    }

    public MediaDot(int x, int y, Dot.DotType type, int strokesID, int pageID, long timelong, float time, int videoID, int audioID, String penMac) {
        this.x = x;
        this.y = y;
        this.cx = x;
        this.cy = y;
        this.type = type;
        this.strokesID = strokesID;
        this.pageID = pageID;
        this.timelong = timelong;
        this.time = time;
        this.videoID = videoID;
        this.audioID = audioID;
        this.penMac = penMac;
    }

    public MediaDot(MediaDot mediaDot) {
        this.x = mediaDot.x;
        this.y = mediaDot.y;
        this.cx = mediaDot.cx;
        this.cy = mediaDot.y;
        this.type = mediaDot.type;
        this.strokesID = mediaDot.strokesID;
        this.pageID = mediaDot.pageID;
        this.timelong = mediaDot.timelong;
        this.time = mediaDot.time;
        this.videoID = mediaDot.videoID;
        this.audioID = mediaDot.audioID;
        this.penMac = mediaDot.penMac;
        this.color = mediaDot.color;
        this.width = mediaDot.width;
        this.ins = mediaDot.ins;
    }


    public float getCx(){
        return this.cx;
    }

    public float getCy(){
        return this.cy;
    }

    public void setCx(float cx) {
        this.cx = cx;
        this.x = (int)cx;
//        this.x = Math.round(cx);
    }

    public void setCy(float cy) {
        this.cy = cy;
        this.y = (int)cy;
//        this.y = Math.round(cy);
    }

    /**
     * 计算两点距离
     * @param mDot1
     * @param mDot2
     * @return
     */
    public static double computeDistance(MediaDot mDot1, MediaDot mDot2){
        return Math.sqrt((mDot2.cx - mDot1.cx)*(mDot2.cx - mDot1.cx) + (mDot2.cy - mDot1.cy)*(mDot2.cy - mDot1.cy));
    }

    /**
     * 计算两点距离
     * @param mDot1
     * @param mDot2
     * @return
     */
    public static double computeDispersedDistance(MediaDot mDot1, MediaDot mDot2){
        return Math.sqrt((mDot2.x - mDot1.x)*(mDot2.x - mDot1.x) + (mDot2.y - mDot1.y)*(mDot2.y - mDot1.y));
    }

    /**
     * 利用原始dot的timelong属性修正视频进度
     * @param timelong dot原始未修正的时间戳属性timelong
     * @param timeS 视频进度。请确保传入的timeS为调用该方法时刻的timeS
     * @return 返回修正后的视频进度，即timelong对应的视频进度
     */
    public static float reviseTimeS(long timelong, float timeS) throws ParseException {
        long c = System.currentTimeMillis();
        return (float) (timeS - (c-reviseTimelong(timelong))/1000.0);
    }

    //将Dot的timelong起始时间从2010-01-01 00:00:00 000修正为1970-01-01 00:00:00 000
    public static long reviseTimelong(long timelong) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        Date datetime = sdf.parse("2010-01-01 00:00:00 000");
        return datetime.getTime() + timelong;
    }

    //格式化显示timelong
    public static String timelongFormat(long timelong){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日-hh时mm分ss秒");
        Date datetime = new Date(timelong);
        return sdf.format(datetime);
    }

    //格式化显示timelong
    public static String timelongFormat2(long timelong){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-hh:mm:ss");
        Date datetime = new Date(timelong);
        return sdf.format(datetime);
    }

    public int getIns() {
        return ins;
    }

    public void setIns(int ins) {
        this.ins = ins;
    }

    @Override
    public String toString() {
        return "MediaDot{" +
                "x=" + x +
                ", y=" + y +
                ", cx=" + cx +
                ", cy=" + cy +
                ", type=" + type +
                ", strokesID=" + strokesID +
                ", pageID=" + pageID +
                ", timelong=" + timelong +
                ", penMac='" + penMac + '\'' +
                ", videoID=" + videoID +
                ", time=" + time +
                ", audioID=" + audioID +
                ", color=" + color +
                ", width=" + width +
                ", ins=" + ins +
                '}';
    }

    /**
     * 存储格式（单行）：pageID strokesID type cx cy videoIDS timeS audioIDS penMac timelong color width
     * @return
     */
    public String storageFormat(){
        StringBuilder string = new StringBuilder();
        string.append(pageID+" ");
        string.append(strokesID+" ");
        string.append(type+" ");
        string.append(cx+" ");
        string.append(cy+" ");
        string.append(videoID +" ");
        string.append(time +" ");
        string.append(audioID +" ");
        string.append(penMac+" ");
        string.append(timelong+" ");
        string.append(color+" ");
        string.append(width+" ");
        string.append(ins);
        return string.toString();
    }

    /**
     * 将单行“笔迹点”字符串解析为MediaDot类型对象，遵循storageFormat()中定义的字符串格式
     * @param line 单行“笔迹点”字符串
     * @return 解析后的MediaDot类型对象
     */
    public static MediaDot parse(String line){
        int start=0,end=0;

        MediaDot mediaDot = new MediaDot();

        end = line.indexOf(" ", start);
        mediaDot.pageID = Integer.valueOf(line.substring(start, end));//PageID

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.strokesID = Integer.valueOf(line.substring(start, end));//strokesID

        start = end+1;
        end = line.indexOf(" ", start);
        String s = line.substring(start, end);
        switch (s){
            case "PEN_DOWN":
                mediaDot.type = Dot.DotType.PEN_DOWN;
                break;
            case "PEN_MOVE":
                mediaDot.type = Dot.DotType.PEN_MOVE;
                break;
            case "PEN_UP":
                mediaDot.type = Dot.DotType.PEN_UP;
                break;
            default:
        }
        //        mediaDot.type = Dot.DotType.valueOf(line.substring(start, end));//type

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.setCx(Float.valueOf(line.substring(start, end)));//横坐标

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.setCy(Float.valueOf(line.substring(start, end)));//纵坐标

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.videoID = Integer.valueOf(line.substring(start, end));//视频ID

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.time = Float.valueOf(line.substring(start, end));//视频进度

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.audioID = Integer.valueOf(line.substring(start, end));//音频ID

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.penMac = line.substring(start, end);//笔mac地址

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.timelong = Long.valueOf(line.substring(start, end));//时间戳

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.color = Integer.valueOf(line.substring(start, end));//颜色

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.width = Integer.valueOf(line.substring(start, end));//宽度

        start = end+1;
        mediaDot.ins = Integer.valueOf(line.substring(start));//命令Id

        //笔划ID为程序内部生成，仅作用于内部，不存于文件，故在方法外面单独处理
        return mediaDot;
    }

    //判断是否为空点，即只存储时序信息，不存储实际坐标的点
    public boolean isEmptyDot(){
        if((x == -1 && y == -1) || (x == -2 && y == -2) || (x == -3 && y == -3) || (x == -4 && y == -4) || (x == -5 && y == -5)){
            return true;
        }
        return false;
    }

    /**
     * 判断是否附加了音频信息
     * @return
     */
    public boolean isAudioDot(){
        if(audioID == XApp.DEFAULT_INT){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 判断是否附加了视频信息
     * @return
     */
    public boolean isVideoDot(){
        if(videoID == XApp.DEFAULT_INT){
            return false;
        }else {
           return true;
        }
    }

    /**
     * 是否为字符命令笔迹点
     * @return
     */
    public boolean isCharInstruction(){
        return this.ins > 0 && this.ins != 1 && this.ins != 2 && this.ins != 3 && this.ins < 10;
    }

}
