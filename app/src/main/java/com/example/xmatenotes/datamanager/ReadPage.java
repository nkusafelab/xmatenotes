package com.example.xmatenotes.datamanager;

public interface ReadPage {

    //书写区判定
    boolean isShuXieQu(int pageNumber, int x, int y);
    //文字区判定
    boolean isWenZiQu(int pageNumber, int x, int y);
    //资源卡判定
    boolean isZiYuanKa(int pageNumber, int x, int y);
    //认知发问区判定
    boolean isRenZhiFaWen(int pageNumber, int x, int y);
    //交叉创造区判定
    boolean isJiaoChaChuangZao(int pageNumber, int x, int y);

}
