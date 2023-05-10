package com.example.xmatenotes;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;



//新建一个类继承View
public class DrawView extends View{
	private static final String TAG = "DrawView";
	private int mov_x;//声明起点坐标
	private int mov_y;
	public Paint paint;//声明画笔
	public Canvas canvas;//画布
	public Bitmap bitmap;//位图

	private int blcolor=Color.RED;
	private int bwidth=3;


	private BaseActivity mcontext;

	public DrawView(BaseActivity context)
	{
		super(context);
		this.mcontext = context;
		initDraw();
	}

	//画位图
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		Paint bmpPaint = new Paint();
		canvas.drawBitmap(bitmap, 0, 0, bmpPaint);
	}

	public void initDraw()
	{
		paint=null;
		//bitmap=null;
		canvas=null;
		paint=new Paint(Paint.DITHER_FLAG);//创建一个画笔]
		//	bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888); //设置位图的宽高
		//bitmap = Bitmap.createBitmap(1200, 1824, Bitmap.Config.ARGB_8888); //设置位图的宽高
		//bitmap = Bitmap.createBitmap(1550, 2320, Bitmap.Config.ARGB_8888); //设置位图的宽高

	//	BitmapFactory.Options options = new BitmapFactory.Options();
	//	options.inJustDecodeBounds = true;      // 设置为true，不将图片解码到内存中
	//	BitmapFactory.decodeResource(getResources(),R.drawable.x1, options);
	//	options.inSampleSize = 2;
	//	options.inJustDecodeBounds = false;
		if(bitmap == null){
			bitmap = Bitmap.createBitmap(1900, 2300, Bitmap.Config.ARGB_8888); //设置位图的宽高
	//		bitmap = BitmapFactory.decodeResource(getResources(),R.id.imageview, options);
		}

		canvas=new Canvas();
	//	paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	//	canvas.drawPaint(paint);
	//	paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		///canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

	//	canvas.setBitmap(bitmap);
		canvas.drawBitmap(bitmap,0,0,paint);

		paint.setStyle(Style.STROKE);//设置非填充
		//  paint.setStrokeWidth(bwidth);//笔宽5像素
		//  paint.setColor(blcolor);//设置为红笔
		paint.setAntiAlias(true);//锯齿不显示
		paint.setDither(true);  //防抖动
		invalidate();
	}
	public void setVcolor(int vlcolor)
	{
		blcolor=vlcolor;
		paint.setColor(blcolor);
	}
	public void setVwidth(int vwidth)
	{
		bwidth=vwidth;
		paint.setStrokeWidth(bwidth);
	}

	public void DrawDestroy(){
		if(bitmap != null && !bitmap.isRecycled()){
			bitmap.recycle();
			bitmap = null;
		}

		paint=null;
		canvas=null;
	}

	/*
	//触摸事件
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	if (event.getAction()==MotionEvent.ACTION_MOVE) {//如果拖动
	 canvas.drawLine(mov_x, mov_y, event.getX(), event.getY(), paint);//画线
	 invalidate();
	}
	if (event.getAction()==MotionEvent.ACTION_DOWN) {//如果点击
	 mov_x=(int) event.getX();
	 mov_y=(int) event.getY();
	 Log.d("USB_HOST", "x:"+mov_x+"y:"+mov_y);
	 canvas.drawPoint(mov_x, mov_y, paint);//画点
	 invalidate();
	}
	mov_x=(int) event.getX();
	mov_y=(int) event.getY();
	//return true;
	return super.onTouchEvent(event);
	}
	*/
	}
