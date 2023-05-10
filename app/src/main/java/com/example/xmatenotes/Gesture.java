package com.example.xmatenotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.R.string;
import android.graphics.RectF;
import android.util.Log;

import com.example.xmatenotes.DotClass.SimpleDot;
import com.tqltech.tqlpencomm.bean.Dot;
import android.util.Log;

/**
 * 指令符类
 */

public class Gesture {
	
	private final static String TAG = "Gesture";
	
	private long duration;//手势时间长度，从第一个笔划的“PEN_DOWN”点到最后一个笔划的“PEN_UP”点的时间
	
	private long frontTSpan;//距离前一笔划的时间间隔，从前一笔画“PEN_UP”到该笔划“PEN_DOWN”
	
//	private double xMin, xMax, yMin, yMax;//刻画笔划的坐标范围
	private RectF rectF;//刻画笔划的矩形坐标范围
	 
	private Map<String,ArrayList<SimpleDot>> strokes;//按时间顺序存储多笔划的笔迹点
	
	private int strokesNumber;//记录该待定命令包含的笔划数		 	 

	public Gesture(long duration, long frontTSpan, RectF rectF, Map<String, ArrayList<SimpleDot>> strokes) {
		super();
		this.duration = duration;
		this.frontTSpan = frontTSpan;
		this.rectF = new RectF(rectF);
		this.strokes = strokes;
	}

	public Gesture() {
		super();
		this.strokes = new HashMap<String, ArrayList<SimpleDot>>();
	}
	
	public int getStrokesNumber() {
		return strokesNumber;
	}

	public void setStrokesNumber(int strokesNumber) {
		if(strokesNumber<1) {
			Log.e(TAG, "命令的笔划数出现异常！");
		}
		this.strokesNumber = strokesNumber;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getFrontTSpan() {
		return frontTSpan;
	}

	public void setFrontTSpan(long frontTSpan) {
		this.frontTSpan = frontTSpan;
	}

	public RectF getRectF() {
		return rectF;
	}

	public void setRectF(RectF rectF) {
		this.rectF = new RectF(rectF);
	}

	public float getWidth(){
		if(rectF != null){
			return rectF.width();
		}
		return -1f;
	}

	public float getHeight(){
		if(rectF != null){
			return rectF.height();
		}
		return -1f;
	}

	public Map<String, ArrayList<SimpleDot>> getStrokes() {
		Log.e(TAG, "getStrokes()执行");
		return strokes;
	}

	public void setStrokes(Map<String, ArrayList<SimpleDot>> strokes) {
		this.strokes = strokes;
	}
	
	public void clear() {
		this.duration = 0;
		this.frontTSpan = 0;
		this.rectF = null;
		this.strokes.clear();
		this.strokesNumber = 0;
	}
     
}
