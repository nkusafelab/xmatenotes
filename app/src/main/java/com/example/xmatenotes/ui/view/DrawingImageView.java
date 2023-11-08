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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


import androidx.appcompat.widget.AppCompatImageView;

import com.example.xmatenotes.R;
import com.example.xmatenotes.logic.manager.CoordinateConverter;
import com.example.xmatenotes.logic.manager.Writer;
import com.example.xmatenotes.logic.model.handwriting.HandWriting;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.model.handwriting.SingleHandWriting;
import com.example.xmatenotes.logic.model.handwriting.Stroke;
import com.example.xmatenotes.ui.qrcode.CardProcessActivity;
import com.example.xmatenotes.util.LogUtil;
import com.tqltech.tqlpencomm.bean.Dot;

import java.util.ArrayList;
import java.util.List;

public class DrawingImageView extends AppCompatImageView {

    private static final String TAG = "DrawingImageView";
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Matrix mMatrix;
    private Paint mPaint;
    private Path mPath;
    private Writer writer = null;

    //坐标转换器
    private CoordinateConverter coordinateConverter;
    private CardProcessActivity cardProcessActivity = null;

    private List<Draw> mDraws = new ArrayList<>();
    private List<Draw> mUndoneDraws = new ArrayList<>();

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 10;
    private boolean mMoveEventTriggered;
    private float mDownX, mDownY;

    private abstract class Draw {
        long time;
    }

    //笔迹
    private class PathDraw extends Draw {
        Path path;
        int color;
        float textSize;
        Typeface typeface;
    }

    //字迹
    private class TextDraw extends Draw {
        String text;
        float x, y;
        int color;
        float textSize;
        Typeface typeface;

        //这个方法用于实时绘制
        public TextDraw(String text, float x, float y, int color, float textSize, Typeface typeface) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.textSize = textSize;
            this.typeface = typeface;
            this.time = System.currentTimeMillis();
        }
    }

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
        mMatrix = new Matrix();

        if(getContext() instanceof CardProcessActivity){
            this.cardProcessActivity = (CardProcessActivity) getContext();
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
//        if (mBitmap != null) {
//            canvas.drawBitmap(mBitmap, mMatrix, null);
//        }
        if(this.coordinateConverter == null && this.cardProcessActivity != null){
            RectF rectF = getIntrinsicRectF();
//            LogUtil.e(TAG, "getWidth(): " + getWidth() + " getHeight(): " + getHeight());
            LogUtil.e(TAG, "onDraw: rectF: "+rectF);
            setCoordinateConverter(this.cardProcessActivity.getCoordinateConverter(rectF.left, rectF.top, rectF.width(), rectF.height()));
        }

//        Rect rect = this.getDrawable().getBounds();

        //获取内部图片在屏幕上的真实坐标范围
//        Matrix matrix = getImageMatrix();
//        RectF rectF = new RectF();
//        Drawable drawable = getDrawable();
//        if (drawable != null) {
//            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//            matrix.mapRect(rectF);
//        }
//        canvas.drawRect(rectF,mPaint);

        //初始化设置属性等
//        for (Draw d : mDraws) {
//            if (d instanceof PathDraw) {
//                PathDraw pd = (PathDraw) d;
//                int originalColor = mPaint.getColor();
//                float originalSize = mPaint.getTextSize();
//                Typeface originalTypeface = mPaint.getTypeface();
//
//                mPaint.setColor(pd.color);
//                mPaint.setTextSize(pd.textSize);
//                mPaint.setTypeface(pd.typeface);
//
//                canvas.drawPath(pd.path, mPaint);
//
//                mPaint.setColor(originalColor);
//                mPaint.setTextSize(originalSize);
//                mPaint.setTypeface(originalTypeface);
//            } else if (d instanceof TextDraw) {
//                TextDraw t = (TextDraw) d;
//                int originalColor = mPaint.getColor();
//                float originalSize = mPaint.getTextSize();
//                Typeface originalTypeface = mPaint.getTypeface();
//
//                mPaint.setColor(t.color);
//                mPaint.setTextSize(t.textSize);
//                mPaint.setTypeface(t.typeface);
//
//                canvas.save();
//                canvas.drawText(t.text, t.x, t.y, mPaint);
//                canvas.restore();
//
//                mPaint.setColor(originalColor);
//                mPaint.setTextSize(originalSize);
//                mPaint.setTypeface(originalTypeface);
//            }
//        }

        //绘画正在画的路径
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    public void drawDots(List<SingleHandWriting> singleHandWritingList){
        mPath = drawDots(singleHandWritingList, this.coordinateConverter);
        postInvalidate();
    }
    public Path drawDots(List<SingleHandWriting> singleHandWritingList, CoordinateConverter converter){
        LogUtil.e(TAG, "绘制笔迹");
        Path path = new Path();
        if(converter != null){
            for (SingleHandWriting singleHandWriting: singleHandWritingList) {
                //旧笔迹不绘制
                if(!singleHandWriting.isNew()){
                    LogUtil.e(TAG,"旧笔迹不绘制");
                    continue;
                }
                for (HandWriting handWriting: singleHandWriting.getHandWritings()) {
                    for (Stroke stroke: handWriting.getStrokes()) {
                        for (SimpleDot simpleDot : stroke.getDots()) {
                            SimpleDot outSimpleDot = simpleDot;
                            //将UI坐标转换为内部真实物理坐标
                            outSimpleDot = converter.convertOut(simpleDot);
                            if(simpleDot instanceof MediaDot){
                            }
                            LogUtil.e(TAG, "绘制"+outSimpleDot);
                            switch (outSimpleDot.type){
                                case PEN_DOWN:
                                    path.moveTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
                                    break;
                                case PEN_MOVE:
                                case PEN_UP:
                                    path.lineTo(outSimpleDot.getFloatX(), outSimpleDot.getFloatY());
                                    break;
                            }
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

    /**
     * 设置UI坐标到真实物理坐标的转换参数
     * @param coordinateConverter
     * @return
     */
    public void setCoordinateConverter(CoordinateConverter coordinateConverter){
        this.coordinateConverter = coordinateConverter;
        LogUtil.e(TAG, "配置坐标转换器");
    }

    public CoordinateConverter getCoordinateConverter() {
        return coordinateConverter;
    }

    /**
     * 绑定writer
     * @param writer
     */
    public void bindWriter(Writer writer){
        this.writer = writer;
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
//                mDownX = x;
//                mDownY = y;
//                mMoveEventTriggered = false;
//                touchStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                simpleDot.type = Dot.DotType.PEN_MOVE;
//                touchMove(x, y);
//                if (distance(x, y, mDownX, mDownY) > TOUCH_TOLERANCE) {
//                    mMoveEventTriggered = true;
//                }
                break;
            case MotionEvent.ACTION_UP:
                simpleDot.type = Dot.DotType.PEN_UP;
//                if (!mMoveEventTriggered) {
//                    showDialog(x, y);
//                } else {
//                    touchUp();
//                }
//                mPath = null;
                break;
        }

        LogUtil.e(TAG, "触摸点："+simpleDot);
        SimpleDot inSimpleDot = simpleDot;
        //将UI坐标转换为内部真实物理坐标
        if(this.coordinateConverter != null){
            inSimpleDot = this.coordinateConverter.convertIn(simpleDot);
        }

        if(this.cardProcessActivity != null){
            this.cardProcessActivity.processEachDot(inSimpleDot);
        }

//        invalidate();
        return true;
    }

    public void bindActivity(CardProcessActivity cardProcessActivity ){
        this.cardProcessActivity = cardProcessActivity;
    }

    //弹出对话框输入文本，并进行实时绘制
    public void showDialog(final float x, final float y) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext(), R.style.TransparentDialog);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom, null);
        alert.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.input);
        Button buttonOk = dialogView.findViewById(R.id.button_ok);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        AlertDialog dialog = alert.create();
        dialog.show();

        buttonOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = input.getText().toString();
                if (!value.isEmpty()) {
                    TextDraw t = new TextDraw(value, x, y, mPaint.getColor(), mPaint.getTextSize(), mPaint.getTypeface());
                    mDraws.add(t);
                }
                dialog.dismiss();  // 关闭对话框
            }
        });

        buttonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mDraws.isEmpty() && mDraws.get(mDraws.size() - 1) instanceof TextDraw) {
                    mDraws.remove(mDraws.size() - 1);
                }
                invalidate();
                dialog.dismiss();  // 关闭对话框
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mDraws.isEmpty() && mDraws.get(mDraws.size() - 1) instanceof TextDraw) {
                    mDraws.remove(mDraws.size() - 1);
                }
                TextDraw t = new TextDraw(s.toString(), x, y, mPaint.getColor(), mPaint.getTextSize(), mPaint.getTypeface());
                mDraws.add(t);

                invalidate();
            }
        });

        final Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {

            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.dimAmount = 0.0f;

            dialogWindow.setAttributes(lp);


            dialogWindow.setBackgroundDrawableResource(R.drawable.dialog_border);

            View dialogRootView = dialogWindow.getDecorView();

            dialogRootView.setOnTouchListener(new OnTouchListener() {
                private float dx; // 记录点击位置在窗口中的位置
                private float dy;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    float x = event.getRawX();
                    float y = event.getRawY();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: // 按下时记录点击位置
                            dx = x - dialogWindow.getAttributes().x;
                            dy = y - dialogWindow.getAttributes().y;
                            break;
                        case MotionEvent.ACTION_MOVE: // 拖动时更新窗口位置
                            WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
                            layoutParams.x = (int) (x - dx);
                            layoutParams.y = (int) (y - dy);
                            dialogWindow.setAttributes(layoutParams);
                            break;
                    }
                    return false;
                }
            });
        }
    }

    public void undo() {
        if (!mDraws.isEmpty()) {
            mUndoneDraws.add(mDraws.remove(mDraws.size() - 1));
        }
        invalidate();
    }

    //撤销撤销,暂时用不上?
//    public void redo() {
//        if (!mUndoneDraws.isEmpty()) {
//            mDraws.add(mUndoneDraws.remove(mUndoneDraws.size() - 1));
//        }
//        invalidate();
//    }

    public void commit() {
        Bitmap committedBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas committedCanvas = new Canvas(committedBitmap);
        committedCanvas.drawBitmap(mBitmap, mMatrix, null);
        for (Draw d : mDraws) {
            if (d instanceof PathDraw) {
                committedCanvas.drawPath(((PathDraw) d).path, mPaint);
            } else if (d instanceof TextDraw) {
                TextDraw t = (TextDraw) d;
                committedCanvas.save();
                committedCanvas.drawText(t.text, t.x, t.y, mPaint);
                committedCanvas.restore();
            }
        }

        mBitmap = committedBitmap;
        mMatrix.reset();

        mDraws.clear();
        mUndoneDraws.clear();
        invalidate();
    }

    //清空
    public void clear() {
        //创建一个新的空位图和画布
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        //清空已绘制路径的列表
        mDraws.clear();
        mUndoneDraws.clear();

        invalidate();
    }


    private void touchStart(float x, float y) {
        mPath = new Path();
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
        PathDraw pd = new PathDraw();
        pd.path = mPath;
        pd.time = System.currentTimeMillis();
        pd.color = mPaint.getColor();
        pd.textSize = mPaint.getTextSize();
        pd.typeface = mPaint.getTypeface();
        mDraws.add(pd);
    }

    public void setPaintSize(float size) {
        mPaint.setTextSize(size);
    }

    public void setPaintTypeface(Typeface typeface) {
        mPaint.setTypeface(typeface);
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }



    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

}
