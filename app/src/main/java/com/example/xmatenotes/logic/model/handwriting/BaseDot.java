package com.example.xmatenotes.logic.model.handwriting;

import android.graphics.Point;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * 点基类
 */
public class BaseDot implements Serializable,Cloneable,Comparable<BaseDot> {

    private static final String TAG = "BaseDot";
    private static final long serialVersionUID = -2969091827840758620L;

    /**
     * 点横坐标，整数部分
     */
    protected int x;

    /**
     * 点纵坐标，整数部分
     */
    protected int y;

    /**
     * 点完整横坐标
     */
    protected float fx;

    /**
     * 点完整纵坐标
     */
    protected float fy;

    public BaseDot(int x, int y){
        setX(x);
        setY(y);
    }

    public BaseDot(float fx, float fy){
        setX(fx);
        setY(fy);
    }

    public BaseDot() {

    }

    public void setX(int x) {
        this.x = x;
        this.fx = x;
    }

    public void setX(float fx) {
        this.x = (int) fx;
        this.fx = fx;
    }

    public void setY(int y) {
        this.y = y;
        this.fy = y;
    }

    public void setY(float fy) {
        this.y = (int) fy;
        this.fy = fy;
    }

    public int getIntX(){
        return x;
    }

    public int getIntY(){
        return y;
    }

    public float getFloatX(){
        return fx;
    }

    public float getFloatY(){
        return fy;
    }

    public static float computeCompletedDot(int z, int fz){
        return (float) (z+fz/100.0);
    }

    /**
     * 计算两点距离
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double computeDistance(double x1, double y1, double x2, double y2){
        return Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));
    }

    /**
     * 判断是否为空点，即只存储时序信息，不存储实际坐标的点
     * @return 是空点则返回true
     */
    public boolean isEmptyDot(){
        if(x < 0 && y < 0){
            return true;
        }
//        if((x == -1 && y == -1) || (x == -2 && y == -2) || (x == -3 && y == -3) || (x == -4 && y == -4) || (x == -5 && y == -5)){
//            return true;
//        }
        return false;
    }

    @Override
    public String toString() {
        return "BaseDot{" +
                "x=" + x +
                ", y=" + y +
                ", fx=" + fx +
                ", fy=" + fy +
                '}';
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int compareTo(BaseDot o) {
        if(this.fx > o.fx && this.fy > o.fy){
            return 1;
        }
        if(this.fx < o.fx && this.fy < o.fy){
            return -1;
        }
        return 0;
    }
}
