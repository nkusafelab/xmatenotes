package com.example.xmatenotes.logic.model.handwriting;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.presetable.LogUtil;
import com.google.gson.Gson;
import com.tqltech.tqlpencomm.bean.Dot;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 笔划类
 */
public class Stroke implements Serializable, Cloneable {

    private static final String TAG = "Stroke";
    private static final long serialVersionUID = -4140045423314807525L;

    /**
     * 点列表
     */
    private ArrayList<SimpleDot> dots = new ArrayList<>();

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
     * 实时最小矩形
     */
    private SerializableRectF boundRect = new SerializableRectF();
    private boolean isClosed = false;

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

    /**
     * 所在版面宽
     */
    private float pageWidth;

    /**
     * 所在版面高
     */
    private float pageHeight;


    public Stroke(long prePeriod) {
        this.prePeriod = prePeriod;
    }

//    /**
//     * 请确保传入的点参数正确
//     * @param bdot
//     * @return
//     */
//    public Stroke addDot(BaseDot bdot){
//        if(bdot instanceof SimpleDot){
//            SimpleDot sd = (SimpleDot)bdot;
//            if(dots.isEmpty()){
//                firsttime = sd.timelong;
//            }
//            if(firsttime != XmateNotesApplication.DEFAULT_LONG){
//                duration = sd.timelong - firsttime;
//            }
//        }
//
//        if(dots.isEmpty()){
//            boundRect.left = bdot.getFloatX();
//            boundRect.right = bdot.getFloatX();
//            boundRect.top = bdot.getFloatY();
//            boundRect.bottom = bdot.getFloatY();
//        }
//
//        dots.add(bdot);
//
//        boundRect.union(bdot.getFloatX(), bdot.getFloatY());
//
//        if(bdot instanceof SimpleDot){
//            SimpleDot sd = (SimpleDot)bdot;
//            if(sd.type == Dot.DotType.PEN_UP){
//                isClosed = true;
//            }
//        }
//        return this;
//    }

    /**
     * 请确保传入的点参数正确
     * @param sDot
     * @return
     */
    public Stroke addDot(SimpleDot sDot){

        if(dots.isEmpty()){
            firsttime = sDot.timelong;
            boundRect.left = sDot.getFloatX();
            boundRect.right = sDot.getFloatX();
            boundRect.top = sDot.getFloatY();
            boundRect.bottom = sDot.getFloatY();
        }

        if(firsttime != XmateNotesApplication.DEFAULT_LONG){
            duration = sDot.timelong - firsttime;
        }

        dots.add(sDot);

        boundRect.union(sDot.getFloatX(), sDot.getFloatY());
        LogUtil.e(TAG, "Stroke's boundRect: "+boundRect);

        if(sDot.type == Dot.DotType.PEN_UP){
            isClosed = true;
        }
        return this;
    }

    /**
     * 未接收到PEN_UP点的主动闭合方式
     */
    public void close(){
        if(!isClosed){
//            BaseDot bDot = this.dots.get(dots.size()-1);
//            if(bDot instanceof SimpleDot){
//                SimpleDot sDot = new SimpleDot((SimpleDot) bDot);
//                sDot.type = Dot.DotType.PEN_UP;
//                dots.add(sDot);
//            }
            SimpleDot sDot = new SimpleDot(this.dots.get(dots.size()-1));
            sDot.type = Dot.DotType.PEN_UP;
            dots.add(sDot);
            isClosed = true;
        }
    }

    public boolean isClosed(){
        return isClosed;
    }

    public void setPrePeriod(long prePeriod) {
        this.prePeriod = prePeriod;
    }

    public SimpleDot getFirstDot(){
        if(dots.size() > 0){
            return dots.get(0);
        }
        return null;
    }

    public SerializableRectF getBoundRectF() {
        return boundRect;
    }

    public ArrayList<SimpleDot> getDots() {
        return dots;
    }

    public long getPrePeriod() {
        return prePeriod;
    }

    public long getFirstTime() {
        return firsttime;
    }

    public long getLastTime() {
        return firsttime+duration;
    }

    public long getDuration() {
        return duration;
    }

    public int getDotsNumber(){
        return dots.size();
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

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Stroke stroke = (Stroke) super.clone();
        stroke.dots = (ArrayList<SimpleDot>) this.dots.clone();
        Gson gson = new Gson();
        stroke.boundRect = gson.fromJson(gson.toJson(this.boundRect), SerializableRectF.class);
        return stroke;
    }

    @Override
    public String toString() {
        return "Stroke{" +
                "dots=" + dots +
                ", prePeriod=" + prePeriod +
                ", firsttime=" + firsttime +
                ", duration=" + duration +
                ", boundRect=" + boundRect +
                ", isClosed=" + isClosed +
                ", color=" + color +
                ", width=" + width +
                ", pageWidth=" + pageWidth +
                ", pageHeight=" + pageHeight +
                '}';
    }
}
