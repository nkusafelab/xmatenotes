package com.example.xmatenotes.ui.view;

import android.content.Context;
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
import com.example.xmatenotes.ui.PageActivity;
import com.example.xmatenotes.ui.qrcode.CardProcessActivity;
import com.example.xmatenotes.util.LogUtil;
import com.tqltech.tqlpencomm.bean.Dot;

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

    private PageActivity pageActivity = null;

    //坐标转换器
    private CoordinateConverter coordinateConverter;


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
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(2f);
        mPaint.setStyle(Paint.Style.STROKE);
        mBitmap = null;
        mMatrix = new Matrix();

        if (getContext() instanceof PageActivity) {
            this.pageActivity = (PageActivity) getContext();
        }

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

//        RectF rectF = getIntrinsicRectF();

        if (this.coordinateConverter == null && this.pageActivity != null) {
            LogUtil.e(TAG, "getWidth(): " + getWidth() + " getHeight(): " + getHeight());
            RectF rectF = getIntrinsicRectF();
            setCoordinateConverter(this.pageActivity.getCoordinateConverter(rectF.left, rectF.top, rectF.width(), rectF.height()));
        }

//        mPaint.setColor(Color.RED);
//        canvas.drawRect(rectF, mPaint);
//
//        mPaint.setColor(Color.BLACK);
        //绘画正在画的路径
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    public void drawDots(List<SingleHandWriting> singleHandWritingList) {
        mPath = drawDots(singleHandWritingList, this.coordinateConverter, null);
        postInvalidate();
    }

    public void drawDots(List<SingleHandWriting> singleHandWritingList, CoordinateConverter.CoordinateCropper cropper) {
        mPath = drawDots(singleHandWritingList, this.coordinateConverter, cropper);
        postInvalidate();
    }

    public Path drawDots(List<SingleHandWriting> singleHandWritingList, CoordinateConverter converter, CoordinateConverter.CoordinateCropper cropper) {
        LogUtil.e(TAG, "绘制笔迹");
        Path path = new Path();
        LogUtil.e(TAG, "converter != null: "+(converter != null));
        if (converter != null) {
            for (SingleHandWriting singleHandWriting : singleHandWritingList) {
                //旧笔迹不绘制
                if (!singleHandWriting.isNew()) {
                    LogUtil.e(TAG, "旧笔迹不绘制");
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
                            LogUtil.e(TAG, "绘制" + outSimpleDot);
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
            return path;
        }
        return null;
    }

    //处理位图的缩放和平移
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        mCanvas = new Canvas(mBitmap);
        calculateMatrix();
        invalidate();
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
     * @param coordinateConverter
     * @return
     */
    public void setCoordinateConverter(CoordinateConverter coordinateConverter) {
        this.coordinateConverter = coordinateConverter;
        LogUtil.e(TAG, "配置坐标转换器");
    }

    public CoordinateConverter getCoordinateConverter() {
        return coordinateConverter;
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
        if (this.coordinateConverter != null) {
            inSimpleDot = this.coordinateConverter.convertIn(simpleDot);
        }

        if (this.pageActivity != null) {
            this.pageActivity.processEachDot(inSimpleDot);
        }

        return true;
    }

}

