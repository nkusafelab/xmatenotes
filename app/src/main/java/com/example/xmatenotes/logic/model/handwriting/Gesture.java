package com.example.xmatenotes.logic.model.handwriting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * 指令符类
 */

public class Gesture {
	
	private final static String TAG = "Gesture";
	
	private long duration;//手势时间长度，从第一个笔划的“PEN_DOWN”点到最后一个笔划的“PEN_UP”点的时间
	
	private long frontTSpan;//距离前一笔划的时间间隔，从前一笔画“PEN_UP”到该笔划“PEN_DOWN”

	private int insId = 0;//命令ID
	
//	private double xMin, xMax, yMin, yMax;//刻画笔划的坐标范围
	private RectF rectF;//刻画笔划的矩形坐标范围
	 
	private Map<String,ArrayList<MediaDot>> strokes;//按时间顺序存储多笔划的笔迹点
	
	private int strokesNumber;//记录该待定命令包含的笔划数

	private boolean isRes = true;//是否参加响应

	public static final int INS_COLOR = Color.BLUE;//命令默认颜色

	public Gesture(long duration, long frontTSpan, RectF rectF, Map<String, ArrayList<MediaDot>> strokes) {
		super();
		this.duration = duration;
		this.frontTSpan = frontTSpan;
		this.rectF = new RectF(rectF);
		this.strokes = strokes;
	}

	public Gesture(Gesture ges){
		this.duration = ges.duration;
		this.frontTSpan = ges.frontTSpan;
		this.rectF = new RectF(ges.getRectF());
		this.strokes = new HashMap<String, ArrayList<MediaDot>>(ges.getStrokes());
	}

	public Gesture() {
		super();
		this.strokes = new HashMap<String, ArrayList<MediaDot>>();
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

	public int getInsId() {
		return insId;
	}

	public void setInsId(int insId) {
		this.insId = insId;
		if(isCharInstruction()){
			Set<Map.Entry<String, ArrayList<MediaDot>>> set = this.getStrokes().entrySet();
			for (Map.Entry<String, ArrayList<MediaDot>> node: set) {
				for(MediaDot mDot: node.getValue()){
					mDot.color = Color.BLUE;
					mDot.width = MediaDot.DEFAULT_BOLD_WIDTH;
					mDot.setIns(insId);
					Log.e(TAG, "setInsId: insId: "+insId);
					Log.e(TAG, "setInsId: isCharInstruction(): "+isCharInstruction());
				}
			}
		}else {
			Set<Map.Entry<String, ArrayList<MediaDot>>> set = this.getStrokes().entrySet();
			for (Map.Entry<String, ArrayList<MediaDot>> node: set) {
				for(MediaDot mDot: node.getValue()){
					mDot.setIns(insId);
				}
			}
		}

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

	public Map<String, ArrayList<MediaDot>> getStrokes() {
		Log.e(TAG, "getStrokes()执行");
		return strokes;
	}

	public void setStrokes(Map<String, ArrayList<MediaDot>> strokes) {
		this.strokes = strokes;
	}
	
	public void clear() {
		this.duration = 0;
		this.frontTSpan = 0;
		this.rectF = null;
		this.strokes.clear();
		this.strokesNumber = 0;
	}

	/**
	 * 是否为字符命令
	 * @return
	 */
	public boolean isCharInstruction(){
		if(isInstruction() && !isActInstruction()){
			return true;
		}
		return false;
	}

	/**
	 * 是否为动作命令
	 * @return
	 */
	public boolean isActInstruction(){
		if(insId == 1 || insId == 2 || insId == 3){
			return true;
		}
		return false;
	}

	/**
	 * 是否为命令
	 * @return
	 */
	public boolean isInstruction(){
		return insId > 0 && insId < 10;
	}

	public boolean isHandWriting(){
		return insId == 0 || insId == 10;
	}

	public static Rect rectFToRect(RectF rectF){
		return new Rect((int)rectF.left, (int)rectF.top, (int)Math.ceil(rectF.right), (int)Math.ceil(rectF.bottom));
	}

	public boolean isRes() {
		return isRes;
	}

	public void setRes(boolean res) {
		isRes = res;
	}
}
