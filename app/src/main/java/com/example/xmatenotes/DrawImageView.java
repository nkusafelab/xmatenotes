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
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.xmatenotes.logic.model.handwriting.SimpleDot;
import com.example.xmatenotes.logic.model.handwriting.TimelongDot;
import com.tqltech.tqlpencomm.bean.Dot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

//import com.example.xmatenotes.App.XApp;

public class DrawImageView extends View {

    private static final String TAG = "DrawImageView";

    public Paint paint = null;//声明画笔
    public Canvas mCanvas = null;//画布
    public Bitmap bitmap = null;//位图
    public Bitmap backupBitmap = null;//备份位图
    public Path path = null;

    public float x1,x2,y1,y2;

    //当前image实际绘制宽和高
    public int imageWidth;
    public int imageHeight;
    //当前image原始加载尺寸
    public int originalImageWidth;
    public int originalImageHeight;
    public Rect originalRect;
    //imageView的实际绘制上下左右边坐标
    public Rect rectImage;

    //被加载图片长宽比
    public Double scale;

    public static boolean onDrawed = false;//

    //bitmap压缩存储路径
    private String savePath;

    //文字朝向是否正常
//    public static boolean isForward = true;

    //标志绘制目标图片时需要的旋转角度，一般取-90、90、180
    public int rotate = 0;

    //是否清空当前画布上的笔迹
    private boolean isClear = false;

    //是否需要创建空白画布
    private boolean isEmptyCBitmap = false;

//    private ImageView imageView;

    private int bwidth = 1;

    //中心点坐标
    private int centerX;
    private int centerY;

    public int resourceId = 0;//当前展示资源id
    private int offsetY;//绘制图片相对View在竖直方向上的偏移量
    private int offsetX;//绘制图片相对View在水平方向上的偏移量

//    private BaseActivity context;

    public DrawImageView(@NonNull Context context,int resId) {
        super(context);
        resourceId = resId;
        initDraw();
    }
    public DrawImageView(@NonNull Context context,int resId, Rect rect) {
        super(context);
        resourceId = resId;
        originalRect = rect;
        initDraw();
    }


    public DrawImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DrawImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupAttributes(attrs);
        initDraw();
    }

    private void setupAttributes(AttributeSet attrs) {
        // Obtain a typed array of attributes
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.DrawImageView, 0, 0);
        // Extract custom attributes into member variables
        try {
//            resourceId = a.getInteger(R.styleable.DrawImageView_src, R.drawable.x1);
        } finally {
            // TypedArray objects are shared and must be recycled.
            a.recycle();
        }
    }

    public void initDraw() {

        paint = new Paint();
        path = new Path();
        mCanvas=new Canvas();
        //默认图像文件保存路径
        savePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/XAppBmps";

        paint.setStyle(Paint.Style.STROKE);//设置非填充
        paint.setStrokeWidth(bwidth);//设置笔宽
        paint.setAntiAlias(true);//锯齿不显示
        paint.setDither(true);  //防抖动
        paint.setStrokeJoin(Paint.Join.ROUND);//设置线段连接处样式为圆弧
        paint.setStrokeCap(Paint.Cap.ROUND);//设置线冒样式为圆形线冒

        if(resourceId == 0){
            resourceId = R.drawable.x1;
        }
        if(originalRect != null){
            setImageBitmapByResId(resourceId, originalRect);
        }else {
            setImageBitmapByResId(resourceId);
        }

    }

    public void setImageBitmapByResId(int resId, Rect rect) {
        resourceId = resId;
        bitmap = null;
        backupBitmap = null;
        originalRect = rect;
        rectImage = rect;
        path.reset();
        invalidate();
    }

    public void setImageBitmapByResId(int resId){
        Log.e(TAG,"resId: "+resId);
        if(resId == -1){
            isEmptyCBitmap = true;
            resourceId = -1;
            rotate = 0;
        }else {
            isEmptyCBitmap = false;
            resourceId = resId;
        }
        bitmap = null;
        backupBitmap = null;
        rectImage = null;
        path.reset();
        invalidate();
//        imageView = (ImageView) mcontext.findViewById(resourceId);
//        imageView.
//        setImageBitmap(decodeSampledBitmapFromResource(getResources(), resourceId, XApp.screenWidth, XApp.screenHeight));

//        this.post(new Runnable() {
//            @Override
//            public void run() {
//
//
//                Drawable drawable = getDrawable();
//                //获得ImageView中Image的变换矩阵
//                Matrix matrix = getImageMatrix();
//
//                int width = drawable.getBounds().width();
//                int height = drawable.getBounds().height();
//                Log.e(TAG,"width: "+width+" height: "+height);
//
//                if (drawable != null) {
//                    RectF rectf = new RectF();
//                    rectf.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//                    matrix.mapRect(rectf);     //最关键的一句
//
//                    rectImage = rectf;
//                    //imageView的实际绘制上下左右边坐标
//                    Log.e(TAG, "rectf: " + rectf.left + "  " + rectf.top + "  " + rectf.right + "  " + rectf.bottom);
//
//                }
//
//                float[] values = new float[10];
//                matrix.getValues(values);
//                //Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
//                float sx = values[0];
//                float sy = values[4];
//
//                //imageView中图片的实际绘制宽和高
//                imageWidth = (int) (width * sx);
//                imageHeight = (int) (height * sy);
//                //imageView的实际绘制宽和高
//                Log.e(TAG,"imageWidth: "+imageWidth+" imageHeight: "+imageHeight);
//
//                Log.e(TAG,"imageView.getWidth(): "+getWidth()+" imageView.getHeight(): "+getHeight());
//
//                Log.e(TAG,"imageView.getX(): "+getX()+" imageView.getY(): "+getY());
//                Log.e(TAG,"getLeft(): "+getLeft()+" getRight(): "+getRight()+" getTop(): "+getTop()+" getBottom(): "+getBottom());
//
//            }(int)((double)getWidth()*9936/7020)
//        });
//        bitmap = decodeSampledBitmapFromResource(getResources(), resourceId, XApp.screenWidth, XApp.screenHeight);setImageBitmap(bitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(bitmap == null) {
            Log.e(TAG, "getWidth(): "+getWidth() +" getHeight(): "+getHeight());
            if(isEmptyCBitmap){
                bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);//空白画布
            }else {
                if(rectImage == null){
                    bitmap = decodeSampledBitmapFromResource(getResources(), resourceId, getWidth(), getHeight());
                }else {
                    bitmap = decodeRectFOfBitmapFromResource(getResources(), resourceId, getWidth(), getHeight(), originalRect);
                }
            }
            backupBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
            imageWidth = bitmap.getWidth();
            imageHeight = bitmap.getHeight();
            if(rotate == 90){
                int tmp = imageHeight;
                imageHeight = imageWidth;
                imageWidth = tmp;
            }
            Log.e(TAG, "imageWidth: "+imageWidth +" imageHeight: "+imageHeight);
            mCanvas.setBitmap(bitmap);
            offsetY = (int) (double) (getHeight() - imageHeight) / 2;
            offsetX = (int) (double) (getWidth() - imageWidth) / 2;
            centerX = (int)Math.round((double)(getLeft() + getRight())/2);
            centerY = (int)Math.round((double)(getTop() + getBottom())/2);
        }

        canvas.save();
        if(rotate == 90){
            canvas.rotate(90);
            canvas.translate(offsetY,offsetX-getWidth());

        }else if(rotate == 0){
            canvas.translate(offsetX,offsetY);
        }

        canvas.drawBitmap(bitmap, 0, 0, paint);
//        canvas.drawBitmap(backupBitmap, 0, 0, paint);
//        canvas.drawPath(path,paint);
        Log.e(TAG,"onDraw(): canvas.drawBitmap(bitmap, 0, 0, paint);");

        canvas.restore();

//        onDrawed = true;
    }

    //解析图片资源原始尺寸
    public static Rect decodeDimensionOfImageFromResource(Resources res, int resId){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        return new Rect(0,0,options.outWidth,options.outHeight);

    }

    //解析图片资源区域显示
    public Bitmap decodeRectFOfBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight, Rect rect){
        InputStream in = res.openRawResource(resId);//资源id转换为InputStream

        originalImageWidth = rect.width();
        originalImageHeight = rect.height();

        BitmapRegionDecoder bitmapRegionDecoder = null;
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(in, false);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"资源解析失败！");
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bp = bitmapRegionDecoder.decodeRegion(rect, options);

        computeRotateAndScale(rect.width(), rect.height(), reqWidth, reqHeight);

        return createScaledBitmap(bp, reqWidth, reqHeight);
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, resId, options);
        originalImageWidth = options.outWidth;
        originalImageHeight = options.outHeight;
        originalRect = new Rect(0,0,originalImageWidth,originalImageHeight);
        Log.e(TAG, "options.outWidth: "+options.outWidth +" options.outHeight: "+options.outHeight);

        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeResource(res, resId, options).copy(Bitmap.Config.ARGB_8888, true);

        return createScaledBitmap(bm, reqWidth, reqHeight);
    }

    public Bitmap createScaledBitmap(Bitmap bmp, int reqWidth, int reqHeight){
        double reqScale = (double) reqHeight/reqWidth;//需要的高宽比

        //自动适配需要的尺寸
        if(rotate == 90){
            if(scale < reqScale){
                return Bitmap.createScaledBitmap(bmp,(int)(reqWidth*scale),reqWidth,true);
            }else {
                return Bitmap.createScaledBitmap(bmp,reqHeight,(int)(reqHeight/scale),true);
            }

        }else {
            if(scale < reqScale){
                return Bitmap.createScaledBitmap(bmp,reqWidth,(int)(reqWidth*scale),true);
            }else {
                return Bitmap.createScaledBitmap(bmp,(int)(reqHeight/scale),reqHeight,true);
            }
        }
    }

    public void computeRotateAndScale(int width, int height, int reqWidth, int reqHeight){
        if(reqWidth < reqHeight && width > height){
            rotate = 90;//标志绘制时需要右转90度

            int tmp = width;
            width = height;
            height = tmp;
        }else if(reqWidth > reqHeight && width < height){
            rotate = 90;//标志绘制时需要右转90度

            int tmp = width;
            width = height;
            height = tmp;
        }else {
            rotate = 0;;
        }

        scale = (double) height/width;//高宽比
    }

    public int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight){
        //Raw height and width of image
        int inSampleSize = 1;

        if(reqWidth < reqHeight && width > height){
            rotate = 90;//标志绘制时需要右转90度

            int tmp = width;
            width = height;
            height = tmp;
        }else if(reqWidth > reqHeight && width < height){
            rotate = 90;//标志绘制时需要右转90度

            int tmp = width;
            width = height;
            height = tmp;
        }else {
            rotate = 0;;
        }

        scale = (double) height/width;//高宽比

        if (height > reqHeight || width > reqHeight){
            // 计算出实际宽高和目标宽高的比率
//            final int heightRatio = Math.round((float) height / (float) reqHeight);
//            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
//            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            Log.e(TAG,"halfHeight: "+halfHeight +" halfWidth： "+halfWidth +" inSampleSize: "+inSampleSize+" reqHeight: "+reqHeight+" reqWidth: "+reqWidth);
            while ((halfHeight / inSampleSize)>=reqHeight && (halfWidth / inSampleSize)>=reqWidth){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;

    }

    private float mPreX, mPreY;//上一个点
    public void drawPath(float x, float y, Dot.DotType dotType){

        //坐标换算
//        float x = BaseActivity.baseActivity.mapX(bitmap.getWidth(), dx);
//        float y = BaseActivity.baseActivity.mapY(bitmap.getHeight(), dy);
        Log.e(TAG,"bitmap.getWidth(): "+bitmap.getWidth() +" bitmap.getHeight(): "+bitmap.getHeight());
        Log.e(TAG,"x: "+x +" y: "+y);
        Log.e(TAG,"dotType: "+dotType);

        switch (dotType){
            case PEN_DOWN:
                path.moveTo(x,y);
                mPreX = x;
                mPreY = y;
                Log.e(TAG,"moveTo("+x+","+y+")");
                break;
            case PEN_MOVE:
//                path.lineTo(x,y);
                float endX = (mPreX + x) / 2;
                float endy = (mPreY + y) / 2;
                //绘制贝塞尔曲线
                path.quadTo(mPreX,mPreY,endX,endy);
                mPreX = x;
                mPreY = y;
                Log.e(TAG,"lineTo("+x+","+y+")");
                break;
            default:
        }
    }

    public void drawDot(float dx, float dy, Dot.DotType dotType){
        if(bitmap != null){
            drawPath(dx, dy, dotType);
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空bitmap，否则会有锯齿
            mCanvas.drawBitmap(backupBitmap, 0, 0,paint);
            mCanvas.drawPath(path,paint);

            postInvalidate();
//            forceLayout();
//            requestLayout();
        }
    }

    public void drawDot(Dot dot){
        drawDot(new SimpleDot(dot));
    }

    public void drawDot(SimpleDot simpleDot){
        drawDot(simpleDot.getFloatX(), simpleDot.getFloatY(), simpleDot.type);
    }

    //绘制一组点
    public <T extends SimpleDot> void drawDots(ArrayList<T> simpleDots){
        final ArrayList<T> sDots = (ArrayList<T>)simpleDots.clone();
        for(T sDot : sDots){
            if(sDot instanceof TimelongDot){
                if(((TimelongDot) sDot).isEmptyDot()){
                    Log.e(TAG,"drawDots(): 是空点");
                    continue;
                }
            }
            Log.e(TAG,"drawDots(): sDot.x: "+sDot.getFloatX()+" sDot.y: "+sDot.getFloatY()+" sDot.type: "+sDot.type);
            drawPath(sDot.getFloatX(), sDot.getFloatY(), sDot.type);
        }
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空bitmap，否则会有锯齿
        mCanvas.drawBitmap(backupBitmap, 0, 0,paint);
        mCanvas.drawPath(path,paint);
        postInvalidate();
    }

    //参数应为完整的路径，如: Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+path
    public boolean setSavePath(String path){
        File file = new File(path);
        if(!file.exists()) {
            file.mkdirs();
        }
        if(!file.exists()){
            return false;
        }else{
            this.savePath = path;
            return true;
        }
    }

    //只改变存储路径后面的部分，如: XAppBmps/oneBmp
    public boolean changeSavePath(String filePath){
        return setSavePath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+filePath);
    }

    //参数为不包含后缀的文件名
    public boolean saveBmp(String fileName){
        return saveBmp(fileName+".png",10);
    }

    //参数为包含后缀的文件名，如: 1-1.png
    public boolean saveBmp(String fileName, int quality){
        if(!fileName.endsWith(".png")){
            Log.e(TAG,fileName);
            Log.e(TAG,"saveBmp(): bmp存储文件名后缀不合法");
            return false;
        }
        File file = new File(savePath,fileName);
        File fileParent = file.getParentFile();
        if(!fileParent.exists()){
            fileParent.mkdirs();
        }
        if(file.exists()) {
            Log.e(TAG,"saveBmp(): bmp存储文件已存在，请更换文件名");
            return false;
        }

        FileOutputStream out = null;
        try{
            out = new FileOutputStream(file);
            if(bitmap != null){
                bitmap.compress(Bitmap.CompressFormat.PNG,quality,out);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null){
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
    public void clear(){
        bitmap = backupBitmap;
        backupBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        mCanvas.setBitmap(bitmap);
        path.reset();
        postInvalidate();
    }

    public void reDraw(int speed){

    }

    public void drawDestroy(){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }

        paint=null;
        mCanvas=null;
    }


}
