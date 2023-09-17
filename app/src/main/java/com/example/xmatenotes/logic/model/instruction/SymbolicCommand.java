package com.example.xmatenotes.logic.model.instruction;

import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.model.handwriting.Stroke;
import com.example.xmatenotes.util.LogUtil;
import com.tqltech.tqlpencomm.bean.Dot;

public class SymbolicCommand extends Command{

    private static final String TAG = "SymbolicCommand";

    public final static int MAX_STROKES_NUMBER = 5;//定义单击命令的笔划数
    public final static int MIN_STROKES_NUMBER = 1;//定义单击命令的笔划数

    public static final int MAX_ID = 17;
    public static final int MIN_ID = 4;

    private CommandDetector commandDetector;
    private Command nextCommand;
    private int id;
    private String name;

    static {
        System.loadLibrary("gestureteast");
    }

    public SymbolicCommand(){

    }

    public SymbolicCommand(CommandDetector commandDetector) {
        this.commandDetector = commandDetector;
    }

    private SymbolicCommand(HandWriting handWriting, int id, String name) {
        super(handWriting);
        this.id = id;
        this.name = name;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Command handle(HandWriting handWriting) {
        Command command = null;
        if(isAvailable()){
            setHandWriting(handWriting);
            this.recognize(handWriting);
        }
        if(this.nextCommand != null){
            command = this.nextCommand.handle(handWriting);
        }
        return command;
    }

//    private HandWriting handWritingCache;

    @Override
    public void setNext(Command command){
        this.nextCommand = command;
    }

    protected boolean recognize(HandWriting handWriting) {

        int strokesNumber = handWriting.getStrokesNumber();
        if(strokesNumber >= MIN_STROKES_NUMBER && strokesNumber <= MAX_STROKES_NUMBER){
            for (Stroke stroke:handWriting.getStrokes()) {
                for (SimpleDot simpleDot : stroke.getDots()) {
                    recognizeGestures(simpleDot, strokesNumber);
                }
            }
            recognizeGestures(new SimpleDot(0, 0), strokesNumber);
        }
        return false;
    }

    protected Command createCommand(HandWriting handWriting) {
        String name;
        switch (id){
            case 4:
                name = "ZhiLingKongZhi";
                break;
            case 5:
                name = "Dui";
                break;
            case 6:
                name = "BanDui";
                break;
            case 7:
                name = "BanBanDui";
                break;
            case 8:
                name = "BanBanBanDui";
                break;
            case 9:
                name = "Cha";
                break;
            case 10:
                name = "Wen";
                break;
            case 11:
                name = "BanWen";
                break;
            case 12:
                name = "BanBanWen";
                break;
            case 13:
                name = "BanBanBanWen";
                break;
            case 14:
                name = "Tan";
                break;
            case 15:
                name = "BanTan";
                break;
            case 16:
                name = "BanBanTan";
                break;
            case 17:
                name = "BanBanBanTan";
                break;
            default:
                name = null;
        }
        return new SymbolicCommand(handWriting, id, name);
    }

    /*************************************so手势识别*******************************************/

    //电磁板单位毫米对应坐标范围：101
    public static final int UNIT_PERDIST = 101;

    //电磁板单次笔迹识别初始坐标点横坐标
    public static final short ERDOTXF = 20000;

    //电磁板单次笔迹识别初始坐标点纵坐标
    public static final short ERDOTYF = 10000;

    //压力值
    public static final short PRESS = 680;

    //状态值
    public static final byte STATUS_0x11 = 17;//笔接触
    public static final byte STATUS_0x10 = 16;//笔悬浮
    public static final byte STATUS_0x00 = 0;//笔离开

    //单次笔迹识别初始坐标点横坐标
    private static float rDotxFirst;

    //单次笔迹识别初始坐标点纵坐标
    private static float rDotyFirst;

    public static float getrDotxFirst() {
        return rDotxFirst;
    }

    public static void setrDotxFirst(float rDotxFirst) {
        SymbolicCommand.rDotxFirst = rDotxFirst;
    }

    public static float getrDotyFirst() {
        return rDotyFirst;
    }

    public static void setrDotyFirst(float rDotyFirst) {
        SymbolicCommand.rDotyFirst = rDotyFirst;
    }

    //调用此函数之前应先通过setrDotxFirst()和setrDotyFirst()设置单次笔迹识别初始坐标点
    //手势识别
    public void recognizeGestures(SimpleDot sDot, int count){
        //点阵坐标转换为电磁坐标
        float xDIs = sDot.getFloatX() - getrDotxFirst();
        float yDIs = sDot.getFloatY() - getrDotyFirst();
        short eX = (short) (ERDOTXF - Math.round(yDIs*UNIT_PERDIST));
        short eY = (short) (ERDOTYF + Math.round(xDIs*UNIT_PERDIST));

        if(sDot.type == Dot.DotType.PEN_UP){
            encapsulation(STATUS_0x10, eX, eY, (short) 0, count);
        }else {
            encapsulation(STATUS_0x11, eX, eY, PRESS, count);
        }
    }

    //调用so库
    public native void encapsulation(byte status, short nX, short nY, short nPress, int count);

    //获取so手势识别返回结果
    public void observe(int result){

        LogUtil.e(TAG, "observe: " +result);
        id = result;
        if(commandDetector != null){
            commandDetector.response(createCommand(getHandWriting()));
        }
    }
}
