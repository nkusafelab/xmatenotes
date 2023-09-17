package com.example.xmatenotes.logic.manager;

import com.example.xmatenotes.logic.model.handwriting.MediaDot;


import java.io.BufferedWriter;
import java.io.FileOutputStream;

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
