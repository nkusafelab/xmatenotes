package com.example.xmatenotes.logic.manager;

import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.util.LogUtil;

import java.io.Serializable;

/**
 * 坐标转换器
 */
public class CoordinateConverter {

    private static final String TAG = "CoordinateConverter";

    private CoordinateCropper coordinateCropper;

    private CoordinateScaler coordinateScaler;

    public CoordinateConverter(float showWidth, float showHeight, float realWidth, float realHeight) {
        this(0F, 0F, showWidth, showHeight, realWidth, realHeight);
    }

    public CoordinateConverter(float left, float top, float showWidth, float showHeight, float realWidth, float realHeight) {
        this.coordinateCropper = new CoordinateCropper(left, top);
        this.coordinateScaler = new CoordinateScaler(showWidth, showHeight, realWidth, realHeight);
    }

    public CoordinateCropper getCoordinateCropper() {
        return coordinateCropper;
    }

    public void setCoordinateCropper(CoordinateCropper coordinateCropper) {
        this.coordinateCropper = coordinateCropper;
    }

    public CoordinateScaler getCoordinateScaler() {
        return coordinateScaler;
    }

    public void setCoordinateScaler(CoordinateScaler coordinateScaler) {
        this.coordinateScaler = coordinateScaler;
    }

    /**
     * 将UI坐标转换为内部的真实物理坐标
     * @param outSimpleDot
     * @return
     */
    public SimpleDot convertIn(SimpleDot outSimpleDot){
        if(this.coordinateCropper != null){
            if(this.coordinateScaler != null){
                return this.coordinateScaler.scaleIn(this.coordinateCropper.cropIn(outSimpleDot));
            } else {
                LogUtil.e(TAG, "coordinateScaler为空！");
            }
        } else {
            LogUtil.e(TAG, "coordinateCropper为空！");
        }
        return null;
    }

    /**
     * 将内部的真实物理坐标转换为Ui坐标
     * @param inSimpleDot
     * @return
     */
    public SimpleDot convertOut(SimpleDot inSimpleDot){
        if(this.coordinateCropper != null){
            if(this.coordinateScaler != null){
                return this.coordinateCropper.cropOut(this.coordinateScaler.scaleOut(inSimpleDot));
            } else {
                LogUtil.e(TAG, "coordinateScaler为空！");
            }
        } else {
            LogUtil.e(TAG, "coordinateCropper为空！");
        }
        return null;
    }

    @Override
    public String toString() {
        return "CoordinateConverter{" +
                "coordinateCropper=" + coordinateCropper +
                ", coordinateScaler=" + coordinateScaler +
                '}';
    }

    /**
     * 坐标裁剪器
     */
    public static class CoordinateCropper implements Serializable {

        private static final String TAG = "CoordinateCropper";
        private static final long serialVersionUID = 3922787934389024485L;

        private float left = 0F;

        private float top = 0F;

        public CoordinateCropper(float left, float top) {
            this.left = left;
            this.top = top;
        }

        /**
         * 裁进坐标点
         * @param outSimpleDot
         * @return 新坐标点对象
         */
        public SimpleDot cropIn(SimpleDot outSimpleDot){
            SimpleDot inSimpleDot = null;
            if(outSimpleDot instanceof MediaDot){
                inSimpleDot = new MediaDot((MediaDot) outSimpleDot);
            } else {
                inSimpleDot = new SimpleDot(outSimpleDot);
            }

            inSimpleDot.setX(inSimpleDot.getFloatX() - this.left);
            inSimpleDot.setY(inSimpleDot.getFloatY() - this.top);
            LogUtil.e(TAG, "cropIn: "+inSimpleDot);
            return inSimpleDot;
        }

        /**
         * 裁出坐标点
         * @param inSimpleDot
         * @return 新坐标点对象
         */
        public SimpleDot cropOut(SimpleDot inSimpleDot){
            SimpleDot outSimpleDot = null;
            if(inSimpleDot instanceof MediaDot){
                outSimpleDot = new MediaDot((MediaDot) inSimpleDot);
            } else {
                outSimpleDot = new SimpleDot(inSimpleDot);
            }

            outSimpleDot.setX(outSimpleDot.getFloatX() + this.left);
            outSimpleDot.setY(outSimpleDot.getFloatY() + this.top);
            LogUtil.e(TAG, "cropOut: "+outSimpleDot);
            return outSimpleDot;
        }

        public float getLeft() {
            return left;
        }

        public void setLeft(float left) {
            this.left = left;
        }

        public float getTop() {
            return top;
        }

        public void setTop(float top) {
            this.top = top;
        }

        @Override
        public String toString() {
            return "CoordinateCropper{" +
                    "left=" + left +
                    ", top=" + top +
                    '}';
        }
    }

    /**
     * 坐标缩放器
     */
    public class CoordinateScaler {

        private static final String TAG = "CoordinateScaler";

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

        public CoordinateScaler(float showWidth, float showHeight, float realWidth, float realHeight) {
            this.showWidth = showWidth;
            this.showHeight = showHeight;
            this.realWidth = realWidth;
            this.realHeight = realHeight;
        }

        /**
         * 将UI坐标转换为内部的真实物理坐标
         * @param outSimpleDot
         * @return
         */
        public SimpleDot scaleIn(SimpleDot outSimpleDot){
            SimpleDot inSimpleDot = null;
            if(outSimpleDot instanceof MediaDot){
                inSimpleDot = new MediaDot((MediaDot) outSimpleDot);
            } else {
                inSimpleDot = new SimpleDot(outSimpleDot);
            }
            inSimpleDot.setX((inSimpleDot.getFloatX() / this.showWidth) * this.realWidth);
            inSimpleDot.setY((inSimpleDot.getFloatY() / this.showHeight) * this.realHeight);
            LogUtil.e(TAG, "scaleIn: "+inSimpleDot);
            return inSimpleDot;
        }

        /**
         * 将内部的真实物理坐标转换为Ui坐标
         * @param inSimpleDot
         * @return
         */
        public SimpleDot scaleOut(SimpleDot inSimpleDot){
            SimpleDot outSimpleDot = null;
            if(inSimpleDot instanceof MediaDot){
                outSimpleDot = new MediaDot((MediaDot) inSimpleDot);
            } else {
                outSimpleDot = new SimpleDot(inSimpleDot);
            }
            outSimpleDot.setX((outSimpleDot.getFloatX() / this.realWidth) * this.showWidth);
            outSimpleDot.setY((outSimpleDot.getFloatY() / this.realHeight) * this.showHeight);
            LogUtil.e(TAG, "scaleOut: "+outSimpleDot);
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
            return "CoordinateScaler{" +
                    "showWidth=" + showWidth +
                    ", showHeight=" + showHeight +
                    ", realWidth=" + realWidth +
                    ", realHeight=" + realHeight +
                    '}';
        }
    }
}
