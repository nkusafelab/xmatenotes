package com.example.xmatenotes;

import static com.example.xmatenotes.Constants.A3_ABSCISSA_RANGE;
import static com.example.xmatenotes.Constants.A3_ORDINATE_RANGE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Random;


import com.example.xmatenotes.App.XApp;
import com.example.xmatenotes.DotClass.MediaDot;
import com.example.xmatenotes.datamanager.AudioManager;
import com.example.xmatenotes.datamanager.ExcelReader;
import com.example.xmatenotes.datamanager.LocalRect;
import com.example.xmatenotes.datamanager.Page;
import com.example.xmatenotes.datamanager.PageManager;
import com.example.xmatenotes.datamanager.PenMacManager;
import com.example.xmatenotes.instruction.Instruction;
import com.google.common.collect.ArrayListMultimap;
import com.tqltech.tqlpencomm.bean.Dot;

import com.tqltech.tqlpencomm.PenCommAgent;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

    private final static String TAG = "MainActivity";

    private boolean bIsReply = false;
    private ImageView imageView;
    private RelativeLayout gLayout;
    private DrawView[] bDrawl = new DrawView[2];  //add 2016-06-15 for draw
//    private DrawImageView drawImageView = null;//画图控件
    private DrawImageSurfaceView drawImageSurfaceView = null;//画图控件

    private final static boolean isSaveLog = false;          //是否保存绘制数据到日志
    private final static String LOGPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TQL/"; //绘制数据保存目录

    private static final int REQUEST_SELECT_DEVICE = 1;      //蓝牙扫描
    private static final int REQUEST_ENABLE_BT = 2;          //开启蓝牙
    private static final int REQUEST_LOCATION_CODE = 100;    //请求位置权限
    private static final int GET_FILEPATH_SUCCESS_CODE = 1000;//获取txt文档路径成功

    private int penType = 1;                                 //笔类型（0：TQL-101  1：TQL-111  2：TQL-112 3: TQL-101A）

    private double XDIST_PERUNIT = Constants.XDIST_PERUNIT;  //码点宽
    private double YDIST_PERUNIT = Constants.YDIST_PERUNIT;  //码点高
    private double A5_WIDTH = Constants.A5_WIDTH;            //本子宽
    private double A5_HEIGHT = Constants.A5_HEIGHT;          //本子高
    public double A4_WIDTH = Constants.A4_WIDTH; // 本子宽
    public double A4_HEIGHT = Constants.A4_HEIGHT; // 本子高
    private double  A5_BG_REAL_WIDTH = Constants.A5_BG_REAL_WIDTH;     //资源背景图宽
    private double A5_BG_REAL_HEIGHT = Constants.A5_BG_REAL_HEIGHT;   //资源背景图高
    private double A4_BG_REAL_WIDTH = Constants.A4_BG_REAL_WIDTH;     //资源背景图宽
    private double A4_BG_REAL_HEIGHT = Constants.A4_BG_REAL_HEIGHT;   //资源背景图高

    private int BG_WIDTH;                                    //显示背景图宽
    private int BG_HEIGHT;                                   //显示背景图高
    private int A5_X_OFFSET;                                 //笔迹X轴偏移量
    private int A5_Y_OFFSET;                                 //笔迹Y轴偏移量
    private int gcontentLeft;                                //内容显示区域left坐标
    private int gcontentTop;                                 //内容显示区域top坐标

    public static int mWidth;                              //屏幕宽
    public static int mHeight;                             //屏幕高

    private float mov_x;                                     //声明起点坐标
    private float mov_y;                                     //声明起点坐标
    private int gCurPageID = -1;                             //当前PageID
    private int gCurBookID = -1;                             //当前BookID
    private float gScale = 1;                                //笔迹缩放比例
    private int gColor = 6;                                  //笔迹颜色
    private int gWidth = 3;                                  //笔迹粗细
    private int gSpeed = 30;                                 //笔迹回放速度
    private float gOffsetX = 0;                              //笔迹x偏移
    private float gOffsetY = 0;                              //笔迹y偏移

    private ArrayListMultimap<Integer, Dots> dot_number = ArrayListMultimap.create();  //Book=100笔迹数据
    private ArrayListMultimap<Integer, Dots> dot_number1 = ArrayListMultimap.create(); //Book=0笔迹数据
    private ArrayListMultimap<Integer, Dots> dot_number2 = ArrayListMultimap.create(); //Book=1笔迹数据
    private ArrayListMultimap<Integer, Dots> dot_number4 = ArrayListMultimap.create(); //笔迹回放数据
    private Intent serverIntent = null;
    private Intent ckplayerIntent = null;
    private Intent LogIntent = null;
    private PenCommAgent bleManager;
    private String penAddress;

    public static float g_x0, g_x1, g_x2, g_x3;
    public static float g_y0, g_y1, g_y2, g_y3;
    public static float g_p0, g_p1, g_p2, g_p3;
    public static float g_vx01, g_vy01, g_n_x0, g_n_y0;
    public static float g_vx21, g_vy21;
    public static float g_norm;
    public static float g_n_x2, g_n_y2;

    private int gPIndex = -1;
    private boolean gbSetNormal = false;
    private boolean gbCover = false;

    private float pointX;
    private float pointY;
    private int pointZ;

    private boolean bIsOfficeLine = false;
    //    private RoundProgressBar bar;
    private BluetoothLEService mService = null;              //蓝牙服务
    BluetoothLEService.OnDataReceiveListener dotsListener = null;

    private RelativeLayout dialog;
    private Button confirmBtn;
    private TextView showInftTextView;

    private float gpointX;
    private float gpointY;

    private String gStrHH = "";
    private boolean bLogStart = false;

    public int mN;

    private volatile long penUpTime;
    private volatile boolean firstpen=true;
    protected volatile long curTime;

    private AudioManager audioManager = null;
    private PenMacManager penMacManager = null;//管理所有mac地址的对象
    private PageManager pageManager = null;
    private ExcelReader excelReader = null;

    private String penMac = XApp.mBTMac;//存储笔mac地址

    private boolean wTimer = false;//普通书写完毕计时器开关
    private long wStartTime = 0;//普通书写完毕计时起点

    private int currentPageID = -1;//当前pageID

    private boolean audioRecorder = false;//录音开关

    public int videoNoteColor = Color.BLUE;//视频笔迹颜色
    public int audioNoteColor = Color.GREEN;//音频笔记颜色

    private LocalRect lastLocalRect = null;//上一个局域

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(final ComponentName className, IBinder rawBinder) {
            mService = ((BluetoothLEService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                finish();
            }

            dotsListener = new BluetoothLEService.OnDataReceiveListener(){
                @Override
                public void onDataReceive(final Dot dot) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.i(TAG,"Dot信息,BookID"+dot.BookID);
//                            Log.i(TAG,"Dot信息,ab_x"+dot.ab_x);
                            processDots(dot);
                        }
                    });
                }

                @Override
                public void onOfflineDataReceive(final Dot dot) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processDots(dot);
                        }
                    });
                }

                @Override
                public void onFinishedOfflineDown(boolean success) {
                    //Log.i(TAG, "---------onFinishedOfflineDown--------" + success);
/*
                    layout.setVisibility(View.GONE);
                    bar.setProgress(0);
*/
                }

                @Override
                public void onOfflineDataNum(final int num) {
                    //Log.i(TAG, "---------onOfflineDataNum1--------" + num);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                	textView.setText("离线数量有" + Integer.toString(num * 10) + "bytes");
 /*

                                    //if (num == 0) {
                                    //    return;
                                    //}

                                	Log.e("zgm","R.id.dialog1"+R.id.dialog);
                                    dialog = (RelativeLayout)findViewById(R.id.dialog);
                                    Log.e("zgm","dialog"+dialog.getId());
                                    dialog.setVisibility(View.VISIBLE);
                                    textView = (TextView) findViewById(R.id.textView2);
                                    textView.setText("离线数量有" + Integer.toString(num * 10) + "bytes");
                                    confirmBtn = (Button) findViewById(R.id.button);
                                    confirmBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.setVisibility(View.GONE);
                                        }
                                    });
*/
                                }

                            });
                        }
                    });
                }

                @Override
                public void onReceiveOIDSize(final int OIDSize) {
                    Log.i("TEST1", "-----read OIDSize=====" + OIDSize);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            gCurPageID = -1;
                            showInftTextView.setText("点读！点读值为："+OIDSize);
                        }
                    });
                }

                @Override
                public void onReceiveOfflineProgress(final int i) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
/*
                            if (startOffline) {

                                layout.setVisibility(View.VISIBLE);
                                text.setText("开始缓存离线数据");
                                bar.setProgress(i);
                                Log.e(TAG, "onReceiveOfflineProgress----" + i);
                                if (i == 100) {
                                    layout.setVisibility(View.GONE);
                                    bar.setProgress(0);
                                }
                            } else {
                                layout.setVisibility(View.GONE);
                                bar.setProgress(0);
                            }
  */
                        }

                    });
                }

                @Override
                public void onDownloadOfflineProgress(final int i) {

                }

                @Override
                public void onReceivePenLED(final byte color) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "receive led is " + color);
                            switch (color) {
                                case 1: // blue
                                    gColor = 5;
                                    break;
                                case 2: // green
                                    gColor = 3;
                                    break;
                                case 3: // cyan
                                    gColor = 8;
                                    break;
                                case 4: // red
                                    gColor = 1;
                                    break;
                                case 5: // magenta
                                    gColor = 7;
                                    break;
                                case 6: // yellow
                                    gColor = 2;
                                    break;
                                case 7: // white
                                    gColor = 6;
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }

                @Override
                public void onOfflineDataNumCmdResult(boolean success) {
                    //Log.i(TAG, "onOfflineDataNumCmdResult---------->" + success);
                }

                @Override
                public void onDownOfflineDataCmdResult(boolean success) {
                    //Log.i(TAG, "onDownOfflineDataCmdResult---------->" + success);
                }

                @Override
                public void onWriteCmdResult(int code) {
                    //Log.i(TAG, "onWriteCmdResult---------->" + code);
                }

                @Override
                public void onReceivePenType(int type) {
                    //Log.i(TAG, "onReceivePenType type---------->" + type);
                    penType = type;
                }
            };
            mService.setOnDataReceiveListener(dotsListener);
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;Log.d(TAG, "onServiceDisconnected");
        }
    };

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @SuppressLint("NewApi") @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    String toast = msg.getData().getString("Toast");
                    Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private void handlerToast(String toast){
        Message message = new Message();
        message.what = 0;
        Bundle bundle = new Bundle();
        bundle.putString("Toast",toast);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent gattServiceIntent = new Intent(this, BluetoothLEService.class);
        boolean bBind = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        bleManager = PenCommAgent.GetInstance(getApplication());
        excelReader = ExcelReader.getInstance();

        //初始化音频管理器
        audioManager = AudioManager.getInstance();
        audioManager.audioInit(this);

        pageManager = PageManager.getInstance();
        Log.e(TAG,"PageManager.getInstance()");

        penMac = XApp.mBTMac;
        penMacManager = PenMacManager.getInstance();//必须在加载数据之前

//        drawImageView = (DrawImageView)findViewById(R.id.drawImageView);
        drawImageSurfaceView = (DrawImageSurfaceView)findViewById(R.id.drawImageSurfaceView);
        switchPage(0);
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        mWidth = dm.widthPixels;
//        mHeight = dm.heightPixels;
//        XApp.screenWidth = mWidth;
//        XApp.screenHeight = mHeight;
//
//        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
//        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
//        Log.e(TAG, "density=======>" + density + ",densityDpi=======>" + densityDpi);
//        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
//        int screenWidth = (int) (mWidth / density);  // 屏幕宽度(dp)
//        int screenHeight = (int) (mHeight / density);// 屏幕高度(dp)
//        Log.e(TAG, "width=======>" + screenWidth);
//        Log.e(TAG, "height=======>" + screenHeight);
//
//        Log.e(TAG, "-----screen pixel-----width:" + mWidth + ",height:" + mHeight);
//
//        //       gLayout = (RelativeLayout) findViewById(R.id.mylayout);
//        //       gLayout = (RelativeLayout) findViewById(R.id.mylayout);
//        RelativeLayout mreLayout=new RelativeLayout(this);
//        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
//                LayoutParams.MATCH_PARENT,
//                LayoutParams.MATCH_PARENT);
//        param.width = (int) mWidth;
//        param.height = (int) mHeight - getSupportActionBar().getHeight();
//        param.rightMargin = 1;
//        param.bottomMargin = 1;
//        param.topMargin=300;

//        drawImageView = new DrawImageView(this,R.id.drawImageView);
//        showInftTextView=(TextView) findViewById(R.id.maintextview);
//        imageView= (ImageView) findViewById(R.id.drawImageView);
//        showInftTextView.setText("动作模式");


        //        mreLayout.setBackgroundColor(Color.WHITE);
//        drawImageView = new DrawImageView(this, R.drawable.x1);
//        mreLayout.addView(drawImageView, param);
//        this.addContentView(mreLayout, param);
//        drawInit();

        copy(R.raw.characterstroke, "characterstroke.txt");
        copy(R.raw.collectdatastroke, "collectdatastroke.txt");
        copy(R.raw.modelstroke, "modelstroke.txt");
        copy(R.raw.outpoint, "outpoint.txt");
        copy(R.raw.point, "point.txt");
        copy(R.raw.pointcharacter, "pointcharacter.txt");
        copy(R.raw.statisticalresults, "statisticalresults.txt");
        copy(R.raw.tempstroke, "tempstroke.txt");
        copy(R.raw.trainstroke, "trainstroke.txt");
        copy(R.raw.trainstroke3, "trainstroke3.txt");
        copy(R.raw.trainstroke12, "trainstroke12.txt");

        Log.e(TAG,"MainActivity.onCreate()");

        /*
        gLayout = (RelativeLayout) findViewById(R.cid.mylayout);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        param.width = (int) mWidth;
        param.height = (int) mHeight;
        param.rightMargin = 1;
        param.bottomMargin = 1;
        drawInit();
        */

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(BluetoothLEService.isPenConnected){
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name)+"（蓝牙已连接）");
        }else {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name)+"（蓝牙未连接）");
        }
        if(mService != null){
            mService.setOnDataReceiveListener(dotsListener);//添加监听器
        }
        Log.e(TAG,"MainActivity.onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //如果正在录音，结束录音
        if(audioRecorder == true){
            audioRecorder = false;
            audioManager.stopRATimer();
        }
        Log.e(TAG,"MainActivity.onPause()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        switchPage(pageManager.currentPageID);
        Log.e(TAG,"MainActivity.onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,"MainActivity.onStop()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        //计算
        float ratio = 1f;
        ratio = (float) ((ratio * mWidth) / A5_BG_REAL_WIDTH);
        BG_WIDTH = (int) (A5_BG_REAL_WIDTH * ratio);
        BG_HEIGHT = (int) (A5_BG_REAL_HEIGHT * ratio);

        A5_X_OFFSET = 20;
        A5_Y_OFFSET = 100;

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        bleManager = PenCommAgent.GetInstance(getApplication());

        // 0-free format;1-for A4;2-for A3
        //Log.i(TAG, "-----------setDataFormat-------------");
//        bleManager.setXYDataFormat(1);//设置码点输出规格

        switch (item.getItemId()) {

            case R.id.action_settings:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, SelectDeviceActivity.class);Log.e(TAG,"action_settings");
                startActivityForResult(serverIntent, REQUEST_SELECT_DEVICE);
                return true;
            case R.id.action_ckplayer:
                ckplayerIntent = new Intent(this, CkplayerActivity.class);Log.e(TAG,"action_ckplayer");
                startActivity(ckplayerIntent);
                return true;
            case R.id.action_clear:
                pageManager.clear();
                drawImageSurfaceView.clear();
                Log.e(TAG,"action_clear");
                return true;
            case R.id.dot_info_intent:
                Intent dotInfoIntent = new Intent(this, DotInfoActivity.class);Log.e(TAG,"dot_info_intent");
                startActivity(dotInfoIntent);
                return true;
            default:
/*
            case R.id.clear:
                drawInit();
                bDrawl[0].canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                if (!bIsReply) {
                    dot_number.clear();
                    dot_number1.clear();
                    dot_number2.clear();
                    dot_number4.clear();
                }

                return true;
 */
        }
        return false;
    }

    public void copy(int para, String filename){
        InputStream in = null;
        //FileInputStream fis = null;
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try{
            //创建一个输入流对象
            //fis = new FileInputStream();
            in = getResources().openRawResource(para);  /* 从raw文件夹中读取文件，不需要后缀 */
            InputStreamReader isReader = new InputStreamReader(in, "UTF-8");
            BufferedReader reader = new BufferedReader(isReader);

            //创建一个输出流对象
            out = openFileOutput(filename, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));

            String s = null;

            //边读边写
            while ((s=reader.readLine()) != null){
                writer.write(s);
                writer.newLine();
                writer.flush();
//                System.out.println(s);
            }
//            byte[] bytes = new byte[1024*1024]; //1MB
//            int readCount = 0;
//            while((readCount = in.read(bytes)) != -1){
//                writer.write(bytes, 0, readCount);
//            }
            //输出流最后要刷新
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally{
            //分开try，不要一起try
            //一起try的时候，其中一个出现异常，可能会影响到另一个流的关闭
            if (writer != null){
                try{
                    writer.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if (in != null){
                try{
                    in.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isWrite = false;
    private boolean isWriteFirst = false;
    private MediaDot lastDot = null;
    /*
     * 将原始点数据进行处理（主要是坐标变换）并保存
     */
    private void processEachDot(Dot dot) {

        MediaDot mediaDot =null;
        try {
            mediaDot = new MediaDot(dot);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(dot.type == Dot.DotType.PEN_DOWN){
            lastDot = mediaDot;

            if(pageManager.currentPageID != dot.PageID){//是否更换页码
                Page p = pageManager.getPageByPageID(pageManager.currentPageID);
                if(p != null){
                    int pPN = p.getPageNumber();//上一页号
                    if(pPN != -1){
                        //跨区域自动存储上一局域底图
                        if(lastLocalRect != null){
                            int currentSaveBmpNumber = p.addCurrentSaveBmpNumber();
                            Log.e(TAG,"currentSaveBmpNumber: "+currentSaveBmpNumber);
                            drawImageSurfaceView.saveBmp(pPN+"-"+lastLocalRect.firstLocalCode+"-"+ lastLocalRect.secondLocalCode+"-"+currentSaveBmpNumber, lastLocalRect.rect);
                            handlerToast("底图已存储");
                        }
                    }
                }
                lastLocalRect = null;
                switchPage(dot.PageID);
            }
        }

        if(dot.type == Dot.DotType.PEN_MOVE){
            lastDot = mediaDot;
        }

        if(dot.type == Dot.DotType.PEN_UP){
            isWrite = false;
        }

        float time = XApp.DEFAULT_FLOAT;
        int videoID = XApp.DEFAULT_INT;
        int audioID = XApp.DEFAULT_INT;

        //如果正在录音，再次长压结束录音
        if(audioRecorder == true){
            audioID = audioManager.getCurrentRecordAudioNumber();
        }

        int result = XApp.DEFAULT_INT;//接收识别结果
        try {
            result = XApp.instruction.processEachDot(dot, time, videoID, audioID);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(result == 0){

            if(!isWrite){
                isWrite = true;
                isWriteFirst = true;
            }

            if(isWriteFirst){
                drawImageSurfaceView.drawDots(Instruction.simpleDots);
                isWriteFirst = false;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    drawImageSurfaceView.drawDot(dot);
                }
            }).start();

//            drawImageSurfaceView.drawDot(dot);
        }

    }


    private void processDots(Dot dot) {
        ////Log.i(TAG, "=======222draw dot=======" + dot.toString());
/*
        // 回放模式，不接受点
        if (bIsReply) {
            return;
        }
*/
        processEachDot(dot);
        Log.e(TAG,"processDots");
    }

    /**
     * 切换当前Page
     * @param pageID pageID
     */
    private void switchPage(int pageID){
        pageManager.currentPageID = pageID;
        int resID = pageManager.getResIDByPageID(pageID);
        if(resID != -1){
            drawImageSurfaceView.setImageBitmapByResId(resID);
        }else {
            drawImageSurfaceView.setImageBitmapByResId(-1);
            Toast.makeText(MainActivity.this, "不存在页码对应的图片资源", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"switchPage(): 不存在页码对应的图片资源");
        }

        Page page = pageManager.getPageByPageID(pageID);
        if(page != null){
            //确保在绘制新页码底图之后再绘制，否则上一个invalidate()发送的WM_PAINT消息还在队列里没取出来，bitmap都还是null
            drawImageSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    drawImageSurfaceView.drawDots(page.getPageDotsBuffer());
                    Log.e(TAG,"switchPage(): 新页上的点绘制完毕");
                }
            });
        }else {
            Log.e(TAG,"switchPage(): 尚未存储该页");
        }

    }

    @Override
    public void receiveRecognizeResult(int i, int pageID, int firstX, int firstY) {
        super.receiveRecognizeResult(i, pageID, firstX, firstY);

        Log.e(TAG,"receiveRecognizeResult(): i: "+i+" pageID: "+ pageID + " firstX: "+firstX+" firstY: "+firstY);

        if(i == 0){//普通书写，包含在基础响应中
//            Message message = new Message();
//            message.what = 0;
//            Bundle bundle = new Bundle();
//            bundle.putString("showInftTextView","普通书写");
//            message.setData(bundle);
//            handler.sendMessage(message);

//            Toast.makeText(MainActivity.this, "普通书写", Toast.LENGTH_SHORT).show();
//            java.lang.RuntimeException: Can't toast on a thread that has not called Looper.prepare()
        }else if(i == 1){//单击，包含在基础响应中

        }else if(i == 2){//双击
            handlerToast("双击命令");

            int pN = pageManager.getPageNumberByPageID(pageID);
            //long start = System.currentTimeMillis();
            LocalRect lR = excelReader.getLocalRectByXY(pN, firstX, firstY);
            //Log.e(TAG,"start - end = "+(System.currentTimeMillis()-start)+" ms");
            //耗时一般在十几ms以内
            if(lR != null){
                Log.e(TAG,"局域编码: "+lR.getLocalCode());
            }

            MediaDot mediaDot = pageManager.getDotMedia(pageID,firstX,firstY);
            if(mediaDot != null){
                Log.e(TAG,mediaDot.toString());

                if(mediaDot.penMac.equals(XApp.mBTMac)){
                    if(mediaDot.isVideoDot()) {
                        //跳转至ck
                        Intent ckIntent = new Intent(this, CkplayerActivity.class);
                        ckIntent.putExtra("time", mediaDot.time);
                        ckIntent.putExtra("videoID", mediaDot.videoID);
                        startActivity(ckIntent);
//                }else if(mediaDot.audioID != 0){
                    }else {
                        Log.e(TAG,"receiveRecognizeResult(): 跳转至笔迹复现页面");
                        //跳转至笔迹复现页面
                        Intent rpIntent = new Intent(this,ReplayActivity.class);
//                    rpIntent.putExtra("audioID",mediaDot.audioID);
                        rpIntent.putExtra("pageID",mediaDot.pageID);

                        Log.e(TAG, "lR != null: "+(lR != null));
                        if(lR != null){
                            //学程样例纸张
                            Log.e(TAG, "lR: "+lR.rect.toString());
                            //扩展“余光”
                            rpIntent.putExtra("rectLeft",Math.max(lR.rect.left - ReplayActivity.MARGIN, 0));
                            rpIntent.putExtra("rectTop",Math.max(lR.rect.top - ReplayActivity.MARGIN, 0));
                            rpIntent.putExtra("rectRight",Math.min(lR.rect.right + ReplayActivity.MARGIN, A3_ABSCISSA_RANGE));
                            rpIntent.putExtra("rectBottom",Math.min(lR.rect.bottom + ReplayActivity.MARGIN, A3_ORDINATE_RANGE));
                            rpIntent.putExtra("localCode",lR.getLocalCode());
                            rpIntent.putExtra("localHWsMapID",mediaDot.strokesID);
                            startActivity(rpIntent);
                        }else {
                            //普通点阵纸张
                            rpIntent.putExtra("localCode","");
                            rpIntent.putExtra("localHWsMapID",mediaDot.strokesID);
                            startActivity(rpIntent);
                        }
                    }
                }

                if(mediaDot.y < 20){
                    drawImageSurfaceView.saveBmp(mediaDot.pageID+"-"+pageManager.getPageByPageID(mediaDot.pageID).addCurrentSaveBmpNumber());

                    handlerToast("底图已存储");

                }
            }

            if(lR != null){
                Log.e(TAG, "lR: "+lR.toString());
                if("资源卡".equals(lR.localName)){
                    Log.e(TAG, "双击资源卡");
                    //跳转至ck
                    Intent ckIntent = new Intent(this,CkplayerActivity.class);
                    ckIntent.putExtra("time",0.0f);

//                    Random random = new Random();
//                    int videoID = random.nextInt(5)+1;
                    int videoID = lR.getVideoIDByAddInf();
                    String videoName = lR.getVideoNameByAddInf();

                    Log.e(TAG, "ckplayer跳转至videoID: " + String.valueOf(videoID));
                    Log.e(TAG, "ckplayer跳转至videoName: " + videoName);
                    ckIntent.putExtra("videoID",videoID);
                    ckIntent.putExtra("videoName",videoName);
                    startActivity(ckIntent);
                }
            }

        }else if(i == 3){//长压
            handlerToast("长压命令");

//            //如果正在录音，再次长压结束录音
//            if(audioRecorder == true){
//                audioRecorder = false;
//                audioManager.stopRATimer();
//
//                drawImageSurfaceView.saveBmp(mediaDot.pageID+"-"+pageManager.getPageByPageID(mediaDot.pageID).addCurrentSaveBmpNumber());
//
//                message = new Message();
//                message.what = 0;
//                bundle = new Bundle();
//                bundle.putString("showInftTextView","底图已存储");
//                message.setData(bundle);
//                handler.sendMessage(message);
//                Log.e(TAG,"receiveRecognizeResult(): 关闭录音");
//                return;
//            }

            //开启录音
//            audioManager.startRATimer();
//            audioRecorder = true;
//            Log.e(TAG,"receiveRecognizeResult(): 开启录音");

        }else if(i == 4){
            //指令控制符
            Log.e(TAG, "receiveRecognizeResult: 指令控制符命令 ");
            audioManager.startRATimer();
            audioRecorder = true;
            Log.e(TAG,"receiveRecognizeResult(): 开启录音");
        }else if(i == 5){
            //对钩
            Log.e(TAG, "receiveRecognizeResult: 对钩命令 ");
        }
    }

    @Override
    public void writeTimerFinished(int pageID, int x, int y) {
        super.writeTimerFinished(pageID, x, y);

        handlerToast("普通书写存储完毕");

        Log.e(TAG,"writeTimerFinished(): audioRecorder: "+audioRecorder);

        int pN = pageManager.getPageNumberByPageID(pageID);
        LocalRect lR = excelReader.getLocalRectByXY(pN, x, y);

        if(lR != null){
            Log.e(TAG,"lR: "+lR);
        }

        //如果正在录音，普通书写延迟响应结束录音
        if(audioRecorder == true){
            audioRecorder = false;
            audioManager.stopRATimer();

            Page p = pageManager.getPageByPageID(pageID);
            p.addAudio(lR, audioManager.currentRecordAudioName);
            if(lR != null){
                Log.e(TAG,"writeTimerFinished(): lR != null: "+(lR != null));
//                while (Page.lockSaveBmpNumber == false){//确保录音结束相关逻辑处理完毕，拿到的SaveBmpNumber是最新的
//                    Log.e(TAG,"Page.lockSaveBmpNumber = false");
//                    try {
//                        Thread.sleep(50);//避免这里死循环，而解锁的子线程
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                int currentSaveBmpNumber = pageManager.getPageByPageID(pageManager.currentPageID).addCurrentSaveBmpNumber();
                Log.e(TAG,"currentSaveBmpNumber: "+currentSaveBmpNumber);
                drawImageSurfaceView.saveBmp(pN+"-"+lR.firstLocalCode+"-"+ lR.secondLocalCode+"-"+currentSaveBmpNumber, lR.rect);
                handlerToast("底图已存储");
//                Page.lockSaveBmpNumber = false;
            }

        }

        //同页跨区域自动存储上一局域底图
        if(lR != null){
            if(lastLocalRect != null){
                if(lR.firstLocalCode != lastLocalRect.firstLocalCode || lR.secondLocalCode != lastLocalRect.secondLocalCode){

                    int currentSaveBmpNumber = pageManager.getPageByPageID(pageManager.currentPageID).addCurrentSaveBmpNumber();
                    Log.e(TAG,"currentSaveBmpNumber: "+currentSaveBmpNumber);
                    drawImageSurfaceView.saveBmp(pN+"-"+lastLocalRect.firstLocalCode+"-"+ lastLocalRect.secondLocalCode+"-"+currentSaveBmpNumber, lastLocalRect.rect);
                    handlerToast("底图已存储");
                    lastLocalRect = lR;
                }
            }else {
                lastLocalRect = lR;
            }

        }
    }

    /*
     * onActivityResult用来接收Intent的数据
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    try {
                        boolean flag = mService.connect(deviceAddress);
                        if(flag){
                            final Intent intent = new Intent(BluetoothLEService.ACTION_GATT_CONNECTED);
                            sendBroadcast(intent);
//                            getSupportActionBar().setTitle("@string/app_name"+"（蓝牙已连接）");
                        }
                        penAddress = deviceAddress;
                        // TODO spp
                        //bleManager.setSppConnect(deviceAddress);
                    } catch (Exception e) {
                        //Log.i(TAG, "connect-----" + e.toString());
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case GET_FILEPATH_SUCCESS_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    String path = "";
                    Uri uri = data.getData();

                    final String str = path;
                    //Log.i(TAG, "onActivityResult: path="+str);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            bleManager.readTestData(str);
                        }
                    }).start();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

}