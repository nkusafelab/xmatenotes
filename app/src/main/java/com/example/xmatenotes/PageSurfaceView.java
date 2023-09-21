package com.example.xmatenotes;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.xmatenotes.app.ax.A3;
import com.example.xmatenotes.logic.manager.PageManager;
import com.example.xmatenotes.ui.BaseActivity;
import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.logic.model.handwriting.MediaDot;
import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.model.handwriting.TimelongDot;
import com.example.xmatenotes.logic.manager.LocalRect;
import com.example.xmatenotes.logic.model.Page.Page;
import com.tqltech.tqlpencomm.bean.Dot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义控件
 * 主要功能包括绘制书写背景、实时绘制书写笔迹、笔迹呈现、存储局域背景底图等
 */
public class PageSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable {

    private final static String TAG = "DrawImageSurfaceView";

    private SurfaceHolder surfaceHolder;
    private Canvas dCanvas;
    private boolean isDrawing;

    public Paint paint = null;//声明画笔
    public int paintColor = Color.BLACK;//画笔颜色
    public Canvas mCanvas = null;//自用缓冲画布
    public Bitmap bitmap = null;//位图
    public Bitmap backupBitmap = null;//备份位图
    public Bitmap originalBitmap = null;//原始尺寸的
    public Path path = null;

    //自身控件的大小宽和高
    private int width;
    private int height;

    //当前image实际绘制宽和高
    public int imageWidth;
    public int imageHeight;
    //当前image原始加载尺寸
    public Rect originalRect;
    //imageView的实际绘制上下左右边坐标
    public Rect rectImage;

    //被加载图片长宽比
    public Double scale;

    //bitmap压缩存储路径
    private String savePath;

    //标志绘制目标图片时需要的旋转角度，一般取-90、90、180
    public int rotate = 0;

    //是否需要创建空白画布
    private boolean isEmptyCBitmap = false;

    //画笔宽度
    private int bwidth = MediaDot.DEFAULT_WIDTH;

    //中心点坐标
    private int centerX;
    private int centerY;

    public int resourceId = 0;//当前展示资源id
    private int offsetY;//绘制图片相对View在竖直方向上的偏移量
    private int offsetX;//绘制图片相对View在水平方向上的偏移量

    public int pageId;//当前版面id
    public LocalRect lR;//当前局域
    private ShapeDrawable peoNumSD;
    public Rect peoNumRect;
    public int peoNumber;
    private ShapeDrawable hwNumSD;
    public Rect hwNumRect;
    public int hwNumber;
    public int peoOrHW = 0;//1为选中peoNum,2为选中hwNum
    public boolean isLRInforShow = false;//是否展示局域统计信息
    public boolean isHWInforShow = false;//是否展示局域对应笔迹信息

    public PageSurfaceView(@NonNull Context context, int resId) {
        super(context);
        resourceId = resId;
        initView();
    }

    public PageSurfaceView(@NonNull Context context, int resId, Rect rect) {
        super(context);
        resourceId = resId;
        originalRect = rect;
        Log.e(TAG, "-------------------------------------------------------------------");
        initView();
    }

    public PageSurfaceView(Context context) {
        super(context);
        initView();
    }

    public PageSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupAttributes(attrs);
        initView();
    }

    public PageSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupAttributes(attrs);
        initView();
    }

    private void setupAttributes(AttributeSet attrs) {
        // Obtain a typed array of attributes
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.DrawImageSurfaceView, 0, 0);
        // Extract custom attributes into member variables
        try {
//            resourceId = a.getInteger(R.styleable.DrawImageSurfaceView_src, R.drawable.x1);
            resourceId = a.getResourceId(R.styleable.DrawImageSurfaceView_src, R.drawable.x1);
        } finally {
            // TypedArray objects are shared and must be recycled.
            a.recycle();
        }
    }

    private void initView() {
        Log.e(TAG, "initView(): start");
        setLayerType(LAYER_TYPE_SOFTWARE, null);//禁用硬件加速
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
//        surfaceHolder.setFormat(PixelFormat.OPAQUE);

        initPaint();
        path = new Path();
        mCanvas = new Canvas();
        //默认图像文件保存路径
        savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/XAppBmps";


        if (resourceId == 0) {
            resourceId = R.drawable.x1;
        }
        if (originalRect != null) {
            setImageBitmapByResId(resourceId, originalRect);
        } else {
            setImageBitmapByResId(resourceId);
        }
        Log.e(TAG, "initView(): end");
    }

    /**
     * 初始化画笔
     */
    private void initPaint(){
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);//设置非填充
        paint.setStrokeWidth(bwidth);//设置笔宽
        paint.setAntiAlias(true);//锯齿不显示
        paint.setDither(true);  //防抖动
        paint.setStrokeJoin(Paint.Join.ROUND);//设置线段连接处样式为圆弧
        paint.setStrokeCap(Paint.Cap.ROUND);//设置线冒样式为圆形线冒
        paint.setColor(paintColor);
    }

    /**
     * 恢复画笔参数为初始状态
     */
    private void restorePaint(){
        paint.setStrokeWidth(bwidth);//设置笔宽
        paint.setColor(paintColor);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
        isDrawing = true;
        width = getWidth();
        height = getHeight();
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isDrawing = false;
    }

    @Override
    public void run() {
        while (isDrawing) {
            draw();
        }
    }

    private void draw() {
//        Log.e(TAG,"draw(): start");

        try {
            dCanvas = surfaceHolder.lockCanvas();
            dCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            dCanvas.drawColor(Color.WHITE);
            initBitmap();

            dCanvas.save();

            if (rotate == 90) {
                dCanvas.rotate(90);
                dCanvas.translate(offsetY, offsetX - getWidth());

            } else if (rotate == 0) {
                dCanvas.translate(offsetX, offsetY);
            }

            synchronized (this) {
                dCanvas.drawBitmap(bitmap, 0, 0, paint);
                Log.e(TAG, "draw: isLRInforShow: "+isLRInforShow);
                if(isLRInforShow){
                    //呈现局域统计信息
                    drawlRInfor(dCanvas);
                    if(peoOrHW == 2){//选中
                        drawlROnHwNum(lR, dCanvas);
                    }
                    if(peoOrHW == 1){//选中
                        drawlROnPeoNum(lR,dCanvas);
                    }
                }
                if(isDdrawLocalHWMap){


                }
            }

            //        canvas.drawBitmap(backupBitmap, 0, 0, paint);
//        canvas.drawPath(path,paint);
//            Log.e(TAG,"dCanvas.drawBitmap(bitmap, 0, 0, paint);");

            dCanvas.restore();
        } catch (Exception e) {

        } finally {
            if (dCanvas != null) {
                surfaceHolder.unlockCanvasAndPost(dCanvas);
            }
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Log.e(TAG,"draw(): end");
    }

    private Paint drawlRInforPaint;
    private Paint drawlRInforTextPaint;
    public void drawlRInfor(Canvas canvas){
        if(drawlRInforPaint == null){
            drawlRInforPaint = new Paint(paint);
        }
        if(drawlRInforTextPaint == null){
            drawlRInforTextPaint = new Paint(paint);
        }
        drawlRInforPaint.setTextSize(20);
        drawlRInforTextPaint.setTextSize(15);
        drawlRInforPaint.setColor(Color.YELLOW);
        hwNumber = XmateNotesApplication.pageManager.getPageByPageID(pageId).getHandWritingsNum(lR.getLocalCode());
        peoNumber = XmateNotesApplication.pageManager.getPageByPageID(pageId).getPeopleNum(lR.getLocalCode());
//        canvas.drawRect(new Rect(500,500,1000,1000),paint);
        Log.e(TAG, "drawlRInfor: hwNumRect: "+hwNumRect);
        Log.e(TAG, "drawlRInfor: peoNumRect: "+peoNumRect);
        if(hwNumSD == null){
            //笔迹次数
            hwNumSD = new ShapeDrawable(new OvalShape());
            hwNumSD.getPaint().setColor(Color.rgb(144,238,144));
        }
        hwNumSD.setBounds(hwNumRect);
        hwNumSD.draw(canvas);
        if(peoOrHW == 2){//选中
            drawlRInforPaint.setStrokeWidth(10);
            drawlRInforPaint.setColor(Color.RED);
            canvas.drawText(String.valueOf(hwNumber),hwNumRect.left+hwNumRect.width()/3-1, hwNumRect.bottom-hwNumRect.height()/3+1, drawlRInforPaint);
            drawlRInforPaint.setStrokeWidth(3);
            drawlRInforPaint.setColor(Color.YELLOW);
//            drawlROnHwNum(lR);
        }

        canvas.drawText(String.valueOf(hwNumber),hwNumRect.left+hwNumRect.width()/3-1, hwNumRect.bottom-hwNumRect.height()/3+1, drawlRInforPaint);
        canvas.drawText("消创",hwNumRect.centerX()-43,hwNumRect.centerY()-3,drawlRInforTextPaint);//笔迹次数
        canvas.drawText("节律",hwNumRect.centerX()-43,hwNumRect.centerY()+16-3,drawlRInforTextPaint);
        if(peoNumSD == null){
            //笔迹人数
            peoNumSD = new ShapeDrawable(new OvalShape());
            peoNumSD.getPaint().setColor(Color.rgb(144,238,144));
        }
        peoNumSD.setBounds(peoNumRect);
        peoNumSD.draw(canvas);
        if(peoOrHW == 1){//选中
            drawlRInforPaint.setStrokeWidth(10);
            drawlRInforPaint.setColor(Color.RED);
            canvas.drawText(String.valueOf(peoNumber),peoNumRect.left+peoNumRect.width()/3-1, peoNumRect.bottom-peoNumRect.height()/3+1, drawlRInforPaint);
            drawlRInforPaint.setStrokeWidth(3);
            drawlRInforPaint.setColor(Color.YELLOW);
//            drawlROnPeoNum(lR);
        }

        canvas.drawText(String.valueOf(peoNumber),peoNumRect.left+peoNumRect.width()/3-1, peoNumRect.bottom-peoNumRect.height()/3+1, drawlRInforPaint);
        canvas.drawText("痕迹",peoNumRect.centerX()-43,peoNumRect.centerY()-3,drawlRInforTextPaint);
        canvas.drawText("节律",peoNumRect.centerX()-43,peoNumRect.centerY()+16-3,drawlRInforTextPaint);
    }

    /**
     * 按照笔迹人数信息重绘局域
     * @param lR
     */
    public void drawlROnPeoNum(LocalRect lR, Canvas canvas){
        Page page = XmateNotesApplication.pageManager.getPageByPageID(pageId);
        ArrayList<Page.LocalHandwritingsMap> localHwsMapList = page.getLocalHandwritings(lR.getLocalCode());
        ArrayList<MediaDot> mediaDots = page.getPageDotsBuffer();
        if(mediaDots == null || localHwsMapList == null){
            return;
        }

        restoreRect(lR.rect);

        Paint p = new Paint(paint);
        p.setStrokeWidth(1);
        p.setTextSize(15);
        p.setColor(Color.BLACK);
        Map<String, Integer> map = new HashMap<>();
        colorNumber = -1;MediaDot preD = null;MediaDot d = null;
        float maxY = 0,minX = Float.MAX_VALUE;
        for(Page.LocalHandwritingsMap lhm : localHwsMapList){
            for (int i = lhm.getBegin();i <= lhm.getEnd();i++){
                d = mediaDots.get(i);
                if(!d.isEmptyDot()){
                    if(!map.containsKey(d.penMac)){
                        map.put(d.penMac,getColor());

                    }
                    drawDot(d.getFloatX(), d.getFloatY(), d.type, map.get(d.penMac), 2, null);
                    if(d.getFloatY() > maxY){
                        maxY = d.getFloatY();
                    }
                    if(d.getFloatX() < minX){
                        minX = d.getFloatX();

                    }
                    preD = d;
                }
            }
            if(d != null && !d.isEmptyDot()){
                String s = XmateNotesApplication.penMacManager.getNameByMac(d.penMac);
                if(s != null){
                    canvas.drawText(s, mapX(bitmap.getWidth(), minX), mapY(bitmap.getHeight(), maxY)+15, p);
                }
            } else {
                if(preD != null){
                    String s = XmateNotesApplication.penMacManager.getNameByMac(preD.penMac);
                    if(s != null){
                        canvas.drawText(s, mapX(bitmap.getWidth(), minX), mapY(bitmap.getHeight(), maxY)+15, p);
                    }
                }
            }
            maxY = 0;minX = Float.MAX_VALUE;
        }

    }

    /**
     * 按照笔迹次数信息重绘局域
     * @param lR
     */
    public void drawlROnHwNum(LocalRect lR, Canvas canvas){
        Page page = PageManager.getPageByPageID(pageId);
        ArrayList<Page.LocalHandwritingsMap> localHwsMapList = page.getLocalHandwritings(lR.getLocalCode());
        ArrayList<MediaDot> mediaDots = page.getPageDotsBuffer();
        if(mediaDots == null || localHwsMapList == null){
            return;
        }

        restoreRect(lR.rect);

        Paint p = new Paint(paint);
        p.setStrokeWidth(1);
        p.setTextSize(15);
        p.setColor(Color.BLACK);
        colorNumber = -1;MediaDot preD = null;MediaDot d = null;
        float maxY = 0,minX = Float.MAX_VALUE;
        for(Page.LocalHandwritingsMap lhm : localHwsMapList){
            int color = getColor();
            for (int i = lhm.getBegin();i <= lhm.getEnd();i++){
                d = mediaDots.get(i);
                if(!d.isEmptyDot()){
                    drawDot(d.getFloatX(), d.getFloatY(), d.type, color, 2, null);
                    if(d.getFloatY() > maxY){
                        maxY = d.getFloatY();
                    }
                    if(d.getFloatX() < minX){
                        minX = d.getFloatX();
                    }
                    preD = d;
                }
            }

            if(d != null && !d.isEmptyDot()){
                canvas.drawText(MediaDot.timelongFormat2(d.timelong), mapX(bitmap.getWidth(), minX), mapY(bitmap.getHeight(), maxY)+15, p);
            } else {
                if(preD != null){
                    canvas.drawText(MediaDot.timelongFormat2(preD.timelong), mapX(bitmap.getWidth(), minX), mapY(bitmap.getHeight(), maxY)+15, p);
                }
            }

            maxY = 0;minX = Float.MAX_VALUE;

        }
    }

    /**
     * 重绘某个局域
     * @param lR
     */
    public void drawlR(LocalRect lR){
        Page page = PageManager.getPageByPageID(pageId);
        ArrayList<Page.LocalHandwritingsMap> localHwsMapList = page.getLocalHandwritings(lR.getLocalCode());
        ArrayList<MediaDot> mediaDots = page.getPageDotsBuffer();
        if(mediaDots == null || localHwsMapList == null){
            return;
        }

        restoreRect(lR.rect);

        for(Page.LocalHandwritingsMap lhm : localHwsMapList){
            for (int i = lhm.getBegin();i <= lhm.getEnd();i++){
                MediaDot tD = mediaDots.get(i);
                if(!tD.isEmptyDot()){
                    drawMDot(tD);
                }
            }
        }
    }

    public boolean isDdrawLocalHWMap = false;//是否调用drawLocalHWMap()

    /**
     * 按照特别需要画出当前单次笔迹
     */
    public void drawLocalHWMap(LocalRect lR, Page.LocalHandwritingsMap lhwm ,ArrayList<MediaDot> mediaDots){
        restoreRect(lhwm.getBounds());
        MediaDot d = null, preD = null;
        RectF rectF = null;Paint p = new Paint(paint);p.setStrokeWidth(3);
        p.setColor(Color.BLACK);
        float maxY = 0,minX = Float.MAX_VALUE;
        for (int i = lhwm.getBegin();i <= lhwm.getEnd();i++){
            d = mediaDots.get(i);
            if(!d.isEmptyDot()){
                drawDot(d.getFloatX(), d.getFloatY(), d.type, d.color, 3, null);
                if(rectF == null){
                    rectF = new RectF(d.getFloatX(),d.getFloatY(), d.getFloatX(), d.getFloatY());
                }
                rectF.union(d.getFloatX(), d.getFloatY());
                preD = d;
            }
        }
        rectF = mapRect(rectF);rectF.left -= 5;rectF.top -= 5;rectF.right += 5;rectF.bottom += 5;
        mCanvas.drawRect(rectF, p);
        p.setTextSize(15);p.setColor(Color.BLACK);p.setStrokeWidth(1);
        mCanvas.drawText(XmateNotesApplication.penMacManager.getNameByMac(preD.penMac)+"|"+MediaDot.timelongFormat2(preD.timelong), rectF.left, rectF.bottom+15, p);
    }

    private int[] color = new int[]{Color.RED, 0xFFFFA500, Color.YELLOW, Color.GREEN, 0xFF008F8F, Color.BLUE, 0xFF8F008F};
    private int colorNumber = -1;//颜色索引
    private int getColor(){
        colorNumber = (colorNumber+1)%7;
        return color[colorNumber];
    }

    public void setImageBitmapByResId(int resId, Rect rect) {
        Log.e(TAG, "setImageBitmapByResId(" + resId + ", " + rect + "): start");
        resourceId = resId;
        backupBitmap = null;
        originalRect = rect;
        rectImage = rect;
        path.reset();
        synchronized (this) {
            bitmap = null;
            initBitmap();
        }
        Log.e(TAG, "setImageBitmapByResId(" + resId + ", " + rect + "): end");
    }

    public void setImageBitmapByResId(int resId) {
        Log.e(TAG, "setImageBitmapByResId(" + resId + "): start");
        if (resId == -1) {
            isEmptyCBitmap = true;
            resourceId = -1;
            rotate = 0;
        } else {
            isEmptyCBitmap = false;
            resourceId = resId;
        }

        backupBitmap = null;
        originalRect = null;
        rectImage = null;
        path.reset();

        synchronized (this) {
            bitmap = null;
            initBitmap();
        }
        Log.e(TAG, "setImageBitmapByResId(" + resId + "): end");
    }

    private void initBitmap() {

        if (bitmap == null && width != 0 && height != 0) {
            Log.e(TAG, "initBitmap(): start");

            Log.e(TAG, "getWidth(): " + width + " getHeight(): " + height);
            if (isEmptyCBitmap) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//空白画布
            } else {
                if (originalRect == null) {
                    bitmap = decodeSampledBitmapFromResource(getResources(), resourceId, width, height);
                } else {
                    bitmap = decodeRectFOfBitmapFromResource(getResources(), resourceId, width, height, originalRect);
                }
            }
            backupBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (backupBitmap == null) {
                Log.e(TAG, "initBitmap(): backupBitmap为空");
            }
            imageWidth = bitmap.getWidth();
            imageHeight = bitmap.getHeight();
            if (rotate == 90) {
                int tmp = imageHeight;
                imageHeight = imageWidth;
                imageWidth = tmp;
            }
            Log.e(TAG, "imageWidth: " + imageWidth + " imageHeight: " + imageHeight);
            mCanvas.setBitmap(bitmap);
            offsetY = (int) (double) (getHeight() - imageHeight) / 2;
            offsetX = (int) (double) (getWidth() - imageWidth) / 2;
            rectImage = new Rect(offsetX, offsetY, offsetX+imageWidth, offsetY+imageHeight);
            centerX = (int) Math.round((double) (getLeft() + getRight()) / 2);
            centerY = (int) Math.round((double) (getTop() + getBottom()) / 2);
            Log.e(TAG, "initBitmap(): end");
        }
    }

    //解析图片资源原始尺寸
    public static Rect decodeDimensionOfImageFromResource(Resources res, int resId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        return new Rect(0, 0, options.outWidth, options.outHeight);

    }

    //解析图片资源区域显示
    public Bitmap decodeRectFOfBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight, Rect rect) {
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

        computeRotateAndScale(rect.width(), rect.height(), reqWidth, reqHeight);

        return createScaledBitmap(bp, reqWidth, reqHeight);
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, resId, options);
        originalRect = new Rect(0, 0, options.outWidth, options.outHeight);

        Log.e(TAG, "options.outWidth: " + options.outWidth + " options.outHeight: " + options.outHeight);

        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeResource(res, resId, options).copy(Bitmap.Config.ARGB_8888, true);

        return createScaledBitmap(bm, reqWidth, reqHeight);
    }

    public Bitmap createScaledBitmap(Bitmap bmp, int reqWidth, int reqHeight) {
        double reqScale = (double) reqHeight / reqWidth;//需要的高宽比

        //自动适配需要的尺寸
        if (rotate == 90) {
            if (scale < reqScale) {
                return Bitmap.createScaledBitmap(bmp, (int) (reqWidth * scale), reqWidth, true);
            } else {
                return Bitmap.createScaledBitmap(bmp, reqHeight, (int) (reqHeight / scale), true);
            }

        } else {
            if (scale < reqScale) {
                return Bitmap.createScaledBitmap(bmp, reqWidth, (int) (reqWidth * scale), true);
            } else {
                return Bitmap.createScaledBitmap(bmp, (int) (reqHeight / scale), reqHeight, true);
            }
        }
    }

    public void computeRotateAndScale(int width, int height, int reqWidth, int reqHeight) {
        if (reqWidth < reqHeight && width > height) {
            rotate = 90;//标志绘制时需要右转90度

            int tmp = width;
            width = height;
            height = tmp;
        } else if (reqWidth > reqHeight && width < height) {
            rotate = 90;//标志绘制时需要右转90度

            int tmp = width;
            width = height;
            height = tmp;
        } else {
            rotate = 0;
            ;
        }

        scale = (double) height / width;//高宽比
    }

    public int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        //Raw height and width of image
        int inSampleSize = 1;

        if (reqWidth < reqHeight && width > height) {
            rotate = 90;//标志绘制时需要右转90度

            int tmp = width;
            width = height;
            height = tmp;
        } else if (reqWidth > reqHeight && width < height) {
            rotate = 90;//标志绘制时需要右转90度

            int tmp = width;
            width = height;
            height = tmp;
        } else {
            rotate = 0;
            ;
        }

        scale = (double) height / width;//高宽比

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
        return inSampleSize;

    }

    public void drawBitmap(Rect rect, Bitmap bitmap){
        synchronized (this) {
            mCanvas.drawBitmap(bitmap , null, rect, paint);
        }
    }

    public void restoreRect(Rect rect){
        rect = mapRect(rect);
        synchronized (this) {
            mCanvas.drawBitmap(backupBitmap ,rect ,rect ,paint);
        }
    }

    private float pPreX, pPreY;//上一个点

    public void drawPath(float dx, float dy, Dot.DotType dotType, Rect rect) {

        Log.e(TAG, "dx: " + dx + " dy: " + dy);
        //坐标换算
        float x = mapX(bitmap.getWidth(), dx, rect);
        float y = mapY(bitmap.getHeight(), dy, rect);
        Log.e(TAG, "bitmap.getWidth(): " + bitmap.getWidth() + " bitmap.getHeight(): " + bitmap.getHeight());
        Log.e(TAG, "x: " + x + " y: " + y);
        Log.e(TAG, "dotType: " + dotType);

        switch (dotType) {
            case PEN_DOWN:
                path.moveTo(x, y);
                pPreX = x;
                pPreY = y;
                Log.e(TAG, "moveTo(" + x + "," + y + ")");
                break;
            case PEN_MOVE:
            case PEN_UP:
//                path.lineTo(x,y);
                float endX = (pPreX + x) / 2;
                float endy = (pPreY + y) / 2;
                //绘制贝塞尔曲线
                path.quadTo(pPreX, pPreY, endX, endy);
                pPreX = x;
                pPreY = y;
                Log.e(TAG, "lineTo(" + x + "," + y + ")");
                break;
            default:
        }

        //把所有对bitmap的操作原子化，否则可能会绘制出一些过渡状态的bitmap，而导致绘制闪烁
        synchronized (this) {
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空bitmap，否则会有锯齿
            if (backupBitmap == null) {
                Log.e(TAG, "drawDot: backupBitmap为空");
            }
            mCanvas.drawBitmap(backupBitmap, 0, 0, paint);
            mCanvas.drawPath(path, paint);
        }
    }

    private float lPreX = -1, lPreY = -1;//上一个点

    public void drawLine(float dx, float dy, Dot.DotType dotType, Rect rect){
        drawLine(dx, dy, dotType, -1, -1, rect);
    }

    public void drawLine(float dx, float dy, Dot.DotType dotType, int color, int width, Rect rect) {
        Log.e(TAG, "dx: " + dx + " dy: " + dy);

        if(color != -1){
            paint.setColor(color);
        }

        if(width != -1){
            paint.setStrokeWidth(width);//设置笔宽
        }

        //坐标换算
        float x = mapX(bitmap.getWidth(), dx, rect);
        float y = mapY(bitmap.getHeight(), dy, rect);
        Log.e(TAG, "bitmap.getWidth(): " + bitmap.getWidth() + " bitmap.getHeight(): " + bitmap.getHeight());
        Log.e(TAG, "x: " + x + " y: " + y);
        Log.e(TAG, "dotType: " + dotType);

        switch (dotType) {
            case PEN_DOWN:
                lPreX = x;
                lPreY = y;
                Log.e(TAG, "moveTo(" + x + "," + y + ")");
                break;
            case PEN_MOVE:
            case PEN_UP:
                if (lPreX != -1 && lPreY != -1) {
                    //把所有对bitmap的操作原子化，否则可能会绘制出一些过渡状态的bitmap，而导致绘制闪烁
                    synchronized (this) {
                        mCanvas.drawLine(lPreX, lPreY, x, y, paint);
                    }
                }
                lPreX = x;
                lPreY = y;
                Log.e(TAG, "lineTo(" + x + "," + y + ")");
                break;
            default:
        }
    }


    public void drawDot(float dx, float dy, Dot.DotType dotType, int color, int width, Rect rect) {
        if (bitmap != null) {
//            drawPath(dx, dy, dotType, rect);
            drawLine(dx, dy, dotType, color, width, rect);//避免每次重绘整个path
        } else {
            Log.e(TAG, "drawDot(): bitmap == null");
        }
    }

    public void drawDot(float dx, float dy, Dot.DotType dotType, int color, int width) {
        drawDot(dx, dy, dotType, color, width, null);
    }

    public void drawDot(Dot dot) {

        drawDot(dot, null);
    }

    public void drawDot(Dot dot, Rect rect) {
        drawSDot(new SimpleDot(dot), rect);
    }

    public void drawSDot(SimpleDot simpleDot) {
        drawSDot(simpleDot, null);
    }

    public void drawSDot(SimpleDot simpleDot, Rect rect) {
        drawDot(simpleDot.getFloatX(), simpleDot.getFloatY(), simpleDot.type, -1,-1 , rect);
    }

    public void drawMDot(MediaDot mediaDot) {
        drawMDot(mediaDot, null);
    }

    /**
     * 当前MediaDot
     */
    public MediaDot curMediaDot = null;

    /**
     * 上一个MediaDot
     */
    public MediaDot lastMediaDot = null;

    private Bitmap audioBmp = null;
    private Bitmap videoBmp = null;

    private RectF insRectF;//命令加框

    //连着命令会崩溃
    public void drawMDot(MediaDot mediaDot, Rect rect) {
        if(mediaDot.isEmptyDot()){
            return;
        }
        Log.e(TAG, "drawMDot: lastMediaDot1: "+lastMediaDot);
        Log.e(TAG, "drawMDot: curMediaDot1: "+curMediaDot);
        lastMediaDot = curMediaDot;
        curMediaDot = mediaDot;
        Log.e(TAG, "drawMDot: lastMediaDot2: "+lastMediaDot);
        if(lastMediaDot != null){
            Log.e(TAG, "drawMDot: "+"lastMediaDot.isCharInstruction(): "+lastMediaDot.isCharInstruction());
        }
        Log.e(TAG, "drawMDot: curMediaDot2: "+curMediaDot);
        if(curMediaDot != null){
            Log.e(TAG, "drawMDot: "+"curMediaDot.isCharInstruction(): "+curMediaDot.isCharInstruction());
        }

        if(curMediaDot.isCharInstruction() && curMediaDot.type == Dot.DotType.PEN_DOWN){
            insRectF = new RectF(curMediaDot.getFloatX(), curMediaDot.getFloatY(), curMediaDot.getFloatX(), curMediaDot.getFloatY());
        }
//        if(lastMediaDot == null){
//            if(curMediaDot.isCharInstruction()){
//                insRectF = new RectF(curMediaDot.getCx(), curMediaDot.getCy(), curMediaDot.getCx(), curMediaDot.getCy());
//                Log.e(TAG, "drawMDot: insRectF初始化");
//            }
//        }else {
//            if(!lastMediaDot.isCharInstruction() && curMediaDot.isCharInstruction()){
//                insRectF = new RectF(curMediaDot.getCx(), curMediaDot.getCy(), curMediaDot.getCx(), curMediaDot.getCy());
//                Log.e(TAG, "drawMDot: insRectF初始化");
//            }
//        }
        if(curMediaDot.isCharInstruction()){
            Log.e(TAG, "drawMDot: union: insRectF: "+insRectF);
            insRectF.union(curMediaDot.getFloatX(), curMediaDot.getFloatY());
        }
        if(curMediaDot.isCharInstruction() && curMediaDot.type == Dot.DotType.PEN_UP){
            paint.setColor(Color.RED);
            paint.setStrokeWidth(MediaDot.DEFAULT_BOLD_WIDTH);
            Log.e(TAG, "drawMDot: drawRect: insRectF: "+insRectF);
            insRectF = mapRect(insRectF);
            insRectF.left -= 4;
            insRectF.top -= 4;
            insRectF.right += 4;
            insRectF.bottom += 4;

//            Log.e(TAG, "drawMDot: imageWidth: "+imageWidth +" imageHeight: "+imageHeight);
//            Log.e(TAG, "drawMDot: mapRect-imageWidth: "+mapRect(insRectF, new Rect(0, 0, imageWidth, imageHeight)));
//            Log.e(TAG, "drawMDot: bitmap.getWidth(): "+bitmap.getWidth()+" bitmap.getHeight(): "+bitmap.getHeight());
//            Log.e(TAG, "drawMDot: bitmapRect: "+mapRect(insRectF));
            synchronized (this) {
                mCanvas.drawRect(insRectF, paint);
            }
        }

        drawIcon(curMediaDot, lastMediaDot);

        drawDot(mediaDot.getFloatX(), mediaDot.getFloatY(), mediaDot.type, mediaDot.color, mediaDot.width, rect);
    }

    private Rect cRect(float x, float y, int diam, Bitmap bmp){
        return new Rect((int)x, (int)(y-diam), (int)(x+diam), (int)(y-diam+diam*bmp.getHeight()/bmp.getWidth()));
    }

    private void drawIcon(MediaDot curMediaDot, MediaDot lastMediaDot){

        int diam = 20;
        //坐标换算
        float x = mapX(bitmap.getWidth(), curMediaDot.getFloatX());
        float y = mapY(bitmap.getHeight(), curMediaDot.getFloatY());

        if(lastMediaDot == null){
            if(curMediaDot.isAudioDot()){
                //绘制音频图标
                if(audioBmp == null){
                    audioBmp = decodeBmp(R.drawable.audioexist);
                }
                drawBitmap(cRect(x, y, diam, audioBmp), audioBmp);
            }
            if(curMediaDot.isVideoDot()){
                //绘制视频图标
                if(videoBmp == null){
                    videoBmp = decodeBmp(R.drawable.videoplay);
                }
                drawBitmap(cRect(x, y, diam, videoBmp), videoBmp);
            }
            if(curMediaDot.isVideoDot() && curMediaDot.isAudioDot()){
                //绘制视频图标和音频图标
                if(audioBmp == null){
                    audioBmp = decodeBmp(R.drawable.audioexist);
                }
                if(videoBmp == null){
                    videoBmp = decodeBmp(R.drawable.videoplay);
                }
                drawBitmap(cRect(x, y, diam, audioBmp), audioBmp);
                drawBitmap(cRect(x+diam+5, y, diam, videoBmp), videoBmp);
            }
        }else {
            if(curMediaDot.isAudioDot() && !lastMediaDot.isAudioDot()){
                //绘制音频图标
                if(audioBmp == null){
                    audioBmp = decodeBmp(R.drawable.audioexist);
                }
                drawBitmap(cRect(x, y, diam, audioBmp), audioBmp);
            }
            if(curMediaDot.isVideoDot() && !lastMediaDot.isVideoDot()){
                //绘制视频图标
                if(videoBmp == null){
                    videoBmp = decodeBmp(R.drawable.videoplay);
                }
                drawBitmap(cRect(x, y, diam, videoBmp), videoBmp);
            }
            if(curMediaDot.isVideoDot() && curMediaDot.isAudioDot() && (!lastMediaDot.isAudioDot() || !lastMediaDot.isVideoDot())){
                //绘制视频图标和音频图标
                if(audioBmp == null){
                    audioBmp = decodeBmp(R.drawable.audioexist);
                }
                if(videoBmp == null){
                    videoBmp = decodeBmp(R.drawable.videoplay);
                }
                drawBitmap(cRect(x, y, diam, audioBmp), audioBmp);
                drawBitmap(cRect(x+diam+5, y, diam, videoBmp), videoBmp);
            }
        }
    }

    public Bitmap decodeBmp(int resId){
        return BitmapFactory.decodeResource(getResources(), resId);
    }
    //绘制一组点
    public <T extends SimpleDot> void drawSDots(ArrayList<T> simpleDots) {
        drawSDots(simpleDots, null);
    }

    //绘制一组点
    public <T extends SimpleDot> void drawSDots(ArrayList<T> simpleDots, Rect rect) {
        final ArrayList<T> sDots = (ArrayList<T>) simpleDots.clone();
        for (T sDot : sDots) {
            if (sDot instanceof TimelongDot) {
                if (((TimelongDot) sDot).isEmptyDot()) {
                    Log.e(TAG, "drawDots(): 是空点");
                    continue;
                }
            }
            Log.e(TAG, "drawDots(): sDot.x: " + sDot.getFloatX() + " sDot.y: " + sDot.getFloatY() + " sDot.type: " + sDot.type);
//            drawPath(sDot.x, sDot.y, sDot.type, rect);
            drawLine(sDot.getFloatX(), sDot.getFloatY(), sDot.type, rect);
        }

//        //把所有对bitmap的操作原子化，否则可能会绘制出一些过渡状态的bitmap，而导致绘制闪烁
//        synchronized (this){
//            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空bitmap，否则会有锯齿
//            mCanvas.drawBitmap(backupBitmap, 0, 0,paint);
//            mCanvas.drawPath(path,paint);
//        }
    }

    //绘制一组点
    public <T extends MediaDot> void drawMDots(ArrayList<T> meidiaDots) {
        drawMDots(meidiaDots, null);
    }

    //绘制一组点
    public <T extends MediaDot> void drawMDots(ArrayList<T> meidiaDots, Rect rect) {
        final ArrayList<T> sDots = (ArrayList<T>) meidiaDots.clone();
        for (T sDot : sDots) {
            if (sDot instanceof MediaDot) {
                if (((MediaDot) sDot).isEmptyDot()) {
                    Log.e(TAG, "drawDots(): 是空点");
                    continue;
                }
            }
            Log.e(TAG, "drawDots(): sDot.x: " + sDot.getFloatX() + " sDot.y: " + sDot.getFloatY() + " sDot.type: " + sDot.type);
//            drawPath(sDot.x, sDot.y, sDot.type, rect);
//            drawLine(sDot.getCx(), sDot.getCy(), sDot.type, sDot.color, sDot.width, rect);
            drawMDot(sDot, rect);
        }

        initPaint();
    }

    /*****************************属性设置与获取********************************/

    //参数应为完整的路径，如: Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+path
    public boolean setSavePath (String path){
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.exists()) {
            return false;
        } else {
            this.savePath = path;
            return true;
        }
    }

    //只改变存储路径后面的部分，如: XAppBmps/oneBmp
    public boolean changeSavePath (String filePath){
        return setSavePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filePath);
    }

    public void setPenWidth ( int w){
        bwidth = w;
        paint.setStrokeWidth(bwidth);
    }

    public int getPenWidth () {
        return bwidth;
    }

    public void setPenColor ( int color){
        paintColor = color;
        paint.setColor(paintColor);
    }

    public SimpleDot touchDotToBmpDot(SimpleDot simpleDot){
        float x = simpleDot.getFloatX();
        float y = simpleDot.getFloatY();
        if(y < offsetY || y >offsetY+bitmap.getWidth()){//超出范围
            return null;
        }

        simpleDot.setX(y - offsetY);
        simpleDot.setY(getWidth() - x);
        return simpleDot;
    }

    private boolean isOnTE = false;

    public boolean onTouchEvent(MotionEvent event){

        float pointX = event.getX();
        float pointY = event.getY();

        SimpleDot simpleDot = touchDotToBmpDot(new SimpleDot(pointX, pointY));
        if(simpleDot == null){
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                simpleDot.type = Dot.DotType.PEN_DOWN;
                if(isLRInforShow){
                    if(hwNumRect.contains(Math.round(simpleDot.getFloatX()), Math.round(simpleDot.getFloatY()))){
                        if(peoOrHW != 2){
                            peoOrHW = 2;
                        }else {
                            peoOrHW = 0;
                            drawlR(lR);
                        }
                        isOnTE = true;
                        return true;

                    }
                    if(peoNumRect.contains(Math.round(simpleDot.getFloatX()), Math.round(simpleDot.getFloatY()))){
                        if(peoOrHW != 1){
                            peoOrHW = 1;
                        }else {
                            peoOrHW = 0;
                            drawlR(lR);
                        }
                        isOnTE = true;
                        return true;

                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isOnTE){
                    return true;
                }
                simpleDot.type = Dot.DotType.PEN_MOVE;
                break;
            case MotionEvent.ACTION_UP:
                if(isOnTE){
                    isOnTE = false;
                    return true;
                }
                simpleDot.type = Dot.DotType.PEN_UP;
                break;
            default:
                return false;
        }

        simpleDot.setX(simpleDot.getFloatX()* A3.ABSCISSA_RANGE/bitmap.getWidth());
        simpleDot.setY(simpleDot.getFloatY()*A3.ORDINATE_RANGE/bitmap.getHeight());

        MediaDot mediaDot = new MediaDot(simpleDot);
        mediaDot.pageID = XmateNotesApplication.pageManager.currentPageID;
        mediaDot.penMac = XmateNotesApplication.mBTMac;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if("MainActivity".equals(BaseActivity.baseActivity.getClass().getSimpleName())){
                    BaseActivity.baseActivity.processEachDot(mediaDot);
                }else {
                    XmateNotesApplication.instruction.processEachDot(mediaDot);
                }
            }
        }).start();


        return true;
    }


    /*****************************属性设置与获取********************************/

    //将坐标映射到绘图区域上

    public static float mapX ( int width, float fx, Rect rect){
        if (rect != null) {
//            return (fx-rect.left)*width/rect.width();
            return (fx - rect.left) * width / rect.width();
        } else {
            return fx * width / A3.ABSCISSA_RANGE;
        }
    }

    public static float mapX ( int width, float fx){
        return mapX(width, fx, null);
    }

    public static float mapY ( int height, float fy, Rect rect){
        if (rect != null) {
            return (fy - rect.top) * height / rect.height();
        } else {
            return fy * height / A3.ORDINATE_RANGE;
        }
    }
    public static float mapY ( int height, float fy){
        return mapY(height, fy, null);
    }


    //将纸张上的矩形坐标范围映射到目标Rect上
        public static Rect mapRect (Rect srcRect, Rect desRect){
            Rect rect = new Rect();
            //坐标换算
        rect.left = Math.round(mapX(desRect.width(), (float) srcRect.left));
        rect.right = Math.round(mapX(desRect.width(), (float) srcRect.right));
        rect.top = Math.round(mapY(desRect.height(), (float) srcRect.top));
        rect.bottom = Math.round(mapY(desRect.height(), (float) srcRect.bottom));
        return rect;
    }
    //将纸张上的矩形坐标范围映射到目标Rect上
    public static RectF mapRect (RectF srcRect, Rect desRect){
        RectF rect = new RectF();
        //坐标换算
        rect.left = Math.round(mapX(desRect.width(), (float) srcRect.left));
        rect.right = Math.round(mapX(desRect.width(), (float) srcRect.right));
        rect.top = Math.round(mapY(desRect.height(), (float) srcRect.top));
        rect.bottom = Math.round(mapY(desRect.height(), (float) srcRect.bottom));
        return rect;
    }

    public RectF mapRect (RectF srcRect){
        return mapRect(srcRect, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));

    }

    public Rect mapRect (Rect srcRect){
        return mapRect(srcRect, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));

    }
    //参数为不包含后缀的文件名
    public boolean saveBmp (String fileName){
        return saveBmp(fileName + ".png", Bitmap.CompressFormat.PNG, 10, null);
    }
    //存储局域bitmap,参数为不包含后缀的文件名
    public boolean saveBmp (String fileName, Rect rect){
        return saveBmp(fileName + ".png", Bitmap.CompressFormat.PNG, 10, rect);
    }

    //参数为包含后缀的文件名，如: 1-1.png
    private boolean saveBmp (String fileName, Bitmap.CompressFormat compressFormat,
    int quality, Rect rect){
        if (!fileName.endsWith(".png")) {
            Log.e(TAG, fileName);
            Log.e(TAG, "saveBmp(): bmp存储文件名后缀不合法");
            return false;
        }
        File file = new File(savePath, fileName);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
//        if(file.exists()) {
//            Log.e(TAG,"saveBmp(): bmp存储文件已存在，请更换文件名");
//            return false;
//        }
        Bitmap bmp = bitmap;
        if (rect != null) {
            rect = mapRect(rect, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            bmp = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());//裁剪bitmap
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if (bmp != null) {
                bmp.compress(compressFormat, quality, out);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            out = null;
        }
        return true;
    }

    //清除当前页面上的笔迹
    public void clear () {
        bitmap = backupBitmap;
        backupBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas.setBitmap(bitmap);
        path.reset();

    }

    public void reDraw ( int speed){

    }

    public void drawDestroy () {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }

        paint = null;
        mCanvas = null;
    }
}
