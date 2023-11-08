package com.example.xmatenotes.logic.manager;

import android.graphics.Rect;

import java.util.Objects;

/**
 * 局部区域信息块
 */
public class LocalRect {
    public Rect rect;//区域范围
    public int firstLocalCode;//一级局域编码
    public int secondLocalCode;//二级局域编码
    public String localName;//二级区域标识
    public String addInf = null;//提示区信息，只有当二级区域标识为“资源卡”时，值不为null
    public boolean leftOrRight;//标记区域位于左半页或者右半页;true表示左半页，false表示右半页

    public LocalRect(){
        rect = new Rect();
    }

    public LocalRect(Rect rect, int firstLocalCode, int secondLocalCode, String localName, String addInf, boolean leftOrRight) {
        this.rect = rect;
        this.firstLocalCode = firstLocalCode;
        this.secondLocalCode = secondLocalCode;
        this.localName = localName;
        this.addInf = addInf;
        this.leftOrRight = leftOrRight;
    }

    public LocalRect(boolean leftOrRight) {
        this.leftOrRight = leftOrRight;
        rect = new Rect();
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public void setFirstLocalCode(int firstLocalCode) {
        this.firstLocalCode = firstLocalCode;
    }

    public void setSecondLocalCode(int secondLocalCode) {
        this.secondLocalCode = secondLocalCode;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public void setLeftOrRight(boolean leftOrRight) {
        this.leftOrRight = leftOrRight;
    }

    //获取完整局域编码
    public String getLocalCode() {
        return firstLocalCode+"-"+secondLocalCode;
    }

    //可能为null
    public String getAddInf() {
        return addInf;
    }

    public void setAddInf(String addInf) {
        this.addInf = addInf;
    }

    public int getVideoIDByAddInf(){
        if("资源卡".equals(localName)){
            return Integer.valueOf(addInf.substring(0, 3));
        }else {
            return -1;
        }
    }
    public String getVideoNameByAddInf(){
        if("资源卡".equals(localName)){
            int start=3,end=0;
            end = addInf.indexOf(".", start);
            return addInf.substring(start, end);
        }else {
            return null;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalRect localRect = (LocalRect) o;
        return firstLocalCode == localRect.firstLocalCode && secondLocalCode == localRect.secondLocalCode && leftOrRight == localRect.leftOrRight && Objects.equals(rect, localRect.rect) && Objects.equals(localName, localRect.localName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rect, firstLocalCode, secondLocalCode, localName, leftOrRight);
    }

    @Override
    public String toString() {
        return "LocalRect{" +
                "rect=" + rect +
                ", firstLocalCode=" + firstLocalCode +
                ", secondLocalCode=" + secondLocalCode +
                ", localName='" + localName + '\'' +
                ", leftOrRight=" + leftOrRight +
                '}';
    }
}
