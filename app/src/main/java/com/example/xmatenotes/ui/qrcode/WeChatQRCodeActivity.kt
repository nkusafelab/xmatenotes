package com.example.xmatenotes.ui.qrcode


import android.Manifest
import android.content.Intent
import android.graphics.*
import android.graphics.Rect
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
import com.example.xmatenotes.*
import com.example.xmatenotes.logic.manager.Storager
import com.example.xmatenotes.logic.model.Page.Card
import com.example.xmatenotes.logic.model.Page.QRObject
import com.example.xmatenotes.ui.qrcode.CircleRunnable.CircleCallBack
import com.example.xmatenotes.util.LogUtil
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
import com.lark.oapi.Client
import com.lark.oapi.core.request.RequestOptions
import com.lark.oapi.core.utils.Jsons
import com.lark.oapi.service.bitable.v1.model.ListAppTableRecordReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Point
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


    private var preCodeString:String = "前置编码:"
    private var postCodeString:String = "后置编码:"
    private var subjectString:String = "学科名:"
    private var unitString:String = "单元名:"
    private var stageString:String = "阶段:"
    private var classString:String = "课时:"
    private var groupString:String = "小组:"
    private var roomString:String = "班级:"
    private var gradeString:String = "年级:"

    private lateinit var preCodeText:TextView
    private lateinit var postCodeText:TextView
    private lateinit var subjectText:TextView
    private lateinit var unitText:TextView
    private lateinit var stageText:TextView
    private lateinit var classTimeText:TextView
    private lateinit var weekText:TextView
    private lateinit var groupText:TextView
    private lateinit var roomText:TextView
    private lateinit var gradeText:TextView
    private lateinit var termText:TextView
    private lateinit var dayText:TextView


    private lateinit var backgroundLayout:RelativeLayout

    private lateinit var captureButton:Button

    private lateinit var qrObject: QRObject

    private var cardData = Card()//卡片数据

    /**
     * 拍摄按键状态
     */
    private var keyDown:Boolean = false

    private lateinit var circleRunnable : CircleRunnable

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

        preCodeText = findViewById(R.id.precode_text)
        postCodeText = findViewById(R.id.postcode_text)
        subjectText= findViewById(R.id.subject_text)
        unitText= findViewById(R.id.unit_text)
        stageText = findViewById(R.id.stage_text)
        classTimeText = findViewById(R.id.classtime_text)
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

        })

        runOnUiThread {
            setTopActionBar(null, null,null, "一元一次不等式", "整体认知构建", "2")
            setOrganInfor(null, cardData.getGroup(), cardData.getClassG(), cardData.getGrade(),"秋季学期","20230911")
            LogUtil.e(TAG, "初始化卡片完成")
        }

        cardData.init()
        LogUtil.e(TAG, "初始化cardData完成")

        circleRunnable = CircleRunnable(object : CircleCallBack {

            override fun stopableCallBack() {
                updateData()
            }

            override fun circleCallBack() {
                runOnUiThread {
                    setDayText(getTime())
                }
            }

        })
        Thread(circleRunnable).start()

    }

    fun updateData(){

        getSubject(object : CallBack {
            override fun onCallBack(subjectMap: MutableMap<String, Any>) {
                if(circleRunnable.isAlive){
                    runOnUiThread {
                        setSubjectText(subjectMap.get("科目").toString())
                        setTermText(subjectMap.get("学期").toString())
                        setWeekText(subjectMap.get("教学周").toString())
                        LogUtil.e(TAG, "更新Subject")
                    }
                }
            }
        })

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
        val cameraIntent = Intent(this, OpenCameraActivity::class.java)
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
        val options = ActivityOptionsCompat.makeCustomAnimation(this,
            R.anim.alpha_in,
            R.anim.alpha_out
        )
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
        //cameraScan.setAnalyzeImage(true) // 继续扫码分析
    }

    override fun onStart() {
        super.onStart()
        ivResult.setImageBitmap(null)
        cameraScan.setAnalyzeImage(true)
        previewView.alpha = 1.0F
    }

    override fun onScanResultCallback(result: AnalyzeResult<List<String>>) {
        // 停止分析
        if(keyDown){
            cameraScan.setAnalyzeImage(false)
        }
        LogUtil.d(TAG, result.result.toString())

        // 当初始化 WeChatScanningAnalyzer 时，如果是需要二维码的位置信息，则会返回 WeChatScanningAnalyzer.QRCodeAnalyzeResult
//        if (result is WeChatScanningAnalyzer.QRCodeAnalyzeResult) { // 如果需要处理结果二维码的位置信息
//        if (result is OpenCVScanningAnalyzer.QRCodeAnalyzeResult) { // 如果需要处理结果二维码的位置信息
        if (result is MLScanningAnalyzer.MLQRCodeAnalyzeResult) { // 如果需要处理结果二维码的位置信息
            //取预览当前帧图片并显示，为结果点提供参照
//            ivResult.setImageBitmap(previewView.bitmap)
            var qrBitMapPoints = ArrayList<org.opencv.core.Point>() //存储镜头中二维码四角坐标
            var qrEdgeBitmapPoints = ArrayList<org.opencv.core.Point>() //存储镜头中含白边的二维码四角坐标
            var pageBitMapPoints = ArrayList<org.opencv.core.Point>() //存储镜头中版面四角坐标
            var viewfinderViewPoints = ArrayList<android.graphics.Point>()
            var qrObjectList = ArrayList<QRObject>()
            var isQRPToPagePList = ArrayList<Boolean>()
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
                var qrO : QRObject? = null
                try {
                    qrO = gson.fromJson<QRObject>(it, QRObject::class.java)
                    qrObjectList.add(qrO)
                    isQRPToPagePList.add(true)
                } catch (e: JsonSyntaxException) {
                    LogUtil.e(TAG, "JSON解析失败: ${e.message}")
                    isQRPToPagePList.add(false)
                }

                if(qrObjectList.size > 0){
                    qrObject = qrObjectList.get(qrObjectList.size-1)
                    LogUtil.e(TAG, "qrObject.pn: "+qrObject.pn+" cardData.preCode: "+cardData.preCode)
                    LogUtil.e(TAG, "qrObject.pn.equals(cardData.preCode.toString()): "+qrObject.pn.equals(cardData.preCode.toString()))
                    if(Integer.parseInt(qrObject.pn) != cardData.preCode){
                        circleRunnable.stop()
                        getSubject(qrObject.te, object : CallBack {

                            override fun onCallBack(subjectMap: MutableMap<String, Any>) {

                                runOnUiThread {
                                    setSubjectText(subjectMap.get("科目").toString())
                                }
                            }
                        })
                        setPreCodeText(qrObject.pn)
                        cardData.setIteration(qrObject.data)
                        LogUtil.e(TAG, "使用二维码内容更新cardData")
                    }
                }
            }

            var i = 0
            var j = 0
            LogUtil.e(TAG, "onScanResultCallback: before: pageBitMapPoints.size: "+pageBitMapPoints.size)
            result.points?.forEach { mat ->
//                var points = detectQREdgePoints(mat, result.bitmap)
//                if(points.size != 0){
                var MLPoints = MLMatToPoints(mat)
                qrBitMapPoints.addAll(MLPoints)
                if(isQRPToPagePList.get(i)){
                    processMLMat(MLPoints, result, qrObjectList.get(j++), isQRPToPagePList.get(i))?.let { pageBitMapPoints.addAll(it) }
                }
                i++
//                    if(isQRPToPageP){
//                        QRPointsToPagePoints(points, qrObject, result.bitmap.width, result.bitmap.height)?.let {
//                            bitMapPoints.addAll(
//                                it
//                            )
//                        }
//                    } else {
//                        bitMapPoints.addAll(points)
//                    }
//                bitMapPoints.addAll(processWeChatMat(mat, result, isQRPToPageP))

//                }
            }

            LogUtil.e(TAG, "onScanResultCallback: after: pageBitMapPoints.size: "+pageBitMapPoints.size)
//            for (i in 0 until result.points.rows()) {
//                result.points.row(i).let { mat ->
//                    processOpenCVMat(mat, result, isQRPToPageP)?.let { bitMapPoints.addAll(it) }
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
                LogUtil.e(TAG, "result.bitmap.width: "+(result.bitmap.width) +" viewfinderView.width: "+viewfinderView.width+" previewView.bitmap?.width: "+previewView.bitmap?.width+" ivResult.width: "+ivResult.width)
                LogUtil.e(TAG, "result.bitmap.height: "+(result.bitmap.height) +" viewfinderView.height: "+viewfinderView.height+" previewView.bitmap?.height: "+previewView.bitmap?.height+" ivResult.height: "+ivResult.height)

                var desWidth : Int = srcPWidth
                var desHeight: Int = srcPHeight
                var isrotated = false
                if(srcPWidth < srcPHeight){
                    isrotated = true
                }
                var points = ArrayList<Point>()
                var rectCrop: org.opencv.core.Rect
                if(isrotated){

                    points.add(Point(0.0, srcPWidth.toDouble()))
                    points.add(Point(0.0, 0.0))
                    points.add(Point(srcPHeight.toDouble(), 0.0))
                    points.add(Point(srcPHeight.toDouble(), srcPWidth.toDouble()))
                    desPoints = transformoocPoint(points, srcPHeight, srcPWidth, srcVWidth, srcVHeight)
                    desWidth = (desPoints.get(3).x - desPoints.get(1).x).toInt()
                    desHeight = (desPoints.get(3).y - desPoints.get(1).y).toInt()
                    rectCrop = org.opencv.core.Rect(desPoints.get(1).x.toInt(),
                        desPoints.get(1).y.toInt(), desWidth, desHeight
                    )
                } else {

                    points.add(Point(0.0, 0.0))
                    points.add(Point(srcPWidth.toDouble(), 0.0))
                    points.add(Point(srcPWidth.toDouble(), srcPHeight.toDouble()))
                    points.add(Point(0.0, srcPHeight.toDouble()))
                    desPoints = transformoocPoint(points, srcPWidth, srcPHeight, srcVWidth, srcVHeight)
                    desWidth = (desPoints.get(2).x - desPoints.get(0).x).toInt()
                    desHeight = (desPoints.get(2).y - desPoints.get(0).y).toInt()
                    rectCrop = org.opencv.core.Rect(desPoints.get(0).x.toInt(),
                        desPoints.get(0).y.toInt(), desWidth, desHeight
                    )

                }

//                if ((srcPWidth / srcPHeight) > (srcVWidth!! / srcVHeight!!)) {
//                    desWidth = srcVWidth
//                    desHeight = desWidth*srcPHeight/srcPWidth
//                    desPoints.add(Point(0.0, ((srcVHeight-desHeight)/2).toDouble()))
//                    desPoints.add(Point(desWidth.toDouble(), ((srcVHeight-desHeight)/2).toDouble()))
//                    desPoints.add(Point(desWidth.toDouble(), ((srcVHeight-desHeight)/2+desHeight).toDouble()))
//                    desPoints.add(Point(0.0, ((srcVHeight-desHeight)/2).toDouble()))
//
////                    desPoints.add(Point(0.0, 0.0))
////                    desPoints.add(Point(srcVWidth.toDouble(), 0.0))
////                    desPoints.add(Point(srcVWidth.toDouble(), srcVHeight.toDouble()))
////                    desPoints.add(Point(0.0, srcVHeight.toDouble()))
//
//                } else {
//                    desWidth = srcVHeight*srcPWidth/srcPHeight
//                    desHeight = srcVHeight
//                    if(srcPWidth > srcPHeight){
//
//                        desPoints.add(Point(((srcVWidth-desWidth)/2).toDouble(), 0.0))
//                        desPoints.add(Point(((srcVWidth-desWidth)/2+desWidth).toDouble(), 0.0))
//                        desPoints.add(Point(((srcVWidth-desWidth)/2+desWidth).toDouble(), desHeight.toDouble()))
//                        desPoints.add(Point(((srcVWidth-desWidth)/2).toDouble(), desHeight.toDouble()))
//
////                        desPoints.add(Point(0.0, 0.0))
////                        desPoints.add(Point(srcVWidth.toDouble(), 0.0))
////                        desPoints.add(Point(srcVWidth.toDouble(), srcVHeight.toDouble()))
////                        desPoints.add(Point(0.0, srcVHeight.toDouble()))
//                    } else{
//                        desPoints.add(Point(0.0, srcVHeight.toDouble()))
//                        desPoints.add(Point(0.0, 0.0))
//                        desPoints.add(Point(srcVWidth.toDouble(), 0.0))
//                        desPoints.add(Point(srcVWidth.toDouble(), srcVHeight.toDouble()))
//                    }
//                }
//                desPoints.add(Point(0.0, 0.0))
//                desPoints.add(Point(desWidth.toDouble(), 0.0))
//                desPoints.add(Point(desWidth.toDouble(), desHeight.toDouble()))
//                desPoints.add(Point(0.0, desHeight.toDouble()))

                var pageBitMapPs = filterPoints(pageBitMapPoints,
                    result.bitmap.width.toFloat(), result.bitmap.height.toFloat()
                )

                if(pageBitMapPs != null){
                    Log.e(TAG, "onScanResultCallback: pageBitMapPoints != null")
                    var square: ArrayList<RectF>? = null
                    var bMap = result.bitmap?.let {
                        var perspectiveTransform = getPerspectiveTransform(pageBitMapPs, desPoints)
                        var newQRCodePoints = warpPerspective(qrBitMapPoints, perspectiveTransform)
                        square = getMinSquare(newQRCodePoints)
                        warpPerspective(it, perspectiveTransform, srcVWidth, srcVHeight, rectCrop)
                    }

                    if (bMap != null) {
                        if(square != null){
                            bMap = cropBitmap(bMap!!, rectCrop)
                            var canvas = Canvas(bMap!!)
                            var paint = Paint()
                            for (rectF in square!!){
                                LogUtil.e(TAG, "变换前:"+rectF.toString())

                                cropRect(rectF, rectCrop)
                                cardData.setQRCodeRect(rectF.left.toInt(), rectF.top.toInt(), rectF.width().toInt())
                                cardData.setPageRect(bMap.width, bMap.height)
                                cardData.toQRObject().toQRCodeBitmap(rectF)
                                    ?.let {
                                        LogUtil.e(TAG, "变换后:"+rectF.toString())
                                        canvas.drawBitmap(it, rectF.left, rectF.top, paint) }
                            }
                        }

//                var bMap = result.bitmap?.let { warpPerspective(it, pageBitMapPs, desPoints, desWidth, desHeight) }
                        ivResult.setImageBitmap(bMap)
                        previewView.alpha = 0F

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
                        Storager.cardCache = cardData
                        BitmapCacheManager.putBitmap("WeChatQRCodeBitmap", bitmap)

                        //初始化回false为下次做准备
                        keyDown=false
                        val intent = Intent(getContext(), CardProcessActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    keyDown = false
                    cameraScan.setAnalyzeImage(true)
                    previewView.alpha = 1.0F
                }

            } else {
                pageBitMapPoints.addAll(qrBitMapPoints)
                Log.e(TAG, "onScanResultCallback: last: pageBitMapPoints.size: "+pageBitMapPoints.size)
                for(point in pageBitMapPoints){
                    Log.e(TAG, "onScanResultCallback: "+point.toString())
                }
                viewfinderViewPoints.addAll(transformagPoint(pageBitMapPoints, result.bitmap.width, result.bitmap. height, viewfinderView.width, viewfinderView.height))
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

    private fun getMinSquare(newQRCodePoints: ArrayList<Point>): ArrayList<RectF> {
        var rectFs = ArrayList<RectF>()
        var rectF : RectF? = null
        for(i in 0 until newQRCodePoints.size){
            if(i%4 == 0){
                rectF = RectF(newQRCodePoints.get(i).x.toFloat(), newQRCodePoints.get(i).y.toFloat(), newQRCodePoints.get(i).x.toFloat(), newQRCodePoints.get(i).y.toFloat())
            }

            rectF!!.union(newQRCodePoints.get(i).x.toFloat(), newQRCodePoints.get(i).y.toFloat())

            if(i%3 == 0){
                var l = Math.max(rectF.width(), rectF.height())
                rectF = RectF(rectF.left, rectF.top, rectF.left+l, rectF.top+l)
                rectFs.add(rectF)
            }
        }
        return rectFs
    }

    fun transformPanelCornersPp(
        qrRealCorners: Array<Point>,
        qrCorners: Array<Point>,
        panelRealCorners: Array<Point>
    ): Array<Point> {
        // 计算透视变换矩阵
        val srcMat = MatOfPoint2f(*qrRealCorners)
        val dstMat = MatOfPoint2f(*qrCorners)
        val matrix = Imgproc.getPerspectiveTransform(srcMat, dstMat)

        // 用矩阵和已知点求解
        val panelSrcMat = MatOfPoint2f(*panelRealCorners)
        val panelDstMat = MatOfPoint2f()
        panelDstMat.create(panelSrcMat.rows(), panelSrcMat.cols(), panelSrcMat.type())
        Core.perspectiveTransform(panelSrcMat, panelDstMat, matrix)

        return panelDstMat.toArray()
    }

    fun transformPanelCornersWa(
        qrRealCorners: Array<Point>,
        qrCorners: Array<Point>,
        panelRealCorners: Array<Point>
    ): Array<Point> {
        // 计算仿射变换矩阵
        val srcMat = MatOfPoint2f(*qrRealCorners)
        val dstMat = MatOfPoint2f(*qrCorners)
        val matrix = Imgproc.getAffineTransform(srcMat, dstMat)

        // 用矩阵和已知点求解
        val panelSrcMat = MatOfPoint2f(*panelRealCorners)
        val panelDstMat = MatOfPoint2f()
        Imgproc.warpAffine(panelSrcMat, panelDstMat, matrix, Size(panelSrcMat.cols().toDouble(), panelSrcMat.rows().toDouble()))

        return panelDstMat.toArray()
    }


    fun processOpenCVMat(mat:Mat, result: AnalyzeResult<List<String>>, isQRPToPageP: Boolean) : ArrayList<Point>? {

        var points = openCVMatToPoints(mat)

        if(isQRPToPageP){
            return QRPointsToPagePoints(points, qrObject, result.bitmap.width, result.bitmap.height)
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

    fun MLMatToPoints(mat: Mat): ArrayList<org.opencv.core.Point> {
        var points = ArrayList<org.opencv.core.Point>()
        for( i in 0..3){
            points.add(org.opencv.core.Point(mat[i, i][0], mat[i, i][1]))
            Log.d(OpenCVQRCodeActivity.TAG, "point$i: ${mat[i, i][0]}, ${mat[i, i][1]}")
        }
        return points
    }

    fun processMLMat(points: ArrayList<Point>, result: AnalyzeResult<List<String>>, qrObject: QRObject, isQRPToPageP: Boolean) : ArrayList<Point>? {
        if(isQRPToPageP){
            return QRPointsToPagePoints(points, qrObject, result.bitmap.width, result.bitmap.height)
        }
        return null
    }

    fun processMLMat(mat:Mat, result: AnalyzeResult<List<String>>, qrObject: QRObject, isQRPToPageP: Boolean) : ArrayList<Point>? {
        var points = MLMatToPoints(mat)
        if(isQRPToPageP){
            return QRPointsToPagePoints(points, qrObject, result.bitmap.width, result.bitmap.height)
        }
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
     * 计算四边形面积
     */
    fun calculateArea(p1: Point, p2: Point, p3: Point, p4: Point): Double {
        // 计算第一个三角形的面积
        val area1 = 0.5 * Math.abs((p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y)))

        // 计算第二个三角形的面积
        val area2 = 0.5 * Math.abs((p1.x * (p3.y - p4.y) + p3.x * (p4.y - p1.y) + p4.x * (p1.y - p3.y)))

        // 返回四边形的面积
        return area1 + area2
    }

    /**
     * 判断一组点是否都在目标版面内
     */
    fun pointsExist(points: ArrayList<Point>, width: Float, height: Float) : Boolean {

        var rectf = RectF(0F, 0F, width, height)

        for(point in points){
            if(!rectf.contains(point.x.toFloat(), point.y.toFloat())){
                return false
            }
        }
        return true
    }

    /**
     * 在所有版面四边形中过滤掉不符合要求的四边形
     */
    fun filterPoints(points: ArrayList<Point>, width: Float, height: Float) : ArrayList<Point>? {
        var pointsBuffer : ArrayList<Point>? = null
        var pointsP : ArrayList<Point>? = null
        var area : Double = 0.0
        for(i in 0 until points.size){
            if(i%4 == 0){
                pointsBuffer = ArrayList<Point>()
            }
            pointsBuffer?.add(points.get(i))
            if((i+1)%4 == 0){
                if(pointsBuffer?.let { pointsExist(it, width, height) } == true){
                    var s = calculateArea(pointsBuffer.get(0), pointsBuffer.get(1), pointsBuffer.get(2), pointsBuffer.get(3))
                    if(area < s){
                        area = s
                        pointsP = pointsBuffer
                    }
                }
            }
        }
        return pointsP
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
    fun transformoocPoint(points: ArrayList<org.opencv.core.Point>, srcWidth: Int, srcHeight: Int, desWidth: Int, desHeight: Int): ArrayList<org.opencv.core.Point> {
        var newPoints = ArrayList<org.opencv.core.Point>()
        for( point in points){
            newPoints.add(agPTooocP(PointUtils.transform(point.x.toInt(), point.y.toInt(), srcWidth, srcHeight, desWidth, desHeight, true)))
        }
        return newPoints
    }

    /**
     * 将bitmap上的坐标转换为viewfinderView上的坐标,以生成新Point的方式返回,不改变传入的points
     */
    fun transformagPoint(points: ArrayList<org.opencv.core.Point>, srcWidth: Int, srcHeight: Int, desWidth: Int, desHeight: Int): ArrayList<android.graphics.Point> {
        var newPoints = ArrayList<android.graphics.Point>()
        for( point in points){
            newPoints.add(PointUtils.transform(point.x.toInt(), point.y.toInt(), srcWidth, srcHeight, desWidth, desHeight, false))
        }
        return newPoints
    }

    /**
     * android.graphics.Point转换为org.opencv.core.Point
     */
    fun agPTooocP(point :android.graphics.Point):org.opencv.core.Point{
        return org.opencv.core.Point(point.x.toDouble(), point.y.toDouble())
    }

    /**
     * 二维码四角坐标转换为包含白边的二维码四角坐标,前提是识别出的二维码信息是有效的;会改变传入的points
     */
    fun QRPointsToEdgePoints(points: ArrayList<org.opencv.core.Point>, qrO: QRObject, width: Int, height: Int): ArrayList<org.opencv.core.Point>? {
        if (qrO != null) {
            var corner = Point(points.get(0).x, points.get(0).y)
            val qrRealCorners = arrayOf(
                Point(corner.x, corner.y),
                Point(corner.x + qrO.ql.toDouble(), corner.y),
                Point(corner.x + qrO.ql.toDouble(), corner.y + qrO.ql.toDouble()),
                Point(corner.x, corner.y + qrO.ql.toDouble())
            )
            val corner1 = Point(corner.x - qrO.qx.toDouble(), corner.y - qrO.qy.toDouble())
            val panelRealCorners = arrayOf(
                Point(corner1.x, corner1.y),
                Point(corner1.x + qrO.psx.toDouble(), corner1.y),
                Point(corner1.x + qrO.psx.toDouble(), corner1.y + qrO.psy.toDouble()),
                Point(corner1.x, corner1.y + qrO.psy.toDouble())
            )

            var qrCorners = points.toTypedArray()

            return ArrayList(
                transformPanelCornersPp(
                    qrRealCorners,
                    qrCorners,
                    panelRealCorners
                ).toList()
            )
        }
        return null
    }

    /**
     * 二维码四角坐标转换为版面四角坐标,前提是识别出的二维码信息是有效的;会改变传入的points
     */
    fun QRPointsToPagePoints(points: ArrayList<org.opencv.core.Point>, qrO: QRObject, width: Int, height: Int): ArrayList<org.opencv.core.Point>? {
        if(qrO != null){
            var corner = Point(points.get(0).x, points.get(0).y)
            val qrRealCorners = arrayOf(
                Point(corner.x, corner.y),
                Point(corner.x+qrO.ql.toDouble(), corner.y),
                Point(corner.x+qrO.ql.toDouble(), corner.y+qrO.ql.toDouble()),
                Point(corner.x, corner.y+qrO.ql.toDouble())
            )
            val corner1 = Point(corner.x - qrO.qx.toDouble(), corner.y - qrO.qy.toDouble())
            val panelRealCorners = arrayOf(
                Point(corner1.x, corner1.y),
                Point(corner1.x + qrO.psx.toDouble(), corner1.y),
                Point(corner1.x + qrO.psx.toDouble(), corner1.y + qrO.psy.toDouble()),
                Point(corner1.x, corner1.y + qrO.psy.toDouble())
            )
//            var qrCorners = arrayOf(
//                Point(points.get(0).x, points.get(0).y),
//                Point(points.get(1).x, points.get(1).y),
//                Point(points.get(2).x, points.get(2).y),
//                Point(points.get(3).x, points.get(3).y)
//            )
            var qrCorners = points.toTypedArray()
//            var qrCorners = arrayOf(
//                points.get(0),
//                points.get(1),
//                points.get(3)
//            )

            return ArrayList(transformPanelCornersPp(qrRealCorners, qrCorners, panelRealCorners).toList())
            var oldPoints = java.util.ArrayList<org.opencv.core.Point>();
            for(i in 0..3){
                oldPoints.add(org.opencv.core.Point(points.get(i).x, points.get(i).y))
            }

            var offSetX = qrO.qx
            var offSetY = qrO.qy

            var ratioOffX : Double = offSetX * (1.0 / qrO.ql)
            var ratioOffY : Double = offSetY * (1.0 / qrO.ql)

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

            var ratioX : Double = qrO.psx *(1.0/ qrO.ql)
            var ratioY : Double = qrO.psy *(1.0/ qrO.ql)
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

            oldPoints.addAll(points)
            return oldPoints
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
     * 透视变换图像
     */
    fun warpPerspective(bitmap:Bitmap, srcPoints:ArrayList<org.opencv.core.Point>, desPoints: ArrayList<org.opencv.core.Point>, width: Int, height: Int): Bitmap {
        return warpPerspective(bitmap, srcPoints, desPoints, width, height, null)
    }

    fun warpPerspective(bitmap:Bitmap, srcPoints:ArrayList<org.opencv.core.Point>, desPoints: ArrayList<org.opencv.core.Point>, width: Int, height: Int, rect: org.opencv.core.Rect?): Bitmap {
        return warpPerspective(bitmap, getPerspectiveTransform(srcPoints, desPoints), width, height, rect)
    }

    /**
     * 透视变换图像
     */
    fun warpPerspective(bitmap:Bitmap, perspectiveTransform:Mat, width: Int, height: Int, rect: org.opencv.core.Rect?): Bitmap {

        // 定义目标图中的边框上的四个点
//        val dstWidth = bitmap.width
//        val dstHeight = bitmap.height
        val dstWidth = width
        val dstHeight = height

        // 进行透视变换
        var transformedMat = Mat()
        val inputMatResult = Mat()
        Utils.bitmapToMat(bitmap, inputMatResult)
        Imgproc.warpPerspective(inputMatResult, transformedMat, perspectiveTransform, Size(dstWidth.toDouble(), dstHeight.toDouble()))

        //         将 Mat 转换回 Bitmap
        var resultBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
//        if(rect != null){
//            // 裁剪图像
//            transformedMat = Mat(transformedMat, rect)
//            resultBitmap = Bitmap.createBitmap(rect.width, rect.height, Bitmap.Config.ARGB_8888)
//        }

        Utils.matToBitmap(transformedMat, resultBitmap)

        return resultBitmap
    }

    fun cropBitmap(bitmap: Bitmap, rect: org.opencv.core.Rect): Bitmap {
        LogUtil.e(TAG, "bitmap.width: "+bitmap.width+" bitmap.height: "+bitmap.height+" rect: "+rect.toString())
        return Bitmap.createBitmap(bitmap, rect.x, rect.y, rect.width, rect.height)
    }

    fun cropRect(qrRectF: RectF, rect: org.opencv.core.Rect): RectF {
        qrRectF.offset((-rect.x).toFloat(), (-rect.y).toFloat())
        return qrRectF
    }

    /**
     * 透视变换多个坐标点
     */
    fun warpPerspective(srcPoints:ArrayList<org.opencv.core.Point>, perspectiveTransform:Mat): ArrayList<org.opencv.core.Point> {

        var desPoints = ArrayList<org.opencv.core.Point>()
        for (point in srcPoints){
            desPoints.add(warpPerspective(point, perspectiveTransform))
        }

        return desPoints
    }

    /**
     * 透视变换单个坐标点
     */
    fun warpPerspective(srcPoint:org.opencv.core.Point, perspectiveTransform:Mat): org.opencv.core.Point {

        val srcPoint = MatOfPoint2f(srcPoint)

        // 进行透视变换
        // 进行透视变换
        val dstPoint = MatOfPoint2f()
        Core.perspectiveTransform(srcPoint, dstPoint, perspectiveTransform)

        return Point(dstPoint[0, 0])
    }

    /**
     * 计算透视变换矩阵
     */
    fun getPerspectiveTransform(srcPoints:ArrayList<org.opencv.core.Point>, desPoints: ArrayList<org.opencv.core.Point>) : Mat{
        //定义原图中边框上的四个点
        val srcPoints = MatOfPoint2f(
            srcPoints.get(0), // 左上角
            srcPoints.get(1), // 右上角
            srcPoints.get(2), // 右下角
            srcPoints.get(3)  // 左下角
        )

        val dstPoints = MatOfPoint2f(
            desPoints.get(0),
            desPoints.get(1),
            desPoints.get(2),
            desPoints.get(3)
        )

        return Imgproc.getPerspectiveTransform(srcPoints, dstPoints)
    }

    /**
     * 输入,空OpenCV点数组及位图,输出左上点0,右上点1,右下点2,左下点3
     */
    fun detectQREdgePoints(mat: Mat, bitmap: Bitmap): ArrayList<org.opencv.core.Point> {
        var pointsList = ArrayList<org.opencv.core.Point>()
//        val result = WeChatQRCodeDetector.detectAndDecode(bitmap, points)
//        if (result.isNotEmpty()) {
            // 计算二维码矩形的坐标
        val allPoints = ArrayList<android.graphics.Point>()
        for (i in 0..3) {
            val x = mat[i, 0][0].toInt()
            val y = mat[i, 1][0].toInt()
            allPoints.add(android.graphics.Point(x, y))
        }

        //防止裁剪过多
        val margin: Int = 100
        var a:Int
        a = (allPoints.minByOrNull { it.x }?.x ?: 0) - margin
        val minX = if (a >= 0) a else 0
        a = (allPoints.minByOrNull { it.y }?.y ?: 0) - margin
        val minY = if (a >= 0) a else 0
        a = (allPoints.maxByOrNull { it.x }?.x ?: 0) + margin
        val maxX = if (a <= bitmap.width) a else bitmap.width
        a = (allPoints.maxByOrNull { it.y }?.y ?: 0) + margin
        val maxY = if (a <= bitmap.height) a else bitmap.height
        // 创建矩形
        val rect = Rect(minX, minY, maxX, maxY)
        // 裁剪位图
        val croppedBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
        //转成Mat
        val srcMat = Mat()
        Utils.bitmapToMat(croppedBitmap, srcMat)
        //灰度
        val srcGray = Mat()
        Imgproc.cvtColor(srcMat, srcGray, Imgproc.COLOR_BGR2GRAY)


        //处理反而使得有效点被处理，噪声成为有效点，也许调参或者其他方法可行？
//                        // 滤波
//                        Imgproc.blur(srcGray, srcGray, Size(3.0, 3.0))
//                        // 直方图均衡化
//                        Imgproc.equalizeHist(srcGray, srcGray)

        //二值化
        val thresholdMat = Mat()
        Imgproc.threshold(srcGray, thresholdMat, 112.0, 255.0, Imgproc.THRESH_BINARY)

        // 寻找轮廓
        val contours: ArrayList<MatOfPoint> = ArrayList()
        val hierarchy = Mat()
        Imgproc.findContours(
            thresholdMat,
            contours,
            hierarchy,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        val markerContours = contours.filterIndexed { index, _ ->
            val childIndex1 = hierarchy[0, index][2].toInt()
            if (childIndex1 == -1) {
                false
            } else {
                val childIndex2 = hierarchy[0, childIndex1][2].toInt()
                childIndex2 != -1
            }
        }

        val markerCenters = markerContours.map {
            val moments = Imgproc.moments(it)
            org.opencv.core.Point(moments.m10 / moments.m00, moments.m01 / moments.m00)
        }

        //用于寻找三个点
        val sortedByX = markerCenters.sortedBy { it.x }
        val sortedByY = markerCenters.sortedBy { it.y }

        val markerCentersSize = markerCenters.size

        val originalMat = Mat()
        Utils.bitmapToMat(bitmap, originalMat)

        if (markerCentersSize < 3) {
            Log.d("QR Code Detection", "没有检测到足够的点")
        } else if (markerCentersSize == 3) {  // 如果我们检测到正好3个点，就画出它们
            val smallestXPoints = sortedByX.take(2)
            val smallestYPoints = sortedByY.take(2)

            val topLeft = smallestXPoints.intersect(smallestYPoints).minByOrNull { it.x + it.y }
            val otherPoint = smallestXPoints.union(smallestYPoints).filter { it != topLeft }

            val topRight = otherPoint.maxByOrNull { it.x }
            val bottomLeft = otherPoint.maxByOrNull { it.y }

            if (topLeft != null && topRight != null && bottomLeft != null) {
                // 将点的坐标转换到原图的坐标系中
                val transformedTopLeft =
                    org.opencv.core.Point(topLeft.x + minX, topLeft.y + minY)
                val transformedTopRight =
                    org.opencv.core.Point(topRight.x + minX, topRight.y + minY)
                val transformedBottomLeft =
                    org.opencv.core.Point(bottomLeft.x + minX, bottomLeft.y + minY)
                val transformedBottomRight =
                    org.opencv.core.Point (topRight.x + bottomLeft.x - topLeft.x + minX, topRight.y + bottomLeft.y - topLeft.y + minY)
                pointsList.add(transformedTopLeft)
                pointsList.add(transformedTopRight)
                pointsList.add(transformedBottomRight)
                pointsList.add(transformedBottomLeft)

            }else {
                Log.d("QR Code Detection 3", "没有检测到足够有效的点")
            }


        }
        else {
            val smallestXPoints = sortedByX.take(2)
            val smallestYPoints = sortedByY.take(2)

            val topLeft = smallestXPoints.intersect(smallestYPoints).minByOrNull { it.x + it.y }
            val otherPoint = smallestXPoints.union(smallestYPoints).filter { it != topLeft }

            val topRight = otherPoint.maxByOrNull { it.x }
            val bottomLeft = otherPoint.maxByOrNull { it.y }

            if (topLeft != null && topRight != null && bottomLeft != null) {
                // 将点的坐标转换到原图的坐标系中
                val transformedTopLeft =
                    org.opencv.core.Point(topLeft.x + minX, topLeft.y + minY)
                val transformedTopRight =
                    org.opencv.core.Point(topRight.x + minX, topRight.y + minY)
                val transformedBottomLeft =
                    org.opencv.core.Point(bottomLeft.x + minX, bottomLeft.y + minY)
                val transformedBottomRight =
                    org.opencv.core.Point (topRight.x + bottomLeft.x - topLeft.x + minX, topRight.y + bottomLeft.y - topLeft.y + minY)
                pointsList.add(transformedTopLeft)
                pointsList.add(transformedTopRight)
                pointsList.add(transformedBottomRight)
                pointsList.add(transformedBottomLeft)


            } else {
                Log.d("QR Code Detection 4", "没有检测到足够有效的点")
            }
        }
//        }
        return pointsList
    }

    override fun createAnalyzer(): Analyzer<MutableList<String>>? {
        // 分析器默认不会返回结果二维码的位置信息
//        return WeChatScanningAnalyzer()
        // 如果需要返回结果二维码位置信息，则初始化分析器时，参数传 true 即可
//        return WeChatScanningAnalyzer(true)
//        return return OpenCVScanningAnalyzer(true)
        return MLScanningAnalyzer(true);
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
        fun onCallBack(string: MutableMap<String, Any>)
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
                .viewId("vew8cHdAE6")
                .filter("AND(CurrentValue.[起始时间]<NOW(),NOW()<CurrentValue.[结束时间])")
                .pageSize(20)
                .build()


            // 发起请求
            // 如开启了Sdk的token管理功能，就无需调用 RequestOptions.newBuilder().tenantAccessToken("t-xxx").build()来设置租户token了
            val resp = client.bitable().appTableRecord().list(
                req, RequestOptions.newBuilder()
                    .tenantAccessToken("t-g1049bkqIY5R5ZXKGVNT2XSXBLOQBE6W7FLNOGCS")
                    .build()
            )

            // 处理服务端错误
            if (!resp.success()) {
                LogUtil.e(TAG, String.format(
                    "code:%s,msg:%s,reqId:%s",
                    resp.code,
                    resp.msg,
                    resp.requestId
                ))
            }

            LogUtil.e(TAG, Jsons.DEFAULT.toJson(resp.getData()))
            if(resp.data.items.isNotEmpty()) {
                callBack.onCallBack(resp.data.items.get(0).getFields())
            }

        }.start()

    }

    /**
     * 通过教师编号从多维表格获取学科
     */
    fun getSubject(teacherNumber: String, callBack: CallBack) {
        LogUtil.e(TAG, "teacherNumber: "+teacherNumber)
        if(teacherNumber.equals("")){
            return
        }

        // 构建client
//        client = Client.newBuilder("cli_a4ac1c99553b9013", "dJnppJxBQQKd4QGmXSrP3fwdvcT5iNZ6").build()

        Thread {
            // 创建请求对象
            val req = ListAppTableRecordReq.newBuilder()
                .appToken("bascn3zrUMtRbKme8rlcyRKfDSc")
                .tableId("tblpAZmppl1siFd7")
                .viewId("vew8cHdAE6")
                .filter("CurrentValue.[教师编号] = \"$teacherNumber\"")
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
                LogUtil.e(TAG, String.format(
                    "code:%s,msg:%s,reqId:%s",
                    resp.code,
                    resp.msg,
                    resp.requestId
                ))
            }

            if(resp.data.items.isNotEmpty()){
                callBack.onCallBack(resp.data.items[0].fields)
            }


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

    fun setPreCodeText(string:String){
        preCodeText.text = preCodeString+string
        cardData.setPreCode(string)
    }

    fun setPostCodeText(string:String){
        postCodeText.text = postCodeString+string
        cardData.postCode = Integer.parseInt(string)
    }
    fun setSubjectText(string:String){
        subjectText.text = subjectString+string
        cardData.cardDataLabel.subjectName = string
        backgroundColorChange(string)
    }
    fun setUnitText(string:String){
        unitText.text = unitString+string
        cardData.cardDataLabel.unitName = string
    }
    fun setStageText(string:String){
        stageText.text = stageString+string
        cardData.cardDataLabel.stage = string
    }
    fun setClassTimeText(string:String){
        classTimeText.text = classString+string
        cardData.cardDataLabel.classTime = string
    }
    fun setWeekText(string:String){
        weekText.text = "第"+string+"周"
        cardData.cardDataLabel.week = string
    }
    fun setGroupText(string:String){
        groupText.text = groupString+string
        cardData.qrObject.gn = string
    }
    fun setRoomText(string:String){
        roomText.text = roomString+string
        cardData.qrObject.cl = string
    }
    fun setGradeText(string:String){
        gradeText.text = gradeString+string
        cardData.qrObject.gr = string
    }
    fun setTermText(string:String){
        termText.text = string
        cardData.cardDataLabel.term = string
    }

    fun setDayText(string: String){
        dayText.text = string
        cardData.cardDataLabel.day = string
    }

    /**
     * 设置顶部学科栏
     */
    fun setTopActionBar(preCode: String?, postCode: String?, subject: String?, unit: String?, stage: String?, classTime: String?) {
        if(preCode != null){
            setPreCodeText(preCode)
        }

        if(postCode != null){
            setPostCodeText(postCode)
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
            setClassTimeText(classTime)
        }
    }

    /**
     * 设置组织信息
     */
    fun setOrganInfor(week: String?, group: String?, room: String?, grade: String?, term: String?, day: String?) {
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
            if (cardData.subjectToColorMap[string] != null) {
                var red: Int = cardData.subjectToColorMap[string]!! and 0xff0000 shr 16
                var green: Int = cardData.subjectToColorMap[string]!! and 0x00ff00 shr 8
                var blue: Int = cardData.subjectToColorMap[string]!! and 0x0000ff
                drawable.setColorFilter(Color.rgb(red, green, blue), PorterDuff.Mode.SRC_ATOP)

                //backgroundLayout.setBackgroundDrawable(drawable)
                backgroundLayout.background = drawable

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