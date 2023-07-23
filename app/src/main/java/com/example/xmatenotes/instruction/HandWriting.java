package com.example.xmatenotes.instruction;

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import com.example.xmatenotes.App.XApp;
import com.example.xmatenotes.DotClass.MediaDot;

/**
 * <p><strong>普通书写</strong></p>
 * 存储一次普通书写笔划的相关信息
 */
public class HandWriting extends Instruction{

    private static final String TAG = "HandWriting";

    /**
     * 定义一次普通书写笔划完毕后，最大延迟响应时间，单位ms
     */
    public final static long DELAY_PERIOD = 2000;

    /**
     * 单次笔迹最大延迟响应时间，单位ms
     */
    public final static long SINGLE_HANDWRITING_DELAY_PERIOD = 5000;

    //组成同一普通书写笔迹的所有笔迹点所对应的下列变量值应是相同的
    /**
     * 存储该普通书写笔迹对应视频碎片时刻信息
     */
    private float time = XApp.DEFAULT_FLOAT;

    /**
     * 存储该普通书写笔迹对应视频碎片ID信息
     */
    private int videoID = XApp.DEFAULT_INT;

    /**
     * 存储该普通书写笔迹对应音频碎片文件名信息(录音文件ID)
     */
//    private String audioName = "";
    private int audioID = XApp.DEFAULT_INT;

    /**
     * 存储该普通书写笔迹对应笔Mac地址信息
     */
    private String penMac = "";

    /**
     * 索引所在的单次笔迹
     */
    private int localHWsMapID = XApp.DEFAULT_INT;

    /**
     * 记录普通书写笔迹对应的平面区域，由组成该普通书写笔迹的若干笔划对应的平面矩形合并而成
     */
    private Region region = new Region();

    public HandWriting() {
    }

    public void addRect(Rect rect){
        region.union(rect);
    }

    public Region getRegion(){
        return region;
    }

    /**
     * 是否包含目标点
     * @param x
     * @param y
     * @return
     */
    public boolean contains(int x, int y){
        return region.contains(x, y);
    }

    /**
     * 初始化普通书写笔迹各属性
     * @param time
     * @param videoID
     * @param audioID
     * @param penMac
     */
    public void addAttribute(float time, int videoID, int audioID, String penMac){
        this.time = time;
        this.videoID = videoID;
        this.audioID = audioID;
        this.penMac = penMac;
    }

    /**
     * 初始化普通书写笔迹各属性
     * @param mediaDot
     */
    public void addAttribute(MediaDot mediaDot){
        this.time = mediaDot.time;
        this.videoID = mediaDot.videoID;
        this.audioID = mediaDot.audioID;
        this.penMac = mediaDot.penMac;
    }

    public float getTime() {
        return time;
    }

    public int getVideoID() {
        return videoID;
    }

    public int getAudioID() {
        return audioID;
    }

    public String getPenMac() {
        return penMac;
    }

    public int getLocalHWsMapID() {
        return localHWsMapID;
    }

    public void setLocalHWsMapID(int localHWsMapID) {
        this.localHWsMapID = localHWsMapID;
    }

    @Override
    public String toString() {
        return "HandWriting{" +
                "time=" + time +
                ", videoID=" + videoID +
                ", audioID=" + audioID +
                ", penMac='" + penMac + '\'' +
                ", localHWsMapID=" + localHWsMapID +
                ", region=" + region +
                '}';
    }
}
