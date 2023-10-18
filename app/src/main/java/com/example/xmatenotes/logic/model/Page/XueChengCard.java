package com.example.xmatenotes.logic.model.Page;

import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;
import com.example.xmatenotes.util.LogUtil;

/**
 * 学程卡片，组成完整学程页面
 */
public class XueChengCard extends Page{
    private static final String TAG = "XueChengCard";

    private static final long serialVersionUID = -3788224606221847952L;

    /**
     * 学程卡片编码
     * 2位子域编码, 用于组合版面, 表示部分与整体的关系;"FF"表示没有子域
     */
    private String subCode = "FF";

    /**
     * 本页PageID属性
     */
    private long pageID;

    /**
     * 页号。初始值为-1
     */
    private int pageNumber = -1;

    public XueChengCard(String subCode, long pageID, int pageNumber, float realLeft, float realTop, float realWidth, float realHeight) {
        this.subCode = subCode;
        this.pageID = pageID;
        this.pageNumber = pageNumber;
        this.code = getCodeByPageId(this.pageID)+this.subCode;
        setPosition(realLeft, realTop);
        setRealDimensions(realWidth, realHeight);
    }

    protected String getCodeByPageId(long pageId){
        String pi = String.valueOf(pageId);
        if(pi.length() < 5){//是四位数
            int n = 4 - pi.length();
            StringBuffer sb = new StringBuffer();
            while (n > 0){
                sb.append("0");
                n--;
            }
            return sb+pi;
        } else {
            LogUtil.e(TAG, "getCodeByPageId: pageId超出4位");
        }
        return null;
    }

    @Override
    public String getPageName() {
        this.pageStorageName = getPageStorageName(this.code, this.createTime);
        return this.pageStorageName;
    }

    public String getSuperPageName(){
        String superCode = this.code.substring(0,4)+"00"+this.createTime;
        return getPageStorageName(superCode, this.createTime);
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }


    /**
     * 是否包含目标singleHandWriting
     * @param singleHandWriting
     * @return
     */
    public boolean contains(SingleHandWriting singleHandWriting){
        SimpleDot dot = singleHandWriting.getFirstDot();
        if(dot != null){
            if(this.realLeft <= dot.getFloatX() && (this.realLeft + this.realWidth) > dot.getFloatX() && this.realTop <= dot.getFloatY() && (this.realTop + this.realHeight) > dot.getFloatY()){
                return true;
            }
        }
        return false;
    }

}
