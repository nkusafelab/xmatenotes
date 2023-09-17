package com.example.xmatenotes

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.xmatenotes.logic.model.Page.QRObject
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class QRGenerateActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerate)
//        bitmap  = BitmapCacheManager.getBitmap("Bitmap")
        val generateButton: Button = findViewById(R.id.buttonGenerateQRCode)
        val qrObject = QRObject(
            p = "01",
            psx = 2290,
            psy = 1700,
            pn = "003",
            sc = "01",
            gr = "07",
            cl = "03",
            ca = "2100_150",
            au = "03",
            te = "037",
            st = "01GM",
            gn = "03",
            gl = "6",
            sub = "01",
            data = "011",
            time = "20230717155323",
            qx = 60,
            qy = 60,
            ql = 270
        )
        findViewById<EditText>(R.id.editTextPage).setText(qrObject.p)
        findViewById<EditText>(R.id.editTextPageSizeX).setText(qrObject.psx)
        findViewById<EditText>(R.id.editTextPageSizeY).setText(qrObject.psy)
        findViewById<EditText>(R.id.editTextPageNumber).setText(qrObject.pn)
        findViewById<EditText>(R.id.editTextSchoolCode).setText(qrObject.sc)
        findViewById<EditText>(R.id.editTextGradeCode).setText(qrObject.gr)
        findViewById<EditText>(R.id.editTextClassCode).setText(qrObject.cl)
        findViewById<EditText>(R.id.editTextCameraSize).setText(qrObject.ca)
        findViewById<EditText>(R.id.editTextDataSource).setText(qrObject.au)
        findViewById<EditText>(R.id.editTextTeacherCode).setText(qrObject.te)
        findViewById<EditText>(R.id.editTextStudentCode).setText(qrObject.st)
        findViewById<EditText>(R.id.editTextGroupCode).setText(qrObject.gn)
        findViewById<EditText>(R.id.editTextGroupType).setText(qrObject.gl)
        findViewById<EditText>(R.id.editTextSubject).setText(qrObject.sub)
        findViewById<EditText>(R.id.editTextData).setText(qrObject.data)
        findViewById<EditText>(R.id.editTextTime).setText(qrObject.time)
        findViewById<EditText>(R.id.editQRCodeX).setText(qrObject.qx)
        findViewById<EditText>(R.id.editQRCodeY).setText(qrObject.qy)
        findViewById<EditText>(R.id.editQRCodeLength).setText(qrObject.ql)


        generateButton.setOnClickListener {
            generateQRCode()
        }
    }

    private fun generateQRCode() {


        val qrObject = QRObject(
            p = findViewById<EditText>(R.id.editTextPage).text.toString(),
            psx = Integer.parseInt(findViewById<EditText>(R.id.editTextPageSizeX).text.toString()),
            psy = Integer.parseInt(findViewById<EditText>(R.id.editTextPageSizeY).text.toString()),
            pn = findViewById<EditText>(R.id.editTextPageNumber).text.toString(),
            sc = findViewById<EditText>(R.id.editTextSchoolCode).text.toString(),
            gr = findViewById<EditText>(R.id.editTextGradeCode).text.toString(),
            cl = findViewById<EditText>(R.id.editTextClassCode).text.toString(),
            ca = findViewById<EditText>(R.id.editTextCameraSize).text.toString(),
            au = findViewById<EditText>(R.id.editTextDataSource).text.toString(),
            te = findViewById<EditText>(R.id.editTextTeacherCode).text.toString(),
            st = findViewById<EditText>(R.id.editTextStudentCode).text.toString(),
            gn = findViewById<EditText>(R.id.editTextGroupCode).text.toString(),
            gl = findViewById<EditText>(R.id.editTextGroupType).text.toString(),
            sub = findViewById<EditText>(R.id.editTextSubject).text.toString(),
            data = findViewById<EditText>(R.id.editTextData).text.toString(),
            time = findViewById<EditText>(R.id.editTextTime).text.toString(),
            qx = Integer.parseInt(findViewById<EditText>(R.id.editQRCodeX).text.toString()),
            qy = Integer.parseInt(findViewById<EditText>(R.id.editQRCodeY).text.toString()),
            ql = Integer.parseInt(findViewById<EditText>(R.id.editQRCodeLength).text.toString()),
        )

        val gson = Gson()
        val qrJson = gson.toJson(qrObject)

        val qrCodeWidth = 150
        val qrCodeHeight = 150
        val qrBitmap = generateQRCodeBitmap(qrJson, qrCodeWidth, qrCodeHeight)

        val intent = Intent(this, ImgActivity::class.java)

            if (qrBitmap != null) {
                BitmapCacheManager.putBitmap("QRBitmap", qrBitmap)
            }

        startActivity(intent)
        //拼接图片
//        if(qrBitmap!=null) {
//            val combinedBitmap = overlayBitmaps(bitmap, qrBitmap)
//            //跳转活动，传递bitmap参数
//            val intent = Intent(this, ImgActivity::class.java)
//
//            if (combinedBitmap != null) {
//                BitmapCacheManager.putBitmap("QRBitmap", combinedBitmap)
//            }
//
//            startActivity(intent)
//        }



    }

    private fun generateQRCodeBitmap(text: String, width: Int, height: Int): Bitmap? {
        try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)

            val qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    qrBitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            return qrBitmap
        } catch (e: WriterException) {
            Log.e("QRCode", "生成二维码时出错: ${e.message}")
            Toast.makeText(this, "生成二维码时出错", Toast.LENGTH_SHORT).show()
        }
        return null
    }

}

private fun overlayBitmaps(bitmap: Bitmap, qrbitmap: Bitmap): Bitmap {
    val combinedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val canvas = Canvas(combinedBitmap)

    // 绘制原始bitmap
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    canvas.drawBitmap(bitmap, rect, rect, null)

    // 计算qrbitmap应该放置的位置（左上角）
    val qrX = 0
    val qrY = 0

    // 绘制qrbitmap
    canvas.drawBitmap(qrbitmap, qrX.toFloat(), qrY.toFloat(), null)

    return combinedBitmap
}

