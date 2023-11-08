package com.example.xmatenotes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import com.example.xmatenotes.ui.BaseActivity;
import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.manager.OldPageManager;
import com.example.xmatenotes.logic.manager.PenMacManager;
import com.tqltech.tqlpencomm.bean.Dot;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 本地笔迹数据存储文件内容呈现活动
 */
public class DotInfoActivity extends BaseActivity {
	
	private final static String TAG = "DotInfoActivity";
	
	private final static String FILE_NAME = "XmateNotes";//存储文件名

	private PenMacManager penMacManager;//所有mac地址管理对象
	private OldPageManager oldPageManager = null;
    
	private ArrayList<String> data = new ArrayList<String>();
	private ArrayList<String> dataSquare = new ArrayList<String>();
	
	private int number = 0;//笔迹点序号
	private int strokesNumber = 0;//笔划序号
	
//	private int xMin, xMax, yMin, yMax;//刻画当前笔划的坐标范围
	private Rect rect = null;
	private int xD,yD;//当前笔迹点坐标
	private long page = XmateNotesApplication.DEFAULT_INT;//当前页码
	private float time = XmateNotesApplication.DEFAULT_FLOAT;//存储当前视频碎片时刻信息
	private int videoID = XmateNotesApplication.DEFAULT_INT;//存储当前视频碎片ID信息
	private byte penID = XmateNotesApplication.DEFAULT_INT;//存储当前笔ID
	private String timelong = null;//存储当前时间戳
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);

		penMacManager = PenMacManager.getInstance();
		oldPageManager = OldPageManager.getInstance();

		loadData();
		Collections.reverse(dataSquare);
		dataSquare.add(0, "笔划ID"+"   PageID"+"   左上点坐标"+"  右下点坐标"+"      视频进度"+"   视频ID   笔ID"+"                                 时间戳");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				DotInfoActivity.this, android.R.layout.simple_list_item_1, dataSquare);
		ListView listView = (ListView) findViewById(R.id.dot_list_view);
		listView.setAdapter(adapter);
		
	}

	public void loadData() {
		File file = new File(XmateNotesApplication.context.getFilesDir().getAbsolutePath(), FILE_NAME);
		Log.e(TAG, XmateNotesApplication.context.getFilesDir().getAbsolutePath()+FILE_NAME);

		if(!file.exists()) {
			Log.e(TAG, FILE_NAME+"文件不存在！");
		}else {
			FileInputStream in = null;
			BufferedReader reader = null;
			try {
				in = XmateNotesApplication.context.openFileInput(FILE_NAME);
				reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				while ((line = reader.readLine()) != null) {
					processMediaDot(MediaDot.parse(line));
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
		Toast.makeText(XmateNotesApplication.context, "笔迹数据加载完毕",Toast.LENGTH_SHORT).show();
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

		xD = mediaDot.getIntX();
		yD = mediaDot.getIntY();
		
		if(mediaDot.type == Dot.DotType.PEN_DOWN){
			bufferInit(xD, yD);
			penID = penMacManager.getPenIDByMac(mediaDot.penMac);
			page = mediaDot.pageId;
        	time = mediaDot.videoTime;
			videoID = mediaDot.videoId;
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
