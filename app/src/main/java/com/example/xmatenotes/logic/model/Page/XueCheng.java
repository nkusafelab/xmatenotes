package com.example.xmatenotes.logic.model.Page;

import com.example.xmatenotes.app.ax.A3;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;
import com.example.xmatenotes.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class XueCheng extends CompositePage {

    private static final String TAG = "XueCheng";

    private static final long serialVersionUID = 8993160003099341776L;

    /**
     * 本页PageID属性
     */
    private long pageId;

    /**
     * 页号。初始值为-1
     */
    private int pageNumber = -1;

    /**
     * 子Page
     */
    private Map<String, Page> subPages = new HashMap<>();

    public XueCheng(long pageId, int pageNumber) {
        this.pageId = pageId;
        this.pageNumber = pageNumber;
        this.code = getCodeByPageId(this.pageId)+"00";

        this.setRealDimensions((float) (A3.PAPER_WIDTH-8-7.5), (float) (A3.PAPER_HEIGHT-8-7.5));
//        this.setRealDimensions(A3.ABSCISSA_RANGE,A3.ORDINATE_RANGE);//采用真实尺寸太大，命令识别不准

        //各子版面无交叉重合部分
        this.subPages.put("01", new XueChengCard("01", pageId, pageNumber, 0F, 0F, this.realWidth, this.realHeight));
        this.subPages.put("02", new XueChengCard("02", pageId, pageNumber, this.realWidth/2, 0F, this.realWidth/2, this.realHeight));
    }

    /**
     * 在pageId前填充0，构成4位数
     * @param pageId 不超过4位的长整型数
     * @return
     */
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
    public XueCheng addSingleHandWriting(SingleHandWriting singleHandWriting) {
        SingleHandWriting shw = getLastSingleHandWriting();
        if(shw != null){
            //分发笔迹
            Set<Map.Entry<String, Page>> set = this.subPages.entrySet();
            Iterator<Map.Entry<String, Page>> it = set.iterator();
            while (it.hasNext()){
                Map.Entry<String, Page> node = it.next();
                XueChengCard xcc = (XueChengCard)node.getValue();
                if(xcc.contains(shw)){
                    xcc.addSingleHandWriting(shw);
                    LogUtil.e(TAG, "addSingleHandWriting: 分发笔迹至: "+xcc.getCode());
                    break;
                }
            }
        } else {
            LogUtil.e(TAG, "addSingleHandWriting: 没有可分发笔迹！");
        }
        super.addSingleHandWriting(singleHandWriting);
        return this;
    }

    public String getPageName() {
        this.pageStorageName = Page.getPageStorageName(this.code, this.createTime);
        return this.pageStorageName;
    }

    /**
     *
     * @param simpleDot 真实物理坐标点
     * @return
     */
    public Page getSubPageByCoordinate(SimpleDot simpleDot){
        Set<Map.Entry<String, Page>> set = this.subPages.entrySet();
        Iterator<Map.Entry<String, Page>> it = set.iterator();
        while (it.hasNext()){
            Page subPage = (Page)it.next();
            if(subPage.getPageBounds().contains(simpleDot.getFloatX(), simpleDot.getFloatY())){
                return subPage;
            }
        }
        LogUtil.e(TAG, "未找到包含目标坐标的subPage!");
        return null;
    }
}
