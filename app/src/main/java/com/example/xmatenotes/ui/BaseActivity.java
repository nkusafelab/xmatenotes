package com.example.xmatenotes.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xmatenotes.BluetoothLEService;
import com.example.xmatenotes.R;
import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.model.handwriting.Gesture;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.util.ActivityCollector;
import com.example.xmatenotes.util.LogUtil;

/**
 * 活动基类
 * 所有活动需继承该类
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    //当前活动
    public static BaseActivity baseActivity = null;

    private static BLEStatusReceiver bleStatusReceiver = null;

    private boolean isSupportActionBarDisplayConnected = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, getClass().getSimpleName());
        ActivityCollector.addActivity(this);

        LogUtil.w(TAG,"BaseActivity-"+getClass().getSimpleName()+": onCreate()");
        baseActivity = this;
        LogUtil.e(TAG,"当前活动： "+baseActivity+" onCreate()");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        LogUtil.w(TAG,"BaseActivity-"+getClass().getSimpleName()+": onDestroy()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.w(TAG,"BaseActivity-"+getClass().getSimpleName()+": onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();

        LogUtil.w(TAG,"BaseActivity-"+getClass().getSimpleName()+": onStop()");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //注册接收蓝牙状态变化的广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        bleStatusReceiver = new BLEStatusReceiver();
        registerReceiver(bleStatusReceiver,intentFilter);

        baseActivity = this;
        LogUtil.e(TAG,"当前活动： "+baseActivity+" onResume()");

        LogUtil.w(TAG,"BaseActivity-"+getClass().getSimpleName()+": onResume()");

        //每个活动标题栏都能看到蓝牙实时连接状态
        if(BluetoothLEService.isPenConnected && isSupportActionBarDisplayConnected){
            final Intent intent = new Intent(BluetoothLEService.ACTION_GATT_CONNECTED);
            sendBroadcast(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //注销接收蓝牙状态变化的广播
        unregisterReceiver(bleStatusReceiver);
        LogUtil.w(TAG,"BaseActivity-"+getClass().getSimpleName()+": onPause()");

        LogUtil.e(TAG,"当前活动： "+baseActivity+" onPause()");
    }

    public boolean isSupportActionBarDisplayConnected() {
        return isSupportActionBarDisplayConnected;
    }

    public void setSupportActionBarDisplayConnected(boolean supportActionBarDisplayConnected) {
        isSupportActionBarDisplayConnected = supportActionBarDisplayConnected;
    }

    /**
     * 获取识别结果的回调方法
     * @param i 命令ID
     * @param pageID 命令第一个点的所在的pageID
     * @param x 命令第一个点的横坐标
     * @param y 命令第一个点的纵坐标
     */
    public void receiveRecognizeResult(Gesture ges, long pageID, int x, int y){
        Log.e(TAG,"BaseActivity: receiveRecognizeResult()");
    }

    //普通书写延迟响应回调方法
    public void writeTimerFinished(long pageID, int x, int y){

    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @SuppressLint("NewApi") @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    String toast = msg.getData().getString("Toast");
                    Toast.makeText(XmateNotesApplication.context, toast, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    protected void showToast(String toast){
        Message message = new Message();
        message.what = 0;
        Bundle bundle = new Bundle();
        bundle.putString("Toast",toast);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    /**
     * “蓝牙连接状态”广播接收器
     */
    class BLEStatusReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //将“蓝牙连接状态”广播至全局
            if(action == BluetoothLEService.ACTION_GATT_CONNECTED){
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name)+"（蓝牙已连接）");
            }else if(action == BluetoothLEService.ACTION_GATT_DISCONNECTED){
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name)+"（蓝牙未连接）");
            }
        }
    }

}
