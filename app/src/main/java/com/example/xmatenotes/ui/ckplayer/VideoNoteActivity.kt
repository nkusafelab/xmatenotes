package com.example.xmatenotes.ui.ckplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import com.example.xmatenotes.DotInfoActivity
import com.example.xmatenotes.R
import com.example.xmatenotes.app.XmateNotesApplication
import com.example.xmatenotes.logic.manager.PenMacManager
import com.example.xmatenotes.logic.manager.VideoManager
import com.example.xmatenotes.logic.manager.Writer.ResponseWorker
import com.example.xmatenotes.logic.model.handwriting.MediaDot
import com.example.xmatenotes.logic.model.instruction.Command
import com.example.xmatenotes.logic.model.instruction.Responser
import com.example.xmatenotes.util.LogUtil
import com.example.xmatenotes.ui.PageActivity
import com.tqltech.tqlpencomm.bean.Dot
import java.text.ParseException

abstract class VideoNoteActivity : PageActivity() {

    companion object {
        private const val TAG = "VideoNoteActivity"
        private const val SEEK_TIME_DELAY_PERIOD: Long = 120000 //定义视频碎片复现时间长度为2min
        private const val VIDEO_FRAGMENT_WORKER = "视频碎片计时任务"
    }

    protected val videoManager = VideoManager.getInstance()

    protected var webView: WebView? = null
    protected var ckTextView: TextView? = null

    protected var time = 0f //记录当前视频进度
    protected var duration = 0f //记录当前视频总时长
    protected var currentId = 0 //记录当前视频ID
    protected var playOrFalse = 0 //记录视频播放状态

    protected var videoFragmentWorker: ResponseWorker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initWebView()

        val time = intent.getFloatExtra("time", -1f)
        val videoID = intent.getIntExtra("videoID", -1)
        val videoName = intent.getStringExtra("videoName")
        videoManager.addVideo(videoID, videoName)

//		currentID = 1;//第一个视频ID是1，不是0
        currentId = videoID
        LogUtil.e(TAG, "currentID: $currentId")
        LogUtil.e(TAG, "videoManager.videos.size(): " + VideoManager.videos.size + "")

        if (time != -1f && videoID != -1) {
            Thread {
                try {
                    Thread.sleep(200) //避免直接跳转导致的视频加载无限循环等莫名其妙问题
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                seekTime(time, videoID)
            }.start()
        }

    }

    override fun onResume() {
        super.onResume()
        if (playOrFalsebuffer == 1) {
            callJavaScript("play()")
            playOrFalsebuffer = 0
        }
    }

    private var playOrFalsebuffer = 0 //根据需要暂时存储视频当前播放状态

    override fun onPause() {
        super.onPause()
        if (playOrFalse == 1) {
            callJavaScript("pause()")
            playOrFalsebuffer = 1
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_video_note
    }

    override fun initUI() {
        super.initUI()
        ckTextView = findViewById<View>(R.id.cktextview) as TextView
    }

    override fun initPage() {
        super.initPage()
    }

    fun initWebView(){
        webView = findViewById<View>(R.id.web_view) as WebView
        val settings = webView!!.settings
        settings.javaScriptEnabled = true //让 WebView支持 JavaScript脚本，允许Android代码与JavaScript代码交互

        //设置缓存模式
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        //开启DOM storage API功能
        settings.domStorageEnabled = true
        //开启database storage API功能
        settings.databaseEnabled = true

        //允许网页视频自动播放
        settings.mediaPlaybackRequiresUserGesture = false

//        settings.setAppCacheEnabled(true);
        val cachePath = applicationContext.cacheDir.path
        //把内部私有缓存目录‘/data/data/包名/cache/'作为WebView的AppCache的存储路径
//        settings.setAppCachePath(cachePath);
//        settings.setAppCacheMaxSize(500*1024*1024);

        //加速丝滑
//        getBinding().web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        webView!!.webViewClient = object : WebViewClient() {
            //当需要从一个网页跳转到另一个网页时，目标网页仍然在当前 WebView 中显示，而不是打开系统浏览器
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url) //根据传入的参数再去加载新的网页
                return true
            }
        }
        webView!!.loadUrl("http://62.234.184.102/") //在 WebView中加载本地html文件

        //对象映射：第一个参数为实例化的自定义内部类对象 第二个参数为提供给JS端调用使用的对象名
        webView!!.addJavascriptInterface(JsOperation(this@VideoNoteActivity), "VideoNoteActivity")
    }

    override fun getResponser(): Responser {
        return VideoNoteResponser()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when (id) {
            android.R.id.home -> finish()
            R.id.action_clear -> {
                //				pageManager.clear();Log.e(TAG,"清除数据");
                val v1 = videoManager.getVideoByID(currentId)
                ckTextView!!.text =
                    "[视频编号： " + v1.videoID + " ][视频名称： " + v1.videoName + " ][笔记人数：" + v1.matesNumber + " ][笔记页数： " + v1.pageNumber + " ]"
            }

            R.id.dot_info_intent -> {
                val dotInfoIntent = Intent(this, DotInfoActivity::class.java)
                startActivity(dotInfoIntent)
            }

            else -> {}
        }

        return super.onOptionsItemSelected(item)
    }

    override fun processEachDot(mediaDot: MediaDot) {
        if (this.writer.containsResponseWorker(this.videoFragmentWorker)){
            this.writer.deleteResponseWorker(this.videoFragmentWorker)
            LogUtil.e(TAG, "视频碎片计时器关闭")
        }
        super.processEachDot(mediaDot)
    }

    override fun createMediaDot(dot: Dot): MediaDot {
        var mediaDot = super.createMediaDot(dot)

        var timeR = time
        try {
            //修正视频进度;
            timeR = MediaDot.reviseTimeS(dot.timelong, time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        mediaDot.videoTime = timeR
        mediaDot.videoID = currentId
        mediaDot.color = MediaDot.DEEP_ORANGE

        return mediaDot
    }

    protected fun seekTime(time: Float, videoId: Int) {
        val seekTime = Math.round(time) //四舍五入
        LogUtil.e(TAG, "seekTime: ($seekTime,$videoId)")
        val v1 = videoManager.getVideoByID(videoId)
        LogUtil.e(TAG, "seekTime: videoId: $v1")
        setCkTextView("[视频编号： " + v1.videoID + " ][视频名称： " + v1.videoName + " ][笔记人数：" + v1.matesNumber + " ][笔记页数： " + v1.pageNumber + " ]")
        callJS("seekTime(" + seekTime + "," + (videoId - 1) + ")")
        showToast("seekTime: 双击命令：视频碎片复现跳转至第" + videoId + "个视频的第" + seekTime + "秒")

        if (this.writer.containsResponseWorker(this.videoFragmentWorker)){
            this.writer.deleteResponseWorker(this.videoFragmentWorker)
        }

        //启动视频碎片计时器
        this.videoFragmentWorker = this.writer.addResponseWorker(
            VIDEO_FRAGMENT_WORKER, SEEK_TIME_DELAY_PERIOD
        ) {
            if (playOrFalse == 1) {
                callJS("pause()")
                Log.e(TAG, "seekTime: videoFragmentWorker finish")
            }
            showToast("视频碎片复现完毕")
        }
        LogUtil.e(TAG, "seekTime: 视频碎片计时器打开")
    }

    private fun setCkTextView(text: String?) {
        if (text == null) {
            val v1 = videoManager.getVideoByID(currentId)
            setCkTextView("[视频编号： " + v1.videoID + " ][视频名称： " + v1.videoName + " ][笔记人数：" + v1.matesNumber + " ][笔记页数： " + v1.pageNumber + " ]")
            return
        }
        val message = Message()
        message.what = 1
        val bundle = Bundle()
        bundle.putString("text", text)
        message.data = bundle
        handler.sendMessage(message)
    }

    //通过handler发送消息的方式调用JavaScript方法
    fun callJS(call: String?) {
        val message = Message()
        message.what = 0
        val bundle = Bundle()
        bundle.putString("call", call)
        message.data = bundle
        handler.sendMessage(message)
    }

    @SuppressLint("HandlerLeak")
    var handler: Handler = object : Handler() {
        @SuppressLint("NewApi", "SetTextI18n")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                0 -> {
                    val call = msg.data.getString("call")
                    callJavaScript(call!!)
                }

                1 -> {
                    val text = msg.data.getString("text")
                    ckTextView!!.text = text
                }

                else -> {}
            }
        }
    }

    //通过Java调用JavaScript代码，参数为字符串形式的待调用JavaScript函数
    @SuppressLint("NewApi")
    private fun callJavaScript(str: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView!!.evaluateJavascript("javascript:$str") {
                //此处编写收到JavaScript函数返回值后待执行的逻辑
            }
        } else {
            webView!!.loadUrl("javascript:$str")
        }
        LogUtil.e(TAG, "javascript:$str")
    }

    open inner class VideoNoteResponser : SmartPenResponser() {

        override fun onCalligraphy(command: Command?): Boolean {
            if(!super.onCalligraphy(command)){
                return false
            }

            if (playOrFalse == 1){
                callJS("pause()")
            }

            return true
        }

        override fun onDoubleClick(command: Command?): Boolean {
            if(!super.onDoubleClick(command)){
                return false
            }

            command?.handWriting?.firstDot?.let {coordinate->
                page.getHandWritingByCoordinate(coordinate)?.let {
                    if(it.hasVideo() && (coordinate as MediaDot).penMac.equals(XmateNotesApplication.mBTMac)){
                        //跳转视频
                        seekTime(coordinate.videoTime, coordinate.videoID)
                        return false
                    } else {
                        //跳转笔迹动态复现
                    }
                }
            }
            return true
        }

        override fun onDelayHandWriting(command: Command?): Boolean {
            if(!super.onDelayHandWriting(command)){
                return false
            }

            if (command != null) {
                val penID = PenMacManager.getPenIDByMac(XmateNotesApplication.mBTMac)
                videoManager.getVideoByID(currentId).addMate(penID)
                videoManager.getVideoByID(currentId).addPage((command.handWriting.firstDot as MediaDot).pageID)
            }
            val v1 = videoManager.getVideoByID(currentId)
            setCkTextView("[视频编号： " + v1.videoID + " ][视频名称： " + v1.videoName + " ][笔记人数：" + v1.matesNumber + " ][笔记页数： " + v1.pageNumber + " ]")

            if (playOrFalse == 0) {
                callJS("play()")
            }
            //如果正在书写区答题，退出视频笔记

            return true
        }
    }

    //自定义内部类，提供给JS调用
    inner class JsOperation(private val videoNote: VideoNoteActivity) {
        @JavascriptInterface //该注解一定要加，从而让Javascript可以访问
        fun setTime(t: Float) {
            videoNote.time = t //从JS传递当前视频进度

        }

        @JavascriptInterface //该注解一定要加，从而让Javascript可以访问
        fun setCurrentID(cid: Int) {
            if (currentId != cid + 1) {
                currentId = cid + 1 //从JS传递当前视频ID
                LogUtil.e(TAG, "当前视频ID变化为: $currentId")
                val v1: VideoManager.Video = videoManager.getVideoByID(currentId)
                setCkTextView("[视频编号： " + v1.videoID + " ][视频名称： " + v1.videoName + " ][笔记人数：" + v1.matesNumber + " ][笔记页数： " + v1.pageNumber + " ]")
            }
            Log.e(TAG, "currentID: $currentId")
        }

        @JavascriptInterface //该注解一定要加，从而让Javascript可以访问
        fun setDuration(d: Float) {
            videoNote.duration = d //从JS传递当前视频总时长
        }

        @JavascriptInterface //该注解一定要加，从而让Javascript可以访问
        fun setVideoStatus(p: Int) {
            videoNote.playOrFalse = p //从JS传递当前视频播放状态
        }
    }
}