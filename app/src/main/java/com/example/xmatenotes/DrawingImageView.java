package com.example.xmatenotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Stack;

public class DrawingImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private Path mPath;
    private Stack<Path> mPaths = new Stack<>(); // 新增保存路径的栈
    private Stack<Path> mUndonePaths = new Stack<>(); // 新增保存被撤销路径的栈
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    public DrawingImageView(@NonNull Context context) {
        super(context);
        init();
    }

    public DrawingImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(2f);
        mPaint.setStyle(Paint.Style.STROKE);
        mBitmap = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        for (Path p : mPaths) { // 修改为绘制所有路径
            canvas.drawPath(p, mPaint);
        }
    }


    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        invalidate();
    }

//    @Override
//    public void setImageBitmap(Bitmap bm) {
//        // 获取 ImageView 的尺寸
//        int imageViewWidth = this.getWidth();
//        int imageViewHeight = this.getHeight();
//
//        // 获取 Bitmap 的尺寸
//        int bitmapWidth = bm.getWidth();
//        int bitmapHeight = bm.getHeight();
//
//        // 计算缩放比例和对齐的偏移量
//        float scaleFactor = Math.min((float) imageViewWidth / (float) bitmapWidth, (float) imageViewHeight / (float) bitmapHeight);
//        float offsetX = (imageViewWidth - bitmapWidth * scaleFactor) / 2;
//        float offsetY = (imageViewHeight - bitmapHeight * scaleFactor) / 2;
//
//        // 创建一个新的矩阵进行变换
//        Matrix matrix = new Matrix();
//        matrix.preScale(scaleFactor, scaleFactor);
//        matrix.postTranslate(offsetX, offsetY);
//
//        // 使用原始 Bitmap 和矩阵创建一个新的 Bitmap
//        Bitmap scaledBitmap = Bitmap.createBitmap(bm, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
//
//        // 使用新的 Bitmap 调用父类方法
//        super.setImageBitmap(scaledBitmap);
//    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path(); // 新增创建新路径
                mPaths.push(mPath); // 新增将新路径推入栈中
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }





        public void undo() {
            if (mPaths.size() > 0) {
                mUndonePaths.push(mPaths.pop());
                invalidate();
            }
        }

        public void redo() {
            if (mUndonePaths.size() > 0) {
                mPaths.push(mUndonePaths.pop());
                invalidate();
            }
        }

    public void commit() {
        // 创建一个新的Bitmap，尺寸和当前的ImageView一样大
        Bitmap committedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // 创建一个新的Canvas，我们将在这个Canvas上绘制图像和所有的笔迹
        Canvas committedCanvas = new Canvas(committedBitmap);

        // 在新的Canvas上绘制原始的图像
        committedCanvas.drawBitmap(mBitmap, 0, 0, null);

        // 然后，在新的Canvas上绘制所有的笔迹
        for (Path p : mPaths) {
            committedCanvas.drawPath(p, mPaint);
        }

        // 设置新的Bitmap为ImageView的图像
        mBitmap = committedBitmap;

        // 清空所有的路径
        mPaths.clear();
        mUndonePaths.clear();
        invalidate();
    }


    private void touchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

}
