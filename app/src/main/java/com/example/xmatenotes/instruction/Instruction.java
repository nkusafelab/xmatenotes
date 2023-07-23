package com.example.xmatenotes.instruction;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;

import com.example.xmatenotes.App.XApp;
import com.example.xmatenotes.BaseActivity;
import com.example.xmatenotes.DotClass.MediaDot;
import com.example.xmatenotes.DotClass.SimpleDot;
import com.example.xmatenotes.Gesture;
import com.example.xmatenotes.datamanager.AudioManager;
import com.example.xmatenotes.datamanager.ExcelReader;
import com.example.xmatenotes.datamanager.PageManager;
import com.example.xmatenotes.datamanager.PenMacManager;
import com.tqltech.tqlpencomm.bean.Dot;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * 命令类的祖宗，所有简单命令应继承自该类
 */
public class Instruction {

    private static final String TAG = "Instruction";

    static {
        System.loadLibrary("gestureteast");
    }

    private static PageManager pageManager;
    private static PenMacManager penMacManager;
    private static AudioManager audioManager;
    private static ExcelReader excelReader;

    private static BaseActivity baseActivity = null;

    private static long strokeBeginT = 0;//记录当前笔划开始的时刻，即当前类型为PEN_DOWN的笔迹点产生的时刻
    private static long strokeOverT = System.currentTimeMillis();
    ;//记录当前笔划结束的时刻，即当前类型为PEN_UP的笔迹点产生的时刻
    private static long frontTSpan = 0;//记录当前笔划距离前一笔划的时间间隔，从前一笔画“PEN_UP”到该笔划“PEN_DOWN”
    private static long dcStartTime = 0;//双击两笔划间隔计时起点
    private static long wStartTime = 0;//普通书写延迟响应计时起点
    private static long shStartTime = 0;//单次笔迹延迟响应计时起点

    //计时器开关
    private static boolean dcTimer = false;//双击两笔划间隔计时器开关
    private static Thread doubleClickTimer = null;//双击两笔划间隔计时器

    private static boolean wTimer = false;//普通书写完毕计时器开关
    private static Thread writeTimer = null;//普通书写延迟响应计时器

    private static boolean shTimer = false;//单次笔迹完毕计时器开关
    private static Thread singleHandwritingTimer = null;//单次笔迹延迟响应计时器

    private static boolean seekTimer = false;//视频碎片复现计时器开关

    private static int getureStrokeN = 0;//记录当前待定动作命令的笔划数
    private static int strokesID = 0;//全局当前书写笔划编号：1,2...，命令或无效笔划不算，也是当前已有笔划数量，该值不写入文件

    private static MediaDot dotDown = null;//记录每次类型为PEN_DOWN的笔迹点
    private static MediaDot dcdotFirst = null;//记录双击动作的初始笔迹点
    private static MediaDot hwDotFirst = null;//记录普通书写笔迹起始点
    private static MediaDot shwDotFirst = null;//记录单次笔迹起始点
    private static MediaDot lastDot = null;//上一个Dot
    private static MediaDot curMediaDot = null;//当前MediaDot
    private static SimpleDot simpleDot = null;//当前SimpleDot

    private static double xMin;
    private static double xMax;
    private static double yMin;
    private static double yMax;//刻画当前笔划的坐标范围
    private static RectF rectF = null;//刻画当前笔划的矩形范围
    private static HandWriting handWriting = null;//当前HandWriting

    /**
     * 当前指令识别上下文环境
     * 0:普通书写; 1:单击; 2:双击; 3:长压
     * 4:指令控制符; 5:对钩
     *普通书写延迟响应结束后应复位-1
     */
    private int curRecEnv = -1;

    //当前笔迹点完整坐标
    private static float xD;
    private static float yD;
    private static Dot.DotType currentDotType;//当前dot类型

    public static ArrayList<MediaDot> mediaDots;//MediaDot类型笔迹点暂存区
    public static ArrayList<SimpleDot> simpleDots;//SimpleDot类型笔迹点暂存区

    public Instruction() {
        pageManager = PageManager.getInstance();
        penMacManager = PenMacManager.getInstance();
        audioManager = AudioManager.getInstance();
        excelReader = ExcelReader.getInstance();
    }

    public static boolean rr = true;

    /***************************************指令识别*****************************************/

    /**
     * 指令符识别
     * @param rGesture 待识别指令符
     * @return 识别结果: 0:普通书写; 1:单击; 2:双击; 3:长压; 4:指令控制符; 5:对钩
     * <p>如果是单击、双击、长压，则直接返回识别结果；如果是普通书写，则进入C++部分使用SVM算法识别是否是指令控制符或者对钩</p>
     */
    public int recognize(Gesture rGesture) {

        Log.e(TAG, "命令识别开始");

//        int reRes = -1;
//
//        if(gesture.getStrokesNumber() == 1 || gesture.getStrokesNumber() == 2){
//            //先走0类指令识别流程，再走SVM
//            reRes = recognize0Ins(gesture);
//            if(reRes != -1){
//                return reRes;
//            }else {
//                recognize1Ins(gesture);
//            }
//        }else if(gesture.getStrokesNumber() == 3 || gesture.getStrokesNumber() == 4){
//            //如果当前上下文环境是普通书写，走普通书写响应；否则直接走SVM
//            if(curRecEnv == 0){
//                return 0;
//            }else {
//
//            }
//        }else if(gesture.getStrokesNumber() > 4){
//            //走普通书写响应
//            return 0;
//        }

        int strokesNumber = rGesture.getStrokesNumber();

        Log.e(TAG, "rGesture.getDuration(): " + rGesture.getDuration() + "ms");
        Log.e(TAG, "rGesture.getWidth(): " + (rGesture.getWidth()));
        Log.e(TAG, "rGesture.getHeight(): " + (rGesture.getHeight()));
        Log.e(TAG, "rGesture.getStrokesNumber(): " + (rGesture.getStrokesNumber()));


        if (rGesture.getWidth() < DoubleClick.SINGLE_CLICK_dLIMIT && rGesture.getHeight() < DoubleClick.SINGLE_CLICK_dLIMIT) {
            if (rGesture.getDuration() <= DoubleClick.SINGLE_CLICK_tLIMIT) {
                if (strokesNumber == 2) {
                    Log.e(TAG, "recognize():识别出双击");
//                    if(Looper.myLooper() == null) Looper.prepare();
//                    Toast.makeText(XApp.context, "双击命令", Toast.LENGTH_SHORT).show();
//                    Looper.loop();
                    //以上代码使用后，本方法执行完，后续代码就不会继续执行了
                    return 2;
                }
                if (strokesNumber == 1) {
                    dcdotFirst = dotDown;
                    Log.e(TAG, "recognize():识别出单击");
                    return 1;
                }
            }
            if (rGesture.getDuration() >= LongPress.LONG_PRESS_tLIMIT) {
                dcdotFirst = dotDown;
                Log.e(TAG, "recognize():识别出长压");
//                if(Looper.myLooper() == null) Looper.prepare();
//                Toast.makeText(XApp.context, "长压命令", Toast.LENGTH_SHORT).show();
//                Looper.loop();
                return 3;
            }
        }

        wTimer = false;
        shTimer = false;
        Log.e(TAG, "recognize():普通书写和单次笔迹延迟响应计时器关闭");

        //只对普通书写第一个笔划进行SVM识别
        if(reced){
            //普通书写
            dcdotFirst = dotDown;

            if (hwDotFirst == null) {
                hwDotFirst = dotDown;
            }
            Log.e(TAG, "recognize():识别出普通书写");

//        if(Looper.myLooper() == null) Looper.prepare();
//        Toast.makeText(XApp.context, "普通书写", Toast.LENGTH_SHORT).show();
//        Looper.loop();

            return 0;
        }else {
            reced = true;
        }

        gesT = new Gesture(rGesture);
        //指令集识别
        Set<Map.Entry<String,ArrayList<MediaDot>>> set = rGesture.getStrokes().entrySet();
        for (Map.Entry<String,ArrayList<MediaDot>> node:
             set) {
            ArrayList<MediaDot> sDotA = node.getValue();
            for (MediaDot sDot:
                 sDotA) {
                recognizeGestures(new SimpleDot(sDot));
            }
        }

//        Log.e(TAG, "recognize():while (rr)前");

//        while (rr){
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        Log.e(TAG, "recognize():while (rr)后");



        //检查so返回结果
        //指令控制符
//        if(res == 4){
//            rr = true;
//            Log.e(TAG, "recognize: 指令控制符");
//            return res;
//        }
//
//        //对钩
//        if(res == 5){
//            rr = true;
//            Log.e(TAG, "recognize: 对钩");
//            return res;
//        }
//
//        rr = true;

//        dcdotFirst = dotDown;
//
//        if (hwDotFirst == null) {
//            hwDotFirst = dotDown;
//        }
//        Log.e(TAG, "recognize():识别出普通书写");
//
////        if(Looper.myLooper() == null) Looper.prepare();
////        Toast.makeText(XApp.context, "普通书写", Toast.LENGTH_SHORT).show();
////        Looper.loop();
//        wTimer = false;
//        shTimer = false;
//        Log.e(TAG, "recognize():普通书写和单次笔迹延迟响应计时器关闭");
        return -1;
    }

    /**
     * 识别0类指令
     * @param rGesture 待识别手势对象
     * @return -1:非0类指令; 1:单击; 2:双击; 3:长压
     */
    public int recognize0Ins(Gesture rGesture){
        int strokesNumber = rGesture.getStrokesNumber();

        Log.e(TAG, "rGesture.getDuration(): " + rGesture.getDuration() + "ms");
        Log.e(TAG, "rGesture.getWidth(): " + (rGesture.getWidth()));
        Log.e(TAG, "rGesture.getHeight(): " + (rGesture.getHeight()));
        Log.e(TAG, "rGesture.getStrokesNumber(): " + (rGesture.getStrokesNumber()));


        if (rGesture.getWidth() < DoubleClick.SINGLE_CLICK_dLIMIT && rGesture.getHeight() < DoubleClick.SINGLE_CLICK_dLIMIT) {
            if (rGesture.getDuration() <= DoubleClick.SINGLE_CLICK_tLIMIT) {
                if (strokesNumber == 2) {
                    Log.e(TAG, "recognize():识别出双击");
//                    if(Looper.myLooper() == null) Looper.prepare();
//                    Toast.makeText(XApp.context, "双击命令", Toast.LENGTH_SHORT).show();
//                    Looper.loop();
                    //以上代码使用后，本方法执行完，后续代码就不会继续执行了
                    return 2;
                }
                if (strokesNumber == 1) {
                    dcdotFirst = dotDown;
                    Log.e(TAG, "recognize():识别出单击");
                    return 1;
                }
            }
            if (rGesture.getDuration() >= LongPress.LONG_PRESS_tLIMIT) {
                dcdotFirst = dotDown;
                Log.e(TAG, "recognize():识别出长压");
//                if(Looper.myLooper() == null) Looper.prepare();
//                Toast.makeText(XApp.context, "长压命令", Toast.LENGTH_SHORT).show();
//                Looper.loop();
                return 3;
            }
        }

        dcdotFirst = dotDown;

        if (hwDotFirst == null) {
            hwDotFirst = dotDown;
        }
        Log.e(TAG, "recognize():识别出普通书写");

//        if(Looper.myLooper() == null) Looper.prepare();
//        Toast.makeText(XApp.context, "普通书写", Toast.LENGTH_SHORT).show();
//        Looper.loop();
        wTimer = false;
        shTimer = false;
        Log.e(TAG, "recognize():普通书写和单次笔迹延迟响应计时器关闭");
        return 0;
    }

    /**
     * 识别Ⅰ类指令
     * @param rGesture 待识别手势对象
     * @return
     */
    public int recognize1Ins(Gesture rGesture){
        return 0;
    }

    /**
     * 识别Ⅱ类指令
     * @param rGesture 待识别手势对象
     * @return
     */
    public int recognize2Ins(Gesture rGesture){
        return 0;
    }



    /***************************************指令识别*****************************************/

    /**
     * 初始化暂存区
     * @param x 笔划第一个点的横坐标整数部分
     * @param y 笔划第一个点的纵坐标整数部分
     */
    private void bufferInit(float x, float y) {
        Log.e(TAG, "初始化暂存区");

//        xMax = 0;
//        xMin = Double.MAX_VALUE;
//        yMax = 0;
//        yMin = Double.MAX_VALUE;

        if (rectF == null) {
            rectF = new RectF();
        }
        rectF.setEmpty();
        rectF.left = x;
        rectF.right = x;
        rectF.top = y;
        rectF.bottom = y;

        mediaDots = new ArrayList<MediaDot>();
        simpleDots = new ArrayList<SimpleDot>();

        Log.e(TAG, "暂存区初始化成功");
    }

//    //执行代码块，用于形成刻画笔划范围的矩形
//    private static void processRect() {
//
//        xMax = xD > xMax ? xD : xMax;
//        yMax = yD > yMax ? yD : yMax;
//        xMin = xD < xMin ? xD : xMin;
//        yMin = yD < yMin ? yD : yMin;
//
//    }


    public void processGesture() {
        Log.e(TAG, "一个笔划完成");

        getureStrokeN++;
        Log.e(TAG, "当前待定命令包含笔划数量： " + getureStrokeN);

        Gesture gesture = new Gesture();

        gesture.setFrontTSpan(frontTSpan);
        Log.e(TAG, "gesture.frontTSpan: " + frontTSpan + "ms");
        gesture.setRectF(rectF);
        gesture.setDuration(strokeOverT - strokeBeginT);
        gesture.setStrokesNumber(getureStrokeN);

        Log.e(TAG, "gesture.getStrokes().put()开始 ");
        gesture.getStrokes().put(getureStrokeN + "", mediaDots);
        Log.e(TAG, "gesture.getStrokes().put()结束 ");

//        new Thread(new Runnable() {
//            @Override
//            public void run() {

                int res = recognize(gesture);
                if(res == -1){
                    return;
                }

                gesture.setInsId(res);
                Log.e(TAG, "InsId: " + gesture.getInsId());
                Log.e(TAG, "命令识别结束");

                if ((gesture.getInsId() == 1 || gesture.getInsId() == 2) && (wTimer == true)) {
                    gesture.setInsId(0);//当普通书写延时计时器处于打开状态时，无论单击或双击都按照普通书写算
                    Log.e(TAG, "双击或单击判定为普通书写");

                    dcdotFirst = dotDown;
                    wTimer = false;
                    shTimer = false;
                    Log.e(TAG, "普通书写和单次笔迹延迟响应计时器关闭");
                }

                Log.e(TAG, "基础响应开始");
                response(gesture);
                Log.e(TAG, "基础响应结束");

//                simpleDots.clear();
//                mediaDots.clear();
//                Log.e(TAG, "mediaDots.clear(): " + mediaDots.size());

                if (gesture.getInsId() != 1) {
                    Log.e(TAG, "BaseActivity.baseActivity: " + BaseActivity.baseActivity);
                    if (BaseActivity.baseActivity != null) {
                        Log.e(TAG, "回调响应");
                        BaseActivity.baseActivity.receiveRecognizeResult(gesture, dcdotFirst.pageID, dcdotFirst.x, dcdotFirst.y);
                    }
                }
//            }
//        }).start();

    }

    private boolean reTurn = true;//控制ProcessEachDot方法执行退出

    //若当前笔划未写完就识别出了结果，则立刻返回识别出的命令类型：0：普通书写；1：单击；2：双击;3:长压
    //

    /**
     * 对每个接收到的笔迹点进行处理。
     * 命令识别模块的入口，通过全局唯一Instruction对象进行调用
     * 当前在哪个活动界面书写，就在哪个活动中调用该方法传入笔迹点进行处理
     * @param mediaDot 待处理mediaDot
     * @return 若当前笔划未写完就识别出了结果，则立刻返回识别出的命令类型：0：普通书写；1：单击；2：双击;3:长压; 若当前尚未识别出则返回-1
     */
    public int processEachDot(MediaDot mediaDot){
        //传进来的timeS就是已经处理好的
        //float timeS = MediaDot.reviseTimeS(dot.timelong, time);//修正视频进度

        if (reTurn == false && mediaDot.type != Dot.DotType.PEN_UP) {
            return -1;
        }

        Log.e(TAG, "mediaDot.toString(): " + mediaDot.toString());
        Log.e(TAG, "System.currentTimeMillis(): " + System.currentTimeMillis());

        //存储pageID
        pageManager.savePage(mediaDot.pageID);
        pageManager.currentPageID = mediaDot.pageID;

        penMacManager.putMac(XApp.mBTMac);

        simpleDot = new SimpleDot(mediaDot);

        //完整坐标
        xD = simpleDot.x;
        yD = simpleDot.y;

        if (mediaDot.type == Dot.DotType.PEN_DOWN) {
            currentDotType = Dot.DotType.PEN_DOWN;
            Log.e(TAG, "PEN_DOWN");
            bufferInit(simpleDot.x, simpleDot.y);//初始化暂存区

            //新的笔划开始，笔划编号+1
            mediaDot.strokesID = strokesID;

//			lastDot = dotA;//把笔划第一个笔迹点保存在lastDot中
            strokeBeginT = mediaDot.timelong;

            frontTSpan = strokeBeginT - strokeOverT;

            dcTimer = false;
            Log.e(TAG, "双击间隔计时器关闭");

//            if (!shTimer && isSHDelay5s) {//单次笔迹开始
//                isSHDelay5s = false;
////                Page p = pageManager.getPageByPageID(dot.PageID);
////                if(p != null){
//                shwDotFirst = dot;
////                    p.addLocalHwsMapBegin();
////                }
//                MediaDot mD = new MediaDot(mediaDot);//标志单次笔迹的开始
//                mD.setCx(-3);
//                mD.setCy(-3);
//                mediaDots.add(mD);
//                Log.e(TAG, "processEachDot: 添加了(-3, -3)");
//            }

            dotDown = mediaDot;

            mediaDots.add(mediaDot);
//            simpleDots.add(simpleDot);
        }

        rectF.union(simpleDot.x, simpleDot.y);
//        processRect();

        if (mediaDot.type == Dot.DotType.PEN_MOVE) {
            if (currentDotType == Dot.DotType.PEN_UP) {
                mediaDot.type = Dot.DotType.PEN_DOWN;//可能存在PEN_UP后直接PEN_MOVE的情况
                return processEachDot(mediaDot);
            }
            Log.e(TAG, "PEN_MOVE");
            currentDotType = Dot.DotType.PEN_MOVE;

            if (rectF.width() > DoubleClick.SINGLE_CLICK_dLIMIT || rectF.height() > DoubleClick.SINGLE_CLICK_dLIMIT) {

//                wTimer = false;
//                shTimer = false;
//                isSHDelay5s = false;
                Log.e(TAG, "普通书写和单次笔迹延迟响应计时器关闭");

                mediaDots.add(mediaDot);
//                simpleDots.add(simpleDot);
                Log.e(TAG, "笔划坐标范围超出单击坐标范围！识别为非0类指令或普通书写");
                return 0;

            } else if ((mediaDot.timelong - strokeBeginT) < LongPress.LONG_PRESS_tLIMIT && (mediaDot.timelong - strokeBeginT) > DoubleClick.SINGLE_CLICK_tLIMIT) {
                //不是单击、双击，可能是长压
                mediaDots.add(mediaDot);
//                simpleDots.add(simpleDot);
                return -1;
            } else if ((mediaDot.timelong - strokeBeginT) >= LongPress.LONG_PRESS_tLIMIT) {
                Log.e(TAG, "单击超时！识别为长压");
                mediaDot.type = Dot.DotType.PEN_UP;
                //dotA.force = 0;

                strokeOverT = mediaDot.timelong;

                mediaDots.add(mediaDot);
//                simpleDots.add(simpleDot);

//                new Thread(new Runnable()
//                {
//                    @Override
//                    public void run() {
//                        processGesture();
//                    }
//                }).start();
                reTurn = false;
                processGesture();

                return 3;
            }

            mediaDots.add(mediaDot);
//            simpleDots.add(simpleDot);
        }

        if (mediaDot.type == Dot.DotType.PEN_UP) {
            Log.e(TAG, "PEN_UP");
            if (reTurn == false) {
                reTurn = true;
                return -1;
            }
            if (currentDotType == Dot.DotType.PEN_UP) {//可能存在多个连续相同坐标的PEN_UP点
                return -1;
            }
            currentDotType = Dot.DotType.PEN_UP;

            strokeOverT = mediaDot.timelong;
            mediaDots.add(mediaDot);
//            simpleDots.add(simpleDot);

            processGesture();

        }

        return -1;
    }

    //标记一次笔迹是否结束
    public boolean isSHDelay5s = true;

    /**
     * 各命令的基础响应。
     * 除了基础响应，如果还需要在特定场景下进一步完成特殊响应，则需要后续进入特定场景下继续响应
     * @param ges 待响应命令ID
     */
    @SuppressLint("SimpleDateFormat")
    private void response(Gesture ges) {

        if (ges.getInsId() == 1) {
            Log.e(TAG, "单击响应开始");

            ////双击两笔划间隔计时起点
            dcStartTime = System.currentTimeMillis();

            //双击两笔划间隔计时器开关打开
            dcTimer = true;
            Log.e(TAG, "双击间隔计时器打开");

            doubleClickTimer = new Thread(new Runnable() {
                @Override
                public void run() {

                    long curTime = System.currentTimeMillis();

                    while (dcTimer == true && curTime - dcStartTime < DoubleClick.DOUBLE_CLICK_PERIOD) {
                        curTime = System.currentTimeMillis();
                    }

                    //双击间隔计时超时，为单击
                    if (dcTimer == true) {
                        dcTimer = false;
                        Log.e(TAG, "双击间隔计时器关闭");
                        dcdotFirst = null;
//                        gesture.clear();
//                        gesture = new Gesture();
                        Log.e(TAG, "gesture清空");

                        getureStrokeN = 0;
                        Log.e(TAG, "getureStrokeN归零");
                    }
                }
            });
            doubleClickTimer.start();

            return;
        } else {
            getureStrokeN = 0;
            Log.e(TAG, "getureStrokeN归零");

            if (ges.getInsId() == 2) {
                Log.e(TAG, "双击命令基础响应开始");

            }

            if (ges.getInsId() == 3) {
                Log.e(TAG, "长压命令基础响应开始");

            }

            if(ges.getInsId() == 4){
                Log.e(TAG, "指令控制符命令基础响应开始");
            }

            if(ges.getInsId() == 5){
                Log.e(TAG, "对钩命令基础响应开始");
            }

            if (ges.isCharInstruction() || ges.isHandWriting()) {
                Log.e(TAG, "普通书写基础响应开始");

                strokesID++;//书写笔划编号+1

                Log.e(TAG, "processEachDot: shTimer: "+ shTimer);
                Log.e(TAG, "processEachDot: isSHDelay5s: "+ isSHDelay5s);
                if (!shTimer && isSHDelay5s) {//单次笔迹开始
                    isSHDelay5s = false;
//                Page p = pageManager.getPageByPageID(dot.PageID);
//                if(p != null){
                    shwDotFirst = dotDown;
//                    p.addLocalHwsMapBegin();
//                }
                    MediaDot mD = new MediaDot(mediaDots.get(0));//标志单次笔迹的开始
                    mD.setCx(-3);
                    mD.setCy(-3);
                    mediaDots.add(0, mD);
                    Log.e(TAG, "processEachDot: 添加了(-3, -3)");
                }

//                if(handWriting == null){
//                    handWriting = new HandWriting();
//                }
//                handWriting.addRect(new Rect((int)Math.floor(rectF.left), (int)Math.floor(rectF.top), (int)Math.ceil(rectF.right), (int)Math.ceil(rectF.bottom)));
//                handWriting.addAttribute(mediaDot);

                if (writeTimer != null) {
                    if (writeTimer.isAlive()) {
                        //确保上一个writeTimer终止
                        Log.e(TAG, "writeTimer尚未终止");
                    }
                }
                if (singleHandwritingTimer != null) {
                    if (singleHandwritingTimer.isAlive()) {
                        //确保上一个singleHandwritingTimer终止
                        Log.e(TAG, "singleHandwritingTimer尚未终止");
                    }
                }

                wStartTime = System.currentTimeMillis();
                wTimer = true;

                shStartTime = System.currentTimeMillis();
                shTimer = true;

                Log.e(TAG, "普通书写延迟视频播放计时器打开");

                writeTimer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long curTime = System.currentTimeMillis();

                        while (wTimer && curTime - wStartTime < HandWriting.DELAY_PERIOD) {
                            curTime = System.currentTimeMillis();
                        }

                        if (wTimer) {
                            wTimer = false;
                            Log.e(TAG, "普通书写延迟响应计时器关闭");
                            reced = false;

                            if (shTimer && hwDotFirst != null) {
                                MediaDot mD = null;
                                mD = new MediaDot(hwDotFirst);//标志一次普通书写笔迹结束
                                mD.setCx(-5);
                                mD.setCy(-5);
                                mD.timelong = System.currentTimeMillis();
                                mD.type = Dot.DotType.PEN_UP;
                                pageManager.writeDot(mD);

//                                Page p = PageManager.getPageByPageID(hwDotFirst.PageID);//使用普通书写笔迹第一个点
//                                if(p != null){
//                                    p.addHandwriting(handWriting);
//                                    hwDotFirst = null;
//                                    handWriting = null;
//                                }
                            }

                            //延迟响应
                            if (BaseActivity.baseActivity != null && hwDotFirst != null) {
                                BaseActivity.baseActivity.writeTimerFinished(hwDotFirst.pageID, hwDotFirst.x, hwDotFirst.y);
                            }
                            hwDotFirst = null;
                        }

                    }
                });
                writeTimer.start();

                singleHandwritingTimer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long curTime = System.currentTimeMillis();

                        while (shTimer && curTime - shStartTime < HandWriting.SINGLE_HANDWRITING_DELAY_PERIOD) {
                            curTime = System.currentTimeMillis();
                        }

                        if (shTimer) {
                            //单次笔迹结束
                            if (shwDotFirst != null) {
//                                Page p = pageManager.getPageByPageID(shwDotFirst.PageID);//使用单次笔迹第一个点
//                                int pN = pageManager.getPageNumberByPageID(shwDotFirst.PageID);
//                                LocalRect lR = excelReader.getLocalRectByXY(pN, shwDotFirst.x, shwDotFirst.y);
//                                if(lR != null && p != null){
                                MediaDot mD = null;
                                mD = new MediaDot(shwDotFirst);//标志单次笔迹结束
                                mD.setCx(-4);
                                mD.setCy(-4);
                                mD.timelong = System.currentTimeMillis();
                                mD.type = Dot.DotType.PEN_UP;
                                pageManager.writeDot(mD);
//                                        String localCode = lR.firstLocalCode+"-"+ lR.secondLocalCode;
//                                        p.addLocalHwsMapEnd(localCode);
                            }
                            shwDotFirst = null;
                            shTimer = false;
                            isSHDelay5s = true;
                        }

                    }
                });

                singleHandwritingTimer.start();

                //确保这一步在HandWriting.SINGLE_HANDWRITING_DELAY_PERIOD时间范围内一定执行完
                pageManager.writeDots(mediaDots);
//                Log.e(TAG, "response：pause()");
//                int page = pageIDToArray.get(dots.get(0).PageID);
//                Log.e(TAG, "普通书写填充page: " + page);
//
////				if(!videoManager.contains(currentID)){
////					videoManager.addVideo(id, videoName);
////				}
//
//                penMacManager.putMac(XApp.mBTMac);
//                byte penID = penMacManager.getPenID(XApp.mBTMac);
//
//                int audiofileName = audioManager.getCurrentRecordAudioNumber();
//
//                Log.e(TAG, "普通书写填充penID：" + penID);

            }
        }
    }

    public static int getStrokesID() {
        return strokesID;
    }

    public static void setStrokesID(int strokesID) {
        Instruction.strokesID = strokesID;
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
        Instruction.rDotxFirst = rDotxFirst;
    }

    public static float getrDotyFirst() {
        return rDotyFirst;
    }

    public static void setrDotyFirst(float rDotyFirst) {
        Instruction.rDotyFirst = rDotyFirst;
    }

    //调用此函数之前应先通过setrDotxFirst()和setrDotyFirst()设置单次笔迹识别初始坐标点
    //手势识别
    public void recognizeGestures(SimpleDot sDot){
        //点阵坐标转换为电磁坐标
        float xDIs = sDot.x - getrDotxFirst();
        float yDIs = sDot.y - getrDotyFirst();
        short eX = (short) (ERDOTXF - Math.round(yDIs*UNIT_PERDIST));
        short eY = (short) (ERDOTYF + Math.round(xDIs*UNIT_PERDIST));

        if(sDot.type == Dot.DotType.PEN_UP){
            encapsulation(STATUS_0x10, eX, eY, (short) 0);
        }else {
            encapsulation(STATUS_0x11, eX, eY, PRESS);
        }
    }

    //调用so库
    public native void encapsulation(byte status, short nX, short nY, short nPress);

    //保存so手势识别返回结果
    public static int res;

    private Gesture gesT;//仅用作传输gestrue,上一次的已经使用了，下一次的才能往里装

    //获取so手势识别返回结果
    public void observe(int result){
        Log.e(TAG, "observe: " +result);
//        res = result;
//        rr = false;
//        Log.e(TAG, "rr: " + rr);

        Log.e(TAG, "SVM命令识别结束");
//
//        if(result == 4 || result == 5){
////            Log.e(TAG, "基础响应开始");
////            response(result);
////            Log.e(TAG, "基础响应结束");
//        }else{
//            result = 0;
//        }

        dcdotFirst = dotDown;

        if (hwDotFirst == null) {
            hwDotFirst = dotDown;
        }
        Log.e(TAG, "recognize():识别出普通书写");

//        if(Looper.myLooper() == null) Looper.prepare();
//        Toast.makeText(XApp.context, "普通书写", Toast.LENGTH_SHORT).show();
//        Looper.loop();

        Gesture ges = new Gesture(gesT);
        ges.setInsId(result);
        Log.e(TAG, "基础响应开始");
        response(ges);
        Log.e(TAG, "基础响应结束");

//        simpleDots.clear();
//        mediaDots.clear();
//        Log.e(TAG, "mediaDots.clear(): " + mediaDots.size());

//        if (instructionType != 1) {
            Log.e(TAG, "BaseActivity.baseActivity: " + BaseActivity.baseActivity);
            if (BaseActivity.baseActivity != null) {
                Log.e(TAG, "回调响应");
                BaseActivity.baseActivity.receiveRecognizeResult(ges, dcdotFirst.pageID, dcdotFirst.x, dcdotFirst.y);
            }
//        }
    }

    //本次普通书写是否已经完成了单笔识别
    public boolean reced = false;
}
