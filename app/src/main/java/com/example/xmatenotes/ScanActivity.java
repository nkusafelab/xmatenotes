package com.example.xmatenotes;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;




public class ScanActivity extends AppCompatActivity implements QRCodeView.Delegate {

    private ZXingView mZxingView;

    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

    public static final int TAKE_PHOTO = 1;
    public static final int SELECT_PHOTO = 2;

//    private ImageView picture;
    private Uri imageUri;

    private Button btnphoto;
    private Button btnlocal;

    private String photo_path;


    static{
//        List<BarcodeFormat> allFormats = new ArrayList<>();
//        allFormats.add(BarcodeFormat.AZTEC);
//        allFormats.add(BarcodeFormat.CODABAR);
//        allFormats.add(BarcodeFormat.CODE_39);
//        allFormats.add(BarcodeFormat.CODE_93);
//        allFormats.add(BarcodeFormat.CODE_128);
//        allFormats.add(BarcodeFormat.DATA_MATRIX);
//        allFormats.add(BarcodeFormat.EAN_8);
//        allFormats.add(BarcodeFormat.EAN_13);
//        allFormats.add(BarcodeFormat.ITF);
//        allFormats.add(BarcodeFormat.MAXICODE);
//        allFormats.add(BarcodeFormat.PDF_417);
//        allFormats.add(BarcodeFormat.QR_CODE);
//        allFormats.add(BarcodeFormat.RSS_14);
//        allFormats.add(BarcodeFormat.RSS_EXPANDED);
//        allFormats.add(BarcodeFormat.UPC_A);
//        allFormats.add(BarcodeFormat.UPC_E);
//        allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION);
//
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
        HINTS.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("扫一扫");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mZxingView = findViewById(R.id.zxing_view);
        mZxingView.setDelegate(this);
        btnphoto = findViewById(R.id.Button_photo);



        btnphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //启动相机程序
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(takePictureIntent, TAKE_PHOTO);
//                }

            }
        });
        btnlocal = findViewById(R.id.Button_select);
        btnlocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PHOTO);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    // 调用ZXing进行识别
                    String result = decodeBarcode(imageBitmap);
                    if (result != null) {
                        Toast.makeText(this, "识别结果：" + result, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "未识别到二维码", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        if (requestCode == SELECT_PHOTO && resultCode==RESULT_OK) {
            if (data != null && data.getData() != null) {
                Bitmap imageBitmap = getBitmapFromGallery(data.getData());
                if (imageBitmap != null) {
                    String result = decodeBarcode(imageBitmap);
                    if (result != null) {
                        Toast.makeText(this, "识别结果：" + result, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "未识别到二维码", Toast.LENGTH_LONG).show();
                    }
                }
            }

        }
    }

    private Bitmap getBitmapFromGallery(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mZxingView.startCamera();// 打开后置摄像头开始预览，但是并未开始识别
        mZxingView.startSpotAndShowRect();// 显示扫描框，并开始识别
    }


    @Override
    protected void onStop() {
        super.onStop();
        mZxingView.stopCamera();// 关闭摄像头预览，并隐藏扫描框
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Toast.makeText(getBaseContext(), "扫描结果为：" + result, Toast.LENGTH_SHORT).show();
        mZxingView.stopSpot();// 停止识别
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        Log.d("IS_DARK", String.valueOf(isDark));
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Toast.makeText(getBaseContext(), "ERROR", Toast.LENGTH_SHORT).show();
        mZxingView.stopSpot();// 停止识别
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        //android.R.id.home对应应用程序图标的id
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String decodeBarcode(Bitmap bitmap) {
        int maxDimension = 512;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float aspectRatio = (float) width / height;
        if (width > height) {
            width = maxDimension;
            height = (int) (width / aspectRatio);
        } else {
            height = maxDimension;
            width = (int) (height * aspectRatio);
        }

        Bitmap compressedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        String decoded = null;
        int[] pixels = new int[width * height];
        compressedBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource rgbsource = new RGBLuminanceSource(width, height, pixels);
        //LuminanceSource yuvsource = RGBToYUV(rgbsource);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbsource));
        Reader reader = new QRCodeReader();
        try {
            Result result = null;
            result = reader.decode(binaryBitmap, HINTS);
            decoded = result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decoded;
    }
}