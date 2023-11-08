package com.example.xmatenotes

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.xmatenotes.ui.qrcode.BitmapCacheManager
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class ImgActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img)

        imageView= findViewById(R.id.imageView)
        button=findViewById(R.id.saveButton)
        val bitmap: Bitmap? = BitmapCacheManager.getBitmap("QRBitmap")

        button.setOnClickListener {
            if(bitmap!=null) {
                saveImageToGallery(this, bitmap, "123456")
            }
        }
        imageView.setImageBitmap(bitmap)
    }


    // 保存位图到本地相册
    fun saveImageToGallery(context: Context?, bitmap: Bitmap, fileName: String) {
        // 检查SD卡是否可用
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            // 创建保存图片的目录
            val directory =
                File(Environment.getExternalStorageDirectory(), "/Pictures/XMateNotes/")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // 创建保存图片的文件
            val file = File(directory, "$fileName.jpg")
            try {
                val os: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()

                // 发送广播通知系统图库刷新
                MediaScannerConnection.scanFile(
                    context, arrayOf(file.absolutePath), null
                ) { path: String?, uri: Uri? ->
                    // 图片保存成功，显示提示信息
                    Toast.makeText(context, "图片已保存到相册", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "SD卡不可用，无法保存图片", Toast.LENGTH_SHORT).show()
        }
    }
}