package com.example.xmatenotes.util;

import com.example.xmatenotes.logic.model.instruction.DoubleClick;

public class DoubleClickTimer extends Thread{
    @Override
    public void run() {
        try {
            Thread.sleep(DoubleClick.DOUBLE_CLICK_PERIOD);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }
}
