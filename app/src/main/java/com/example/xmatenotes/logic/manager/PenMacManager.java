package com.example.xmatenotes.logic.manager;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.xmatenotes.app.XmateNotesApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * <p><strong></strong>笔mac地址管理类</p>
 * <p>包括新mac地址的存储、mac地址到内存中唯一标识penID的映射等</p>
 * @author xiaofei
 * @since 21/10/22
 */
public class PenMacManager {
    //单例
    private volatile static PenMacManager penMacManager;

    private static final String TAG = "PenMacManager";

    //记录所有笔mac地址到有序数组的映射关系
    private static Map<String, Byte> macMapSToB;
    //记录所有笔有序数组到mac地址的映射关系
    private static Map<Byte, String> macMapBToS;

    //存储当前已有mac地址数量
    private static byte macNumber;
    //能够存储的笔mac地址数量上限
    private static final int MAX_PEN_NUMBER = 127;

    private PenMacManager(){
        macMapSToB = new HashMap<>();
        macMapBToS = new HashMap<>();
        macNumber = 0;
    }

    public static PenMacManager getInstance(){
        if(penMacManager == null){
            synchronized (PenMacManager.class){
                if(penMacManager == null){
                    penMacManager = new PenMacManager();
                }
            }
        }
        return penMacManager;
    }

    /**
     * 通过mac地址获取映射的penID
     * @param mac mac地址
     * @return 返回mac地址映射的penID;返回-2或-3表示获取失败
     */
    public static byte getPenIDByMac(String mac){
        if(mac == null || !isCorrectMac(mac)){
            Log.e(TAG,"非法mac地址："+mac);
            return -2;
        }

        if (!macMapSToB.containsKey(mac)){
            Log.e(TAG,"未存储的mac地址");
            return -3;
        }

        return macMapSToB.get(mac);
    }

    public static String getPenMacByID(byte penId){
        if (penId<1 || penId >macNumber) {
            Log.e(TAG,"非法的或未存储的笔ID");
            return "";
        }
        return macMapBToS.get(penId);
    }

    /**
     * 判断一个字符串是否是正确的mac地址
     * @param mac mac地址
     * @return 如果目标字符串是合法mac地址，返回true; 否则返回false
     */
    public static boolean isCorrectMac(String mac){
        boolean isCorrect = true;
        if(mac.length() != "00:00:00:00:00:00".length()){
            isCorrect = false;
        }else {
            mac = mac.toUpperCase();
            for(int i = 0;i < mac.length();i++){
                char c = mac.charAt(i);
                if((i+1)%3 == 0){
                    if(c != ':'){
                        isCorrect = false;
                        break;
                    }
                }else if((c<'0' || c>'9') && (c<'A' || c>'F')){
                    isCorrect = false;
                    break;
                }
            }
        }
        return isCorrect;
    }

    /**
     * 判断是否已经存储过某个mac地址
     * @param mac
     * @return 已存储返回true，否则为false
     */
    public boolean containMac(String mac){
        return macMapSToB.containsKey(mac);
    }

    /**
     * 存储mac地址，并建立与penID(简单数字(大于0))的一一对应关系，方便使用
     * @param mac
     * @return 存储成功返回映射的数字；否则返回-1
     */
    public byte putMac(String mac){
        if(!isCorrectMac(mac)){
            Log.e(TAG,"非法mac地址： "+mac);
            return -1;
        }

        if(!macMapSToB.containsKey(mac)){
            if(macNumber >= MAX_PEN_NUMBER){
                Toast.makeText(XmateNotesApplication.context, "超出允许使用的智能笔最大数量：" + MAX_PEN_NUMBER,Toast.LENGTH_SHORT).show();
                Log.e(TAG, "超出允许使用的智能笔最大数量：" + MAX_PEN_NUMBER);
                return -1;
            }
            macMapSToB.put(mac, ++macNumber);
            macMapBToS.put(macNumber,mac);
            return macNumber;
        }else {
            Log.e(TAG, "已存储的mac地址："+mac);
            return macMapSToB.get(mac);
        }
    }

    /**
     * 获得当前已存储的笔mac地址数量
     * @return 当前已存储的笔mac地址数量
     */
    public static byte getMacNumber() {
        return macNumber;
    }

    /**
     * 通过Mac地址获取姓名
     * @param mac
     * @return
     */
    public String getNameByMac(String mac){
        SharedPreferences pref = XmateNotesApplication.context.getSharedPreferences(XmateNotesApplication.peopleSharedPreferences, MODE_PRIVATE);
        return pref.getString(mac, null);
    }

}
