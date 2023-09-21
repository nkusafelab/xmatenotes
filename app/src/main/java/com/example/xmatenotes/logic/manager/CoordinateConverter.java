package com.example.xmatenotes.logic.manager;

import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.util.LogUtil;

/**
 * 坐标转换器
 */
public class CoordinateConverter {

    private static final String TAG = "CoordinateConverter";

    /**
     * UI版面宽度
     */
    private float showWidth;

    /**
     * UI版面高度
     */
    private float showHeight;

    /**
     * 真实物理版面宽度
     */
    private float realWidth;

    /**
     * 真实物理版面高度
     */
    private float realHeight;

    public CoordinateConverter(float showWidth, float showHeight, float realWidth, float realHeight) {
        this.showWidth = showWidth;
        this.showHeight = showHeight;
        this.realWidth = realWidth;
        this.realHeight = realHeight;
        LogUtil.e(TAG, toString());
    }

    /**
     * 将UI坐标转换为内部的真实物理坐标
     * @param outSimpleDot
     * @return
     */
    public SimpleDot convertIn(SimpleDot outSimpleDot){
        SimpleDot inSimpleDot = null;
        if(outSimpleDot instanceof MediaDot){
            inSimpleDot = new MediaDot((MediaDot) outSimpleDot);
        } else {
            inSimpleDot = new SimpleDot(outSimpleDot);
        }
        inSimpleDot.setX((inSimpleDot.getFloatX() / this.showWidth) * this.realWidth);
        inSimpleDot.setY((inSimpleDot.getFloatY() / this.showHeight) * this.realHeight);
        LogUtil.e(TAG, "convertIn: "+inSimpleDot);
        return inSimpleDot;
    }

    /**
     * 将内部的真实物理坐标转换为Ui坐标
     * @param inSimpleDot
     * @return
     */
    public SimpleDot convertOut(SimpleDot inSimpleDot){
        SimpleDot outSimpleDot = null;
        if(inSimpleDot instanceof MediaDot){
            outSimpleDot = new MediaDot((MediaDot) inSimpleDot);
        } else {
            outSimpleDot = new SimpleDot(inSimpleDot);
        }
        outSimpleDot.setX((outSimpleDot.getFloatX() / this.realWidth) * this.showWidth);
        outSimpleDot.setY((outSimpleDot.getFloatY() / this.realHeight) * this.showHeight);
        LogUtil.e(TAG, "convertOut: "+outSimpleDot);
        return outSimpleDot;
    }

    public float getShowWidth() {
        return showWidth;
    }

    public void setShowWidth(float showWidth) {
        this.showWidth = showWidth;
    }

    public float getShowHeight() {
        return showHeight;
    }

    public void setShowHeight(float showHeight) {
        this.showHeight = showHeight;
    }

    public float getRealWidth() {
        return realWidth;
    }

    public void setRealWidth(float realWidth) {
        this.realWidth = realWidth;
    }

    public float getRealHeight() {
        return realHeight;
    }

    public void setRealHeight(float realHeight) {
        this.realHeight = realHeight;
    }

    @Override
    public String toString() {
        return "CoordinateConverter{" +
                "showWidth=" + showWidth +
                ", showHeight=" + showHeight +
                ", realWidth=" + realWidth +
                ", realHeight=" + realHeight +
                '}';
    }
}
