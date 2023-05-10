package com.example.xmatenotes.DotClass;

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

    /**
     * 判断是否为空点，即只存储时序信息，不存储实际坐标的点
     * @return 是空点则返回true
     */
    public boolean isEmptyDot(){
        if((x == -1 && y == -1) || (x == -2 && y == -2) || (x == -3 && y == -3) || (x == -4 && y == -4) || (x == -5 && y == -5)){
            return true;
        }
        return false;
    }
}
