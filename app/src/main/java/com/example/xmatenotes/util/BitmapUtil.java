package com.example.xmatenotes.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    /**
     * 解析图片资源区域显示
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @param rect
     * @return
     */
    public static Bitmap decodeRectFOfBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight, Rect rect) {
        InputStream in = res.openRawResource(resId);//资源id转换为InputStream

        BitmapRegionDecoder bitmapRegionDecoder = null;
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(in, false);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "资源解析失败！");
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bp = bitmapRegionDecoder.decodeRegion(rect, options);

        return createScaledBitmap(bp, reqWidth, reqHeight);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
//        return decodeSampledBitmapFromStream(res.openRawResource(resId), reqWidth, reqHeight);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, resId, options);

        LogUtil.e(TAG, "options.outWidth: " + options.outWidth + " options.outHeight: " + options.outHeight);

        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        options.inMutable =true;

//        Bitmap bm = BitmapFactory.decodeResource(res, resId, options).copy(Bitmap.Config.ARGB_8888, true);
        Bitmap bm = BitmapFactory.decodeResource(res, resId, options);

        return createScaledBitmap(bm, reqWidth, reqHeight);
    }

    /**
     * 效率更高
     * @param ins
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromStream(InputStream ins, int reqWidth, int reqHeight) {
        try {
            return decodeSampledBitmapFromByteArray(inputStream2ByteArr(ins),reqWidth, reqHeight);
            //会出现资源释放失败的问题
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] byteArr, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteArr,0,byteArr.length,options);
        LogUtil.e(TAG, "options.outWidth: " + options.outWidth + " options.outHeight: " + options.outHeight);

        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        options.inMutable =true;
//        options.inBitmap = reusable;
        Bitmap bitmap =  BitmapFactory.decodeByteArray(byteArr,0,byteArr.length,options);
        return createScaledBitmap(bitmap, reqWidth, reqHeight);
    }

    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        //Raw height and width of image
        int inSampleSize = 1;

        if (height > reqHeight || width > reqHeight) {
            // 计算出实际宽高和目标宽高的比率
//            final int heightRatio = Math.round((float) height / (float) reqHeight);
//            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
//            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            Log.e(TAG, "halfHeight: " + halfHeight + " halfWidth： " + halfWidth + " inSampleSize: " + inSampleSize + " reqHeight: " + reqHeight + " reqWidth: " + reqWidth);
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        LogUtil.e(TAG, "calculateInSampleSize: inSampleSize: "+inSampleSize);
        return inSampleSize;

    }

    public static Bitmap createScaledBitmap(Bitmap bmp, int reqWidth, int reqHeight) {
        float widthRatio = reqWidth * 1.0f / bmp.getWidth();
        float heightRatio = reqHeight * 1.0f / bmp.getHeight();
        float ratio = Math.min(widthRatio, heightRatio);

        return Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth()*ratio), (int) (bmp.getHeight()*ratio), true);
    }

    /**
     * 旋转图片
     * @param b
     * @param rotateDegree
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap b, float rotateDegree) {
        if (b == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        return rotaBitmap;
    }

    /**
     * 回收bitmap
     * @param bitmap
     */
    public static void recycleBitmap(Bitmap bitmap){
        // 先判断是否已经回收
        if(bitmap != null && !bitmap.isRecycled()){
            // 回收并且置为null
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }

    /**
     * 将输入流转为为字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] inputStream2ByteArr(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, len);
        }
        inputStream.close();
        outputStream.close();
        return outputStream.toByteArray();
    }


}
