package com.example.xmatenotes.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.util.LruCache
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

object BitmapUtil {

    private const val TAG = "BitmapUtil"

    private val cache: LruCache<String, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8 // 设置缓存大小为最大内存的1/8
        cache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // 返回每个Bitmap的大小（以KB为单位）
                return bitmap.byteCount / 1024
            }
        }
    }

    fun getBitmap(key: String): Bitmap? {
        return cache.get(key)
    }

    fun putBitmap(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun removeBitmap(key: String) {
        if (cache.get(key) != null) {
            cache.remove(key)
        } else {
            LogUtil.e(TAG, "removeBitmap: ")
        }
    }

    fun clearCache() {
        cache.evictAll()
    }

    /**
     * 解析图片资源区域显示
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @param rect
     * @return
     */
    fun decodeRectFOfBitmapFromResource(
        res: Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int,
        rect: Rect?
    ): Bitmap? {
        val `in` = res.openRawResource(resId) //资源id转换为InputStream
        var bitmapRegionDecoder: BitmapRegionDecoder? = null
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(`in`, false)
        } catch (e: IOException) {
            e.printStackTrace()
            LogUtil.e(TAG, "资源解析失败！")
        }
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bp = bitmapRegionDecoder!!.decodeRegion(rect, options)
        return createScaledBitmap(bp, reqWidth, reqHeight)
    }

    fun decodeSampledBitmapFromResource(
        res: Resources?,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
//        return decodeSampledBitmapFromStream(res.openRawResource(resId), reqWidth, reqHeight);
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)
        LogUtil.e(
            TAG,
            "options.outWidth: " + options.outWidth + " options.outHeight: " + options.outHeight
        )
        options.inSampleSize =
            calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inMutable = true

//        Bitmap bm = BitmapFactory.decodeResource(res, resId, options).copy(Bitmap.Config.ARGB_8888, true);
        val bm = BitmapFactory.decodeResource(res, resId, options)
        return createScaledBitmap(bm, reqWidth, reqHeight)
    }

    /**
     * 效率更高
     * @param ins
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    fun decodeSampledBitmapFromStream(ins: InputStream, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            decodeSampledBitmapFromByteArray(inputStream2ByteArr(ins), reqWidth, reqHeight)
            //会出现资源释放失败的问题
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//
//        try {
//            LogUtil.e(TAG, "decodeSampledBitmapFromStream: ins.available(): "+(ins.available()));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        BitmapFactory.decodeStream(ins, null, options);
////        BitmapFactory.decodeResource(res, resId, options);
//
//        LogUtil.e(TAG, "options.outWidth: " + options.outWidth + " options.outHeight: " + options.outHeight);
//
//        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
//
//        options.inJustDecodeBounds = false;
//        try {
//            LogUtil.e(TAG, "decodeSampledBitmapFromStream: ins.available(): "+(ins.available()));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        Bitmap bm = BitmapFactory.decodeStream(ins, null, options).copy(Bitmap.Config.ARGB_8888, true);;
////        Bitmap bm = BitmapFactory.decodeResource(res, resId, options).copy(Bitmap.Config.ARGB_8888, true);
//
//        return createScaledBitmap(bm, reqWidth, reqHeight);
    }

    fun decodeSampledBitmapFromByteArray(
        byteArr: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size, options)
        LogUtil.e(
            TAG,
            "options.outWidth: " + options.outWidth + " options.outHeight: " + options.outHeight
        )
        options.inSampleSize =
            calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inMutable = true
        //        options.inBitmap = reusable;
        val bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size, options)
        return createScaledBitmap(bitmap, reqWidth, reqHeight)
    }

    fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        //Raw height and width of image
        var inSampleSize = 1
        if (height > reqHeight || width > reqHeight) {
            // 计算出实际宽高和目标宽高的比率
//            final int heightRatio = Math.round((float) height / (float) reqHeight);
//            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
//            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            val halfHeight = height / 2
            val halfWidth = width / 2
            Log.e(
                TAG,
                "halfHeight: $halfHeight halfWidth： $halfWidth inSampleSize: $inSampleSize reqHeight: $reqHeight reqWidth: $reqWidth"
            )
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        LogUtil.e(
            TAG,
            "calculateInSampleSize: inSampleSize: $inSampleSize"
        )
        return inSampleSize
    }

    fun createScaledBitmap(bmp: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap {
        val widthRatio = reqWidth * 1.0f / bmp.width
        val heightRatio = reqHeight * 1.0f / bmp.height
        val ratio = Math.min(widthRatio, heightRatio)
        return Bitmap.createScaledBitmap(
            bmp,
            (bmp.width * ratio).toInt(),
            (bmp.height * ratio).toInt(),
            true
        )
    }

    //解析图片资源原始尺寸
    fun decodeDimensionOfImageFromResource(res: Resources?, resId: Int): Rect? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)
        return Rect(0, 0, options.outWidth, options.outHeight)
    }

    /**
     * subRect包含于superRect
     * @param subRect
     * @param superRect
     * @param newSuperRect
     * @return
     */
    fun mapRect(subRect: Rect, superRect: Rect, newSuperRect: Rect): Rect {
        val newSubRect = Rect()
        newSubRect.left =
            ((subRect.left - superRect.left).toDouble() / superRect.width() * newSuperRect.width() + newSuperRect.left).toInt()
        newSubRect.top =
            ((subRect.top - superRect.top).toDouble() / superRect.height() * newSuperRect.height() + newSuperRect.top).toInt()
        newSubRect.right =
            (newSuperRect.right - (superRect.right - subRect.right).toDouble() / superRect.width() * newSuperRect.width()).toInt()
        newSubRect.bottom =
            (newSuperRect.bottom - (superRect.bottom - subRect.bottom).toDouble() / superRect.height() * newSuperRect.height()).toInt()
        return newSubRect
    }

    /**
     * subRect包含于superRect
     * @param subRect
     * @param superRect
     * @param newSuperRect
     * @return
     */
    fun mapRect(subRect: RectF, superRect: RectF, newSuperRect: RectF): RectF {
        LogUtil.e(TAG,
            "输入: mapRect: subRect: $subRect superRect: $superRect newSuperRect: $newSuperRect"
        )
        val newSubRect = RectF()
        newSubRect.left =
            ((subRect.left - superRect.left).toDouble() / superRect.width() * newSuperRect.width() + newSuperRect.left).toFloat()
        newSubRect.top =
            ((subRect.top - superRect.top).toDouble() / superRect.height() * newSuperRect.height() + newSuperRect.top).toFloat()
        newSubRect.right =
            (newSuperRect.right - (superRect.right - subRect.right).toDouble() / superRect.width() * newSuperRect.width()).toFloat()
        newSubRect.bottom =
            (newSuperRect.bottom - (superRect.bottom - subRect.bottom).toDouble() / superRect.height() * newSuperRect.height()).toFloat()
        LogUtil.e(TAG, "mapRect: 输出: newSubRect: $newSubRect")
        return newSubRect
    }

    /**
     * 旋转图片
     * @param b
     * @param rotateDegree
     * @return
     */
    fun rotateBitmap(
        b: Bitmap?,
        rotateDegree: Float
    ): Bitmap? {
        if (b == null) {
            return null
        }
        val matrix = Matrix()
        matrix.postRotate(rotateDegree)
        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
    }

    /**
     * 回收bitmap
     * @param bitmap
     */
    fun recycleBitmap(bitmap: Bitmap?) {
        // 先判断是否已经回收
        var bitmap = bitmap
        if (bitmap != null && !bitmap.isRecycled) {
            // 回收并且置为null
            LogUtil.e(
                TAG,
                "recycleBitmap: 回收bitmap: $bitmap"
            )
            bitmap.recycle()
            bitmap = null
        }
        System.gc()
    }

    /**
     * 将输入流转为为字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun inputStream2ByteArr(inputStream: InputStream): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val buff = ByteArray(1024)
        var len = 0
        while (inputStream.read(buff).also { len = it } != -1) {
            outputStream.write(buff, 0, len)
        }
        inputStream.close()
        outputStream.close()
        return outputStream.toByteArray()
    }

}
