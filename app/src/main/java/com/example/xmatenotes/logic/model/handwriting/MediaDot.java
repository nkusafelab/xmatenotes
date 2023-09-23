package com.example.xmatenotes.logic.model.handwriting;

import android.graphics.Color;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.util.DateUtil;
import com.tqltech.tqlpencomm.bean.Dot;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 附加媒体信息的点
 */
public class MediaDot extends SimpleDot {

    private final static String  TAG = "MediaDot";
    private static final long serialVersionUID = 8348010102856369940L;

    /**
     * 笔划ID，同页内唯一
     */
    public int strokesID;

    /**
     * 页号
     */
    public long pageID;

    /**
     * 笔Mac地址
     */
    public String penMac;

    /***************************媒体信息*****************************/
    /**
     * 视频ID
     */
    public int videoID;

    /**
     * 视频进度
     */
    public float videoTime;

    /**
     * 音频ID
     */
    public int audioID = XmateNotesApplication.DEFAULT_INT;
    /***************************媒体信息*****************************/

    /***************************形态信息*****************************/
    /**
     * 颜色
     */
    public int color;
    private static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEEP_GREEN = Color.rgb(5, 102, 8);
    public static final int DEEP_ORANGE = Color.rgb(243,117,44);

    /**
     * 大小(或宽度)
     */
    public int width;
    public static final int DEFAULT_WIDTH = 1;
    public static final int DEFAULT_BOLD_WIDTH = 3;

    /**
     * 命令Id
     */
    public int ins;
    public static final int DEFAULT_INS = 0;

    /***************************形态信息*****************************/

    public MediaDot() {
        super();
    }

    public MediaDot(float fx, float fy) {
        super(fx, fy);
    }

    public MediaDot(SimpleDot simpleDot){
        super(simpleDot.getFloatX(), simpleDot.getFloatY(), simpleDot.type, simpleDot.timelong);
        this.pageID = XmateNotesApplication.DEFAULT_INT;
        this.videoTime = XmateNotesApplication.DEFAULT_FLOAT;
        this.videoID = XmateNotesApplication.DEFAULT_INT;
        this.audioID = XmateNotesApplication.DEFAULT_INT;
        this.color = DEFAULT_COLOR;
        this.width = DEFAULT_WIDTH;
        this.ins = DEFAULT_INS;
    }

    public MediaDot(Dot dot) {
        super(dot);
        this.pageID = dot.PageID;
        this.videoTime = XmateNotesApplication.DEFAULT_FLOAT;
        this.videoID = XmateNotesApplication.DEFAULT_INT;
        this.audioID = XmateNotesApplication.DEFAULT_INT;
        this.color = DEFAULT_COLOR;
        this.width = DEFAULT_WIDTH;
        this.ins = DEFAULT_INS;
    }

    public MediaDot(Dot dot, int strokesID, float timeS, int videoIDS, int audioIDS, String penMac) throws ParseException {
        this(dot);

        this.strokesID = strokesID;
        this.videoTime = timeS;
        this.videoID = videoIDS;
        this.audioID = audioIDS;
        this.penMac = penMac;
    }

    public MediaDot(int x, int y, Dot.DotType type, int strokesID, int pageID, long timelong, float videoTime, int videoID, int audioID, String penMac) {
        super(x, y);
        this.type = type;
        this.strokesID = strokesID;
        this.pageID = pageID;
        this.timelong = timelong;
        this.videoTime = videoTime;
        this.videoID = videoID;
        this.audioID = audioID;
        this.penMac = penMac;
    }

    public MediaDot(MediaDot mediaDot) {
        super(mediaDot);
        this.strokesID = mediaDot.strokesID;
        this.pageID = mediaDot.pageID;
        this.videoTime = mediaDot.videoTime;
        this.videoID = mediaDot.videoID;
        this.audioID = mediaDot.audioID;
        this.penMac = mediaDot.penMac;
        this.color = mediaDot.color;
        this.width = mediaDot.width;
        this.ins = mediaDot.ins;
    }

    /**
     * 利用原始dot的timelong属性修正视频进度
     * @param timelong dot原始未修正的时间戳属性timelong
     * @param timeS 视频进度。请确保传入的timeS为调用该方法时刻的timeS
     * @return 返回修正后的视频进度，即timelong对应的视频进度
     */
    public static float reviseTimeS(long timelong, float timeS) throws ParseException {
        long c = System.currentTimeMillis();
        return (float) (timeS - (c-reviseTimelong(timelong))/1000.0);
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

    //格式化显示timelong
    public static String timelongFormat(long timelong){
        return DateUtil.formatTimelong(timelong, "yyyy年MM月dd日-hh时mm分ss秒");
    }

    //格式化显示timelong
    public static String timelongFormat2(long timelong){
        return DateUtil.formatTimelong(timelong, "yyyy/MM/dd-hh:mm:ss");
    }

    public int getIns() {
        return ins;
    }

    public void setIns(int ins) {
        this.ins = ins;
    }

    @Override
    public String toString() {
        return "MediaDot{" +
                "strokesID=" + strokesID +
                ", pageID=" + pageID +
                ", penMac='" + penMac + '\'' +
                ", videoID=" + videoID +
                ", videoTime=" + videoTime +
                ", audioID=" + audioID +
                ", color=" + color +
                ", width=" + width +
                ", ins=" + ins +
                ", type=" + type +
                ", timelong=" + timelong +
                ", x=" + x +
                ", y=" + y +
                ", fx=" + fx +
                ", fy=" + fy +
                '}';
    }

    /**
     * 存储格式（单行）：pageID strokesID type cx cy videoIDS timeS audioIDS penMac timelong color width
     * @return
     */
    public String storageFormat(){
        StringBuilder string = new StringBuilder();
        string.append(pageID+" ");
        string.append(strokesID+" ");
        string.append(type+" ");
        string.append(fx+" ");
        string.append(fy+" ");
        string.append(videoID +" ");
        string.append(videoTime +" ");
        string.append(audioID +" ");
        string.append(penMac+" ");
        string.append(timelong+" ");
        string.append(color+" ");
        string.append(width+" ");
        string.append(ins);
        return string.toString();
    }

    /**
     * 将单行“笔迹点”字符串解析为MediaDot类型对象，遵循storageFormat()中定义的字符串格式
     * @param line 单行“笔迹点”字符串
     * @return 解析后的MediaDot类型对象
     */
    public static MediaDot parse(String line){
        int start=0,end=0;

        MediaDot mediaDot = new MediaDot();

        end = line.indexOf(" ", start);
        mediaDot.pageID = Integer.parseInt(line.substring(start, end));//PageID

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.strokesID = Integer.parseInt(line.substring(start, end));//strokesID

        start = end+1;
        end = line.indexOf(" ", start);
        String s = line.substring(start, end);
        switch (s){
            case "PEN_DOWN":
                mediaDot.type = Dot.DotType.PEN_DOWN;
                break;
            case "PEN_MOVE":
                mediaDot.type = Dot.DotType.PEN_MOVE;
                break;
            case "PEN_UP":
                mediaDot.type = Dot.DotType.PEN_UP;
                break;
            default:
        }
        //        mediaDot.type = Dot.DotType.valueOf(line.substring(start, end));//type

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.setX(Float.parseFloat(line.substring(start, end)));//横坐标

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.setY(Float.parseFloat(line.substring(start, end)));//纵坐标

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.videoID = Integer.parseInt(line.substring(start, end));//视频ID

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.videoTime = Float.parseFloat(line.substring(start, end));//视频进度

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.audioID = Integer.parseInt(line.substring(start, end));//音频ID

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.penMac = line.substring(start, end);//笔mac地址

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.timelong = Long.parseLong(line.substring(start, end));//时间戳

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.color = Integer.parseInt(line.substring(start, end));//颜色

        start = end+1;
        end = line.indexOf(" ", start);
        mediaDot.width = Integer.parseInt(line.substring(start, end));//宽度

        start = end+1;
        mediaDot.ins = Integer.parseInt(line.substring(start));//命令Id

        //笔划ID为程序内部生成，仅作用于内部，不存于文件，故在方法外面单独处理
        return mediaDot;
    }

    /**
     * 判断是否附加了音频信息
     * @return
     */
    public boolean isAudioDot(){
        if(audioID == XmateNotesApplication.DEFAULT_INT){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 判断是否附加了视频信息
     * @return
     */
    public boolean isVideoDot(){
        if(videoID == XmateNotesApplication.DEFAULT_INT){
            return false;
        }else {
           return true;
        }
    }

    /**
     * 是否为字符命令笔迹点
     * @return
     */
    public boolean isCharInstruction(){
        return this.ins > 0 && this.ins != 1 && this.ins != 2 && this.ins != 3 && this.ins < 10;
    }

}
