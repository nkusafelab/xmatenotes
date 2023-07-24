package com.example.xmatenotes

import android.Manifest
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.king.mlkit.vision.camera.AnalyzeResult
import com.king.mlkit.vision.camera.CameraScan
import com.king.mlkit.vision.camera.analyze.Analyzer
import com.king.mlkit.vision.camera.util.LogUtils
import com.king.mlkit.vision.camera.util.PermissionUtils
import com.king.mlkit.vision.camera.util.PointUtils
import com.king.opencv.qrcode.OpenCVQRCodeDetector
import com.king.view.viewfinderview.ViewfinderView
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity
import com.king.wechat.qrcode.scanning.analyze.WeChatScanningAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.OpenCV

/**
 * 微信二维码扫描实现示例
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
class WeChatQRCodeActivity : WeChatCameraScanActivity() {

    private lateinit var ivResult: ImageView
    private lateinit var viewfinderView: ViewfinderView

    /**
     * OpenCVQRCodeDetector
     */
    private val openCVQRCodeDetector by lazy {
        OpenCVQRCodeDetector()
    }
//    private val openCVQRCodeDetector  = OpenCVQRCodeDetector()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

//        // 初始化OpenCV
//        OpenCV.initAsync(this)
//        // 初始化WeChatQRCodeDetector
//        WeChatQRCodeDetector.init(this)
    }

    private fun getContext() = this


    override fun initUI() {
        super.initUI()
        ivResult = findViewById(R.id.ivResult)
        viewfinderView = findViewById(R.id.viewfinderView)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ScannerActivity.REQUEST_CODE_REQUEST_EXTERNAL_STORAGE && PermissionUtils.requestPermissionsResult(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                permissions,
                grantResults
            )
        ) {
            startPickPhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ScannerActivity.REQUEST_CODE_QRCODE -> processQRCodeResult(data)
                ScannerActivity.REQUEST_CODE_PICK_PHOTO -> processPickPhotoResult(data)
            }
        }
    }

    /**
     * 处理选择图片后，从图片中检测二维码结果
     */
    private fun processPickPhotoResult(data: Intent?) {
        data?.let {
            try {
                lifecycleScope.launch {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it.data)
                    val result = withContext(Dispatchers.IO) {
                        // 通过WeChatQRCodeDetector识别图片中的二维码
                        WeChatQRCodeDetector.detectAndDecode(bitmap)
                    }
                    if (result.isNotEmpty()) {// 不为空，则表示识别成功
                        // 打印所有结果
                        for ((index, text) in result.withIndex()) {
                            LogUtils.d("result$index:$text")
                        }
                        // 一般需求都是识别一个码，所以这里取第0个就可以；有识别多个码的需求，可以取全部
                        Toast.makeText(getContext(), result[0], Toast.LENGTH_SHORT).show()
                    } else {
                        // 为空表示识别失败
                        LogUtils.d("result = null")
                    }
                }
            } catch (e: Exception) {
                LogUtils.w(e)
            }

        }
    }

    private fun processQRCodeResult(intent: Intent?) {
        // 扫码结果
        CameraScan.parseScanResult(intent)?.let {
            Log.d(CameraScan.SCAN_RESULT, it)
            Toast.makeText(getContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickCameraClicked() {

    }

    private fun pickPhotoClicked() {
        if (PermissionUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startPickPhoto()
        } else {
            PermissionUtils.requestPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                ScannerActivity.REQUEST_CODE_REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    private fun startPickPhoto() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(pickIntent, ScannerActivity.REQUEST_CODE_PICK_PHOTO)
    }

    private fun startActivityForResult(clazz: Class<*>) {
        val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.alpha_in, R.anim.alpha_out)
        startActivityForResult(Intent(this, clazz), ScannerActivity.REQUEST_CODE_QRCODE, options.toBundle())
    }

    fun onClick(view: View) {
        when (view.id) {
//            R.id.btnWeChatQRCodeScan -> startActivityForResult(WeChatQRCodeActivity::class.java)
            R.id.back -> finish()
            R.id.Button_scan -> scanAgain()
            R.id.Button_photo -> pickCameraClicked()
            R.id.Button_select -> pickPhotoClicked()
//            R.id.btnWeChatMultiQRCodeScan -> startActivityForResult(WeChatMultiQRCodeActivity::class.java)
//            R.id.btnWeChatQRCodeDecode -> pickPhotoClicked(true)
//            R.id.btnOpenCVQRCodeScan -> startActivityForResult(OpenCVQRCodeActivity::class.java)
//            R.id.btnOpenCVQRCodeDecode -> pickPhotoClicked(false)
        }
    }

    fun scanAgain(){
        viewfinderView.showScanner()
        ivResult.setImageBitmap(null)
        cameraScan.setAnalyzeImage(true) // 继续扫码分析
    }

    override fun onScanResultCallback(result: AnalyzeResult<List<String>>) {
        // 停止分析
        cameraScan.setAnalyzeImage(false)
        Log.d(TAG, result.result.toString())

        // 当初始化 WeChatScanningAnalyzer 时，如果是需要二维码的位置信息，则会返回 WeChatScanningAnalyzer.QRCodeAnalyzeResult
        if (result is WeChatScanningAnalyzer.QRCodeAnalyzeResult) { // 如果需要处理结果二维码的位置信息
            //取预览当前帧图片并显示，为结果点提供参照
            ivResult.setImageBitmap(previewView.bitmap)
            val points = ArrayList<Point>()
            result.points?.forEach { mat ->
                // 扫码结果二维码的四个点（一个矩形）
                Log.d(TAG, "point0: ${mat[0, 0][0]}, ${mat[0, 1][0]}")
                Log.d(TAG, "point1: ${mat[1, 0][0]}, ${mat[1, 1][0]}")
                Log.d(TAG, "point2: ${mat[2, 0][0]}, ${mat[2, 1][0]}")
                Log.d(TAG, "point3: ${mat[3, 0][0]}, ${mat[3, 1][0]}")

                val point0 = Point(mat[0, 0][0].toInt(), mat[0, 1][0].toInt())
                val point1 = Point(mat[1, 0][0].toInt(), mat[1, 1][0].toInt())
                val point2 = Point(mat[2, 0][0].toInt(), mat[2, 1][0].toInt())
                val point3 = Point(mat[3, 0][0].toInt(), mat[3, 1][0].toInt())

                val centerX = (point0.x + point1.x + point2.x + point3.x) / 4
                val centerY = (point0.y + point1.y + point2.y + point3.y) / 4

                //将实际的结果中心点坐标转换成界面预览的坐标
                val point = PointUtils.transform(
                    centerX,
                    centerY,
                    result.bitmap.width,
                    result.bitmap.height,
                    viewfinderView.width,
                    viewfinderView.height
                )
                points.add(point)
            }
            //设置Item点击监听
            viewfinderView.setOnItemClickListener {
                //显示点击Item将所在位置扫码识别的结果返回
                val intent = Intent()
                intent.putExtra(CameraScan.SCAN_RESULT, result.result[it])
                setResult(RESULT_OK, intent)
                finish()
            }
            //显示结果点信息
            viewfinderView.showResultPoints(points)

//            if(result.result.size == 1) {
//                val intent = Intent()
//                intent.putExtra(CameraScan.SCAN_RESULT, result.result[0])
//                setResult(RESULT_OK, intent)
//                finish()
//            }
        } else {
            // 一般需求都是识别一个码，所以这里取第0个就可以；有识别多个码的需求，可以取全部
            val intent = Intent()
            intent.putExtra(CameraScan.SCAN_RESULT, result.result[0])
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun createAnalyzer(): Analyzer<MutableList<String>>? {
        // 分析器默认不会返回结果二维码的位置信息
//        return WeChatScanningAnalyzer()
        // 如果需要返回结果二维码位置信息，则初始化分析器时，参数传 true 即可
        return WeChatScanningAnalyzer(true)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_wechat_qrcode
    }

    override fun onBackPressed() {
        if (viewfinderView.isShowPoints) {// 如果是结果点显示时，用户点击了返回键，则认为是取消选择当前结果，重新开始扫码
            ivResult.setImageResource(0)
            viewfinderView.showScanner()
            cameraScan.setAnalyzeImage(true)
            return
        }
        super.onBackPressed()
    }

    companion object {
        const val TAG = "WeChatQRCodeActivity"
        const val REQUEST_CODE_QRCODE = 0x10
        const val REQUEST_CODE_REQUEST_EXTERNAL_STORAGE = 0x11
        const val REQUEST_CODE_PICK_PHOTO = 0x12
    }

}