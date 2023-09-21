package com.example.xmatenotes.app.ax;

public class A3 extends AX{
    //(本子铺码) A3纸规格尺寸：297mm × 420mm
    private static final int PAPER_WIDTH = 297;
    private static final int PAPER_HEIGHT = 420;
    //A3纸横坐标范围
    public static final int ABSCISSA_RANGE = (int) Math.round(PAPER_HEIGHT / XDIST_PERUNIT);
    //A3纸纵坐标范围
    public static final int ORDINATE_RANGE = (int) Math.round(PAPER_WIDTH / YDIST_PERUNIT);

    //铺码生成的背景图片原始像素
    public static final int ORIGINAL_IMAGE_WIDTH = 7020;
    public static final int ORIGINAL_IMAGE_HEIGHT = 9936;

    //书写本子实际大小/mm
    public  static final double WIDTH=((double)ORIGINAL_IMAGE_WIDTH/ IN_PIXEL)*IN_SIZE;
    public static final double HEIGHT = (((double) ORIGINAL_IMAGE_HEIGHT) / IN_PIXEL) * IN_SIZE;
}
