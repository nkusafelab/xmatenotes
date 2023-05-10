package com.example.xmatenotes.instruction;

/**
 * <p><strong>双击</strong></p>
 */
public class DoubleClick extends Instruction{

    private static final String TAG = "DoubleClick";

    //时间常量
    public final static int DOUBLE_CLICK_PERIOD = 300;//定义双击的两次点击最大时间间隔

    //空间常量
    public final static double SINGLE_CLICK_dLIMIT = 0.7;//定义单击笔划的最大上下或左右距离
    public final static long SINGLE_CLICK_tLIMIT = 300;//定义单击笔划的最大时间跨度

}
