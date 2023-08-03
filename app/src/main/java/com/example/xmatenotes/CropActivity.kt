package com.example.xmatenotes

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.Hashtable


class DraggableViewContainer(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private val paintRed = Paint()
    private val startX = FloatArray(4)
    private val startY = FloatArray(4)
    private val endX = FloatArray(4)
    private val endY = FloatArray(4)

    init {
        paintRed.color = Color.RED
        paintRed.strokeWidth = 5f
    }



    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        updateRedLine()
        // 画红线
        for (i in 0..3) {
            canvas.drawLine(startX[i], startY[i], endX[i], endY[i], paintRed)
        }
    }

    fun setViewPosition(viewId: Int, x: Double, y: Double) {
        val view = findViewById<DraggableView>(viewId)
        view?.let {
            it.x = x.toFloat()
            it.y = y.toFloat()
            updateRedLine()
        }
    }

    fun updateRedLine(){
        val circleLeftTop = findViewById<DraggableView>(R.id.circleLeftTop)
        val circleRightTop = findViewById<DraggableView>(R.id.circleRightTop)
        val circleLeftBottom = findViewById<DraggableView>(R.id.circleLeftBottom)
        val circleRightBottom = findViewById<DraggableView>(R.id.circleRightBottom)

        // Set the positions for the red lines
        startX[0] = circleLeftTop.x + circleLeftTop.width / 2f
        startY[0] = circleLeftTop.y + circleLeftTop.height / 2f
        endX[0] = circleRightTop.x + circleRightTop.width / 2f
        endY[0] = circleRightTop.y + circleRightTop.height / 2f

        startX[1] = circleLeftTop.x + circleLeftTop.width / 2f
        startY[1] = circleLeftTop.y + circleLeftTop.height / 2f
        endX[1] = circleLeftBottom.x + circleLeftBottom.width / 2f
        endY[1] = circleLeftBottom.y + circleLeftBottom.height / 2f

        startX[2] = circleRightBottom.x + circleRightBottom.width / 2f
        startY[2] = circleRightBottom.y + circleRightBottom.height / 2f
        endX[2] = circleRightTop.x + circleRightTop.width / 2f
        endY[2] = circleRightTop.y + circleRightTop.height / 2f

        startX[3] = circleRightBottom.x + circleRightBottom.width / 2f
        startY[3] = circleRightBottom.y + circleRightBottom.height / 2f
        endX[3] = circleLeftBottom.x + circleLeftBottom.width / 2f
        endY[3] = circleLeftBottom.y + circleLeftBottom.height / 2f
    }
}



class DraggableView : View {
    private var lastX = 0
    private var lastY = 0

//    private var centerX : Float = 0.0f
//    private var centerY : Float = 0.0f


    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY
                val left: Int = getLeft() + dx
                val top: Int = getTop() + dy
                val right: Int = getRight() + dx
                val bottom: Int = getBottom() + dy
                layout(left, top, right, bottom)
                lastX = x
                lastY = y
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制背景圆形
        val centerX = width / 2f
        val centerY = height / 2f
//        centerX = width/2f
//        centerY = height/2f
        val radius = width / 2f
        val paintTransparent = Paint()
        paintTransparent.color = Color.GRAY
        paintTransparent.alpha = 50 // 设置透明度值
        canvas.drawCircle(centerX, centerY, radius, paintTransparent)

        // 绘制中间的绿点
        val paintOpaque = Paint()
        paintOpaque.color = Color.GREEN
        paintOpaque.alpha = 255 // 设置透明度值为255，完全不透明
        val innerRadius = radius * 0.1f // 控制内部精确点的大小，这里设置为半径的10%
        canvas.drawCircle(centerX, centerY, innerRadius, paintOpaque)
    }
}

class CropActivity : AppCompatActivity() {

    private fun getContext() = this

    private lateinit var imageView: ImageView
    private lateinit var button_confirm: Button
    private lateinit var button_generate:Button
    private lateinit var QR_String:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        imageView = findViewById(R.id.crop_image)
        button_confirm= findViewById(R.id.crop_confirm)
        button_generate=findViewById(R.id.QR_generate)
        val bitmap: Bitmap? = BitmapCacheManager.getBitmap("OpenCameraBitmap")

        if(bitmap!=null) {
            imageView.setImageBitmap(bitmap)
            val resultBitmap: Bitmap? = findMaxContour(bitmap)
            imageView.setImageBitmap(bitmap)
        }


        button_confirm.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
//                if(bitmap!=null) {
//                    imageView.setImageBitmap(perspectivetTransform(bitmap))
//                }
                val intent = Intent(getContext(), CardProcessActivity::class.java)
                if (bitmap != null) {
                    BitmapCacheManager.putBitmap("OpenCameraBitmap", bitmap)
                }
                startActivity(intent)
            }
        })

//        button_generate.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(view: View) {
//                //showSetPenNameDialog()
//                val qrObject= QRObject("02","1810*1000","001","01","07",
//                    "03","2100_150","00","037a","00GL","03","6","01","012","20230717150121")
//                //val jsonString = Json.encodeToString(qrObject)
//                val string = "hello"
//                var resultBitmap=generateQRCode(string,500)
//                imageView.setImageBitmap(resultBitmap)
//            }
//        })










    }

//    private fun perspectivetTransform(bitmap: Bitmap): Bitmap {
//
////        // 定义原图中边框上的四个点
////        val srcPoints = MatOfPoint2f(
////            vertices[0], // 左下角
////            vertices[1], // 左上角
////            vertices[2], // 右上角
////            vertices[3]  // 右下角
////        )
//
//        // 定义目标图中的边框上的四个点
//        val dstWidth = bitmap.width
//        val dstHeight = bitmap.height
//        val dstPoints = MatOfPoint2f(
//            Point(0.0, 0.0),                     // 左上角
//            Point(dstWidth.toDouble(), 0.0),     // 右上角
//            Point(dstWidth.toDouble(), dstHeight.toDouble()), // 右下角
//            Point(0.0, dstHeight.toDouble())     // 左下角
//        )
//
//        // 计算透视变换矩阵
//        val perspectiveTransform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)
//
//        // 进行透视变换
//        val transformedMat = Mat()
//        val inputMatResult = Mat()
//        Utils.bitmapToMat(bitmap, inputMatResult)
//        Imgproc.warpPerspective(inputMatResult, transformedMat, perspectiveTransform, Size(dstWidth.toDouble(), dstHeight.toDouble()))
//
//        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(transformedMat, resultBitmap)
//
//        return resultBitmap
//    }


    fun findMaxContour(bitmap: Bitmap): Bitmap? {
        // 将 Bitmap 转换为 OpenCV 的 Mat 对象
        // 将 Bitmap 转换为 OpenCV 的 Mat 对象
        val inputMat = Mat()
        Utils.bitmapToMat(bitmap, inputMat)

        // 将 Mat 转换为灰度图像
        val grayMat = Mat()
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)

        // 自适应阈值化进行图像二值化
        val binaryMat = Mat()
        Imgproc.adaptiveThreshold(
            grayMat,
            binaryMat,
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            11,
            2.0
        )

        // 进行边缘检测
        val edgesMat = Mat()
        Imgproc.Laplacian(binaryMat, edgesMat, CvType.CV_8U)

        // 开运算去除边缘混叠问题
        val openedMat = Mat()
        val kernelSize = 3
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(kernelSize.toDouble(), kernelSize.toDouble()))
        Imgproc.morphologyEx(edgesMat, openedMat, Imgproc.MORPH_OPEN, kernel)

        // 寻找图像中的轮廓
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edgesMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS)

        // 找出最大的轮廓
        var maxContour: MatOfPoint? = null
        var maxArea = 0.0
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > maxArea) {
                maxArea = area
                maxContour = contour
            }
        }

        // 求取最小外接矩形，得到梯形的顶点坐标
        val minRect = Imgproc.minAreaRect(MatOfPoint2f(*maxContour?.toArray()))
        val vertices = Array<Point>(4) { Point() }
        minRect.points(vertices)

        Log.d(TAG, "point0: ${vertices[0].x.toFloat()}, ${vertices[0].y.toFloat()}")
        Log.d(TAG, "point1: ${vertices[1].x.toFloat()}, ${vertices[1].y.toFloat()}")
        Log.d(TAG, "point2: ${vertices[2].x.toFloat()}, ${vertices[2].y.toFloat()}")
        Log.d(TAG, "point3: ${vertices[3].x.toFloat()}, ${vertices[3].y.toFloat()}")


        val width = bitmap.width // 获取位图宽度
        val height = bitmap.height // 获取位图高度
        imageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 在这里获取 ImageView 的宽度和高度
                val imageViewWidth = imageView.width
                val imageViewHeight = imageView.height

                // 获取 ImageView 中显示的图片的 Drawable
                val drawable = imageView.drawable

                if (drawable != null) {
                    // 获取图片的宽度和高度
                    val imageWidth = drawable.intrinsicWidth
                    val imageHeight = drawable.intrinsicHeight


                    val imageMatrix = imageView.imageMatrix
                    val imageRect = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
                    imageMatrix.mapRect(imageRect)


                    // 计算图片四个角在屏幕上的坐标
                    val topLeftPoint = Point(((imageRect.left).toDouble()), (imageRect.top).toDouble())

                    val screenVertices = Array<Point>(4) { Point() }

                    screenVertices[0].x=topLeftPoint.x+((vertices[1].x*imageViewWidth)/width)
                    screenVertices[0].y=topLeftPoint.y+((vertices[1].y*imageViewHeight)/height)
//                    screenVertices[0].x=topLeftPoint.x
//                    screenVertices[0].y=topLeftPoint.y
                    screenVertices[1].x=topLeftPoint.x+((vertices[2].x*imageViewWidth)/width)
                    screenVertices[1].y=topLeftPoint.y+((vertices[2].y*imageViewHeight)/height)
                    screenVertices[2].x=topLeftPoint.x+((vertices[3].x*imageViewWidth)/width)
                    screenVertices[2].y=topLeftPoint.y+((vertices[3].y*imageViewHeight)/height)
                    screenVertices[3].x=topLeftPoint.x+((vertices[0].x*imageViewWidth)/width)
                    screenVertices[3].y=topLeftPoint.y+((vertices[0].y*imageViewHeight)/height)

                    Log.d(TAG, "ImageViewPoint: ${topLeftPoint.x.toFloat()}, ${topLeftPoint.y.toFloat()}")
                    Log.d(TAG, "screenVertices0: ${screenVertices[0].x.toFloat()}, ${screenVertices[0].y.toFloat()}")
                    Log.d(TAG, "screenVertices1: ${screenVertices[1].x.toFloat()}, ${screenVertices[1].y.toFloat()}")
                    Log.d(TAG, "screenVertices2: ${screenVertices[2].x.toFloat()}, ${screenVertices[2].y.toFloat()}")
                    Log.d(TAG, "screenVertices3: ${screenVertices[3].x.toFloat()}, ${screenVertices[3].y.toFloat()}")


                    val drgViewContainer = findViewById<DraggableViewContainer>(R.id.draggableViewContainer)
                    val rect = imageView.drawable.bounds
                    drgViewContainer.setViewPosition(R.id.circleLeftTop, screenVertices[0].x, screenVertices[0].y)
                    drgViewContainer.setViewPosition(R.id.circleRightTop, screenVertices[1].x, screenVertices[1].y)
                    drgViewContainer.setViewPosition(R.id.circleRightBottom, screenVertices[2].x, screenVertices[2].y)
                    drgViewContainer.setViewPosition(R.id.circleLeftBottom, screenVertices[3].x, screenVertices[3].y)

//                    drgViewContainer.setViewPosition(R.id.circleLeftTop, rect.left.toDouble(), rect.top.toDouble())
//                    drgViewContainer.setViewPosition(R.id.circleRightTop, rect.right.toDouble(), rect.top.toDouble())
//                    drgViewContainer.setViewPosition(R.id.circleRightBottom, rect.right.toDouble(), rect.top.toDouble())
//                    drgViewContainer.setViewPosition(R.id.circleLeftBottom, rect.left.toDouble(), rect.bottom.toDouble())
                }



                // 当获取到宽度和高度后，移除监听器，以免重复回调
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
//        val imageViewWidth = imageView.width
//        val imageViewHeight = imageView.height
//
//        val imageViewLeft = imageView.left
//        val imageViewTop = imageView.top
//
//
//        // 计算左上角的坐标
//        val topLeftPoint = Point(imageViewLeft.toDouble(), imageViewTop.toDouble())
//
//        val screenVertices = Array<Point>(4) { Point() }
//
//        screenVertices[0].x=topLeftPoint.x+((vertices[1].x*imageViewWidth)/width)
//        screenVertices[0].y=topLeftPoint.y+((vertices[1].y*imageViewHeight)/height)
//        screenVertices[1].x=topLeftPoint.x+((vertices[2].x*imageViewWidth)/width)
//        screenVertices[1].y=topLeftPoint.y+((vertices[2].y*imageViewHeight)/height)
//        screenVertices[2].x=topLeftPoint.x+((vertices[3].x*imageViewWidth)/width)
//        screenVertices[2].y=topLeftPoint.y+((vertices[3].y*imageViewHeight)/height)
//        screenVertices[3].x=topLeftPoint.x+((vertices[0].x*imageViewWidth)/width)
//        screenVertices[3].y=topLeftPoint.y+((vertices[0].y*imageViewHeight)/height)
//
//        Log.d(TAG, "ImageViewPoint: ${topLeftPoint.x.toFloat()}, ${topLeftPoint.y.toFloat()}")
//        Log.d(TAG, "screenVertices0: ${screenVertices[0].x.toFloat()}, ${screenVertices[0].y.toFloat()}")
//        Log.d(TAG, "screenVertices1: ${screenVertices[1].x.toFloat()}, ${screenVertices[1].y.toFloat()}")
//        Log.d(TAG, "screenVertices2: ${screenVertices[2].x.toFloat()}, ${screenVertices[2].y.toFloat()}")
//        Log.d(TAG, "screenVertices3: ${screenVertices[3].x.toFloat()}, ${screenVertices[3].y.toFloat()}")
//
//
//        val drgViewContainer = findViewById<DraggableViewContainer>(R.id.draggableViewContainer)
//        drgViewContainer.setViewPosition(R.id.circleLeftTop, screenVertices[0].x, screenVertices[0].y)
//        drgViewContainer.setViewPosition(R.id.circleRightTop, screenVertices[1].x, screenVertices[1].y)
//        drgViewContainer.setViewPosition(R.id.circleRightBottom, screenVertices[2].x, screenVertices[2].y)
//        drgViewContainer.setViewPosition(R.id.circleLeftBottom, screenVertices[3].x, screenVertices[3].y)



        // 定义原图中边框上的四个点
        val srcPoints = MatOfPoint2f(
            vertices[0], // 左下角
            vertices[1], // 左上角
            vertices[2], // 右上角
            vertices[3]  // 右下角
        )

//        // 定义目标图中的边框上的四个点
//        val dstWidth = bitmap.width
//        val dstHeight = bitmap.height
//        val dstPoints = MatOfPoint2f(
//            Point(0.0, 0.0),                     // 左上角
//            Point(dstWidth.toDouble(), 0.0),     // 右上角
//            Point(dstWidth.toDouble(), dstHeight.toDouble()), // 右下角
//            Point(0.0, dstHeight.toDouble())     // 左下角
//        )
//
//        // 计算透视变换矩阵
//        val perspectiveTransform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)
//
//        // 进行透视变换
//        val transformedMat = Mat()
//        val inputMatResult = Mat()
//        Utils.bitmapToMat(bitmap, inputMatResult)
//        Imgproc.warpPerspective(inputMatResult, transformedMat, perspectiveTransform, Size(dstWidth.toDouble(), dstHeight.toDouble()))
//
//        // 将 Mat 转换回 Bitmap
//        val resultBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(transformedMat, resultBitmap)
        return bitmap
        //return resultBitmap
    }

    private fun generateQRCode(data: String, size: Int): Bitmap? {
        try {
            val hints = Hashtable<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix: BitMatrix =
                qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hints)

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            qrCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

            return qrCodeBitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * 设置笔名dialog
     */
    private fun showSetPenNameDialog() {
        val dialog = Dialog(this, R.style.customDialog)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_set_name, null)
        val et_pen_name = view.findViewById<EditText>(R.id.et_pen_name)
        val tv_cancel = view.findViewById<TextView>(R.id.tv_cancel)
        val tv_ok = view.findViewById<TextView>(R.id.tv_ok)
        tv_cancel.setOnClickListener { dialog.dismiss() }
        tv_ok.setOnClickListener(View.OnClickListener {
            QR_String = et_pen_name.text.toString()
            var resultBitmap=generateQRCode(QR_String,500)
            imageView.setImageBitmap(resultBitmap)
            dialog.dismiss()
        })
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        //window.getDecorView().setPadding(0, 0, 0, 0);
        val layoutParams = window.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
        dialog.show()
    }


    companion object {
        const val TAG = "CropActivity"
    }
}





