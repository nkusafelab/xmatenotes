package com.example.xmatenotes.App;


import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.xmatenotes.logic.manager.AudioManager;
import com.example.xmatenotes.logic.manager.ExcelReader;
import com.example.xmatenotes.logic.manager.PageManager;
import com.example.xmatenotes.logic.manager.PenMacManager;
import com.example.xmatenotes.logic.manager.VideoManager;
import com.example.xmatenotes.logic.manager.Writer;
import com.example.xmatenotes.logic.model.Role;
import com.example.xmatenotes.logic.model.instruction.Instruction;
import com.tqltech.tqlpencomm.PenCommAgent;

/**
 * Created by wangyong on 7/4/17.
 */
public class XmateNotesApplication extends Application {

    private final static String TAG = "XApp";

    //全局默认无效值
    public static final float DEFAULT_FLOAT = -1f;
    public static final int DEFAULT_INT = -1;
    public static final long DEFAULT_LONG = -1;

    public static final String peopleSharedPreferences = "people";

    public static String mPenName = "SmartPen";
    public static String mFirmWare = "B736_OID1-V10000";
    public static String mMCUFirmWare = "MCUF_R01";
    public static String mCustomerID = "0000";
    public static String mBTMac = "00:00:00:00:00:00";
    public static int mBattery = 100;
    public static boolean mCharging = false;
    public static int mUsedMem = 0;
    public static boolean mBeep = true;
    public static boolean mPowerOnMode = true;
    public static int mPowerOffTime = 20;
    public static long mTimer = 1262275200; // 2010-01-01 00:00:00
    public static int mPenSens = 0;

    public static String tmp_mPenName;
    public static boolean tmp_mBeep = true;
    public static boolean tmp_mPowerOnMode = true;
    public static boolean tmp_mEnableLED = false;
    public static int tmp_mPowerOffTime;
    public static int tmp_mPenSens;
    public static long tmp_mTimer;

    //全局上下文
    public static Context context = null;

    public static PenCommAgent bleManager = null;
    public static ExcelReader excelReader = null;
    public static PageManager pageManager = null;
    public static PenMacManager penMacManager = null;
    public static VideoManager videoManager = null;
    public static AudioManager audioManager = null;
    public static Instruction instruction = null;
    public static Writer writer = null;

    /**
     * 当前点阵纸张幅面规格
     */
    public static String AX = "A3";

    public static int screenWidth;
    public static int screenHeight;

    public static Role role = null;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        penMacManager = PenMacManager.getInstance();
        excelReader = ExcelReader.getInstance();

        excelReader.openExcel("excel/A3学程样例·纸分区坐标.xlsx","一级局域索引表");
        excelReader.switchSheet("二级局域编码表");


        videoManager = VideoManager.getInstance();

        Log.e(TAG,"PageManager.getInstance() start");
        pageManager = PageManager.getInstance();
        Log.e(TAG,"PageManager.getInstance() end");

        instruction = new Instruction();
//        writer.init();

    }

    /**
     * 获取本地软件版本号
     */
    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
            Log.d("TAG", "本软件的版本号。。" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
            Log.d("TAG", "本软件的版本号。。" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    public static boolean isDebuggable(Context ctx) {
        boolean debuggable = false;
        PackageManager pm = ctx.getApplicationContext().getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(ctx.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
        /*debuggable variable will remain false*/
        }
        return debuggable;
    }
}

