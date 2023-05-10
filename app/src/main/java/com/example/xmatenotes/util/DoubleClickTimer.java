package com.example.xmatenotes.util;

import com.example.xmatenotes.instruction.DoubleClick;
import com.example.xmatenotes.instruction.HandWriting;

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
