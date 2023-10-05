package com.example.xmatenotes.ui.ckplayer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.BluetoothLEService;
import com.example.xmatenotes.DotInfoActivity;
import com.example.xmatenotes.R;
import com.example.xmatenotes.logic.manager.ExcelReader;
import com.example.xmatenotes.logic.manager.LocalRect;
import com.example.xmatenotes.logic.manager.OldPageManager;
import com.example.xmatenotes.logic.manager.PageManager;
import com.example.xmatenotes.logic.manager.PenMacManager;
import com.example.xmatenotes.logic.manager.VideoManager;
import com.example.xmatenotes.logic.manager.Writer;
import com.example.xmatenotes.logic.model.Page.Page;
import com.example.xmatenotes.logic.model.handwriting.Gesture;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.ui.BaseActivity;
import com.google.common.collect.ArrayListMultimap;
import com.tqltech.tqlpencomm.PenCommAgent;
import com.tqltech.tqlpencomm.bean.Dot;

import java.text.ParseException;

//封装笔迹点对应的视频ID和视频进度
class TimeOrder{
	public float time;
    public int videoID;
    
    public TimeOrder(float time, int videoID){
    	this.time = time;
    	this.videoID = videoID;
    }
}

class Point{
	public float x;
    public float y;
    
    public Point(int x, int fx, int y, int fy){
    	this.x = ifMap(x,fx);
    	this.y = ifMap(y,fy);
    }
    
    private float ifMap(int X,int Y){

    	float temp = Y;
    	temp /= 1000.0;
    	return (X + temp);
    	
    }
}

/**
 * 视频笔记活动
 */
public class CkplayerActivity extends BaseActivity {
	
	private final static String TAG = "CkplayerActivity";

    private final static long SEEK_TIME_DELAY_PERIOD = 120000;//定义视频碎片复现时间长度为2min
	
	private WebView webView;
	
	private PenCommAgent bleManager;
	
	private TextView ckTextView;
	
	private float time;//记录当前视频进度
	private float duration;//记录当前视频总时长
	private int currentID;//记录当前视频ID
	
    private int pointX;
    private int pointY;
    private int pointZ;
    private int click_number = 0;//存储当前点击是双击中的第几次
    private long click_first_time;//双击中第一次点击的时间戳
    
    private float last_point = 0.0f;//上一个笔迹点的二维坐标的一维实数映射
    private Dot lastDot = null;//上一个Dot
    private float click_first_key;//双击第一个点坐标
    private int click_first_keyX;
    private int click_first_keyY;
//    private float click_first_bkey;//双击第一个点的前一个点坐标
    private long last_timelong = 0;//上一个笔迹点的时间戳
    //private boolean isSame = false;
    private ArrayListMultimap<Float, TimeOrder> dot_number = ArrayListMultimap.create();//存储所有笔迹点从笔迹点坐标到相应视频参数的映射关系
	private BluetoothLEService ckService = null;
	
//	private Timer timer = null;
	
	private int playOrFalse = 0;//记录视频播放状态
//	private int bplayOrFalse = 0;
//	private boolean noteDelayTimer = false;
//	private boolean fragDelayTimer = false;
//	private long pStart = 0;
	private boolean isClear = false;

	private PenMacManager penMacManager = null;//管理所有mac地址的对象
	private ExcelReader excelReader = null;//操作excel的对象
	private VideoManager videoManager = null;
	private OldPageManager oldPageManager = null;

	private PageManager pageManager = PageManager.getInstance();
	private Writer writer = Writer.getInstance();
	private Page page;

	/**
	 * 当前MediaDot
	 */
	private MediaDot curMediaDot = null;

	/**
	 * 上一个MediaDot
	 */
	private MediaDot lastMediaDot = null;
	
	/*
	 * 根据传入的原始点的部分属性重新打包成Dots，并存放在bookID对应的dot_number dot_number类型为ArrayListMultimap<Integer, Dots>   
	 */
    private void saveData(float pointX, float pointY) {
        //Log.i(TAG, "======savaData pageID======" + pageID + "========sdfsdf" + angle);
        

    }
    
    //完成从二维坐标到一维实数的映射；eg:(123,456)->123.456
    private float xyMap(int X,int Y){

    	float temp = Y;
    	temp /= 100.0;
    	return (X + temp);
    	
    }
    
    //通过Java调用JavaScript代码，参数为字符串形式的待调用JavaScript函数
    @SuppressLint("NewApi") 
    private void callJavaScript(String str){
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			webView.evaluateJavascript( "javascript:" + str , new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //此处编写收到JavaScript函数返回值后待执行的逻辑
                }
            });
        }else{
        	webView.loadUrl("javascript:" + str);
        }
    	Log.e(TAG,"javascript:" + str);
    }

    
//    private void seekTime(Dot dot){
//    	float key = xyMap(dot.x,dot.y);
//    	if( dot_number.containsKey(key) ){
//    		if(last_point != key && dot.type == Dot.DotType.PEN_DOWN){//如果当前笔迹点已经存储过，且与上一个笔迹点坐标不同（两个时间上相邻的笔迹点坐标可能相同），则进行对应视频碎片复现
//    			
//    			Log.e(TAG,"last_point: " + String.valueOf(last_point));
//    			Log.e(TAG,"key: " + String.valueOf(key));
//    			
//    			last_point = key;
//        		for(Point s :dot_number.get(key)){
//        			int seekTime = Math.round(s.time);//四舍五入
//        			Log.e(TAG,"seekTime: (" + seekTime + "," + s.videoID +")");
//        			callJavaScript("seekTime("+ seekTime + "," + s.videoID +")");
//        			break;
//        		}
//    		}      		
//    	}
//    }
    
    private double distance(float x1, float y1, float x2, float y2){
    	return Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));
    }
    
//    private boolean double_click = false;
//    private boolean one_click = false;
//    private boolean same_point = true;
//    private boolean isContainsKey = false;
//    private boolean isDoubleClick(Dot dot){
//    	if (dot.type == Dot.DotType.PEN_DOWN) {
//        	  click_number++;
//        	  
//        	  if ( click_number == 1 ){
//        		  bplayOrFalse = playOrFalse;
//        		click_first_time = dot.timelong;
//        		click_first_keyX = dot.x;
//        		click_first_keyY = dot.y;
//        		one_click = true;
////        		click_first_key = xyMap(dot.x,dot.y);
////        		isContainsKey = dot_number.containsKey(click_first_key);
////        		Log.e(TAG,"timeS: " + timeS[click_first_keyX-1][click_first_keyY-1]);
////        		Log.e(TAG,"videoIDS: " + videoIDS[click_first_keyX-1][click_first_keyY-1]);
////        		Log.e(TAG,"XY[dot.x][dot.y]1 = " + XY[dot.x][dot.y]);
//        		if(timeS[click_first_keyX-1][click_first_keyY-1] != -1 && videoIDS[click_first_keyX-1][click_first_keyY-1] != -1){
//        			isContainsKey = true;
//        		}else{
//        			isContainsKey = false;
//        		}
//        	  }
//        	  
//        	  if ( click_number == 2 ){
//        		  if((dot.timelong - click_first_time) < DOUBLE_CLICK_PERIOD){//判断是否是双击
////        			  
////        			  switch(execode){
////        			  case 0:
////        				  virtualKey(dot);
////        				  break;
////        			  case 1:
////        				  seekTime(dot);
////        				  break;
////        			  default:
////        			  }
////        			  
//        			  
//        			  one_click = false;
//        			  
//            		  click_first_time = 0;
//            		  click_number = 0;
//            		  double_click = true;
//            		  
//                  }else{//若第二次点击距前一次点击时间间隔超过阈值，则第二次点击作为新的第一次点击
////                	  click_first_key = xyMap(dot.x,dot.y);
////                	  isContainsKey = dot_number.containsKey(click_first_key);
//                	  click_first_keyX = dot.x;
//              		  click_first_keyY = dot.y;
////              		Log.e(TAG,"timeS: " + timeS[click_first_keyX-1][click_first_keyY-1]);
////            		Log.e(TAG,"videoIDS: " + videoIDS[click_first_keyX-1][click_first_keyY-1]);
//              		
//              		one_click = true;
//              		if(timeS[click_first_keyX-1][click_first_keyY-1] != -1 && videoIDS[click_first_keyX-1][click_first_keyY-1] != -1){
//            			isContainsKey = true;
//            		}else{
//            			isContainsKey = false;
//            		}
//                	  bplayOrFalse = playOrFalse;
//                	  click_number = 1;
//                	  click_first_time = dot.timelong;
//                  }
//        	  } 
//        	  
//        }
//    	
//    	if(dot.type == Dot.DotType.PEN_MOVE){
//    		Point xy = new Point(dot.x, dot.fx, dot.y, dot.fy);
//    		double dis = distance(xy.x, xy.y, last_Point.x, last_Point.y);
//    		if(dis > 0.2){
//    			same_point = false;
//    		}
//    	}
//    	
//    	if(dot.type == Dot.DotType.PEN_UP){
//    		if(double_click == true && same_point == true){
//    			double_click = false;
//    			Log.e("click", "双击");
//    			Log.e(TAG, "isDoubleClick: true");
//    			return true;
//    		}
//    		if(one_click == true && same_point == true){
//    			one_click = false;
//    			Log.e("click", "单击");
//    			return false;
//    		}
//    		same_point = true;
//    	}
//    	return false;
//    }
	
// // 停止定时器
//    private void stopTimer(){
//        if(timer != null){
//            timer.cancel();
//            // 一定设置为null，否则定时器不会被回收
//            timer = null;
//        }
//    }
    
		@SuppressLint("HandlerLeak")
	  public Handler handler = new Handler(){
	      @SuppressLint({"NewApi", "SetTextI18n"})
		  @Override
	      public void handleMessage(Message msg) {
	          switch (msg.what){
	              case 0:
					  String call = msg.getData().getString("call");
	            	  callJavaScript(call);
	                  break;
				  case 1:
					  String toast = msg.getData().getString("Toast");
					  Toast.makeText(CkplayerActivity.this, toast, Toast.LENGTH_SHORT).show();//不知道为什么这一步会导致ckTextView显示内容为空，所以再调用一次setCkTextView(null);
//					  setCkTextView(null);
				  case 2:
					  String text = msg.getData().getString("Text");
					  ckTextView.setText(text);
	              default:
	                  break;
	          }
	      }
	  };

	//通过handler发送消息的方式调用JavaScript方法
	public void callJS(String call){
		Message message = new Message();
		message.what = 0;
		Bundle bundle = new Bundle();
		bundle.putString("call",call);
		message.setData(bundle);
		handler.sendMessage(message);
	}

	private void handlerToast(String toast){
		Message message = new Message();
		message.what = 1;
		Bundle bundle = new Bundle();
		bundle.putString("Toast",toast);
		message.setData(bundle);
		handler.sendMessage(message);
	}

	private void setCkTextView(String text){
		if(text == null){
			VideoManager.Video v1 = videoManager.getVideoByID(currentID);
			setCkTextView("[视频编号： "+v1.getVideoID()+" ][视频名称： "+v1.getVideoName()+" ][笔记人数："+v1.getMatesNumber()+" ][笔记页数： "+v1.getPageNumber()+" ]");
			return;
		}
		Message message = new Message();
		message.what = 2;
		Bundle bundle = new Bundle();

		bundle.putString("Text",text);
		message.setData(bundle);
		handler.sendMessage(message);
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
		MediaDot mediaDot = null;
		mediaDot = new MediaDot(dot);
		mediaDot.timelong = System.currentTimeMillis();//原始timelong太早，容易早于录音开始，也可能是原始timelong不准的缘故
		mediaDot.audioID = XmateNotesApplication.DEFAULT_INT;

		float timeR = time;
		try {
			//修正视频进度;
			timeR = MediaDot.reviseTimeS(dot.timelong, time);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		mediaDot.videoTime = timeR;
		mediaDot.videoID = currentID;
		mediaDot.color = MediaDot.DEEP_ORANGE;

		mediaDot.penMac = XmateNotesApplication.mBTMac;

		return mediaDot;
	}
	
	private boolean reTurn=true;//控制方法执行退出
	/*
	 * 将原始点数据进行处理（主要是坐标变换）并保存
	 */
	private void ProcessEachDot(Dot dot) {

		lastMediaDot = curMediaDot;
		curMediaDot = createMediaDot(dot);

		this.writer.processEachDot();

		//如果正在书写区答题，退出视频笔记
//		if(dot.type == Dot.DotType.PEN_DOWN){
//			int pN = pageManager.getPageNumberByPageID(dot.PageID);
//			LocalRect lR = excelReader.getLocalRectByXY(pN, dot.x, dot.y);
//			if("书写区".equals(lR.localName)){
//				finish();
//			}
//		}

//		int result = XmateNotesApplication.instruction.processEachDot(curMediaDot);
//		if(result == 0){//普通书写
//			if(skRunnable != null && skRunnable.isAlive()){
//				skRunnable.stop();
//				Log.e(TAG,"视频碎片计时器关闭");
//			}
//			callJavaScript("pause()");
//
//		}else if(result == 1){
//
//		}else if(result == 2){
//
//		}else if(result == 3){
//			if(skRunnable != null && skRunnable.isAlive()){
//				skRunnable.stop();
//				Log.e(TAG,"视频碎片计时器关闭");
//			}
//		}else if(result == -1){
//
//		}
	}

    //接收到数据点后执行的主要逻辑
	private void processDots(Dot dot) {
		ProcessEachDot(dot);
		Log.e(TAG,"processDots");
	}

	@Override
	public void receiveRecognizeResult(Gesture ges, long pageID, int firstX, int firstY) {
		super.receiveRecognizeResult(ges, pageID, firstX, firstY);

		Log.e(TAG,"receiveRecognizeResult(): "+"InsId: "+ges.getInsId()+" pageID: "+pageID+" firstX: "+firstX+" firstY: "+firstY);

		if(ges.getInsId() != 1){//除了单击，都可以关闭视频碎片计时器
			if(skRunnable != null && skRunnable.isAlive()){
				skRunnable.stop();
			}
			Log.e(TAG,"视频碎片计时器关闭");
		}

		if(ges.getInsId() == 0){//普通书写，包含在基础响应中
			Log.e(TAG,"普通书写响应开始");
			byte penID = penMacManager.getPenIDByMac(XmateNotesApplication.mBTMac);
			videoManager.getVideoByID(currentID).addMate(penID);
			videoManager.getVideoByID(currentID).addPage(pageID);
			VideoManager.Video v1 = videoManager.getVideoByID(currentID);
			setCkTextView("[视频编号： "+(v1.getVideoID())+" ][视频名称： "+v1.getVideoName()+" ][笔记人数："+v1.getMatesNumber()+" ][笔记页数： "+v1.getPageNumber()+" ]");
//			ckTextView.setText("[视频编号： "+(v1.getVideoID())+" ][视频名称： "+v1.getVideoName()+" ][笔记人数："+v1.getMatesNumber()+" ][笔记页数： "+v1.getPageNumber()+" ]");

			//如果正在书写区答题，退出视频笔记
			int pN = oldPageManager.getPageNumberByPageID(pageID);
			LocalRect lR = excelReader.getLocalRectByXY(pN, firstX, firstY);
//			if("书写区".equals(lR.localName)){
//				finish();
//			}

		}else if(ges.getInsId() == 1){//单击，包含在基础响应中

		}else if(ges.getInsId() == 2) {//双击
			MediaDot mediaDot = oldPageManager.getDotMedia(pageID,firstX,firstY);
			if(mediaDot != null){
				if(mediaDot.isVideoDot() && mediaDot.penMac.equals(XmateNotesApplication.mBTMac)){//跳转
					seekTime(mediaDot.videoTime, mediaDot.videoID);
				}
			}else {
				int pN = oldPageManager.getPageNumberByPageID(pageID);
				LocalRect lR = excelReader.getLocalRectByXY(pN, firstX, firstY);

				if (lR != null) {
					Log.e(TAG, "局域编码: " + lR.getLocalCode());
					if ("资源卡".equals(lR.localName)) {
						Log.e(TAG, "双击资源卡");

//						//跳转至ck
//						Intent ckIntent = new Intent(this, CkplayerActivity.class);
//						ckIntent.putExtra("time", 5.0f);

						int videoID = lR.getVideoIDByAddInf();
						String videoName = lR.getVideoNameByAddInf();
						videoManager.addVideo(videoID, videoName);

						seekTime(5.0f, videoID);

						Log.e(TAG, "ckplayer跳转至videoID: " + String.valueOf(videoID));
						Log.e(TAG, "ckplayer跳转至videoName: " + videoName);

					}
				} else {
					//播放/暂停
//					switch (playOrFalse) {
//						case 0:
//							callJS("play()");
//							Log.e(TAG, "response：play()");
//							handlerToast("双击命令：播放视频");
//							break;
//						case 1:
//							callJS("pause()");
//							Log.e(TAG, "response：pause()");
//							handlerToast("双击命令：暂停视频");
//							break;
//						default:
//					}
				}
			}
		}else if(ges.getInsId() == 3) {//长压
//			handlerToast("长压命令");//会直接导致活动界面崩溃自动退出
			Log.e(TAG, "receiveRecognizeResult: "+"长压命令");

		}else if(ges.getInsId() == 4){
			//指令控制符
//			handlerToast("指令控制符命令");
			Log.e(TAG, "receiveRecognizeResult: "+"指令控制符命令");
		}else if(ges.getInsId() == 5){
			//对钩
//			handlerToast("对钩命令");
			Log.e(TAG, "receiveRecognizeResult: "+"对钩命令");
		}
	}

	@Override
	public void writeTimerFinished(long pageID, int x, int y) {
		super.writeTimerFinished(pageID, x, y);

		if(playOrFalse == 0) {
			callJS("play()");
		}
		handlerToast("普通书写响应完毕");

	}

	private static Thread seekTimer = null;
	private static SeekRunnable skRunnable = null;

	private void seekTime(float time, int videoID){
		int seekTime = Math.round(time);//四舍五入
		Log.e(TAG,"seekTime: (" + seekTime + "," + videoID +")");


		VideoManager.Video v1 = videoManager.getVideoByID(videoID);
		Log.e(TAG, "seekTime: v1: "+v1);
		setCkTextView("[视频编号： "+v1.getVideoID()+" ][视频名称： "+v1.getVideoName()+" ][笔记人数："+v1.getMatesNumber()+" ][笔记页数： "+v1.getPageNumber()+" ]");

		callJS("seekTime("+ seekTime + "," + (videoID-1) +")");
		handlerToast("双击命令：视频碎片复现跳转至第"+videoID+"个视频的第"+seekTime+"秒");

		if(skRunnable != null && skRunnable.isAlive()){
			skRunnable.stop();
		}

		//启动视频碎片计时器
		skRunnable = new SeekRunnable();
		seekTimer = new Thread(skRunnable);
		seekTimer.start();
		Log.e(TAG,"视频碎片计时器打开");
	}

	/**
	 * 视频碎片计时器
	 */
	class SeekRunnable implements Runnable {

		//控制线程是否终止运行
		private boolean isStart = true;

		//线程起始运行时间
		private long seekStartTime = 0;

		//当前时间
		private long curTime = 0;

		@Override
		public void run() {
			seekStartTime = System.currentTimeMillis();
			curTime=System.currentTimeMillis();

			while(isStart == true && curTime-seekStartTime<SEEK_TIME_DELAY_PERIOD) {
				curTime=System.currentTimeMillis();
			}

			if(isStart == true) {
				isStart = false;
				Log.e(TAG,"视频碎片计时器关闭");

				if(playOrFalse == 1) {
					callJS("pause()");
					Log.e(TAG,"response：pause()");
				}

				handlerToast("视频碎片复现完毕");
//					Looper.prepare();
//					Toast.makeText(CkplayerActivity.this, "视频碎片复现完毕",Toast.LENGTH_SHORT).show();
			}
		}

		//判断线程是否尚未终止
		public boolean isAlive(){
			return isStart;
		}

		//终止线程执行
		public void stop(){
			isStart = false;
		}
	}
	
	private final ServiceConnection ckServiceConnection = new ServiceConnection() {
        public void onServiceConnected(final ComponentName className, IBinder rawBinder) {
            ckService = ((BluetoothLEService.LocalBinder) rawBinder).getService();
            Log.d("ckService:", "onServiceConnected ckService= " + ckService);
            if (!ckService.initialize()) {
                finish();
            }

			//下面的代码容易导致在一些设备上蓝牙直接断开，连接失败，调用onConnectFailed()
//			boolean flag = ckService.connect(XApp.mBTMac);
//			if(flag){
//				getSupportActionBar().setTitle("@string/app_name"+"（蓝牙已连接）");
//			}

            ckService.setOnDataReceiveListener(new BluetoothLEService.OnDataReceiveListener() {
                @Override
                public void onDataReceive(final Dot dot) {
                    runOnUiThread(new Runnable() {
                        @SuppressLint("NewApi") @Override
                        public void run() {
                          Log.e(TAG,dot.toString()); 
                          //Log.e(TAG,"time_period" + String.valueOf(dot.timelong - last_timelong));

                          processDots(dot);

                        }
                    });
                }

                @Override
                public void onOfflineDataReceive(final Dot dot) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           //processDots(dot);
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
                        	 //showInftTextView.setText("点读！点读值为："+OIDSize);
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
//                            Log.e(TAG, "receive led is " + color);
//                            switch (color) {
//                                case 1: // blue
//                                    gColor = 5;
//                                    break;
//                                case 2: // green
//                                    gColor = 3;
//                                    break;
//                                case 3: // cyan
//                                    gColor = 8;
//                                    break;
//                                case 4: // red
//                                    gColor = 1;
//                                    break;
//                                case 5: // magenta
//                                    gColor = 7;
//                                    break;
//                                case 6: // yellow
//                                    gColor = 2;
//                                    break;
//                                case 7: // white
//                                    gColor = 6;
//                                    break;
//                                default:
//                                    break;
//                            }
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
                   // penType = type;
                }
            });
        }

        public void onServiceDisconnected(ComponentName classname) {
            ckService = null;
			Log.e(TAG,"ckService onServiceDisconnected");
        }
    };

    @SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" }) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ckplayer);
//        Arrays.fill(timeS, -1.0f);
//        Arrays.fill(videoIDS, -1);

		ActionBar actionBar = getSupportActionBar();
		//actionBar.setTitle(ApplicationResources.getLocalVersionName(this));
		actionBar.setDisplayHomeAsUpEnabled(true);


		//将当前活动与蓝牙服务绑定，之后就共有两个活动（另一个是主活动）同时绑定了蓝牙服务
		Intent getServiceIntent = new Intent(this, BluetoothLEService.class);
		boolean bBind = bindService(getServiceIntent, ckServiceConnection, BIND_AUTO_CREATE);

        bleManager = PenCommAgent.GetInstance(getApplication());
		oldPageManager = OldPageManager.getInstance();
		videoManager = VideoManager.getInstance();
		penMacManager = PenMacManager.getInstance();//必须在加载数据之前
		excelReader = ExcelReader.getInstance();

		this.page = (Page)this.writer.getBindedPage();

		float time = getIntent().getFloatExtra("time",-1);
		int videoID = getIntent().getIntExtra("videoID",-1);
		String videoName = getIntent().getStringExtra("videoName");
		videoManager.addVideo(videoID, videoName);

//		currentID = 1;//第一个视频ID是1，不是0
		currentID = videoID;
        ckTextView = (TextView) findViewById(R.id.cktextview);
		Log.e(TAG,"currentID"+": "+currentID+"");
		Log.e(TAG,"videoManager.videos.size(): "+videoManager.videos.size()+"");
//		VideoManager.Video v1 = videoManager.getVideoByID(currentID);
//		ckTextView.setText("[视频编号： "+v1.getVideoID()+" ][视频名称： "+v1.getVideoName()+" ][笔记人数："+v1.getMatesNumber()+" ][笔记页数： "+v1.getPageNumber()+" ]");

		webView = (WebView) findViewById(R.id.web_view);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);//让 WebView支持 JavaScript脚本，允许Android代码与JavaScript代码交互
        //设置缓存模式
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //开启DOM storage API功能
        settings.setDomStorageEnabled(true);
        //开启database storage API功能
        settings.setDatabaseEnabled(true);

		//允许网页视频自动播放
		settings.setMediaPlaybackRequiresUserGesture(false);

//        settings.setAppCacheEnabled(true);
        String cachePath = getApplicationContext().getCacheDir().getPath();
        //把内部私有缓存目录‘/data/data/包名/cache/'作为WebView的AppCache的存储路径
//        settings.setAppCachePath(cachePath);
//        settings.setAppCacheMaxSize(500*1024*1024);

        //加速丝滑
//        getBinding().web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            //当需要从一个网页跳转到另一个网页时，目标网页仍然在当前 WebView 中显示，而不是打开系统浏览器
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);//根据传入的参数再去加载新的网页
                return true;
            }
        });
        webView.loadUrl("http://120.46.205.254/");//在 WebView中加载本地html文件

        //对象映射：第一个参数为实例化的自定义内部类对象 第二个参数为提供给JS端调用使用的对象名
        webView.addJavascriptInterface(new JsOperation(CkplayerActivity.this), "CkplayerActivity");

		if(time != -1 && videoID != -1){
			new Thread(new Runnable() {
				@Override
				public void run() {
//					while (playOrFalse == 0){
//						callJS("play()");
//						try {
//							Thread.sleep(500);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//					try {
//						Thread.sleep(200);//避免直接跳转导致的视频加载无限循环
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					try {
						Thread.sleep(200);//避免直接跳转导致的视频加载无限循环等莫名其妙问题
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					seekTime(time, videoID);
				}
			}).start();
		}
	}
    
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();
		if (playOrFalsebuffer == 1) {
			callJavaScript("play()");
			playOrFalsebuffer = 0;
		}

		super.onStart();
	}
    
    private int playOrFalsebuffer = 0;//根据需要暂时存储视频当前播放状态

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		super.onPause();
		if (playOrFalse == 1) {
			callJavaScript("pause()");
			playOrFalsebuffer = 1;
		}
		super.onStop();
	}

	@Override
    protected void onDestroy() {
	    super.onDestroy();
	    Log.d(TAG, "onDestroy");
		unbindService(ckServiceConnection);
    }
    
    //自定义内部类，提供给JS调用
    class JsOperation {
    	
    	private CkplayerActivity ckplayer;
    	
    	public JsOperation(CkplayerActivity context){
    		this.ckplayer = context;
    	}
    	
    	@JavascriptInterface//该注解一定要加，从而让Javascript可以访问
        public void setTime(float t) {
    		
    		ckplayer.time = t;//从JS传递当前视频进度
    		
            //Log.e(TAG,String.valueOf(ckplayer.time));
    		//Toast.makeText(ckplayer, "onSum-" + ckplayer.time, Toast.LENGTH_SHORT).show();
        }
    	
    	@JavascriptInterface//该注解一定要加，从而让Javascript可以访问
    	public void setCurrentID(int cid){
    		
    		if(currentID != cid+1){
    			currentID = cid+1;//从JS传递当前视频ID
				Log.e(TAG,"当前视频ID变化为: "+currentID);
    			
    		    VideoManager.Video v1 = videoManager.getVideoByID(currentID);
				setCkTextView("[视频编号： "+v1.getVideoID()+" ][视频名称： "+v1.getVideoName()+" ][笔记人数："+v1.getMatesNumber()+" ][笔记页数： "+v1.getPageNumber()+" ]");
//    		    ckTextView.setText("[视频编号： "+v1.getVideoID()+" ][视频名称： "+v1.getVideoName()+" ][笔记人数："+v1.getMatesNumber()+" ][笔记页数： "+v1.getPageNumber()+" ]");
    		}
    		
    		Log.e(TAG,"currentID: " + String.valueOf(currentID)); 
    	}
    	
    	@JavascriptInterface//该注解一定要加，从而让Javascript可以访问
    	public void setDuration(float d){
    		ckplayer.duration = d;//从JS传递当前视频总时长
    	}
    	
    	@JavascriptInterface//该注解一定要加，从而让Javascript可以访问
    	public void setVideoStatus(int p){
    		ckplayer.playOrFalse = p;//从JS传递当前视频播放状态
    	}
    	
    	
   }
    


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
    	getMenuInflater().inflate(R.menu.ckplayeractivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
			case android.R.id.home:
				finish();
				break;
	        case R.id.action_clear:
//				pageManager.clear();Log.e(TAG,"清除数据");
				VideoManager.Video v1 = videoManager.getVideoByID(currentID);
				ckTextView.setText("[视频编号： "+v1.getVideoID()+" ][视频名称： "+v1.getVideoName()+" ][笔记人数："+v1.getMatesNumber()+" ][笔记页数： "+v1.getPageNumber()+" ]");
				break;
	        case R.id.dot_info_intent:
	        	Intent dotInfoIntent = new Intent(this, DotInfoActivity.class);
	        	startActivity(dotInfoIntent);
	        	break;
	        default:
        }

        return true;

//        return super.onOptionsItemSelected(item);
    }

}
