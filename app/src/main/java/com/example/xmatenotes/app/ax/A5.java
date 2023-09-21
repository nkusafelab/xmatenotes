package com.example.xmatenotes.app.ax;

public class A5 extends AX{
    //(本子铺码) A5纸规格尺寸：148mm × 210mm
    private static final int PAPER_WIDTH = 148;
    private static final int PAPER_HEIGHT = 210;

    //铺码生成的背景图片原始像素：4300 x 6048
    public static final int ORIGINAL_IMAGE_WIDTH = 4300;
    public static final int ORIGINAL_IMAGE_HEIGHT = 6048;

    //书写本子实际大小/mm
    public static final double WIDTH = (((double) ORIGINAL_IMAGE_WIDTH) / IN_PIXEL) * IN_SIZE;
    public static final double HEIGHT = (((double) ORIGINAL_IMAGE_HEIGHT) / IN_PIXEL) * IN_SIZE;

    //原始背景图片过大，等比例缩小图片宽高得到合适大小背景图片（减少内存开销），放到app中作为背景图片
    public static int BG_REAL_WIDTH = ORIGINAL_IMAGE_WIDTH / 4;
    public static int BG_REAL_HEIGHT = ORIGINAL_IMAGE_HEIGHT / 4;
}
