package com.example.xmatenotes.datamanager;

import android.content.Context;

import com.example.xmatenotes.DotClass.MediaDot;
import com.tqltech.tqlpencomm.bean.Dot;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class DotFileManager {
    private static final String TAG = "DotFileManager";

    //单例
    private volatile static DotFileManager dotFileManager;

    private final static String FILE_NAME = "XmateNotes";//存储文件名
    private FileOutputStream out = null;
    private BufferedWriter writer = null;

    private static MediaDot lastMediaDot = null;

    private DotFileManager() {
    }

    public static DotFileManager getInstance(){
        if(dotFileManager == null){
            synchronized (DotFileManager.class){
                if(dotFileManager == null){
                    dotFileManager = new DotFileManager();
                }
            }
        }
        return dotFileManager;
    }


}
