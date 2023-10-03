package com.example.xmatenotes.ui;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.manager.CoordinateConverter;
import com.example.xmatenotes.logic.manager.LocalA0Rect;
import com.example.xmatenotes.logic.manager.Writer;
import com.example.xmatenotes.logic.model.CardData;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xmatenotes.BluetoothLEService;
import com.example.xmatenotes.logic.manager.OldPageManager;
import com.example.xmatenotes.logic.model.DataList;
import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.R;
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;
import com.example.xmatenotes.logic.model.instruction.Command;
import com.example.xmatenotes.logic.model.instruction.Responser;
import com.example.xmatenotes.logic.network.BitableManager;
import com.example.xmatenotes.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tqltech.tqlpencomm.bean.Dot;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CardshowActivity extends BaseActivity{

    private static final String TAG = "CardshowActivity";

    private static final String PREFS_NAME = "MyPrefs"; //测试

    private static final String KEY_DATA_LIST = "DataList";//测试
    private RecyclerView recyclerView;
    private CardAdapter cardAdapter;

    private BluetoothLEService mService = null;              //蓝牙服务

    private OldPageManager oldPageManager = null;

    private MediaDot lastDot = null;

    private String filePath = "data.txt";

    BitableManager bitableManager = BitableManager.getInstance();

    private Writer writer = null;

    //坐标转换器
    private CoordinateConverter coordinateConverter;

    List<CardData> myStringList = new ArrayList<>();  //先定义一个空的

    List<CardData> myStringList1 = new ArrayList<>();  //先定义一个空的
    LocalA0Rect localA0Rect = new LocalA0Rect();

    BluetoothLEService.OnDataReceiveListener dotsListener = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardshow);

        //配置坐标转换器,maxX，maxY,maxrealX,maxrealY
        this.coordinateConverter = new CoordinateConverter(512, 512, 7803,7803);

        Intent gattServiceIntent = new Intent(this, BluetoothLEService.class);
        boolean bBind = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        List<CardData> d = loadDataList(getApplicationContext());

        if(d != null){
            Date currentTime = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeString = format.format(currentTime);

            myStringList = d;

        }else{
            myStringList = myStringList;
        }

        cardAdapter = new CardAdapter(myStringList);

        recyclerView.setAdapter(cardAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.writer = Writer.getInstance().init().bindPage(null).setResponser(new Responser() {
            @Override
            public boolean onLongPress(Command command) {
                if(!super.onLongPress(command)){
                    return false;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(XmateNotesApplication.context, "长压命令", Toast.LENGTH_SHORT).show();
                    }
                });

                return false;
            }

            @Override
            public boolean onCalligraphy(Command command) {
                if (command != null) {
                    if(command.getHandWriting().isClosed()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(XmateNotesApplication.context, "普通书写", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //普通书写基本延时响应
                        writer.handWritingWorker = writer.addResponseWorker(
                                HandWriting.DELAY_PERIOD + 1000, new Writer.ResponseTask() {
                                    @Override
                                    public void execute() {
                                        LogUtil.e(TAG, "普通书写延迟响应开始");
                                        writer.closeHandWriting();
                                    }
                                }
                        );

                        writer.singleHandWritingWorker = writer.addResponseWorker(
                                SingleHandWriting.SINGLE_HANDWRITING_DELAY_PERIOD, new Writer.ResponseTask() {
                                    @Override
                                    public void execute() {
                                        LogUtil.e(TAG, "单次笔迹延迟响应开始");
                                        writer.closeSingleHandWriting();
                                    }
                                }
                        );
                    }
                }

                return super.onCalligraphy(command);
            }

            @Override
            public boolean onDui(Command command) {
                if(!super.onDui(command)){
                    return false;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(XmateNotesApplication.context, "对勾命令", Toast.LENGTH_SHORT).show();
                    }
                });

                return false;
            }

            @Override
            public boolean onBanDui(Command command) {
                if(!super.onBanDui(command)){
                    return false;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(XmateNotesApplication.context, "半对命令", Toast.LENGTH_SHORT).show();
                    }
                });

                return false;
            }

            @Override
            public boolean onBanBanDui(Command command) {
                if(!super.onBanBanDui(command)){
                    return false;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(XmateNotesApplication.context, "半半对命令", Toast.LENGTH_SHORT).show();
                    }
                });

                return false;
            }

            @Override
            public boolean onBanBanBanDui(Command command) {
                if(!super.onBanBanBanDui(command)){
                    return false;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(XmateNotesApplication.context, "半半半对命令", Toast.LENGTH_SHORT).show();
                    }
                });

                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    public void processEachDot(MediaDot mediaDot) {

        LogUtil.e(TAG, "封装MediaDot: "+mediaDot);
        if(mediaDot.type == Dot.DotType.PEN_DOWN){
            lastDot = mediaDot;
        }

        if(mediaDot.type == Dot.DotType.PEN_MOVE){
            lastDot = mediaDot;
        }

        if(mediaDot.type == Dot.DotType.PEN_UP){

        }

        if(this.writer != null){
            this.writer.processEachDot(mediaDot);
        }

    }

    private void processDots(Dot dot) {
        Log.e(TAG,"processDots");
        processEachDot((MediaDot) this.coordinateConverter.convertIn(createMediaDot(dot)));

    }

    /**
     * 通过Dot构造MediaDot
     * time:实时视频进度。如果当前没有正在播放视频，则传入默认值{@link XmateNotesApplication#DEFAULT_FLOAT}
     * videoID:当前视频ID。如果当前没有正在播放视频，则传入默认值{@link XmateNotesApplication#DEFAULT_INT}
     * audioID:当前正在录制的音频ID。如果当前没有正在录制音频，则传入默认值{@link XmateNotesApplication#DEFAULT_INT}
     * @param dot
     * @return
     */
    private MediaDot createMediaDot(Dot dot){
        MediaDot mediaDot = new MediaDot(dot);
        mediaDot.timelong = System.currentTimeMillis();//原始timelong太早，容易早于录音开始，也可能是原始timelong不准的缘故
        mediaDot.penMac = XmateNotesApplication.mBTMac;

        return mediaDot;
    }

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
                }
            };
            mService.setOnDataReceiveListener(dotsListener);
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;Log.d(TAG, "onServiceDisconnected");
        }
    };

//    @Override
//    public void receiveRecognizeResult(Gesture ges, int pageID, int firstX, int firstY) {
//        super.receiveRecognizeResult(ges, pageID, firstX, firstY);
//        if(ges.getInsId() == 3) {//长压
//            handlerToast("长压命令");//短暂显示
//
//            XApp.excelA0Reader.getPage();
//
//            XApp.excelA0Reader.getLocalRectByXY(pageID, firstX, firstY);  //这里是根据写入符号的位置来判断是具体的什么含义
//
//
//            String role = xApp.getRole();
//            if (role == "助教") {
//                if (localA0Rect.wOrR == "dazu") {  //助教读取组群
//
//                    bitableManager.getRecordGroups(localA0Rect.groupMessage, new BitableManager.callBack2() {
//
//                        @Override
//                        public void Do() {
//                            makeCard("助教 长压 组群",bitableManager.MessageFromTA);
//                        }
//                    }); //读取助教组
//
//                } else if (localA0Rect.wOrR == "xiaozu") { //助教读取小组
//                    bitableManager.getRecordGroups1(localA0Rect.groupMessage, new BitableManager.callBack2() {
//                        @Override
//                        public void Do() {
//                            makeCard("助教 长压 小组"+localA0Rect.groupMessage[0],bitableManager.MessageFromTA);
//                        }
//                    }); //读取小组
//                } else if (localA0Rect.wOrR == "xingming") {  //助教读取姓名
//                    bitableManager.getRecordName(localA0Rect.nameMessage, new BitableManager.callBack2() {
//                        @Override
//                        public void Do() {
//                            makeCard("助教长压学生"+localA0Rect.nameMessage[0],bitableManager.MessageFromTA);
//                        }
//                    });
//                } else if (localA0Rect.wOrR == "zhou") {  //助教读取周
//                    bitableManager.getRecordWeek1(localA0Rect.timeMessage, new BitableManager.callBack2() {
//                        @Override
//                        public void Do() {
//                            makeCard("助教获得周信息"+localA0Rect.timeMessage[0],bitableManager.MessageFromTA);
//                        }
//                    });
//
//                }
//            } else { //不是助教就是组长
//                String zu = xApp.getlocalGroup();//根据笔ID判断是否哪一组的笔
//                if (zu == localA0Rect.Localgroup) { //现在用的笔所绑定的组与坐标位置所标识的组是同一组
//                    handlerToast("小组匹配成功");
//                    if (localA0Rect.wOrR == "dazu") {  //组长读取组群
//
//                        handlerToast("没有此操作的相关权限");
//
//                    } else if (localA0Rect.wOrR == "xiaozu") { //组长读取小组
//                        bitableManager.getRecordGroups2(localA0Rect.groupMessage, new BitableManager.callBack2() {
//                            @Override
//                            public void Do() {
//                                makeCard("组长长压小组"+localA0Rect.groupMessage[0],bitableManager.MessageFromTA);
//                            }
//                        }); //读取小组
//                    } else if (localA0Rect.wOrR == "xingming") {  //组长读取姓名
//                        bitableManager.getRecordName(localA0Rect.nameMessage, new BitableManager.callBack2() {
//                            @Override
//                            public void Do() {
//                                makeCard("组长长压学生"+localA0Rect.nameMessage[0],bitableManager.MessageFromTA);
//                            }
//                        });
//                    } else if (localA0Rect.wOrR == "zhou") {  //助教读取周
//                        bitableManager.getRecordWeekG(localA0Rect.timeMessage, new BitableManager.callBack2() {
//                            @Override
//                            public void Do() {
//                                makeCard("组长获得周信息"+localA0Rect.timeMessage[0],bitableManager.MessageFromTA);
//                            }
//                        });
//
//                    }
//
//
//
//                } else {  //跨组操作
//                    handlerToast("没有操作该组的权限相关权限");//提示没有权限
//                }
//            }
//        }
//    }

    @Override
    public void writeTimerFinished(long pageID, int x, int y) {
        super.writeTimerFinished(pageID, x, y);
    }

    public Handler handler = new Handler(){
        @SuppressLint("NewApi") @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    String toast = msg.getData().getString("Toast");
                    Toast.makeText(CardshowActivity.this, toast, Toast.LENGTH_SHORT).show();
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

    public void makeCard(String A, String B){
        Log.e(TAG,"进入了制作新卡片的函数");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CardData newCardData = new CardData(A, B);

                myStringList.add(newCardData);

                DataList dataList = new DataList(myStringList);

                saveDataList(getApplicationContext(), dataList.getCardDataList());

                cardAdapter.notifyDataSetChanged();
            }
        });

    }

    public static void saveDataList(Context context, List<CardData> dataList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(dataList);
        editor.putString(KEY_DATA_LIST, json);
        editor.apply();
    }

    public static List<CardData> loadDataList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_DATA_LIST, "");
        Type type = new TypeToken<List<CardData>>() {}.getType();
        return gson.fromJson(json, type);
    }

}

