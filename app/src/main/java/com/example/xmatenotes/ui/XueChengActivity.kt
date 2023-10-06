package com.example.xmatenotes.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.example.xmatenotes.R
import com.example.xmatenotes.app.ax.A3
import com.example.xmatenotes.logic.manager.CoordinateConverter
import com.example.xmatenotes.logic.presetable.LogUtil
import com.example.xmatenotes.ui.qrcode.WeChatQRCodeActivity
import com.king.wechat.qrcode.WeChatQRCodeDetector
import org.opencv.OpenCV

/**
 * 学程版面使用的活动
 */
class XueChengActivity : PageActivity() {

    companion object {
        private const val TAG = "XueChengActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_xue_cheng)
        //配置坐标转换器,maxX，maxY,maxrealX,maxrealY

        //配置坐标转换器,maxX，maxY,maxrealX,maxrealY
        this.coordinateConverter = CoordinateConverter(
            A3.ABSCISSA_RANGE.toFloat(),
            A3.ORDINATE_RANGE.toFloat(),
            (A3.PAPER_WIDTH * 10).toFloat(),
            (A3.PAPER_HEIGHT * 10).toFloat()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when (id) {
            android.R.id.home -> finish()
            R.id.video_Notes -> {
                val videoNoteIntent = Intent(this@XueChengActivity, XueChengVideoNoteActivity::class.java)
                LogUtil.e(TAG, "action_ckplayer")
                videoNoteIntent.putExtra("time", 0.0f)

                LogUtil.e(TAG, "ckplayer跳转至videoID: " + 1.toString())
                videoNoteIntent.putExtra("videoID", 1)
                startActivity(videoNoteIntent)
            }

            R.id.photo_notes -> {
                LogUtil.e(TAG, "QR_scan")
                // 初始化OpenCV
                OpenCV.initAsync(this)
                // 初始化WeChatQRCodeDetector
                WeChatQRCodeDetector.init(this)
                startActivityForResult(WeChatQRCodeActivity::class.java)
            }

            else -> {}
        }

        return true
    }
}