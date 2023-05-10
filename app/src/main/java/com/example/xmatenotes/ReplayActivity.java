package com.example.xmatenotes;

import static com.example.xmatenotes.Constants.A3_ABSCISSA_RANGE;
import static com.example.xmatenotes.Constants.A3_ORDINATE_RANGE;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.xmatenotes.DotClass.TimelongDot;
import com.example.xmatenotes.datamanager.AudioManager;
import com.example.xmatenotes.datamanager.Page;
import com.example.xmatenotes.datamanager.PageManager;

import java.util.ArrayList;

/**
 * 录音笔迹动态同步绘制活动
 */
public class ReplayActivity extends BaseActivity {

    private final static String TAG = "ReplayActivity";

    public final static int MARGIN = 10;//“余光”宽度

    public final static long SPEED = 10;//笔迹绘制速度

    public final static long HANDWRITING_PERIOD = 30;//相邻两次笔迹间隔时间

    private PageManager pageManager = null;
    private AudioManager audioManager = null;
//    private DrawImageView drawImageView = null;
    private DrawImageSurfaceView drawImageSurfaceView = null;
    private Rect rectXY = null;//待显示的矩形坐标范围

    private int penWidth = 3;
    private int[] color = new int[]{Color.RED, 0xFFFFA500, Color.YELLOW, Color.GREEN, 0xFF008F8F, Color.BLUE, 0xFF8F008F};
    private int colorNumber = -1;//颜色索引

//    private Thread drawThread = null;
//    private boolean drawThreadRunner = true;//绘制线程开关控制

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        pageManager = PageManager.getInstance();
        //初始化音频管理器
        audioManager = AudioManager.getInstance();
        audioManager.audioInit(this);

//        int audioID = getIntent().getIntExtra("audioID",-1);
        int pageID = getIntent().getIntExtra("pageID",-1);
        rectXY = new Rect();//矩形坐标范围
        rectXY.left = getIntent().getIntExtra("rectLeft",0);
        rectXY.top = getIntent().getIntExtra("rectTop",0);
        rectXY.right = getIntent().getIntExtra("rectRight",0);
        rectXY.bottom = getIntent().getIntExtra("rectBottom",0);
        String localCode = getIntent().getStringExtra("localCode");
        int localHWsMapID = getIntent().getIntExtra("localHWsMapID",0);
        if("".equals(localCode)){
            getSupportActionBar().setTitle("页码: "+localCode);
            localCode = "" + pageID;
        }else {
            getSupportActionBar().setTitle("局域编码: "+localCode);
        }

        Log.e(TAG,"pageID: "+pageID+" rect: "+rectXY.toString()+" localHWsMapID: "+localHWsMapID);
//        Log.e(TAG,"audioID: "+audioID+" pageID: "+pageID+" rect: "+rectXY.toString());

        LinearLayout replayView = (LinearLayout)findViewById(R.id.replayActivity);

        int resID = pageManager.getResIDByPageID(pageID);
        Log.e(TAG,"resID: "+resID);
        Rect originalRect = null;
        if(resID != -1){
            originalRect = DrawImageView.decodeDimensionOfImageFromResource(getResources(), resID);
            if(originalRect != null){
//                int middleWidth = Math.round((float) rect.right/2);
//                if(leftOrRight == 1){//右半边
//                    rect = new Rect(middleWidth,0,rect.right,rect.bottom);
//                }else if(leftOrRight == 0){//左半边
//                    rect = new Rect(0,0,middleWidth,rect.bottom);
//                }
                Rect childRect = DrawImageSurfaceView.mapRect(rectXY, originalRect);
                Log.e(TAG,originalRect.toString());
                if(childRect != null){
                    drawImageSurfaceView = new DrawImageSurfaceView(this,resID, childRect);
                    Log.e(TAG,"new DrawImageSurfaceView(this,resID, originalRect)");
                }
            }
        }else {
            drawImageSurfaceView = new DrawImageSurfaceView(this,-1);
        }

        if(drawImageSurfaceView != null){
            LinearLayout.LayoutParams layoutParams = new
                    LinearLayout.LayoutParams (LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            replayView.addView(drawImageSurfaceView,layoutParams);
            Log.e(TAG,"加载drawImageView完成");

            String finalLocalCode = localCode;
            drawImageSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    drawImageSurfaceView.setPenWidth(penWidth);
                    Page page = pageManager.getPageByPageID(pageID);
                    Log.e(TAG,"page != null?: "+(page != null));
                    if(page != null){
                        ArrayList<String> audioList = page.getAudioList(finalLocalCode);
                        ArrayList<Page.LocalHandwritingsMap> localHwsMapList = page.getLocalHandwritings(finalLocalCode);
                        ArrayList<TimelongDot> timelongDots = page.getPageDotsBuffer();
                        Log.e(TAG,"获取timelongDots完成");
                        if(timelongDots == null || localHwsMapList == null){
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            finish();
                            return;
                        }

                        try {
                            Thread.sleep(100);//否则bitmap还没有初始化，没办法绘制笔迹
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //如果不开启子线程，在循环中sleep时系统可能会提醒程序无响应
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                long dotTimelongStart = 0;
                                long timelongStart = 0;
                                long lastTimelong = 0;
                                int audioID = 0;//音频文件标记
                                boolean isPlayingAudio = false;//标记当前是否正在播放音频
                                int lhmnumber = 0;//lhm下标

                                Log.e(TAG, "localHwsMapList.size(): "+localHwsMapList.size());
                                for(Page.LocalHandwritingsMap lhm : localHwsMapList){
                                    drawImageSurfaceView.setPenColor(getColor());//更换画笔颜色
                                    for (int i = lhm.getBegin();i <= lhm.getEnd();i++){
                                        TimelongDot tD = timelongDots.get(i);
                                        Log.e(TAG, "tD.x: "+ tD.x);
                                        Log.e(TAG, "tD.y: "+ tD.y);
                                        Log.e(TAG,"tD.timelong: "+tD.timelong);

                                        //遇到附加音频信息的笔迹
                                        if(tD.x == -1 && tD.y == -1){
                                            Log.e(TAG, "tD.x == -1 && tD.y == -1");
                                            if(lhmnumber >= localHWsMapID){
                                                isPlayingAudio = true;
                                                dotTimelongStart = tD.timelong;
                                                lastTimelong = dotTimelongStart;
                                                timelongStart = System.currentTimeMillis();
                                                String audioName = audioList.get(audioID++)+".mp4";
                                                audioManager.automaticPlayXAppAudio(audioName);
                                                Log.e(TAG,"开始播放音频文件: "+audioName+" audioID: "+(audioID-1));
                                            }else {
                                                audioID++;
                                            }
                                            continue;
                                        }

                                        if(tD.x == -2 && tD.y == -2){
                                            if(isPlayingAudio){
                                                isPlayingAudio = false;
                                                Log.e(TAG,"音频笔迹结束");
                                            }
                                            continue;
                                        }

                                        if(tD.isEmptyDot()){
                                            continue;
                                        }

                                        //控制音频笔迹同步绘制
                                        if(isPlayingAudio){
                                            while ((System.currentTimeMillis()-timelongStart) < (tD.timelong-dotTimelongStart)){

                                            }
                                        }

                                        Log.e(TAG,"(float)tD.x: "+(float)tD.x+" (float)tD.y: "+(float)tD.y+" tD.type: "+tD.type);
                                        drawImageSurfaceView.drawDot((float)tD.x, (float)tD.y, tD.type, rectXY);//等主线程结束才会绘制

                                        if(lhmnumber >= localHWsMapID){
                                            //控制非音频笔迹绘制速度
                                            if(!isPlayingAudio){
                                                try {
                                                    Thread.sleep(SPEED);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }

                                    if(lhmnumber >= localHWsMapID){
                                        try {
                                            Thread.sleep(HANDWRITING_PERIOD);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    lhmnumber++;
                                }

//                                for(String audioName : audioList){
//                                    AudioManager.AudioDotsMap audioDotsMap = audioManager.getAudioDotsMap(audioName);
//                                    Log.e(TAG,"audioDotsMap != null?: "+(audioDotsMap != null));
//                                    if(audioDotsMap != null){
//                                        drawImageSurfaceView.setPenColor(getColor());
////                                        long dotTimelongStart = 0;
////                                        long timelongStart = 0;
////                                        long lastTimelong = 0;
//                                        for (int i = audioDotsMap.getBegin();i <= audioDotsMap.getEnd();i++){
//                                            TimelongDot tD = timelongDots.get(i);
//                                            Log.e(TAG,"tD.timelong: "+tD.timelong);
//                                            if(tD.x == -1 && tD.y == -1){
//                                                dotTimelongStart = tD.timelong;
//                                                lastTimelong = dotTimelongStart;
//                                                timelongStart = System.currentTimeMillis();
//                                                audioManager.automaticPlayXAppAudio(audioName+".mp4");
//                                                Log.e(TAG,"开始播放音频文件: "+audioID);
//                                                continue;
//                                            }
//                                            if(tD.x == -2 && tD.y == -2){
//                                                Log.e(TAG,"笔迹绘制结束");
//                                                continue;
//                                            }
//                                            while ((System.currentTimeMillis()-timelongStart) < (tD.timelong-dotTimelongStart)){
//
//                                            }
//
////                                        if((System.currentTimeMillis()-timelongStart) < (tD.timelong-dotTimelongStart)){
////                                            try {
////                                                Thread.sleep(tD.timelong-dotTimelongStart-(System.currentTimeMillis()-timelongStart));
////                                            } catch (InterruptedException e) {
////                                                e.printStackTrace();
////                                            }
////                                        }
//
//                                            Log.e(TAG,"(float)tD.x: "+(float)tD.x+" (float)tD.y: "+(float)tD.y+" tD.type: "+tD.type);
//                                            drawImageSurfaceView.drawDot((float)tD.x, (float)tD.y, tD.type, rectXY);//等主线程结束才会绘制
//
//                                        }
//                                        while (audioManager.isPlaying()){
//                                        }
//                                        try {
//                                            Thread.sleep(50);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }

                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                finish();
                            }
                        }).start();

                    }
                }
            });
        }
    }

    private int getColor(){
        colorNumber = (colorNumber+1)%7;
        return color[colorNumber];
    }

    @Override
    protected void onResume() {
        super.onResume();
        audioManager.startPlayAudio();
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioManager.pausePlayAudio();
//        drawThreadRunner = false;
//        drawThread.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(audioManager.isPlaying()){
            audioManager.stopPlayAudio();
        }
    }

}