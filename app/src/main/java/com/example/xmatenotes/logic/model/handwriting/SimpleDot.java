package com.example.xmatenotes.logic.model.handwriting;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.tqltech.tqlpencomm.bean.Dot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


/**
 * 识别所需最简的点，坐标为包含小数部分的完整坐标
 */
public class SimpleDot extends BaseDot implements Cloneable {

    private static final String TAG = "SimpleDot";
    private static final long serialVersionUID = 43869332737683388L;

    /**
     * 点的类型
     */
    public Dot.DotType type;

    /**
     * RTC，起始时间1970-01-01 00:00:00 000，单位为毫秒ms
     */
    public long timelong;

    public SimpleDot() {
        super();
    }

    public SimpleDot(float fx, float fy) {
        super(fx, fy);
    }

    public SimpleDot(float fx, float fy, Dot.DotType type, long timelong) {
        super(fx, fy);
        this.type = type;
        this.timelong = timelong;
    }

    public SimpleDot(SimpleDot simpleDot) {
        this(simpleDot.getFloatX(), simpleDot.getFloatY(), simpleDot.type, simpleDot.timelong);
    }

    public SimpleDot(Dot dot){
        this(computeCompletedDot(dot.x, dot.fx), computeCompletedDot(dot.y, dot.fy));
        this.type = dot.type;
        try {
            this.timelong = reviseTimelong(dot.timelong);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public SimpleDot(MediaDot mediaDot) {
        this(mediaDot.getFloatX(), mediaDot.getFloatY());
        this.type = mediaDot.type;
        this.timelong = mediaDot.timelong;
    }

    /**
     * 通过触摸事件构造SimpleDot
     * @param event
     */
    public SimpleDot(MotionEvent event) {

    }

    /**
     * 计算两点距离
     * @param sDot1
     * @param sDot2
     * @return
     */
    public static double computeDistance(SimpleDot sDot1, SimpleDot sDot2){
        return computeDistance(sDot1.getFloatX(), sDot1.getFloatY(), sDot2.getFloatX(), sDot2.getFloatY());
    }
    /**
     * 将Dot的timelong起始时间从2010-01-01 00:00:00 000修正为1970-01-01 00:00:00 000
     * @param timelong
     * @return
     * @throws ParseException
     */
    public static long reviseTimelong(long timelong) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        Date datetime = sdf.parse("2010-01-01 00:00:00 000");
        return datetime.getTime() + timelong;
    }


    //SimpleDot点转换为Dot类型


    @Override
    public String toString() {
        return "SimpleDot{" +
                "type=" + type +
                ", timelong=" + timelong +
                ", x=" + x +
                ", y=" + y +
                ", fx=" + fx +
                ", fy=" + fy +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SimpleDot simpleDot = (SimpleDot) o;
        return timelong == simpleDot.timelong && type == simpleDot.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, timelong);
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        SimpleDot simpleDot = (SimpleDot) super.clone();
        simpleDot.type = this.type;
        return simpleDot;
    }
}
