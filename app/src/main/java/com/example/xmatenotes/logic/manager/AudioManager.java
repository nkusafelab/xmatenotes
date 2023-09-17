package com.example.xmatenotes.logic.manager;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.example.xmatenotes.App.XmateNotesApplication;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.Page.Page;
import com.tqltech.tqlpencomm.bean.Dot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;

/**
 * <p><strong>音频管理类</strong></p>
 * <p>包括音频的录制、存储和播放等</p>
 * @see MediaRecorder
 * @see MediaPlayer
 * @see PageManager
 */
public class AudioManager {
    private static final String TAG = "AudioManager";

    private volatile static AudioManager audioManager = new AudioManager();
    private MediaRecorder recorder;//录音类
    private MediaPlayer player;//播放类

    //当前音视频文件所在目录的完整绝对路径
    private String audioPath = "";

    //当前录音文件的文件名，不含后缀，默认：“1”、“2”、...
    public static String currentRecordAudioName = "";
    //当前播放文件的文件名，包含后缀
    private String currentPlayAudioName = "";

    private static int recordAudioNumber = 0;

    //记录当前录音开始时间，单位为ms，若未开始录音，值应保持为0
    public static long recordStartTime = 0;

    //存储该页中音频与所属笔迹点的映射关系
    public static Map<String,AudioDotsMap> audioRangeMap = new HashMap<>();

    private PageManager pageManager = null;

    public static int audioFirstDot;//记录当前录音期间的第一个普通书写笔迹点在其所在页的dotsbuffer中的索引
    public static int audioLastDot;//记录当前录音期间的最后一个普通书写笔迹点在其所在页的dotsbuffer中的索引

    /**
     * 记录录音过程中产生的系列笔迹点在dotsbuffer中的起始与结尾位置
     */
    public static class AudioDotsMap{
        private int begin;
        private int end;

        public AudioDotsMap(){
        }

        public AudioDotsMap(int begin, int end){
            this.begin = begin;
            this.end = end;
        }

        public int getBegin() {
            return begin;
        }

        public void setBegin(int begin) {
            this.begin = begin;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }

    private AudioManager(){
    }

    public static AudioManager getInstance(){
//        if(audioManager == null){
//            synchronized (AudioManager.class){
//                if(audioManager == null){
//                    audioManager = new AudioManager();
//                }
//            }
//        }
        return audioManager;
    }

    /**
     * 更改音频存储路径，若更改后的路径不存在，则进行创建
     * @param audioPath 保存音频文件的目录绝对路径
     * @return 更改成功返回true; 否则返回false
     */
    public boolean setAudioPath(String audioPath){
        File file = new File(audioPath);
        if(!file.exists()) {
            file.mkdirs();
        }
        if(!file.exists()){
            return false;
        }else{
            this.audioPath = audioPath;
            return true;
        }

    }

    /**
     * 录音初始化。需要使用录音功能的活动创建时都需要调用此方法
     * @param activity 使用录音功能的活动
     */
    public void audioInit(Activity activity){
        //默认录音文件保存位置
        this.audioPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/XAppSounds";

        pageManager = PageManager.getInstance();
        Log.e(TAG,"PageManager.getInstance()");

        //判断安卓版本
        if(Build.VERSION.SDK_INT >=23){
            //需要申请的权限
            String [] permission={
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            };
            for(int i=0;i<permission.length;i++){
                //判断是否有权限
                if(XmateNotesApplication.context.checkSelfPermission(permission[i]) != PackageManager.PERMISSION_GRANTED){
                    activity.requestPermissions(permission,i);
                }
            }
        }
    }

    public AudioDotsMap getAudioDotsMap(String audioName){
        Log.e(TAG,"audioRangeMap.size(): "+audioRangeMap.size());
        if(audioRangeMap.containsKey(audioName)){
            return audioRangeMap.get(audioName);
        }
        return null;
    }

    //播音初始化
    public void mediaInit(){

    }

    /**
     * 播放assets文件夹下的音频文件
     * @param audioFileName 目标音频文件的文件名，包含后缀
     * @throws IOException
     */
    public void startPlayAssetsAudio(String audioFileName) throws IOException {
        currentPlayAudioName = audioFileName;
        AssetFileDescriptor afd = XmateNotesApplication.context.getApplicationContext().getAssets().openFd("audio/"+audioFileName);
        if(player == null){
            player = new MediaPlayer();
            try {
                player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!player.isPlaying()){
            player.start();
        }
    }

    /**
     * 播放XAppSounds文件夹下的音频文件
     * @param audioFileName 目标音频文件的文件名，包含后缀
     */
    public void startPlayXAppAudio(String audioFileName){
        currentPlayAudioName = audioFileName;
        startPlayAudio(audioPath+"/"+audioFileName);
    }

    /**
     * 播放音频文件
     * @param audioFileName 目标音频文件的完整绝对路径
     */
    public void startPlayAudio(String audioFileName){
        if(player == null){
            player = new MediaPlayer();
            try {
                player.setDataSource(audioFileName);
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!player.isPlaying()){
            player.start();
        }

    }

    /**
     * pause或reset操作后再次播放时调用
     */
    public void startPlayAudio(){
        if(player != null) {
            if(!player.isPlaying()){
                player.start();
            }
        }
    }

    /**
     * 暂停录音
     */
    public void pausePlayAudio(){
        if(player != null) {
            if (player.isPlaying()) {
                player.pause();
            }
        }
    }

    /**
     * 重置player状态，当前只能操作指定的XAppSounds文件夹下的音频文件
     */
    public void resetPlayAudio(){
        if(player != null){
            if(player.isPlaying()){
                player.reset();
            }
            try {
                player.setDataSource(audioPath+"/"+ currentPlayAudioName);
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 停止音频播放并释放相关资源
     */
    public void stopPlayAudio(){
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
    }

    /**
     * 判断player是否正在播放
     * @return 正在播放则返回true
     */
    public boolean isPlaying(){
        if(player != null){
            return player.isPlaying();
        }
       return false;
    }

    /**
     * 开始录音
     * @param audioFileName 待录音文件的文件名，不含后缀
     */
    public void startRecordAudio(String audioFileName){
        File file = new File(audioPath,audioFileName+".mp4");
        File fileParent = file.getParentFile();
        if(!fileParent.exists()){
            fileParent.mkdirs();
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG,"createNewFile() failed");
                e.printStackTrace();
            }
        }
        if(!file.exists()){
            return ;
        }

        setCurrentRecordAudioName(audioFileName);

        if(recorder == null){
            recorder = new MediaRecorder();
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置录音来源
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//设置输出格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//设置编码格式
        recorder.setOutputFile(file.getAbsolutePath());//设置输出路径
        try {
            recorder.prepare();//准备
            recorder.start();//开始录音
            RATimer = true;//打开录音专用计时器开关
            recordStartTime = System.currentTimeMillis();//记录录音开始时间
            Log.e(TAG,"recordStartTime: "+recordStartTime);
//            Toast.makeText(this,"开始录音",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG,"开启录音失败");
            stopRecordAudio();
            e.printStackTrace();
        }
    }

    /**
     * 开始录音，录音文件名采用默认方式
     */
    public void startRecordAudio(){
        startRecordAudio(Page.createRecordAudioName(++recordAudioNumber));
    }

    public int getCurrentRecordAudioNumber(){
        return recordAudioNumber;
    }

    public void setCurrentRecordAudioNumber(int n){
        recordAudioNumber = n;
    }

    public String getCurrentRecordAudioName() {
        return currentRecordAudioName;
    }

    public static void setCurrentRecordAudioName(String name) {
        currentRecordAudioName = name;
    }

    /**
     * 停止录音，并释放相应资源
     */
    public void stopRecordAudio(){
        if(recorder != null){
            try{
                recorder.setOnErrorListener(null);
                recorder.setOnInfoListener(null);
                recorder.setPreviewDisplay(null);
                recorder.stop();//停止录音
                recordStartTime = 0;//录音开始时间归零

                MediaDot mD = new MediaDot();
                mD.setX(-2);
                mD.setY(-2);
                mD.timelong = System.currentTimeMillis();
                mD.type = Dot.DotType.PEN_UP;
                mD.pageID = PageManager.currentPageID;
                mD.audioID = recordAudioNumber;
                mD.penMac = XmateNotesApplication.mBTMac;
                pageManager.writeDot(mD);
//                Page p = pageManager.getPageByPageID(mD.pageID);
//                audioLastDot = p.getPageDotsBufferSize()-1;
//                addAudio();
                Page.isAudioRangeBeginTrue = false;
            }catch (IllegalStateException e){
                recorder = null;
                recorder = new MediaRecorder();
            }

//            recorder.reset();//重置
            recorder.release();//释放资源
            recorder=null;
            Log.e(TAG,"结束录音");
//            Toast.makeText(this,"停止录音",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加录制音频文件名与笔迹的映射关系
     */
    public static void addAudio(){
        audioRangeMap.put(currentRecordAudioName, new AudioDotsMap(audioFirstDot,audioLastDot));
    }

    /**
     * 录音计时器开关
     */
    private static boolean RATimer = false;

    /**
     * 开启录音。该方法会开启线程和计时器来进行录音。对应的关闭录音方法为{@link #stopRATimer()}
     */
    public void startRATimer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //播放录音开始提示音
//                comPlayAssetsAudio("startrecord.mp3");
                try {
                    comPlayAssetsAudio("beep.ogg");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Log.e(TAG,"执行完comPlayAssetsAudio(beep.ogg); "+System.currentTimeMillis()+" ms");
                long a = System.currentTimeMillis();//记录录音开始时间
                startRecordAudio();
                long s = System.currentTimeMillis();
                Log.e(TAG,"startRecordAudio()执行了 "+(s-a)+" ms");
                while (RATimer == true){

                }
                Log.e(TAG, "录音进行了 "+String.valueOf((System.currentTimeMillis()-s)/1000)+" s");
                stopRecordAudio();
                //播放录音结束提示音
//                comPlayAssetsAudio("endrecord.mp3");
                try {
                    comPlayAssetsAudio("beep.ogg");
                    comPlayAssetsAudio("beep.ogg");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
    }

    /**
     * 结束录音。关闭录音线程和计时器。对应的开启录音方法为{@link #startRATimer()}
     */
    public void stopRATimer(){
        RATimer = false;//关闭录音专用计时器开关
    }

    /**
     * 返回录音计时器开关是否开启
     * @return 开启则返回true
     */
    public static boolean isRATimerStart(){
        return RATimer;
    }

    /**
     * 开启线程自动完整播放assets文件目录下的某个音频文件，结束后释放相关资源
     * @param audioFileName 目标音频文件的文件名，包含后缀
     */
    public void autoPlayAssetsAudio(String audioFileName) {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                try {
                    startPlayAssetsAudio(audioFileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                while (isPlaying()){

                }
                stopPlayAudio();
            }
        }).start();
    }

    /**
     * （不开启线程）完整播放assets文件目录下的某个音频文件，结束后释放相关资源
     * @param audioFileName 目标音频文件的文件名，包含后缀
     */
    @SneakyThrows
    public void comPlayAssetsAudio(String audioFileName) throws IOException {
        startPlayAssetsAudio(audioFileName);
        while (isPlaying()){

        }
        Log.e(TAG,"执行stopPlayAudio()前: "+System.currentTimeMillis()+" ms");
        stopPlayAudio();
        Log.e(TAG,"执行stopPlayAudio()后: "+System.currentTimeMillis()+" ms");
    }

    /**
     * 开启线程自动完整播放XAppSounds文件夹下的某个音频文件，结束后释放相关资源
     * @param audioFileName
     */
    public void automaticPlayXAppAudio(String audioFileName){
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                startPlayXAppAudio(audioFileName);
                while (isPlaying()){

                }
                stopPlayAudio();
            }
        }).start();
    }

    public void clear(){
        audioRangeMap.clear();
        recordAudioNumber = 0;
    }

}

