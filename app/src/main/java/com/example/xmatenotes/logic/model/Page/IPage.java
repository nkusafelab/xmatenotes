package com.example.xmatenotes.logic.model.Page;

import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;

public interface IPage {

    public IPage addSingleHandWriting(SingleHandWriting singleHandWriting);

    public String getCode();
}
