package com.example.xmatenotes.util

import android.graphics.*
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

/**
 * 二维码工具，包括二维码的生成、修饰
 */
object QRCodeUtil {

    private const val TAG = "QRCodeUtil"

    /**
     * 标题栏高度占二维码(带白边)边长的比例
     */
    private const val ACTIONBAR_HEIGHT_SCALE = 0.2F

    /**
     * 白边宽度占不带白边部分二维码边长的比例
     */
    private const val WHITE_MARGIN_SCALE = 1 / 14.0F

    /**
     * 迭代次数和对应颜色的映射
     * 当前迭代次数范围0~6
     */
    private val iterationToColorMap:Map<Int,Int> = mapOf(
        0 to Color.GRAY,
        1 to Color.rgb(5,102,8),//绿色
        2 to Color.rgb(255,204,0),//橘黄色
        3 to Color.rgb(128, 0, 128),//紫色
        4 to Color.rgb(0,0,255),//蓝色
        5 to Color.rgb(255,105,97),//淡红色
        6 to Color.rgb(0,255,255),//青色
    )

    /**
     * 供二维码迭代生成使用
     * @param text 二维码内容
     * @param qrBoundsWithoutMargin 不含白边部分二维码的矩形范围，会改动矩形至包含完整二维码
     * @param iteration 迭代次数 0~6
     */
    @JvmStatic
    fun generateQRCodeBitmap(text: String, qrBoundsWithoutMargin: RectF, iteration: Int): Bitmap? {
        return generateQRCodeBitmap(text, qrBoundsWithoutMargin, WHITE_MARGIN_SCALE, "271·NK·01", iterationToColorMap.get(iteration)!!)
    }

    /**
     * 供生成初代二维码使用
     * @param text 二维码内容
     * @param width 包含白边，不含标题栏的目标二维码宽
     * @param height 包含白边，不含标题栏的目标二维码高
     * @param iteration 迭代次数 0~6
     */
    @JvmStatic
    fun generateQRCodeBitmap(text: String, width: Int, height: Int, iteration: Int): Bitmap? {
        var widthWithoutMargin = width.toFloat() * (1 - 2* WHITE_MARGIN_SCALE);
        var heightWithoutMargin = height.toFloat() * (1 - 2* WHITE_MARGIN_SCALE);
        var rectF = RectF(0F, 0F, widthWithoutMargin, heightWithoutMargin)
        return generateQRCodeBitmap(text, rectF, WHITE_MARGIN_SCALE, "271·NK·01", iterationToColorMap.get(iteration)!!)
    }

    /**
     * * 生成不带白边的二维码Bitmap
     *
     * @param content       内容
     * @param widthPix      图片宽度
     * @param heightPix     图片高度
     * @param isDeleteWhite 是否删除白色边框
     * @return 生成的二维码Bitmap
     */
    @JvmStatic
    private fun createQRImageWithoutMargin(
        content: String?,
        widthPix: Int,
        heightPix: Int
    ): Bitmap? {
        return createQRImage(content, widthPix, heightPix, true)
    }

    /**
     * 生成二维码Bitmap
     *
     * @param content       内容
     * @param widthPix      图片宽度
     * @param heightPix     图片高度
     * @param isDeleteWhite 是否删除白色边框
     * @return 生成的二维码Bitmap
     */
    @JvmStatic
    private fun createQRImage(
        content: String?,
        widthPix: Int,
        heightPix: Int,
        isDeleteWhite: Boolean
    ): Bitmap? {
        var widthPix = widthPix
        var heightPix = heightPix
        return try {
            val hints: Hashtable<EncodeHintType, Any?> = Hashtable()
//            hints.put(EncodeHintType.CHARACTER_SET, "utf-8")
//            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            hints.put(EncodeHintType.MARGIN, if (isDeleteWhite) 1 else 0)
            var matrix =
                QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints)
//            var matrix =
//                QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix)
            if (isDeleteWhite) {
                //删除白边
                matrix = deleteWhite(matrix)
            }
            widthPix = matrix.width
            heightPix = matrix.height
            val pixels = IntArray(widthPix * heightPix)
            for (y in 0 until heightPix) {
                for (x in 0 until widthPix) {
                    if (matrix[x, y]) {
                        pixels[y * widthPix + x] = Color.BLACK
                    } else {
                        pixels[y * widthPix + x] = Color.WHITE
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.RGB_565)
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)
            bitmap
        } catch (e: Exception) {
            null
        }
    }


    /**
     * 删除白色边框
     *
     * @param matrix matrix
     * @return BitMatrix
     */
    @JvmStatic
    private fun deleteWhite(matrix: BitMatrix): BitMatrix {
        val rec = matrix.enclosingRectangle
        val resWidth = rec[2] + 1
        val resHeight = rec[3] + 1
        val resMatrix = BitMatrix(resWidth, resHeight)
        resMatrix.clear()
        for (i in 0 until resWidth) {
            for (j in 0 until resHeight) {
                if (matrix[i + rec[0], j + rec[1]]) resMatrix[i] = j
            }
        }
        return resMatrix
    }

    /**
     * 生成二维码Bitmap
     * @param qrBoundsWithOutMargin 为不含白边的矩形，返回的bitmap含有白边
     * @param marginScale 白边占不带白边的二维码部分的比例
     * @param color 迭代颜色
     */
    @JvmStatic
    private fun generateQRCodeBitmap(text: String, qrBoundsWithOutMargin: RectF, marginScale: Float, contentOfActionBar: String, color: Int): Bitmap? {
        return try {
            //生成大小为qrBoundsWithOutMargin不带白边的二维码Bitmap
            var qrCodeImgWithoutMargin = createQRImageWithoutMargin(text, qrBoundsWithOutMargin.width().toInt(), qrBoundsWithOutMargin.height().toInt())
            //放大btmap，得到大小为qrBoundsWithOutMargin不带白边的二维码Bitmap
            qrCodeImgWithoutMargin = qrCodeImgWithoutMargin?.let { Bitmap.createScaledBitmap(it, qrBoundsWithOutMargin.width().toInt(), qrBoundsWithOutMargin.height().toInt(), true) }

            //给不带白边二维码添加样式
            addStyle(qrCodeImgWithoutMargin, qrBoundsWithOutMargin, marginScale, contentOfActionBar, color)
        } catch (e: WriterException) {
            Log.e(TAG, "生成二维码时出错: ${e.message}")
            null
        }
    }

    /**
     * 给不带白边的二维码图片加样式，如白边、标题栏
     * @param qrCodeImgWithoutMargin 待添加样式二维码Bitmap
     * @param qrBoundsWithOutMargin 为不含白边的矩形，返回的bitmap含有白边
     * @param marginScale 白边占不带白边的二维码部分的比例
     * @param contentOfActionBar 标题栏内容
     * @param color 迭代颜色
     *
     */
    private fun addStyle(qrCodeImgWithoutMargin: Bitmap?, qrBoundsWithOutMargin: RectF, marginScale: Float, contentOfActionBar: String, color: Int): Bitmap? {
        return qrCodeImgWithoutMargin?.let {
            //计算边缘
            val margin = ((it.width ) * marginScale)
            val newWidth = it.width + 2 * margin
            val newHeight = it.height + 2 * margin

            //放大矩形
            amplify(qrBoundsWithOutMargin, newWidth, newHeight)

            //加白边
            var qrCodeImg = Bitmap.createBitmap(newWidth.toInt(), newHeight.toInt(), Bitmap.Config.RGB_565)
            var canvas = Canvas(qrCodeImg)
            canvas.drawColor(Color.WHITE)
            var paint = Paint()
            canvas.drawBitmap(qrCodeImgWithoutMargin, margin, margin, paint)

            //绘制标题栏及文字
            //标题栏高度
            val addHeiget = (newHeight * ACTIONBAR_HEIGHT_SCALE)
            qrBoundsWithOutMargin.top -= addHeiget
            val qrBitmap = Bitmap.createBitmap(newWidth.toInt(), (newHeight+addHeiget).toInt(), Bitmap.Config.RGB_565)
            canvas = Canvas(qrBitmap)

            //绘制二维码
            canvas.drawBitmap(qrCodeImg, 0F, addHeiget, paint)

            //绘制标题栏矩形
            paint.setColor(color)
            canvas.drawRect(0F, 0F, newWidth, addHeiget, paint)

            //绘制文字
            paint.textSize = (addHeiget*0.8).toFloat()

            //计算文字绘制基线
            var fm = paint.fontMetrics
            var center = addHeiget / 2.0
            var baselineY = center + (fm.bottom - fm.top)/2 - fm.bottom

            //计算文字绘制起始点横坐标
            var baselineX = (newWidth - paint.measureText(contentOfActionBar)) / 2.0

            paint.setColor(Color.WHITE)
            canvas.drawText(contentOfActionBar, baselineX.toFloat(), baselineY.toFloat(), paint)
            qrBitmap
        }
    }

    /**
     * 保持rectF中心坐标不变放大，将宽放大到width，将高放大到height
     */
    @JvmStatic
    fun amplify(rectF: RectF, width: Float, height: Float){
        rectF.left = rectF.left-(width-rectF.width())/2
        rectF.top = rectF.top-(height-rectF.height())/2
        rectF.right = rectF.left+width
        rectF.bottom = rectF.top+height
    }

    /*******************************************************************************************************/

    /**
     * rectF为不含白边的矩形，返回的bitmap含有白边
     */
    @JvmStatic
    fun generateQRCodeBitmap(text: String, rectF: RectF, colr: Color): Bitmap? {
        generateQRCodeRectF(text, rectF.width().toInt(), rectF.height().toInt())
            ?.let { amplify(it, rectF) }
        return generateQRCodeBitmap(text, rectF.width().toInt(), rectF.height().toInt())
    }


    /**
     * rectF为不含白边的矩形，返回的bitmap含有白边
     */
    @JvmStatic
    fun generateQRCodeBitmap(text: String, width: Int, height: Int): Bitmap? {
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
            Log.e(TAG, "生成二维码时出错: ${e.message}")
        }
        return null
    }

    /**
     * 返回不带白边的目标二维码矩形范围
     */
    @JvmStatic
    fun generateQRCodeRectF(text: String, width: Int, height: Int): RectF? {
        try {
            val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
            var rectF = RectF()
            var b = true
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if(bitMatrix[x, y]){
                        if(b){
                            rectF.left = x.toFloat()
                            rectF.right = x.toFloat()
                            rectF.top = y.toFloat()
                            rectF.bottom = y.toFloat()
                            b = false
                        }
                        rectF.union(x.toFloat(), y.toFloat())
                    }
                }
            }

            return rectF
        } catch (e: WriterException) {
            Log.e(TAG, "生成二维码时出错: ${e.message}")
        }
        return null
    }

    /**
     * 按照比例关系放大二维码矩形bigRectF(会变化)，将二维码不含白边的部分放大到全部
     */
    @JvmStatic
    fun amplify(smallRectF: RectF, bigRectF: RectF) : RectF{
        val newWidth = bigRectF.width()*bigRectF.width()/smallRectF.width()
        val newHeight = bigRectF.height()*bigRectF.height()/smallRectF.height()
        bigRectF.left = bigRectF.left-(newWidth-bigRectF.width())/2
        bigRectF.top = bigRectF.top-(newHeight-bigRectF.height())/2
        bigRectF.right = bigRectF.left+newWidth
        bigRectF.bottom = bigRectF.top+newHeight
        return bigRectF
    }

    /**
     * 生成时自动放大
     */
    @JvmStatic
    fun generateAmplifyQRCodeBitmap(text: String, rectF: RectF): Bitmap? {
        var rectF = generateQRCodeRectF(text, rectF.width().toInt(), rectF.height().toInt())?.let { amplify(it, rectF) }
        if (rectF != null) {
            return this.generateQRCodeBitmap(text, rectF.width().toInt(), rectF.height().toInt())
        }
        return null
    }

}