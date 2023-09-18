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
        SimpleDot inSimpleDot = new SimpleDot(outSimpleDot);
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
        SimpleDot outSimpleDot = new MediaDot(inSimpleDot);
        outSimpleDot.setX((outSimpleDot.getFloatX() / this.realWidth) * this.showWidth);
        outSimpleDot.setY((outSimpleDot.getFloatY() / this.realHeight) * this.showHeight);
        LogUtil.e(TAG, "convertOut: "+outSimpleDot);
        return outSimpleDot;
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
