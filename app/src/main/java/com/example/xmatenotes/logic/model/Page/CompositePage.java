package com.example.xmatenotes.logic.model.Page;

import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;

/**
 * 组合式的Page，内部有嵌入式的Page
 * 主要功能为向嵌入Page分发笔迹
 */
public class CompositePage extends Page {

    private static final String TAG = "CompositePage";

    private static final long serialVersionUID = 2438091954709580872L;

    @Override
    public CompositePage addSingleHandWriting(SingleHandWriting singleHandWriting) {
        super.addSingleHandWriting(singleHandWriting);

        //分发笔迹
        return this;
    }
}
