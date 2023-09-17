package com.example.xmatenotes.logic.manager;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.example.xmatenotes.logic.model.Page.Card;
import com.example.xmatenotes.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Storager {

    private static final String TAG = "Storager";

    private static final Storager storager = new Storager();

    public static Card cardCache;

    private Storager(){

    }

    public static Storager getInstance(){
        return storager;
    }

    public void serializeSaveObject(String absolutePath, Object obj) throws IOException {
        File file = new File(absolutePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(obj);
        oos.flush();
        oos.close();
    }

    //参数为包含后缀的文件绝对路径
    public String saveBmpWithSuffix (String absolutePath, Bitmap bitmap){
        saveBmp(absolutePath, bitmap, Bitmap.CompressFormat.PNG, 10, null);
        return absolutePath + ".png";
    }

    //参数为包含后缀的文件名
    public boolean saveBmpWithSuffix (String savePath, String fileName, Bitmap bitmap){
        return saveBmp(savePath+"/"+fileName, bitmap, Bitmap.CompressFormat.PNG, 10, null);
    }
    //存储局域bitmap,参数为包含后缀的文件名
    public boolean saveBmpWithSuffix (String savePath, String fileName, Bitmap bitmap, Rect rect){
        return saveBmp(savePath+"/"+fileName, bitmap, Bitmap.CompressFormat.PNG, 10, rect);
    }

    //参数为不包含后缀的文件绝对路径
    public String saveBmpWithoutSuffix (String absolutePath, Bitmap bitmap){
        saveBmp(absolutePath + ".png", bitmap, Bitmap.CompressFormat.PNG, 10, null);
        return absolutePath + ".png";
    }

    //参数为不包含后缀的文件名
    public boolean saveBmpWithoutSuffix (String savePath, String fileName, Bitmap bitmap){
        return saveBmp(savePath+"/"+fileName + ".png", bitmap, Bitmap.CompressFormat.PNG, 10, null);
    }
    //存储局域bitmap,参数为不包含后缀的文件名
    public boolean saveBmpWithoutSuffix (String savePath, String fileName, Bitmap bitmap, Rect rect){
        return saveBmp(savePath+"/"+fileName + ".png", bitmap, Bitmap.CompressFormat.PNG, 10, rect);
    }

    //参数为包含后缀的文件绝对路径名，如: 1-1.png
    private boolean saveBmp (String absolutePath, Bitmap bitmap, Bitmap.CompressFormat compressFormat,
                             int quality, Rect rect){
        Log.e(TAG, absolutePath);
        if (!absolutePath.endsWith(".png")) {

            LogUtil.e(TAG, "saveBmp(): bmp存储文件名后缀不合法");
            return false;
        }
        File file = new File(absolutePath);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
//        if(file.exists()) {
//            Log.e(TAG,"saveBmp(): bmp存储文件已存在，请更换文件名");
//            return false;
//        }
        Bitmap bmp = bitmap;
        if (rect != null) {
            bmp = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());//裁剪bitmap
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if (bmp != null) {
                bmp.compress(compressFormat, quality, out);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            out = null;
        }
        return true;
    }
}
