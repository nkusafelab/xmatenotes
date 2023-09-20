package com.example.xmatenotes.logic.model.handwriting;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.xmatenotes.App.XmateNotesApplication;
import com.google.gson.Gson;
import com.tqltech.tqlpencomm.bean.Dot;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 可能会是命令的笔迹
 */
public class HandWriting implements Serializable,Cloneable {

    private static final String TAG = "HandWriting";
    private static final long serialVersionUID = -8355882537505201068L;

    /**
     * 定义一次普通书写笔划完毕后，最大延迟响应时间，单位ms
     */
    public final static long DELAY_PERIOD = 2000;

    /**
     * 笔划列表
     */
    private ArrayList<Stroke> strokes = new ArrayList<>();

    /**
     * 包含的笔划数目
     */
    private int strokesNumber = 0;

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
    private Region region = new Region();
    private SerializableRectF boundRect = new SerializableRectF();

    /**
     * 表示普通书写是否完整
     */
    private boolean isClosed = false;

    /**
     * 音频文件ID
     */
    private int audioId = XmateNotesApplication.DEFAULT_INT;

    /***************************形态信息*****************************/
    /**
     * 颜色
     */
    public int color = DEFAULT_COLOR;
    private static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEEP_GREEN = Color.rgb(5, 102, 8);
    public static final int DEEP_ORANGE = Color.rgb(243,117,44);

    /**
     * 大小(或宽度)
     */
    public int width = DEFAULT_WIDTH;
    public static final int DEFAULT_WIDTH = 1;
    public static final int DEFAULT_BOLD_WIDTH = 3;

    /***************************形态信息*****************************/


    public HandWriting(){
    }

    public HandWriting(long prePeriod, long firsttime) {
        this.prePeriod = prePeriod;
        this.firsttime = firsttime;
    }

//    /**
//     * 请确保传入的点参数正确
//     * @param bdot
//     * @return
//     */
//    public HandWriting addDot(BaseDot bdot){
//
//        if(strokes.isEmpty()){
//            this.strokes.add(new Stroke(this.prePeriod));
//            boundRect.left = bdot.getFloatX();
//            boundRect.right = bdot.getFloatX();
//            boundRect.top = bdot.getFloatY();
//            boundRect.bottom = bdot.getFloatY();
//        }
//
//        if(bdot instanceof SimpleDot){
//
//            SimpleDot sd = (SimpleDot)bdot;
//            if(firsttime == XmateNotesApplication.DEFAULT_LONG){
//                firsttime = sd.timelong;
//            }
//
//            if(sd.type == Dot.DotType.PEN_DOWN && strokesNumber > 0){
//                this.strokes.add(new Stroke(sd.timelong - this.strokes.get(strokes.size()-1).getLastTime()));
//            }
//
//            if(firsttime != XmateNotesApplication.DEFAULT_LONG){
//                duration = sd.timelong - firsttime;
//            }
//        }
//
//        Stroke stroke = this.strokes.get(strokes.size()-1);
//
//        stroke.addDot(bdot);
//        isClosed = false;
//        boundRect.union(bdot.getFloatX(), bdot.getFloatY());
//
//        if(bdot instanceof SimpleDot){
//            SimpleDot sd = (SimpleDot)bdot;
//            if(sd.type == Dot.DotType.PEN_UP){
//                strokesNumber++;
//                region.union(rectFToRect(stroke.getBoundRectF()));
//                isClosed = true;
//            }
//        }
//
//        return this;
//    }

    /**
     * 请确保传入的点参数正确
     * @param sDot
     * @return
     */
    public HandWriting addDot(SimpleDot sDot){

        if(strokes.isEmpty()){
            this.strokes.add(new Stroke(this.prePeriod));
            boundRect.left = sDot.getFloatX();
            boundRect.right = sDot.getFloatX();
            boundRect.top = sDot.getFloatY();
            boundRect.bottom = sDot.getFloatY();
        }

        if(firsttime == XmateNotesApplication.DEFAULT_LONG){
            firsttime = sDot.timelong;
        }

        if(sDot.type == Dot.DotType.PEN_DOWN){
            if(strokesNumber > 0){
                this.strokes.add(new Stroke(sDot.timelong - this.strokes.get(strokes.size()-1).getLastTime()));
            }
            this.strokesNumber++;
        }

        if(firsttime != XmateNotesApplication.DEFAULT_LONG){
            duration = sDot.timelong - firsttime;
        }

        Stroke stroke = this.strokes.get(strokes.size()-1);

        stroke.addDot(sDot);

        if(sDot instanceof  MediaDot){
            sDot = (MediaDot)sDot;
            this.audioId = ((MediaDot) sDot).audioID;
        }

        isClosed = false;
        boundRect.union(sDot.getFloatX(), sDot.getFloatY());

        //自动闭合
        if(sDot.type == Dot.DotType.PEN_UP){
            region.union(rectFToRect(stroke.getBoundRectF()));
            close();
        }

        return this;
    }

    /**
     * 未接收到PEN_UP点的主动闭合方式
     * 一般在普通书写完后一段时间主动调用闭合
     */
    public void close(){
        if(!isClosed()){
//            if(strokesNumber == 0){
//                region.union(rectFToRect(boundRect));
//            } else {
//                Stroke stroke = this.strokes.get(strokes.size()-1);
//                stroke.close();
//                region.union(rectFToRect(stroke.getBoundRectF()));
//            }
            isClosed = true;
        }
    }

    public void clear(){
        this.strokes.clear();
        this.strokesNumber = 0;
        this.firsttime = XmateNotesApplication.DEFAULT_LONG;
        this.boundRect = new SerializableRectF();
        this.region.setEmpty();
    }

    public boolean isClosed(){
        return isClosed;
    }

    public boolean isEmpty(){
        return this.strokes.isEmpty();
    }

    public void setPrePeriod(long prePeriod) {
        this.prePeriod = prePeriod;
    }

    public SimpleDot getFirstDot(){
        if(strokes.size() > 0){
            return strokes.get(0).getFirstDot();
        }
        return null;
    }

    public Stroke getFirstStroke(){
        if(strokes.size() > 0){
            return strokes.get(0);
        }
        return null;
    }

    public SerializableRectF getBoundRectF() {
        return this.boundRect;
    }

    public ArrayList<Stroke> getStrokes() {
        return strokes;
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

    public int getStrokesNumber() {
        return strokesNumber;
    }

    public int getColor() {
        return color;
    }

    public int getWidth() {
        return width;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getAudioId() {
        return audioId;
    }

    public void setAudioId(int audioId) {
        this.audioId = audioId;
    }

    /**
     * 是否包含某个点
     * @param simpleDot
     * @return
     */
    public boolean contains(SimpleDot simpleDot){
        return this.region.contains(simpleDot.getIntX(), simpleDot.getIntY());
    }

    public static Rect rectFToRect(RectF rectF){
        return new Rect((int)rectF.left, (int)rectF.top, (int)Math.ceil(rectF.right), (int)Math.ceil(rectF.bottom));
    }

    public static Rect rectFToRect(SerializableRectF rectF){
        return new Rect((int)rectF.left, (int)rectF.top, (int)Math.ceil(rectF.right), (int)Math.ceil(rectF.bottom));
    }

    @NonNull
    @Override
    public HandWriting clone() {
        HandWriting handWriting = null;
        try {
            handWriting = (HandWriting)super.clone();
            handWriting.strokes = (ArrayList<Stroke>) this.strokes.clone();
            handWriting.boundRect = this.boundRect.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
//        handWriting.boundRect = (SerializableRectF) copy(this.boundRect);
        handWriting.region = (Region) copy(this.region);
        return handWriting;
    }

    public static Parcelable copy(Parcelable input) {
        Parcel parcel = null;
        try {
            parcel = Parcel.obtain();
            parcel.writeParcelable(input, 0);

            parcel.setDataPosition(0);
            return parcel.readParcelable(input.getClass().getClassLoader());
        } finally {
            if (null != parcel) {
                parcel.recycle();
            }
        }
    }

}
