package com.example.xmatenotes.DotClass;

import android.util.Log;

import com.example.xmatenotes.App.XApp;
import com.tqltech.tqlpencomm.bean.Dot;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 附加媒体信息的点
 */
public class MediaDot {

    private final static String  TAG = "MediaDot";
    public int x;//点横坐标，整数部分
    public int y;//点纵坐标，整数部分

    private float cx;//点完整横坐标
    private float cy;//点完整纵坐标

    public Dot.DotType type;//点的类型

    public int strokesID;//笔划ID
    public int pageID;//页号

    public long timelong;//RTC，起始时间1970-01-01 00:00:00 000，单位为毫秒ms
    public float time;//视频进度
    public int videoID;//视频ID
    public int audioID;//音频ID
    public String penMac;//笔Mac地址

    public MediaDot() {
    }

    public MediaDot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public MediaDot(Dot dot) throws ParseException {
        this.x = dot.x;
        this.y = dot.y;
        this.cx = SimpleDot.computeCompletedDot(dot.x, dot.fx);
        this.cy = SimpleDot.computeCompletedDot(dot.y, dot.fy);
        this.type = dot.type;
        this.pageID = dot.PageID;
        this.timelong = reviseTimelong(dot.timelong);//修正起始时间，方便处理
    }

    public MediaDot(Dot dot, int strokesID, float timeS, int videoIDS, int audioIDS, String penMac) throws ParseException {
        this.x = dot.x;
        this.y = dot.y;
        this.cx = SimpleDot.computeCompletedDot(dot.x, dot.fx);
        this.cy = SimpleDot.computeCompletedDot(dot.y, dot.fy);
        this.type = dot.type;
        this.pageID = dot.PageID;
        this.timelong = reviseTimelong(dot.timelong);//修正起始时间，方便处理

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
    }


    public float getCx(){
        return this.cx;
    }

    public float getCy(){
        return this.cy;
    }

    public void setCx(float cx) {
        this.cx = cx;
        this.x = Math.round(cx);
    }

    public void setCy(float cy) {
        this.cy = cy;
        this.y = Math.round(cy);
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
                ", time=" + time +
                ", videoID=" + videoID +
                ", audioID=" + audioID +
                ", penMac='" + penMac + '\'' +
                '}';
    }

    //存储格式（单行）：strokesID type pageID x y timeS videoIDS audioIDS penMac timelong
    public String storageFormat(){
        StringBuilder string = new StringBuilder();
        string.append(strokesID+" ");
        string.append(type+" ");
        string.append(pageID+" ");
        string.append(x+" ");
        string.append(y+" ");
        string.append(time +" ");
        string.append(videoID +" ");
        string.append(audioID +" ");
        string.append(XApp.mBTMac+" ");
        string.append(timelong);
        return string.toString();
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

}
