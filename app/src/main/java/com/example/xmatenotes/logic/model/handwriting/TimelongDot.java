package com.example.xmatenotes.logic.model.handwriting;

import com.tqltech.tqlpencomm.bean.Dot;

import java.text.ParseException;

/**
 * 附加了时间戳的SimpleDot
 */
public class TimelongDot extends SimpleDot{
    private final static String TAG = "TimelongDot";

    public long timelong;

    public TimelongDot() {
    }

    public TimelongDot(float x, float y, long timelong) {
        super(x, y);
        this.timelong = timelong;
    }

    public TimelongDot(Dot dot) throws ParseException {
        super(dot);
        this.timelong = MediaDot.reviseTimelong(dot.timelong);
    }

    public TimelongDot(MediaDot mediaDot){
        super(mediaDot);
        this.timelong = mediaDot.timelong;
    }


}
