package com.example.xmatenotes


import android.Manifest
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.widget.*
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.king.mlkit.vision.camera.AnalyzeResult
import com.king.mlkit.vision.camera.CameraScan
import com.king.mlkit.vision.camera.analyze.Analyzer
import com.king.mlkit.vision.camera.util.LogUtils
import com.king.mlkit.vision.camera.util.PermissionUtils
import com.king.mlkit.vision.camera.util.PointUtils
import com.king.opencv.qrcode.OpenCVQRCodeDetector
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity
import com.king.wechat.qrcode.scanning.analyze.WeChatScanningAnalyzer
import com.lark.oapi.Client
import com.lark.oapi.core.request.RequestOptions
import com.lark.oapi.service.bitable.v1.model.ListAppTableRecordReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * 微信二维码扫描实现示例
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
class WeChatQRCodeActivity : WeChatCameraScanActivity() {

    private lateinit var ivResult: ImageView
    private lateinit var viewfinderView: QRViewfinderView

    private lateinit var imageUri:Uri
    private lateinit var outputImage: File
    private lateinit var mOrientationListener: OrientationEventListener

    /**
     * 上次识别出结果时间
     */
    private var timeLastUpDate : Long = 0

    private var nFlags = 0


    private var codeString:String = "前置编码:"
    private var subjectString:String = "学科名:"
    private var unitString:String = "单元名:"
    private var stageString:String = "阶段:"
    private var classString:String = "课时:"
    private var groupString:String = "小组:"
    private var roomString:String = "班级:"
    private var gradeString:String = "年级:"

    private lateinit var codeText:TextView
    private lateinit var subjectText:TextView
    private lateinit var unitText:TextView
    private lateinit var stageText:TextView
    private lateinit var classText:TextView
    private lateinit var weekText:TextView
    private lateinit var groupText:TextView
    private lateinit var roomText:TextView
    private lateinit var gradeText:TextView
    private lateinit var termText:TextView
    private lateinit var dayText:TextView


    private lateinit var backgroundLayout:RelativeLayout

    private lateinit var captureButton:Button

    private lateinit var qrObject: QRObject

    private var map:Map<String,Int> = mapOf("数学" to 0x7F82BB,"语文" to 0xB5E61D,"英语" to 0x9FFCFD,
        "物理" to 0xEF88BE,"化学" to 0xFFFD55,"生物" to 0x58135E,"政治" to 0x16417C)
    /**
     * 拍摄按键状态
     */
    private var keyDown:Boolean = false

    /**
     * OpenCVQRCodeDetector
     */
    private val openCVQRCodeDetector by lazy {
        OpenCVQRCodeDetector()
    }
//    private val openCVQRCodeDetector  = OpenCVQRCodeDetector()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
          // 初始化OpenCV
//        OpenCV.initAsync(this)
//        // 初始化WeChatQRCodeDetector
//        WeChatQRCodeDetector.init(this)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //startOrientationChangeListener()

//        Log.d("WeChatQRCodeActivity","ButtonPressed")
    }

//    private fun startOrientationChangeListener() {
//        mOrientationListener = object : OrientationEventListener(this) {
//            override fun onOrientationChanged(rotation: Int) {
//                Log.e(TAG, " $rotation")
//
//                // 计算偏移角度
//                val absRotation = if (rotation > 180) 360 - rotation else rotation
//
//                if (absRotation > 15&&nFlags== 0 ) {
//                    Toast.makeText(applicationContext, "偏转角度大于15度，请调整相机！", Toast.LENGTH_SHORT).show()
//                    nFlags=1
//                }
//                if(absRotation < 15){
//                    nFlags=0
//                }
//
//
//            }
//        }
//        mOrientationListener.enable()
//    }

    private fun getContext() = this

    override fun initUI() {
        super.initUI()
        ivResult = findViewById(R.id.ivResult)
        viewfinderView = findViewById(R.id.QRViewfinderView) //p-oijhgvcvxfu2
        viewfinderView.isShowScanner = false

        Thread {
            while (true) {
                if (System.currentTimeMillis() - timeLastUpDate > 500) {
                    viewfinderView.isShowPoints = false
                }
            }
        }.start()

        codeText = findViewById(R.id.code_text)
        subjectText= findViewById(R.id.subject_text)
        unitText= findViewById(R.id.unit_text)
        stageText = findViewById(R.id.stage_text)
        classText = findViewById(R.id.class_text)
        weekText= findViewById(R.id.week_text)
        groupText = findViewById(R.id.group_text)
        roomText= findViewById(R.id.room_text)
        gradeText = findViewById(R.id.grade_text)
        termText= findViewById(R.id.term_text)
        dayText = findViewById(R.id.day_text)

        backgroundLayout = findViewById<RelativeLayout>(R.id.left_layout)

        captureButton= findViewById<Button>(R.id.btnCapture)

        captureButton.setOnClickListener(View.OnClickListener {

            if(keyDown){
                scanAgain()
                keyDown = false
            } else {
                keyDown = true
            }

            //此处写点击响应
//            setCodeText("01")
//            setUnitText("二元一次方程")
//            setStageText("期中")
//            setClassText("4")
//            setWeekText("八")
//            setGroupText("10")
//            setRoomText("1")
//            setGradeText("6")
//            setDayText("20230731")
//            backgroundColorChange("英语")
//            Log.d("WeChatQRCodeActivity","ButtonPressed")
        })

//        setCodeText("01")
//        setSubjectText("语文")
//        setUnitText("智取生辰纲")
//        setStageText("期中")
//        setClassText("4课时")
//        setWeekText("八")
//        setGroupText("10")
//        setRoomText("1")
//        setGradeText("6")
//        setDayText("20230729")
        Thread {
            while (true) {
                getSubject(object : CallBack {
                    override fun onCallBack(string: String) {
                        backgroundColorChange(string)
                    }
                })

                runOnUiThread {
                    setDayText(getTime())
                }

                Thread.sleep(1000)
            }
        }.start()

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
                REQUEST_CODE_TAKE_PHOTO -> processTakePhotoResult()

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
                        Toast.makeText(getContext(),"未识别到二维码", Toast.LENGTH_SHORT).show()
                    }
                    // 检测结果：二维码的位置信息
                    val points = ArrayList<Mat>()
                    //通过WeChatQRCodeDetector识别图片中的二维码并返回二维码的位置信息
                    val results = WeChatQRCodeDetector.detectAndDecode(bitmap, points)
                    points.forEach { mat ->
                        // 扫码结果二维码的四个点（一个矩形）
                        Log.d(TAG, "point0: ${mat[0, 0][0]}, ${mat[0, 1][0]}")
                        Log.d(TAG, "point1: ${mat[1, 0][0]}, ${mat[1, 1][0]}")
                        Log.d(TAG, "point2: ${mat[2, 0][0]}, ${mat[2, 1][0]}")
                        Log.d(TAG, "point3: ${mat[3, 0][0]}, ${mat[3, 1][0]}")
                    }

//                    // 将 Bitmap 转换为可变的 Mat
//                    val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
//                    Utils.bitmapToMat(bitmap, mat)
//
//                    // 创建一个红色 (BGR 格式，所以红色是 [0, 0, 255])
//                    val redColor = org.opencv.core.Scalar(255.0, 0.0, 0.0)
//
//                    val point1 = OpenCVPoint(mat[0, 0][0], mat[0, 1][0])
//                    val point2 = OpenCVPoint(mat[1, 0][0], mat[1, 1][0])
//                    val point3 = OpenCVPoint(mat[2, 0][0], mat[2, 1][0])
//                    val point4 = OpenCVPoint(mat[3, 0][0], mat[3, 1][0])
//
//                    // 在 Mat 上绘制红线
//                    Imgproc.line(mat, point1, point2, redColor, 5)
//                    Imgproc.line(mat, point2, point3, redColor, 5)
//                    Imgproc.line(mat, point3, point4, redColor, 5)
//                    Imgproc.line(mat, point4, point1, redColor, 5)
//
//
//                    // 将绘制后的 Mat 转换回 Bitmap
//                    Utils.matToBitmap(mat, bitmap)
                    //跳转活动，传递bitmap参数
//                    val intent = Intent(getContext(), CropActivity::class.java)
                    val intent = Intent(getContext(), CardProcessActivity::class.java)
                    BitmapCacheManager.putBitmap("WeChatQRCodeBitmap", bitmap)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                LogUtils.w(e)
            }

        }
    }

    private  fun processTakePhotoResult(){
        lifecycleScope.launch {
            val bitmap=BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))


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
                Toast.makeText(getContext(),"未识别到二维码", Toast.LENGTH_SHORT).show()
            }
            // 检测结果：二维码的位置信息
            val points = ArrayList<Mat>()
            //通过WeChatQRCodeDetector识别图片中的二维码并返回二维码的位置信息
            val results = WeChatQRCodeDetector.detectAndDecode(bitmap, points)
            points.forEach { mat ->
                // 扫码结果二维码的四个点（一个矩形）
                Log.d(TAG, "point0: ${mat[0, 0][0]}, ${mat[0, 1][0]}")
                Log.d(TAG, "point1: ${mat[1, 0][0]}, ${mat[1, 1][0]}")
                Log.d(TAG, "point2: ${mat[2, 0][0]}, ${mat[2, 1][0]}")
                Log.d(TAG, "point3: ${mat[3, 0][0]}, ${mat[3, 1][0]}")
            }
            //跳转活动，传递bitmap参数
            val intent = Intent(getContext(), CropActivity::class.java)
            BitmapCacheManager.putBitmap("WeChatQRCodeBitmap", bitmap)
            startActivity(intent)
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

//        outputImage = File(externalCacheDir,"output_image.jpg")
//        if(outputImage.exists()){
//            outputImage.delete()
//        }
//        outputImage.createNewFile()
//        imageUri = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
//            FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)
//        }else{
//            Uri.fromFile(outputImage)
//        }
//        //打开相机
//        val cameraIntent = Intent("android.media.action.IMAGE_CAPTURE")
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
//        startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO)
        val cameraIntent = Intent(this,OpenCameraActivity::class.java)
        startActivity(cameraIntent)

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
            //R.id.back -> finish()
            R.id.Button_scan -> scanAgain()
            R.id.Button_photo -> pickCameraClicked()
            //R.id.Button_select -> pickPhotoClicked()
//            R.id.btnWeChatMultiQRCodeScan -> startActivityForResult(WeChatMultiQRCodeActivity::class.java)
//            R.id.btnWeChatQRCodeDecode -> pickPhotoClicked(true)
//            R.id.btnOpenCVQRCodeScan -> startActivityForResult(OpenCVQRCodeActivity::class.java)
//            R.id.btnOpenCVQRCodeDecode -> pickPhotoClicked(false)
        }
    }

    fun scanAgain(){
//        viewfinderView.showScanner()
        ivResult.setImageBitmap(null)
        cameraScan.setAnalyzeImage(true) // 继续扫码分析
    }

    override fun onScanResultCallback(result: AnalyzeResult<List<String>>) {
        // 停止分析
        if(keyDown){
            cameraScan.setAnalyzeImage(false)
        }
        Log.d(TAG, result.result.toString())

        // 当初始化 WeChatScanningAnalyzer 时，如果是需要二维码的位置信息，则会返回 WeChatScanningAnalyzer.QRCodeAnalyzeResult
        if (result is WeChatScanningAnalyzer.QRCodeAnalyzeResult) { // 如果需要处理结果二维码的位置信息
//        if (result is OpenCVScanningAnalyzer.QRCodeAnalyzeResult) { // 如果需要处理结果二维码的位置信息
            //取预览当前帧图片并显示，为结果点提供参照
//            ivResult.setImageBitmap(previewView.bitmap)
            var bitMapPoints = ArrayList<org.opencv.core.Point>()
            var viewfinderViewPoints = ArrayList<android.graphics.Point>()
//            val resultOpenCV = withContext(Dispatchers.IO) {
//                // 通过OpenCVQRCodeDetector识别图片中的二维码
//                openCVQRCodeDetector.detectAndDecode(result.bitmap)
//            }
//
//            if (!resultOpenCV.isNullOrEmpty()) {// 不为空，则表示识别成功
//                LogUtils.d("result$result")
//                Toast.makeText(getContext(), resultOpenCV, Toast.LENGTH_SHORT).show()
//            } else {
//                // 为空表示识别失败
//                LogUtils.d("result = null")
//            }
            val gson = Gson()
            var isQRPToPageP = true //是否将二维码四角点坐标转换为版面四角点坐标
            result.result.forEach {
                //将二维码字符串解析为数据对象
                try {
                    qrObject = gson.fromJson<QRObject>(it, QRObject::class.java)
//                setTopActionBar()
                    setOrganInfor(null, qrObject.gn, qrObject.cl, qrObject.gr, null, getTime())
                } catch (e: JsonSyntaxException) {
                    println("JSON解析失败: ${e.message}")
                    isQRPToPageP = false
                }
            }

            result.points?.forEach { mat ->
                bitMapPoints.addAll(processWeChatMat(mat, result, isQRPToPageP))
                viewfinderViewPoints.addAll(transformPoint(bitMapPoints, result.bitmap.width, result.bitmap. height, viewfinderView.width, viewfinderView.height))
            }
//            for (i in 0 until result.points.rows()) {
//                result.points.row(i).let { mat ->
//                    bitMapPoints.addAll(processOpenCVMat(mat, result, isQRPToPageP))
//                    viewfinderViewPoints.addAll(transformPoint(bitMapPoints, result.bitmap.width, result.bitmap. height, viewfinderView.width, viewfinderView.height))
//                }
//            }
            if(keyDown){
                var desPoints = ArrayList<org.opencv.core.Point>()
                var srcPWidth = qrObject.psx.toInt()
                var srcPHeight = qrObject.psy.toInt()
//                var srcVWidth = previewView.bitmap?.width
//                var srcVHeight = previewView.bitmap?.height
                var srcVWidth = viewfinderView.width
                var srcVHeight = viewfinderView.height
                Log.e(TAG, "result.bitmap.width: "+(result.bitmap.width) +" viewfinderView.width: "+viewfinderView.width+" previewView.bitmap?.width: "+previewView.bitmap?.width+" ivResult.width: "+ivResult.width)
                Log.e(TAG, "result.bitmap.height: "+(result.bitmap.height) +" viewfinderView.height: "+viewfinderView.height+" previewView.bitmap?.height: "+previewView.bitmap?.height+" ivResult.height: "+ivResult.height)

                var desWidth : Int = srcPWidth
                var desHeight: Int = srcPHeight
                if ((srcPWidth / srcPHeight) > (srcVWidth!! / srcVHeight!!)) {
                    desWidth = srcVWidth
                    desHeight = desWidth*srcPHeight/srcPWidth
                    desPoints.add(Point(0.0, ((srcVHeight-desHeight)/2).toDouble()))
                    desPoints.add(Point(desWidth.toDouble(), ((srcVHeight-desHeight)/2).toDouble()))
                    desPoints.add(Point(desWidth.toDouble(), ((srcVHeight-desHeight)/2+desHeight).toDouble()))
                    desPoints.add(Point(0.0, ((srcVHeight-desHeight)/2).toDouble()))
                } else {
                    desWidth = srcVHeight*srcPWidth/srcPHeight
                    desHeight = srcVHeight
                    desPoints.add(Point(((srcVWidth-desWidth)/2).toDouble(), 0.0))
                    desPoints.add(Point(((srcVWidth-desWidth)/2+desWidth).toDouble(), 0.0))
                    desPoints.add(Point(((srcVWidth-desWidth)/2+desWidth).toDouble(), desHeight.toDouble()))
                    desPoints.add(Point(((srcVWidth-desWidth)/2).toDouble(), desHeight.toDouble()))
                }
                var bMap = result.bitmap?.let { warpPerspective(it, bitMapPoints, desPoints, srcVWidth, srcVHeight) }
                ivResult.setImageBitmap(bMap)
                if (bMap != null) {
                    viewfinderView.isShowPoints = false
                    //截屏
                    // 找到当前Activity的根视图
                    val rootView = window.decorView.rootView
                    // 创建一个Bitmap，大小为要截屏的View的宽高
                    val bitmap = Bitmap.createBitmap(
                        rootView.getWidth(),
                        rootView.getHeight(),
                        Bitmap.Config.ARGB_8888
                    )
                    // 将View绘制到Bitmap上
                    rootView.draw(Canvas(bitmap))
//                    ivResult.setImageBitmap(bitmap)
                    BitmapCacheManager.putBitmap("WeChatQRCodeBitmap",bitmap)
                    val intent = Intent(getContext(), CardProcessActivity::class.java)
                    startActivity(intent)
                }

            } else {
                viewfinderView.setPointRadius(5.0f)
                //显示结果点信息
                viewfinderView.showResultPoints(viewfinderViewPoints)
                timeLastUpDate = System.currentTimeMillis()
            }


            //设置Item点击监听
            viewfinderView.setOnItemClickListener {
                //显示点击Item将所在位置扫码识别的结果返回
                val intent = Intent()
                intent.putExtra(CameraScan.SCAN_RESULT, result.result[it])
                setResult(RESULT_OK, intent)
                finish()
            }


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

    fun processOpenCVMat(mat:Mat, result: AnalyzeResult<List<String>>, isQRPToPageP: Boolean) : ArrayList<org.opencv.core.Point> {

        var points = openCVMatToPoints(mat)

        if(isQRPToPageP){
            QRPointsToPagePoints(points, qrObject, result.bitmap.width, result.bitmap.height)
        }

//        val centerPonit = calculateCenterPoint(points)

        //将实际的结果中心点坐标转换成界面预览的坐标
//                    val point = PointUtils.transform(
//                        centerX,
//                        centerY,
//                        result.bitmap.width,
//                        result.bitmap.height,
//                        viewfinderView.width,
//                        viewfinderView.height
//                    )

        return points
    }

    fun openCVMatToPoints(mat: Mat): ArrayList<org.opencv.core.Point> {
        var points = ArrayList<org.opencv.core.Point>()
        // 扫码结果二维码的四个点（一个四边形）；需要注意的是：OpenCVQRCode识别的二维码和WeChatQRCode的识别的二维码记录在Mat中的点位方式是不一样的
        for( i in 0..3){
            points.add(org.opencv.core.Point(mat[0, i][0], mat[0, i][1]))
            Log.d(OpenCVQRCodeActivity.TAG, "point$i: ${mat[0, i][0]}, ${mat[0, i][1]}")
        }
        return points
    }

    fun processWeChatMat(mat:Mat, result: AnalyzeResult<List<String>>, isQRPToPageP: Boolean) : ArrayList<org.opencv.core.Point> {

        var points = weChatMatToPoints(mat)

        if(isQRPToPageP){
            QRPointsToPagePoints(points, qrObject, result.bitmap.width, result.bitmap.height)
        }

//        val centerPonit = calculateCenterPoint(points)

        //将实际的结果中心点坐标转换成界面预览的坐标
//                    val point = PointUtils.transform(
//                        centerX,
//                        centerY,
//                        result.bitmap.width,
//                        result.bitmap.height,
//                        viewfinderView.width,
//                        viewfinderView.height
//                    )
        return points
    }

    fun weChatMatToPoints(mat: Mat): ArrayList<org.opencv.core.Point> {
        var points = ArrayList<org.opencv.core.Point>()
        // 扫码结果二维码的四个点（一个矩形）
        for( i in 0..3){
            points.add(org.opencv.core.Point(mat[i, 0][0], mat[i, 1][0]))
            Log.d(TAG, "point$i: ${mat[i, 0][0]}, ${mat[i, 1][0]}")
        }
        return points
    }

    /**
     * 计算中心点
     */
    fun calculateCenterPoint(points: ArrayList<android.graphics.Point>): android.graphics.Point {
        var centerX = 0;
        var centerY = 0;
        for(point in points){
            centerX += point.x
            centerY += point.y
        }
        return android.graphics.Point(centerX/points.size, centerY/points.size)
    }

    /**
     * 将bitmap上的坐标转换为viewfinderView上的坐标,以生成新Point的方式返回,不改变传入的points
     */
    fun transformPoint(points: ArrayList<org.opencv.core.Point>, srcWidth: Int, srcHeight: Int, desWidth: Int, desHeight: Int): ArrayList<android.graphics.Point> {
        var newPoints = ArrayList<android.graphics.Point>()
        for( point in points){
            newPoints.add(PointUtils.transform(point.x.toInt(), point.y.toInt(), srcWidth, srcHeight, desWidth, desHeight))
        }
        return newPoints
    }

    /**
     * 二维码四角坐标转换为版面四角坐标,前提是识别出的二维码信息是有效的;会改变传入的points
     */
    fun QRPointsToPagePoints(points: ArrayList<org.opencv.core.Point>, qrO: QRObject, width: Int, height: Int): ArrayList<org.opencv.core.Point>? {
        if(qrO != null){
            var offSetX = Integer.parseInt(qrO.qx)
            var offSetY = Integer.parseInt(qrO.qy)

            var ratioOffX : Double = offSetX * (1.0 / Integer.parseInt(qrO.ql))
            var ratioOffY : Double = offSetY * (1.0 / Integer.parseInt(qrO.ql))

            var pOff1X : Double = points.get(1).x
            var pOff1Y : Double = points.get(1).y
            pOff1X = (pOff1X - points.get(0).x) * ratioOffX
            pOff1Y = (pOff1Y - points.get(0).y) * ratioOffX

            var pOff3X : Double = points.get(3).x
            var pOff3Y : Double = points.get(3).y
            pOff3X = (pOff3X - points.get(0).x) * ratioOffY
            pOff3Y = (pOff3Y - points.get(0).y) * ratioOffY
            var pOff2X = pOff3X + pOff1X
            var pOff2Y = pOff3Y + pOff1Y

            //平移至版面左上角
            for(i in 0..3){
                points.get(i).x -= pOff2X
                points.get(i).y -= pOff2Y
            }

            var ratioX : Double = Integer.parseInt(qrO.psx) *(1.0/ Integer.parseInt(qrO.ql))
            var ratioY : Double = Integer.parseInt(qrO.psy) *(1.0/ Integer.parseInt(qrO.ql))
//            var ratioX : Float = 6.296296f
//            var ratioY : Float = 8.481481f

            var point0 = points.get(0)
            var p1 = points.get(1)
            p1.x = (p1.x - point0.x)*ratioX + point0.x
            p1.y = (p1.y - point0.y)*ratioX + point0.y

            var p3 = points.get(3)
            p3.x = (p3.x - point0.x)*ratioY + point0.x
            p3.y = (p3.y - point0.y)*ratioY + point0.y

            var p2 = points.get(2)
            p2.x = p3.x + p1.x - point0.x
            p2.y = p3.y + p1.y - point0.y

//            var index : Int = 0
//            for(p in points){
//                if(contain(p,width, height)){
//                    break
//                }
//                index++
//            }
//            if(index == 4){
//                Log.e(TAG, "QRPointsToPagePoints: "+"所有点都在屏幕外")
//                return null
//            }else {
//                for(i in (index+1)..(index+3)){
//                    if(!contain(points.get(index%4)))
//                }
//            }

        }
        return points
    }

    /**
     * 目标区域是否包含目标点
     */
    fun contain(point: org.opencv.core.Point, width: Int, height: Int) : Boolean {
        if(point.x.toInt() in 0..width && point.y.toInt() in 0..height){
            return true
        }
        return false
    }

    /**
     * 透视变换
     */
    fun warpPerspective(bitmap:Bitmap, srcPoints:ArrayList<org.opencv.core.Point>, desPoints: ArrayList<org.opencv.core.Point>, width: Int, height: Int): Bitmap {
        //定义原图中边框上的四个点
        val srcPoints = MatOfPoint2f(
            srcPoints.get(0), // 左上角
            srcPoints.get(1), // 右上角
            srcPoints.get(2), // 右下角
            srcPoints.get(3)  // 左下角
        )

        // 定义目标图中的边框上的四个点
//        val dstWidth = bitmap.width
//        val dstHeight = bitmap.height
        val dstWidth = width
        val dstHeight = height
        val dstPoints = MatOfPoint2f(
            desPoints.get(0),
            desPoints.get(1),
            desPoints.get(2),
            desPoints.get(3)
        )

        // 计算透视变换矩阵
        val perspectiveTransform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

        // 进行透视变换
        val transformedMat = Mat()
        val inputMatResult = Mat()
        Utils.bitmapToMat(bitmap, inputMatResult)
        Imgproc.warpPerspective(inputMatResult, transformedMat, perspectiveTransform, Size(dstWidth.toDouble(), dstHeight.toDouble()))

//         将 Mat 转换回 Bitmap
        val resultBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(transformedMat, resultBitmap)

        return resultBitmap
    }

    override fun createAnalyzer(): Analyzer<MutableList<String>>? {
        // 分析器默认不会返回结果二维码的位置信息
//        return WeChatScanningAnalyzer()
        // 如果需要返回结果二维码位置信息，则初始化分析器时，参数传 true 即可
        return WeChatScanningAnalyzer(true)
//        return return OpenCVScanningAnalyzer(true)
    }

    override fun getLayoutId(): Int {
//        return R.layout.activity_wechat_qrcode
        return R.layout.activity_opencamera
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

    private var client: Client= Client.newBuilder("cli_a4ac1c99553b9013", "dJnppJxBQQKd4QGmXSrP3fwdvcT5iNZ6").build()

    interface CallBack {
        fun onCallBack(string: String)
    }
    /**
     * 从多维表格获取学科
     */
    fun getSubject(callBack: CallBack) {

        // 构建client
//        client = Client.newBuilder("cli_a4ac1c99553b9013", "dJnppJxBQQKd4QGmXSrP3fwdvcT5iNZ6").build()

        Thread {
            // 创建请求对象
            val req = ListAppTableRecordReq.newBuilder()
                .appToken("bascn3zrUMtRbKme8rlcyRKfDSc")
                .tableId("tblpAZmppl1siFd7")
                .viewId("vewCJXERGG")
                .filter("AND(CurrentValue.[起始时间]<NOW(),NOW()<CurrentValue.[结束时间])")
                .pageSize(20)
                .build()

            // 发起请求

            // 发起请求
            val resp = client.bitable().appTableRecord().list(
                req, RequestOptions.newBuilder()
                    .build()
            )

            // 处理服务端错误
            if (!resp.success()) {
                println(
                    String.format(
                        "code:%s,msg:%s,reqId:%s",
                        resp.code,
                        resp.msg,
                        resp.requestId
                    )
                )
            }

            callBack.onCallBack(resp.getData().getItems().get(0).getFields().get("科目").toString())

        }.start()

    }

    /**
     * 获取当前时间并转换为特定格式
     */
    fun getTime() : String {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val datetime: Date = Date(System.currentTimeMillis())
        return sdf.format(datetime)
    }

    fun setCodeText(string:String){
        codeText.text = codeString+string
    }
    fun setSubjectText(string:String){
        subjectText.text = subjectString+string
    }
    fun setUnitText(string:String){
        unitText.text = unitString+string
    }
    fun setStageText(string:String){
        stageText.text = stageString+string
    }
    fun setClassText(string:String){
        classText.text = classString+string
    }
    fun setWeekText(string:String){
        weekText.text = "第"+string+"周"
    }
    fun setGroupText(string:String){
        groupText.text = groupString+string
    }
    fun setRoomText(string:String){
        roomText.text = roomString+string
    }
    fun setGradeText(string:String){
        gradeText.text = gradeString+string
    }
    fun setTermText(string:String){
        termText.text = string
    }

    fun setDayText(string: String){
        dayText.text = string
    }

    /**
     * 设置顶部学科栏
     */
    fun setTopActionBar(code: String, subject: String, unit: String, stage: String, classTime: String) {
        if(code != null){
            setCodeText(code)
        }
        if(subject != null){
            setSubjectText(subject)
        }
        if(unit != null){
            setUnitText(unit)
        }
        if(stage != null){
            setStageText(stage)
        }
        if(classTime != null){
            setClassText(classTime)
        }
    }

    /**
     * 设置组织信息
     */
    fun setOrganInfor(week: String?, group: String, room: String, grade: String, term: String?, day: String) {
        if(week != null){
            setWeekText(week)
        }
        if(group != null){
            setGroupText(group)
        }
        if(room != null){
            setRoomText(room)
        }
        if(grade != null){
            setGradeText(grade)
        }
        if(term != null){
            setTermText(term)
        }
        if(day != null){
            setDayText(day)
        }
    }

    /**
     * 改变背景颜色按钮颜色响应
     */
    fun backgroundColorChange(string: String){
        runOnUiThread {
            var drawable: Drawable = getResources().getDrawable(R.drawable.camera_background)
            if (map[string] != null) {
                var red: Int = map[string]!! and 0xff0000 shr 16
                var green: Int = map[string]!! and 0x00ff00 shr 8
                var blue: Int = map[string]!! and 0x0000ff
                drawable.setColorFilter(Color.rgb(red, green, blue), PorterDuff.Mode.SRC_ATOP)

                //backgroundLayout.setBackgroundDrawable(drawable)
                backgroundLayout.background = drawable

                setSubjectText(string)

                captureButton.text = string

                val background: Drawable = captureButton.getBackground().mutate()
                var offset = 50 //颜色整体偏移量
                background.colorFilter = PorterDuffColorFilter(
                    Color.rgb(
                        if ((red + offset) > 0xFF) 0xFF else red + offset,
                        if ((green + offset) > 0xFF) 0xFF else green + offset,
                        if ((blue + offset) > 0xFF) 0xFF else blue + offset
                    ), PorterDuff.Mode.SRC_IN
                )
                captureButton.setBackground(background)
            }
        }

    }

    companion object {
        const val TAG = "WeChatQRCodeActivity"
        const val REQUEST_CODE_QRCODE = 0x10
        const val REQUEST_CODE_REQUEST_EXTERNAL_STORAGE = 0x11
        const val REQUEST_CODE_PICK_PHOTO = 0x12
        const val REQUEST_CODE_TAKE_PHOTO = 0x13
    }

}