package com.example.xmatenotes;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.xmatenotes.logic.model.Page.OldXueCheng;
import com.example.xmatenotes.ui.BaseActivity;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.manager.AudioManager;
import com.example.xmatenotes.logic.manager.OldPageManager;

import java.util.ArrayList;

/**
 * 录音笔迹动态同步绘制活动
 */
public class ReplayActivity extends BaseActivity {

    private final static String TAG = "ReplayActivity";

    public final static int MARGIN = 10;//“余光”宽度

    public final static long SPEED = 10;//笔迹绘制速度

    public final static long HANDWRITING_PERIOD = 30;//相邻两次笔迹间隔时间

    private OldPageManager oldPageManager = null;
    private AudioManager audioManager = null;
//    private DrawImageView drawImageView = null;
    private PageSurfaceView pageSurfaceView = null;
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

        oldPageManager = OldPageManager.getInstance();
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

        int resID = oldPageManager.getResIDByPageID(pageID);
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
                Rect childRect = PageSurfaceView.mapRect(rectXY, originalRect);
                Log.e(TAG,originalRect.toString());
                if(childRect != null){
                    pageSurfaceView = new PageSurfaceView(this,resID, childRect);
                    Log.e(TAG,"new DrawImageSurfaceView(this,resID, originalRect)");
                }
            }
        }else {
            pageSurfaceView = new PageSurfaceView(this,-1);
        }

        if(pageSurfaceView != null){
            LinearLayout.LayoutParams layoutParams = new
                    LinearLayout.LayoutParams (LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            replayView.addView(pageSurfaceView,layoutParams);
            Log.e(TAG,"加载drawImageView完成");

            String finalLocalCode = localCode;
            pageSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    pageSurfaceView.setPenWidth(penWidth);
                    OldXueCheng oldXueCheng = oldPageManager.getPageByPageID(pageID);
                    Log.e(TAG,"page != null?: "+(oldXueCheng != null));
                    if(oldXueCheng != null){
                        ArrayList<String> audioList = oldXueCheng.getAudioList(finalLocalCode);
                        ArrayList<OldXueCheng.LocalHandwritingsMap> localHwsMapList = oldXueCheng.getLocalHandwritings(finalLocalCode);
                        ArrayList<MediaDot> mediaDots = oldXueCheng.getPageDotsBuffer();
                        Log.e(TAG,"获取timelongDots完成");
                        if(mediaDots == null || localHwsMapList == null){
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
                                for(OldXueCheng.LocalHandwritingsMap lhm : localHwsMapList){
                                    pageSurfaceView.setPenColor(getColor());//更换画笔颜色
                                    for (int i = lhm.getBegin();i <= lhm.getEnd();i++){
                                        MediaDot tD = mediaDots.get(i);
                                        Log.e(TAG, "tD.x: "+ tD.getIntX());
                                        Log.e(TAG, "tD.y: "+ tD.getIntY());
                                        Log.e(TAG,"tD.timelong: "+tD.timelong);

                                        //遇到附加音频信息的笔迹
                                        if(tD.getIntX() == -1 && tD.getIntY() == -1){
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

                                        if(tD.getIntX() == -2 && tD.getIntY() == -2){
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

                                        Log.e(TAG,"(float)tD.x: "+(float)tD.getFloatX()+" (float)tD.y: "+(float)tD.getFloatY()+" tD.type: "+tD.type);
                                        pageSurfaceView.drawSDot(new SimpleDot(tD), rectXY);//等主线程结束才会绘制

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
//                                            TimelongDot tD = mediaDots.get(i);
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