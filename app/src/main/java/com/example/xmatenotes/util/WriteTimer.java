package com.example.xmatenotes.util;

import com.example.xmatenotes.instruction.HandWriting;

public class WriteTimer extends Thread{
    @Override
    public void run() {
        try {
            Thread.sleep(HandWriting.DELAY_PERIOD);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }
}
