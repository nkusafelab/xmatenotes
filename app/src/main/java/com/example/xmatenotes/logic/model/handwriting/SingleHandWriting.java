package com.example.xmatenotes.logic.model.handwriting;

import android.util.Log;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.util.LogUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 单次迭代笔迹
 * 颜色相同
 */
public class SingleHandWriting implements Serializable {

    private static final String TAG = "SingleHandWriting";
    private static final long serialVersionUID = 5502460460773096103L;

    /**
     * 单次笔迹最大延迟响应时间，单位ms
     */
    public final static long SINGLE_HANDWRITING_DELAY_PERIOD = 5000;

    /**
     * 书写笔迹列表
     */
    private ArrayList<HandWriting> handWritings = new ArrayList<>();

    /**
     * 包含的书写笔迹数目
     */
    private int handWritingsNumber = 0;

    /**
     * 距前一笔划时间间隔
     */
    private long prePeriod = XmateNotesApplication.DEFAULT_LONG;

    /**
     * 第一个点时间戳
     */
    private long firsttime = XmateNotesApplication.DEFAULT_LONG;

    /**
     * 实时笔划时间间隔
     */
    private long duration = XmateNotesApplication.DEFAULT_LONG;

    /**
     * 是否是新笔迹
     */
    private boolean isNew = true;
//    private Region region = new Region();
    private SerializableRectF boundRect = new SerializableRectF();

    /**
     * 表示单次笔迹是否完整
     */
    private boolean isClosed = false;

    public SingleHandWriting(){
        this(0);
    }

    public SingleHandWriting(long prePeriod) {
        this.prePeriod = prePeriod;
    }

    /**
     *
     * @param handWriting 至少有一个点
     * @return
     */
    public SingleHandWriting addHandWriting(HandWriting handWriting){

        if(this.handWritings.isEmpty()){
            duration = handWriting.getDuration();
        } else {
            duration += (handWriting.getPrePeriod() +handWriting.getDuration());
        }

        if(firsttime == XmateNotesApplication.DEFAULT_LONG){
            firsttime = handWriting.getFirsttime();
        }

//        region.union(HandWriting.rectFToRect(handWriting.getBoundRectF()));
//        boundRect.union(handWriting.getBoundRectF());初始handWriting.getBoundRectF()为单点
        this.handWritings.add(handWriting);
        this.handWritingsNumber++;
        LogUtil.e(TAG, "addHandWriting: "+handWriting);

        return this;
    }

    /**
     * 主动闭合
     */
    public void close(){
        if(!isClosed){
            if(handWritingsNumber == 0){

            } else {
                computeRect();
                this.handWritings.get(handWritings.size()-1).close();
            }
            isClosed = true;
            LogUtil.e(TAG, "close");
        }
    }

    /**
     * 计算boundRect
     * SingleHandWriting的boundRect必须通过主动调用此方法来计算获得，否则boundRect没有面积，无效
     */
    public void computeRect(){
        SerializableRectF rectF = new SerializableRectF();
        for (HandWriting handWriting: this.handWritings) {
            if(handWriting.getBoundRectF().equals(rectF)){
                continue;
            }
            boundRect.union(handWriting.getBoundRectF());
        }
        LogUtil.e(TAG, "computeRect");
    }

//    public boolean isEmpty(){
//        return
//    }

    public void setPrePeriod(long prePeriod) {
        this.prePeriod = prePeriod;
    }

    public SimpleDot getFirstDot(){
        if(handWritings.size() > 0){
            return handWritings.get(0).getFirstDot();
        }
        return null;
    }

    public SimpleDot getLastDot(){
        if(!handWritings.isEmpty()){
            return handWritings.get(handWritings.size()-1).getLastDot();
        }
        LogUtil.e(TAG, "getLastDot: handWritings为空！");
        return null;
    }

    public HandWriting getLastHandWriting(){
        if(!handWritings.isEmpty()){
            return handWritings.get(handWritings.size()-1);
        }
        return null;
    }

    public HandWriting getFirstHandWriting(){
        if(handWritings.size() > 0){
            return handWritings.get(0);
        }
        return null;
    }

    public SerializableRectF getBoundRectF() {
        return boundRect;
    }

    public ArrayList<HandWriting> getHandWritings() {
        return handWritings;
    }

    public long getPrePeriod() {
        return prePeriod;
    }

    public long getFirsttime() {
        return firsttime;
    }

    public long getDuration() {
        return duration;
    }

    public int getHandWritingsNumber() {
        return handWritingsNumber;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public int size(){
        return this.handWritingsNumber;
    }

    public boolean contains(SimpleDot simpleDot){
        for (HandWriting handWriting: this.handWritings) {
            if(handWriting.contains(simpleDot)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "SingleHandWriting{" +
                "handWritings=" + handWritings +
                ", handWritingsNumber=" + handWritingsNumber +
                ", prePeriod=" + prePeriod +
                ", firsttime=" + firsttime +
                ", duration=" + duration +
                ", isNew=" + isNew +
                ", boundRect=" + boundRect +
                ", isClosed=" + isClosed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleHandWriting that = (SingleHandWriting) o;
        return handWritingsNumber == that.handWritingsNumber && prePeriod == that.prePeriod && firsttime == that.firsttime && duration == that.duration && Objects.equals(boundRect, that.boundRect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handWritingsNumber, prePeriod, firsttime, duration, boundRect);
    }
}
