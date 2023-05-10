package com.example.xmatenotes.DotClass;

import com.tqltech.tqlpencomm.bean.Dot;


/**
 * 识别所需最简的点，坐标为包含小数部分的完整坐标
 */
public class SimpleDot {
    public float x;
    public float y;
    public Dot.DotType type;

    public SimpleDot() {
    }

    public SimpleDot(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public SimpleDot(Dot dot){
        this.x = computeCompletedDot(dot.x, dot.fx);
        this.y = computeCompletedDot(dot.y, dot.fy);
        this.type = dot.type;
    }

    public SimpleDot(MediaDot mediaDot) {
        this.x = mediaDot.getCx();
        this.y = mediaDot.getCy();
        this.type = mediaDot.type;
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
     * 计算两点距离
     * @param sDot1
     * @param sDot2
     * @return
     */
    public static double computeDistance(SimpleDot sDot1, SimpleDot sDot2){
        return Math.sqrt((sDot2.x - sDot1.x)*(sDot2.x - sDot1.x) + (sDot2.y - sDot1.y)*(sDot2.y - sDot1.y));
    }

    //SimpleDot点转换为Dot类型

    @Override
    public String toString() {
        return "SimpleDot{" +
                "x=" + x +
                ", y=" + y +
                ", type=" + type +
                '}';
    }
}
