package com.example.xmatenotes.logic.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.xmatenotes.ui.MainActivity;
import com.example.xmatenotes.ui.ckplayer.CkplayerActivity;
import com.example.xmatenotes.ui.ckplayer.VideoNoteActivity;
import com.example.xmatenotes.util.LogUtil;

/**
 * <p><strong>视频管理类</strong></p>
 * <p>包括videoID到Video对象的映射、新Video对象的添加等</p>
 */
public class VideoManager {
	
	private final static String TAG = "VideoManager";

	private static final VideoManager videoManager = new VideoManager();

	public static ArrayList<Video> videos = new ArrayList<Video>();
	private int videosNumber = 0;

	private VideoManager() {

//		videos = new ArrayList<Video>();
//		addVideo(1, "001不等号的由来《一元一次不等式》初一下doc.mp4");
//		addVideo(2, "");
//		addVideo(1,"p1");//如果从0开始，意味着每次新page都要把videoID矩阵所有元素初始化为-1
//		addVideo(2,"p2");
//		addVideo(3,"p3");
//		addVideo(4,"p4");
//		addVideo(5,"p5");
//		Log.e(TAG,"videos.size(): "+videos.size()+"");

	}
	public static VideoManager getInstance(){
//		if(videoManager == null){
//			synchronized (VideoManager.class){
//				if(videoManager == null){
//					Log.e(TAG,videoManager+"");
//					videoManager = new VideoManager();
//					Log.e(TAG,"videoManager = new VideoManager();");
//				}
//			}
//		}
		return videoManager;
	}

	public void init(ExcelManager.DataSheet videoDataSheet){
		final String VIDEO_NAME = "视频名称";
		Map<String, Map<String, String>> map = videoDataSheet.getData();
		Set<Map.Entry<String, Map<String, String>>> set = map.entrySet();
		Iterator<Map.Entry<String, Map<String, String>>> it = set.iterator();
		while (it.hasNext()){
			Map.Entry<String, Map<String, String>> node = it.next();
			addVideo(Integer.parseInt(node.getKey()), node.getValue().get(VIDEO_NAME));
		}
	}
	
	public boolean contains(int id){
		for (int i = 0;i<videos.size();i++) {
			Video v = videos.get(i);
			if(v.videoID == id){
				return true;
			}
		}
		return false;
	}
	
	public Video getVideoByID(int id){
		LogUtil.e(TAG, "getVideoByID: id: "+id);
		for (int i = 0;i<videos.size();i++) {
			LogUtil.e(TAG, "getVideoByID: i: "+i);
			Video v = videos.get(i);
			Log.e(TAG, "getVideoByID: videos.get("+i+").videoID: "+videos.get(i).videoID);

			if(v.videoID == id){
				Log.e(TAG, "getVideoByID: 找到: videos.get("+i+"): "+videos.get(i));
//				return videos.get(i);
				return v;
			}
		}
		return null;
	}

	public Video getVideoByName(String name){
		LogUtil.e(TAG, "getVideoByName: name: "+name);
		for (int i = 0;i<videos.size();i++) {
			LogUtil.e(TAG, "getVideoByName: i: "+i);
			Video v = videos.get(i);
			Log.e(TAG, "getVideoByName: videos.get("+i+").videoID: "+videos.get(i).videoName);

			if(v.videoName.equals(name)){
				Log.e(TAG, "getVideoByName: 找到: videos.get("+i+"): "+videos.get(i));
//				return videos.get(i);
				return v;
			}
		}
		return null;
	}
	
	public void addVideo(int id,String videoName){
		if(contains(id)){
			Log.e(TAG, "已经存在该Video");
			return;
		}
		videos.add(new Video(id,videoName,0,0));
		videosNumber++;
	}

	/**
	 * 启动视频笔记活动
	 * @param context 启动源
	 * @param cls 跳转目标活动，必须是VideoNoteActivity或其子类
	 */
	public static void startVideoNoteActivity(Context context, Class<?> cls, int videoID, float videoTime){
		//跳转至ck
		Intent ckIntent = new Intent(context, cls);
		ckIntent.putExtra("videoTime", videoTime);
		ckIntent.putExtra("videoId", videoID);
		LogUtil.e(TAG, "startVideoNoteActivity: 跳转至: videoID: "+videoID+" videoTime: "+videoTime);
		context.startActivity(ckIntent);
	}
	
	public int getVideosNumber() {
		return videosNumber;
	}

	public void setVideosNumber(int videosNumber) {
		this.videosNumber = videosNumber;
	}

//	public int getVideoIDByID(int id){
//		return videos.get(id-1).getVideoID();
//	}
	
	public String getVideoNameByID(int id){
		Video video = getVideoByID(id);
		if(video != null){
			return video.getVideoName();
		}
		return null;
//		return videos.get(id-1).getVideoName();
	}
	
	public int getMatesNumberByID(int id){
		Video video = getVideoByID(id);
		if(video != null){
			return video.getMatesNumber();
		}
		return 0;
//		return videos.get(id-1).getMatesNumber();
	}
	
	public int getPageNumberByID(int id){
		Video video = getVideoByID(id);
		if(video != null){
			return video.getPageNumber();
		}
		return 0;
//		return videos.get(-1).getPageNumber();
	}
	
	public void clear(){
//		videos.clear();
//		videosNumber = 0;
		for(Video v : videos){
			v.clear();
		}
		videos.clear();
	}

	/**
	 * Video类
	 * 一个Video对象的属性包括videoID、videoName、在该视频下记笔记的人和点阵纸张页码
	 */
	public class Video {

		private final static String TAG = "Video";

		private int videoID = -1;//1/2/3/4/5
		private String videoName = null;
		private int matesNumber = 0;
		private ArrayList<Byte> mates = new ArrayList<Byte>();//存储当前视频对应的多个penID
		private int pageNumber = 0;
		private ArrayList<Long> pages = new ArrayList<>();//存储当前视频对应的多个page

		public Video(int videoID, String videoName, int matesNumber, int pageNumber) {
			super();
			this.videoID = videoID;
			this.videoName = videoName;
			this.matesNumber = matesNumber;
			this.pageNumber = pageNumber;
		}

		//添加penID
		public void addMate(byte penID){

			if(mates.contains(penID)){
				Log.e(TAG, "已经存在该mate");
				return;
			}
			mates.add(penID);
			matesNumber++;
		}

		//添加page
		public void addPage(long pageID){
			if(pages.contains(pageID)){
				Log.e(TAG, "已经存在该page");
				return;
			}
			pages.add(pageID);
			pageNumber++;
		}

		public int getVideoID() {
			return videoID;
		}
		public void setVideoID(int videoID) {
			this.videoID = videoID;
		}
		public String getVideoName() {
			return videoName;
		}
		public void setVideoName(String videoName) {
			this.videoName = videoName;
		}
		public int getMatesNumber() {
			return matesNumber;
		}
		public void setMatesNumber(int matesNumber) {
			this.matesNumber = matesNumber;
		}
		public int getPageNumber() {
			return pageNumber;
		}
		public void setPageNumber(int pageNumber) {
			this.pageNumber = pageNumber;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Video video = (Video) o;
			return videoID == video.videoID && matesNumber == video.matesNumber && pageNumber == video.pageNumber && Objects.equals(videoName, video.videoName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(videoID, videoName, matesNumber, pageNumber);
		}

		@Override
		public String toString() {
			return "Video{" +
					"videoID=" + videoID +
					", videoName='" + videoName + '\'' +
					", matesNumber=" + matesNumber +
					", pageNumber=" + pageNumber +
					'}';
		}

		public void clear(){
//		videoID = -1;
//		videoName = null;
			matesNumber = 0;
			mates.clear();
			pageNumber = 0;
			pages.clear();
		}

	}

}
