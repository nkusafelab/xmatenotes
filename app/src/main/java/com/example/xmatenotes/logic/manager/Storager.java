package com.example.xmatenotes.logic.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.model.Page.Card;
import com.example.xmatenotes.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Storager {

    private static final String TAG = "Storager";

    private static final Storager storager = new Storager();

    public static Card cardCache;

    private Storager(){

    }

    public static Storager getInstance(){
        return storager;
    }

    /**
     * 序列化存储对象
     * @param absolutePath
     * @param obj
     * @throws IOException
     */
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

    /**
     * 序列化解析对象
     * @param absolutePath
     * @throws IOException
     */
    public Object serializeParseObject(String absolutePath) throws IOException, ClassNotFoundException {
        File file = new File(absolutePath);
        if (!file.exists()) {
            LogUtil.e(TAG, "待解析对象文件不存在！: "+absolutePath);
            return null;
        }
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        Object obj = ois.readObject();
        ois.close();
        return obj;
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

    public String getStringBySharedPreferences(String key){
        SharedPreferences prefs = XmateNotesApplication.context.getSharedPreferences(XmateNotesApplication.appSharedPreferences, Context.MODE_PRIVATE);
        String value = prefs.getString(key, "");
        if(!value.isEmpty()){
            return value;
        } else {
            LogUtil.e(TAG, "getStringBySharedPreferences: 未存储目标键值对！");
            return null;
        }
    }

    public void saveStringBySharedPreferences(String key, String value){
        SharedPreferences.Editor editor = XmateNotesApplication.context.getSharedPreferences(XmateNotesApplication.appSharedPreferences, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
        LogUtil.e(TAG, "saveStringBySharedPreferences: 存入键值对: "+key+":"+value);
    }

    public void saveBySharedPreferences(String key, Object value){
        saveBySharedPreferences(new HashMap<String, Object>(){
            {
                put(key, value);
            }
        });
    }

    public void saveBySharedPreferences(Map<String, Object> map){
        SharedPreferences.Editor editor = XmateNotesApplication.context.getSharedPreferences(XmateNotesApplication.appSharedPreferences, Context.MODE_PRIVATE).edit();
        Set<Map.Entry<String, Object>> set = map.entrySet();
        Iterator<Map.Entry<String, Object>> it = set.iterator();
        while (it.hasNext()){
            Map.Entry<String, Object> node = it.next();
            if(node.getValue() instanceof String){
                editor.putString(node.getKey(), (String) node.getValue());
            } else if(node.getValue() instanceof Boolean){
                editor.putBoolean(node.getKey(), (Boolean) node.getValue());
            } else if(node.getValue() instanceof Float){
                editor.putFloat(node.getKey(), (Float) node.getValue());
            } else if(node.getValue() instanceof Integer){
                editor.putInt(node.getKey(), (Integer) node.getValue());
            } else if(node.getValue() instanceof Long){
                editor.putLong(node.getKey(), (Long) node.getValue());
            }
        }
        editor.apply();
    }


}
