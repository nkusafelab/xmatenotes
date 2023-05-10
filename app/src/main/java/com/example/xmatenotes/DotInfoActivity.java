package com.example.xmatenotes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.xmatenotes.App.XApp;
import com.example.xmatenotes.DotClass.MediaDot;
import com.example.xmatenotes.datamanager.PageManager;
import com.example.xmatenotes.datamanager.PenMacManager;
import com.tqltech.tqlpencomm.bean.Dot;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 本地笔迹数据存储文件内容呈现活动
 */
public class DotInfoActivity extends BaseActivity {
	
	private final static String TAG = "DotInfoActivity";
	
	private final static String FILE_NAME = "XmateNotes";//存储文件名

	private PenMacManager penMacManager;//所有mac地址管理对象
	private PageManager pageManager = null;
    
	private ArrayList<String> data = new ArrayList<String>();
	private ArrayList<String> dataSquare = new ArrayList<String>();
	
	private int number = 0;//笔迹点序号
	private int strokesNumber = 0;//笔划序号
	
//	private int xMin, xMax, yMin, yMax;//刻画当前笔划的坐标范围
	private Rect rect = null;
	private int xD,yD;//当前笔迹点坐标
	private int page = XApp.DEFAULT_INT;//当前页码
	private float time = XApp.DEFAULT_FLOAT;//存储当前视频碎片时刻信息
	private int videoID = XApp.DEFAULT_INT;//存储当前视频碎片ID信息
	private byte penID = XApp.DEFAULT_INT;//存储当前笔ID
	private String timelong = null;//存储当前时间戳
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);

		penMacManager = PenMacManager.getInstance();
		pageManager = PageManager.getInstance();

		loadData();
		Collections.reverse(dataSquare);
		dataSquare.add(0, "笔划ID"+"   PageID"+"   左上点坐标"+"  右下点坐标"+"      视频进度"+"   视频ID   笔ID"+"                                 时间戳");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				DotInfoActivity.this, android.R.layout.simple_list_item_1, dataSquare);
		ListView listView = (ListView) findViewById(R.id.dot_list_view);
		listView.setAdapter(adapter);
		
	}

	public void loadData() {
		File file = new File(XApp.context.getFilesDir().getAbsolutePath(), FILE_NAME);
		Log.e(TAG, XApp.context.getFilesDir().getAbsolutePath()+FILE_NAME);

		if(!file.exists()) {
			Log.e(TAG, FILE_NAME+"文件不存在！");
		}else {
			FileInputStream in = null;
			BufferedReader reader = null;
			try {
				in = XApp.context.openFileInput(FILE_NAME);
				reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				line = reader.readLine();
				if(line.length() == 0) {
					Log.e(TAG, "请求的文件内容为空");
				}else if(line.indexOf(" ", 0) != -1){
					Toast.makeText(XApp.context, "笔迹存储文件原有内容格式错误",Toast.LENGTH_SHORT).show();
				}else if( Integer.valueOf(line) != 9){
					Toast.makeText(XApp.context, "笔迹存储文件原有内容格式错误",Toast.LENGTH_SHORT).show();
				}

				if((line = reader.readLine()) == null){
					Toast.makeText(XApp.context, "笔迹存储文件内容为空",Toast.LENGTH_SHORT).show();
					Log.e(TAG, "笔迹存储文件内容为空");
				}else{
					processMediaDot(pageManager.parse(line));
				}

				while ((line = reader.readLine()) != null) {
					processMediaDot(pageManager.parse(line));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				reader = null;
				in = null;
			}
		}
		Toast.makeText(XApp.context, "笔迹数据加载完毕",Toast.LENGTH_SHORT).show();
	}

	//初始化
	private void bufferInit(int x, int y) {

//		xMax = 0;
//		xMin = Integer.MAX_VALUE;
//		yMax = 0;
//		yMin = Integer.MAX_VALUE;
		if(rect == null){
			rect = new Rect();
		}
		rect.setEmpty();
		rect.left = x;
		rect.right = x;
		rect.top = y;
		rect.bottom = y;

	}

	//执行代码块，用于形成刻画笔划范围的矩形
//	private void processRect() {
//
//		xMax = xD > xMax ? xD : xMax;
//		yMax = yD > yMax ? yD : yMax;
//		xMin = xD < xMin ? xD : xMin;
//		yMin = yD < yMin ? yD : yMin;
//
//	}
    
    private void processMediaDot(MediaDot mediaDot) {

		if(mediaDot.isEmptyDot()){
			return;
		}

		xD = mediaDot.x;
		yD = mediaDot.y;
		
		if(mediaDot.type == Dot.DotType.PEN_DOWN){
			bufferInit(xD, yD);
			penID = penMacManager.getPenIDByMac(mediaDot.penMac);
			page = mediaDot.pageID;
        	time = mediaDot.time;
			videoID = mediaDot.videoID;
    		strokesNumber++;
    		timelong = MediaDot.timelongFormat(mediaDot.timelong);
		}
//		processRect();
		rect.union(xD, yD);
		if(mediaDot.type == Dot.DotType.PEN_UP){
			dataSquare.add("   "+strokesNumber+(strokesNumber<10 ? "  ":"")+"           "+page+"            ("+rect.left+","+rect.top+")        ("+rect.bottom+","+rect.right+")         "+time+"s      "+videoID+"           "+penID+"          "+timelong );
		}
	}
}
