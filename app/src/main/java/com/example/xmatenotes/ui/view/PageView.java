package com.example.xmatenotes.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.xmatenotes.logic.manager.CoordinateConverter;
import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;
import com.example.xmatenotes.logic.model.handwriting.Stroke;
import com.example.xmatenotes.ui.CardshowActivity;
import com.example.xmatenotes.ui.HWReplayActivity;
import com.example.xmatenotes.ui.PageViewActivity;
import com.example.xmatenotes.util.BitmapUtil;
import com.example.xmatenotes.util.LogUtil;
import com.tqltech.tqlpencomm.bean.Dot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 支持NUI版面的View
 */
public class PageView extends AppCompatImageView {

    private static final String TAG = "PageView";

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Matrix mMatrix;
    private Paint mPaint;
    private Path mPath;

    private PageViewActivity pageViewActivity = null;
    private HWReplayActivity hwReplayActivity = null;

    //坐标转换器
    private CoordinateConverter screenCoordinateConverter;
    private CoordinateConverter bitmapCoordinateConverter;

    private SimpleDot lastSimpleDot = null;

    private float pWidth = 2f;


    public PageView(@NonNull Context context) {
        super(context);
        init();
    }

    public PageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initPaint();
        mBitmap = null;
        mMatrix = new Matrix();
        mPath = new Path();

        if (getContext() instanceof PageViewActivity) {
            this.pageViewActivity = (PageViewActivity) getContext();
        }

        if(getContext() instanceof HWReplayActivity){
            this.hwReplayActivity = (HWReplayActivity) getContext();
        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    LogUtil.e(TAG, "init: mBitmap: "+mBitmap);
//                    LogUtil.e(TAG, "init: getDrawable(): "+getDrawable());
////                    LogUtil.e(TAG, "init: pageViewActivity.bitmap:  "+pageViewActivity.bitmap);
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }).start();


    }


    /**
     * 获取内部图片在屏幕上的真实坐标范围
     *
     * @return
     */
    public RectF getIntrinsicRectF() {
        Matrix matrix = getImageMatrix();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            RectF rectF = new RectF();
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
            return rectF;
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        canvas.drawRect(new Rect(10, 10, 300, 300), mPaint);
        Log.e(TAG, "onDraw: mBitmap: "+mBitmap);
//        Log.e(TAG, "onDraw: pageViewActivity.bitmap: "+pageViewActivity.bitmap);
        Log.e(TAG, "onDraw: getDrawable(): "+getDrawable());
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        }
//        canvas.drawBitmap(pageViewActivity.bitmap, mMatrix, mPaint);
//        RectF rectF = getIntrinsicRectF();

//        if (this.coordinateConverter == null && this.pageActivity != null) {
//            LogUtil.e(TAG, "getWidth(): " + getWidth() + " getHeight(): " + getHeight());
//            RectF rectF = getIntrinsicRectF();
//            setCoordinateConverter(this.pageActivity.getCoordinateConverter(rectF.left, rectF.top, rectF.width(), rectF.height()));
//        }

//        if(rectF != null){
//            mPaint.setColor(Color.RED);
//            canvas.drawRect(rectF, mPaint);
//            LogUtil.e(TAG, "onDraw: rectF: "+rectF);
//        }

//
//        mPaint.setColor(Color.BLACK);
        //绘画正在画的路径
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    public void drawDots(List<SingleHandWriting> singleHandWritingList) {
        mPath = drawDots(singleHandWritingList, this.screenCoordinateConverter, null);
        postInvalidate();
    }

    public void drawDots(List<SingleHandWriting> singleHandWritingList, CoordinateConverter.CoordinateCropper cropper) {
        mPath = drawDots(singleHandWritingList, this.screenCoordinateConverter, cropper);
        postInvalidate();
    }

    public void drawAddDots(SingleHandWriting singleHandWriting, CoordinateConverter.CoordinateCropper cropper) {
        drawAddDots(new ArrayList<SingleHandWriting>(){
            {
                add(singleHandWriting);
            }
        }, cropper);
    }

    public void drawAddDots(List<SingleHandWriting> singleHandWritingList, CoordinateConverter.CoordinateCropper cropper) {
        mPath.addPath(drawDots(singleHandWritingList, this.screenCoordinateConverter, cropper));
        postInvalidate();
    }

    public Path drawDots(List<SingleHandWriting> singleHandWritingList, CoordinateConverter converter, CoordinateConverter.CoordinateCropper cropper) {
        LogUtil.e(TAG, "drawDots: 绘制笔迹");
        Path path = new Path();
        LogUtil.e(TAG, "drawDots: converter != null: "+(converter != null));
        if (converter != null) {
            for (SingleHandWriting singleHandWriting : singleHandWritingList) {
                //旧笔迹不绘制
                if (!singleHandWriting.isNew()) {
                    LogUtil.e(TAG, "drawDots: 旧笔迹不绘制");
                    continue;
                }
                for (HandWriting handWriting : singleHandWriting.getHandWritings()) {
                    for (Stroke stroke : handWriting.getStrokes()) {
                        for (SimpleDot simpleDot : stroke.getDots()) {
                            SimpleDot outSimpleDot = simpleDot;
                            if(cropper != null){
                                outSimpleDot = cropper.cropIn(simpleDot);
                            }
                            //将内部真实物理坐标转换为UI坐标
                            outSimpleDot = converter.convertOut(outSimpleDot);
                            if (simpleDot instanceof MediaDot) {
                            }
                            LogUtil.e(TAG, "drawDots: 绘制" + outSimpleDot);
                            if(outSimpleDot.type == Dot.DotType.PEN_DOWN){
                                path.moveTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
                            }
                            if(outSimpleDot.type == Dot.DotType.PEN_MOVE){
                                path.lineTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
                            }
                            if(outSimpleDot.type == Dot.DotType.PEN_UP){
                                path.lineTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
                            }
//                            switch (outSimpleDot.type) {
//                                case PEN_DOWN:
//                                    path.moveTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
//                                    break;
//                                case PEN_MOVE:
//                                case PEN_UP:
//                                    path.lineTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
//                                    break;
//                            }
                        }
                    }
                }
            }
        }
        return path;
    }

    public void drawLineDots(SingleHandWriting singleHandWriting, CoordinateConverter.CoordinateCropper cropper) {
        drawLineDots(new ArrayList<SingleHandWriting>(){
            {
                add(singleHandWriting);
            }
        }, cropper);
    }

    public void drawLineDots(List<SingleHandWriting> singleHandWritingList, CoordinateConverter.CoordinateCropper cropper) {
        drawLineDots(singleHandWritingList, this.bitmapCoordinateConverter, cropper);
        postInvalidate();
    }

    public void drawLineDots(List<SingleHandWriting> singleHandWritingList, CoordinateConverter converter, CoordinateConverter.CoordinateCropper cropper) {
        LogUtil.e(TAG, "drawLineDots");
        LogUtil.e(TAG, "drawLineDots: converter != null: "+(converter != null));
        if (converter != null) {
            SimpleDot lastSimpleDot = null;
            for (SingleHandWriting singleHandWriting : singleHandWritingList) {
                //旧笔迹不绘制
                if (!singleHandWriting.isNew()) {
                    LogUtil.e(TAG, "drawLineDots: 旧笔迹不绘制");
                    continue;
                }
                for (HandWriting handWriting : singleHandWriting.getHandWritings()) {
                    for (Stroke stroke : handWriting.getStrokes()) {
                        for (SimpleDot simpleDot : stroke.getDots()) {
                            SimpleDot outSimpleDot = simpleDot;
                            LogUtil.e(TAG, "drawLineDot: 绘制前outSimpleDot: " + outSimpleDot);
                            if(cropper != null){
                                outSimpleDot = cropper.cropIn(simpleDot);
                            }
                            //将内部真实物理坐标转换为UI坐标
                            outSimpleDot = converter.convertOut(outSimpleDot);
                            if (outSimpleDot instanceof MediaDot) {

                            }
                            LogUtil.e(TAG, "drawLineDot: 绘制后outSimpleDot:" + outSimpleDot);
                            if(outSimpleDot.type == Dot.DotType.PEN_DOWN){
                                LogUtil.e(TAG, "drawLineDots: PEN_DOWN: lastSimpleDot: "+lastSimpleDot);
                                lastSimpleDot = outSimpleDot;
                            }
                            if(outSimpleDot.type == Dot.DotType.PEN_MOVE){
                                mCanvas.drawLine(lastSimpleDot.getFloatX(), lastSimpleDot.getFloatY(), outSimpleDot.getFloatX(), outSimpleDot.getFloatY(), mPaint);
                                LogUtil.e(TAG, "drawLineDots: PEN_MOVE: "+lastSimpleDot+" - "+outSimpleDot);
                                lastSimpleDot = outSimpleDot;
                            }
                            if(outSimpleDot.type == Dot.DotType.PEN_UP){
                                mCanvas.drawLine(lastSimpleDot.getFloatX(), lastSimpleDot.getFloatY(), outSimpleDot.getFloatX(), outSimpleDot.getFloatY(), mPaint);
                                LogUtil.e(TAG, "drawLineDots: PEN_UP: "+lastSimpleDot+" - "+outSimpleDot);
                                lastSimpleDot = outSimpleDot;
                            }

                        }
                    }
                }
            }
        }
    }

    public void drawLineDot(SimpleDot simpleDot, CoordinateConverter.CoordinateCropper cropper) {
        drawLineDot(simpleDot, this.bitmapCoordinateConverter, cropper);
        postInvalidate();
    }

    public void drawLineDot(SimpleDot simpleDot, CoordinateConverter converter, CoordinateConverter.CoordinateCropper cropper) {
        LogUtil.e(TAG, "drawLineDot");
        LogUtil.e(TAG, "drawLineDot: converter != null: "+(converter != null));
        if (converter != null) {
            SimpleDot outSimpleDot = simpleDot;
            LogUtil.e(TAG, "drawLineDot: 绘制前outSimpleDot: " + outSimpleDot);
            if(cropper != null){
                outSimpleDot = cropper.cropIn(simpleDot);
            }
            //将内部真实物理坐标转换为UI坐标
            outSimpleDot = converter.convertOut(outSimpleDot);

            LogUtil.e(TAG, "drawLineDot: 绘制后outSimpleDot:" + outSimpleDot);
            if(outSimpleDot.type == Dot.DotType.PEN_DOWN){
                LogUtil.e(TAG, "drawLineDot: PEN_DOWN: lastSimpleDot: "+lastSimpleDot);
                lastSimpleDot = outSimpleDot;
            }
            if(outSimpleDot.type == Dot.DotType.PEN_MOVE){
                mCanvas.drawLine(lastSimpleDot.getFloatX(), lastSimpleDot.getFloatY(), outSimpleDot.getFloatX(), outSimpleDot.getFloatY(), mPaint);
                LogUtil.e(TAG, "drawLineDot: PEN_MOVE: "+lastSimpleDot+" - "+outSimpleDot);
                lastSimpleDot = outSimpleDot;
            }
            if(outSimpleDot.type == Dot.DotType.PEN_UP){
                mCanvas.drawLine(lastSimpleDot.getFloatX(), lastSimpleDot.getFloatY(), outSimpleDot.getFloatX(), outSimpleDot.getFloatY(), mPaint);
                LogUtil.e(TAG, "drawLineDot: PEN_UP: "+lastSimpleDot+" - "+outSimpleDot);
                lastSimpleDot = outSimpleDot;
            }
        }
    }

    public void drawPathDot(MediaDot mediaDot, CoordinateConverter.CoordinateCropper cropper) {
        drawPathDot(mediaDot, this.screenCoordinateConverter, cropper);
        postInvalidate();
    }

    public Path drawPathDot(MediaDot mediaDot, CoordinateConverter converter, CoordinateConverter.CoordinateCropper cropper) {
        LogUtil.e(TAG, "drawDot: 绘制笔迹点");
        LogUtil.e(TAG, "drawDot: converter != null: "+(converter != null));
        if (converter != null && !this.mPath.isEmpty()) {
            SimpleDot outSimpleDot = mediaDot;
            if(cropper != null){
                outSimpleDot = cropper.cropIn(mediaDot);
            }
            //将内部真实物理坐标转换为UI坐标
            outSimpleDot = converter.convertOut(outSimpleDot);

            LogUtil.e(TAG, "drawDot: 绘制" + outSimpleDot);
            if(outSimpleDot.type == Dot.DotType.PEN_DOWN){
                mPath.moveTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
            }
            if(outSimpleDot.type == Dot.DotType.PEN_MOVE){
                mPath.lineTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
            }
            if(outSimpleDot.type == Dot.DotType.PEN_UP){
                mPath.lineTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
            }
        }
        return mPath;
    }

    //处理位图的缩放和平移
    @Override
    public void setImageBitmap(Bitmap bm) {
//        setImageDrawable(new BitmapDrawable(pageViewActivity.getResources(), bm));
//        super.setImageBitmap(pageViewActivity.bitmap);
        super.setImageBitmap(bm);
//        com.example.xmatenotes.util.BitmapUtil.recycleBitmap(mBitmap);
//        mBitmap = bm;
//        mBitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
        mBitmap = bm;
        LogUtil.e(TAG, "setImageBitmap: mBitmap: "+mBitmap);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    LogUtil.e(TAG, "run: mBitmap: "+mBitmap);
//                }
//            }
//        }).start();
        mCanvas = new Canvas(mBitmap);
        calculateMatrix();
        RectF rectF = getIntrinsicRectF();
        LogUtil.e(TAG, "setImageBitmap: getIntrinsicRectF(): "+rectF);
        if(this.pageViewActivity != null){
            setScreenCoordinateConverter(this.pageViewActivity.getCoordinateConverter(rectF.left, rectF.top, rectF.width(), rectF.height()));
            setBitmapCoordinateConverter(this.pageViewActivity.getCoordinateConverter(0, 0, mBitmap.getWidth(), mBitmap.getHeight()));
        }
        if(this.hwReplayActivity != null){
            setScreenCoordinateConverter(this.hwReplayActivity.getCoordinateConverter(rectF.left, rectF.top, rectF.width(), rectF.height()));
            setBitmapCoordinateConverter(this.hwReplayActivity.getCoordinateConverter(0, 0, mBitmap.getWidth(), mBitmap.getHeight()));
        }

//        LogUtil.e(TAG, "setImageBitmap: mBitmap 1: "+mBitmap);
        postInvalidate();
//        LogUtil.e(TAG, "setImageBitmap: mBitmap 2: "+mBitmap);
//        Log.e(TAG, "setImageBitmap: getDrawable(): "+getDrawable());
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    public void setImageRes(Resources res, int resId) {
//        setImageStream(res.openRawResource(resId));
        setImageBitmap(BitmapUtil.INSTANCE.decodeSampledBitmapFromResource(res, resId, getWidth(), getHeight()));
    }

    public void setImageStream(InputStream in) {
        LogUtil.e(TAG, "setImageStream(): getWidth(): " + getWidth() + " getHeight(): " + getHeight());
        setImageBitmap(BitmapUtil.INSTANCE.decodeSampledBitmapFromStream(in, getWidth(), getHeight()));
    }

    /**
     * 初始化画笔配置
     */
    public void initPaint(){
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(pWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);//锯齿不显示
        mPaint.setDither(true);  //防抖动
        mPaint.setStrokeJoin(Paint.Join.ROUND);//设置线段连接处样式为圆弧
        mPaint.setStrokeCap(Paint.Cap.ROUND);//设置线冒样式为圆形线冒
    }

    public void setPenWidth ( int w){
        this.mPaint.setStrokeWidth(w);
    }

    public void setPenColor ( int color){
        mPaint.setColor(color);
    }

    public void setPaintSize(float size) {
        mPaint.setTextSize(size);
    }

    public void setPaintTypeface(Typeface typeface) {
        mPaint.setTypeface(typeface);
    }

    /**
     * 设置UI坐标到真实物理坐标的转换参数
     *
     * @param screenCoordinateConverter
     * @return
     */
    public void setScreenCoordinateConverter(CoordinateConverter screenCoordinateConverter) {
        this.screenCoordinateConverter = screenCoordinateConverter;
        LogUtil.e(TAG, "配置屏幕坐标转换器: "+this.screenCoordinateConverter.toString());
    }

    public CoordinateConverter getScreenCoordinateConverter() {
        return screenCoordinateConverter;
    }

    public CoordinateConverter getBitmapCoordinateConverter() {
        return bitmapCoordinateConverter;
    }

    /**
     * 设置bitmap坐标到真实物理坐标的转换参数
     * @param bitmapCoordinateConverter
     */
    public void setBitmapCoordinateConverter(CoordinateConverter bitmapCoordinateConverter) {
        this.bitmapCoordinateConverter = bitmapCoordinateConverter;
        LogUtil.e(TAG, "配置bitmap坐标转换器: "+this.bitmapCoordinateConverter.toString());
    }

    public void  setCoordinateConverter(float pageRealWidth, float pageRealHeight, RectF subRectF, RectF superRectF){
        RectF rectF = getIntrinsicRectF();
        RectF viewRectF = new RectF(rectF.left, rectF.top, rectF.left+rectF.width(), rectF.top+rectF.height());
        LogUtil.e(TAG, "setCoordinateConverter: 转换前viewRectF: "+viewRectF);
        viewRectF = BitmapUtil.INSTANCE.mapRect(subRectF, superRectF, viewRectF);
        LogUtil.e(TAG, "setCoordinateConverter: 转换后viewRectF: "+viewRectF);
        if(this.pageViewActivity != null){
            setScreenCoordinateConverter(new CoordinateConverter(viewRectF.left, viewRectF.top, viewRectF.width(), viewRectF.height(),pageRealWidth, pageRealHeight));
        }
        if(this.hwReplayActivity != null){
            setScreenCoordinateConverter(new CoordinateConverter(viewRectF.left, viewRectF.top, viewRectF.width(), viewRectF.height(),pageRealWidth, pageRealHeight));
        }
    }

    //利用矩阵缩放位图至合适大小
    private void calculateMatrix() {
        if (mBitmap != null) {
            float viewWidth = getWidth();
            float viewHeight = getHeight();
            float bitmapWidth = mBitmap.getWidth();
            float bitmapHeight = mBitmap.getHeight();

            float scale;
            float offsetX = 0;
            float offsetY = 0;

            if (bitmapWidth / viewWidth > bitmapHeight / viewHeight) {
                scale = viewWidth / bitmapWidth;
                offsetY = (viewHeight - bitmapHeight * scale) / 2;
            } else {
                scale = viewHeight / bitmapHeight;
                offsetX = (viewWidth - bitmapWidth * scale) / 2;
            }

            mMatrix.setScale(scale, scale);
            mMatrix.postTranslate(offsetX, offsetY);
        }
    }

    //处理触摸事件，开始、移动和结束绘制路径
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        SimpleDot simpleDot = new SimpleDot(x, y);
        simpleDot.timelong = System.currentTimeMillis();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                simpleDot.type = Dot.DotType.PEN_DOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                simpleDot.type = Dot.DotType.PEN_MOVE;
                break;
            case MotionEvent.ACTION_UP:
                simpleDot.type = Dot.DotType.PEN_UP;
                break;
        }

        LogUtil.e(TAG, "触摸点：" + simpleDot);
        SimpleDot inSimpleDot = simpleDot;
        //将UI坐标转换为内部真实物理坐标
        if (this.screenCoordinateConverter != null) {
            inSimpleDot = this.screenCoordinateConverter.convertIn(simpleDot);
        }

        if (this.pageViewActivity != null) {
            this.pageViewActivity.processEachDot(inSimpleDot);
        }

        return true;
    }

    /**
     * 重置路径
     */
    public void resetPath(){
        this.mPath.reset();
    }

}

