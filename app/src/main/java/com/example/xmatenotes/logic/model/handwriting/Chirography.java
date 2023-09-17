package com.example.xmatenotes.logic.model.handwriting;

import android.graphics.RectF;

import java.util.ArrayList;

/**
 * 笔迹接口
 */
public interface Chirography {

    /**
     * 获取第一个点
     * @return
     */
    public BaseDot getFirstDot();

    /**
     * 获取最小矩形
     * @return
     */
    public RectF getBoundRectF();

    /**
     * 获取笔迹实体
     * @return
     */
    public ArrayList<Chirography> getChirography();

    /**
     * 获取距前一笔划时间间隔
     * @return
     */
    public long getPrePeriod();

    /**
     * 设置距前一笔划时间间隔
     * @param prePeriod
     */
    public void setPrePeriod(long prePeriod);

    /**
     * 获取第一个点时间戳
     * @return
     */
    public long getFirsttime();

    /**
     * 获取笔划时间间隔
     * @return
     */
    public long getDuration();
}
