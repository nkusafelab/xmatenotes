package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.model.handwriting.MediaDot;

import java.util.concurrent.TimeUnit;


public interface ICommandDetector {


    public ICommandDetector setConverter(float showWidth, float showHeight, float realWidth, float realHeight);

    public void setPenUpTimeWorker(int delay, TimeWorker penUpTimeWorker);

    interface TimeWorker {
        public void run(Command command);
    }

}
