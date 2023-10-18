package com.example.xmatenotes;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import com.example.xmatenotes.ui.qrcode.QRResultListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.king.mlkit.vision.camera.AnalyzeResult;
import com.king.mlkit.vision.camera.analyze.Analyzer;
import com.king.mlkit.vision.camera.util.BitmapUtils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * ML Kit二维码分析器：分析相机预览的帧数据，从中检测识别二维码
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class MLScanningAnalyzer  implements Analyzer<List<String>> {

    private static final String TAG = "MLScanningAnalyzer";

    /**
     * 是否需要输出二维码的各个顶点
     */
    private boolean isOutputVertices;

    private BarcodeScanner scanner;

    public MLScanningAnalyzer() {
        this(false);
    }

    /**
     * 构造
     *
     * @param isOutputVertices 是否需要返回二维码的各个顶点
     */
    public MLScanningAnalyzer(boolean isOutputVertices) {
        this.isOutputVertices = isOutputVertices;
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
//                                Barcode.FORMAT_ALL_FORMATS
                                Barcode.FORMAT_QR_CODE
//                                , Barcode.FORMAT_AZTEC
                        )
                        .build();
        scanner = BarcodeScanning.getClient(options);
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull OnAnalyzeListener<AnalyzeResult<List<String>>> listener) {

        @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            final Bitmap bitmap = BitmapUtils.getBitmap(imageProxy);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

//            InputImage image =
//                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            // Pass image to an ML Kit Vision API
            // ...
//            Log.e(TAG, "analyze: image: "+image);
            Task<List<Barcode>> results = scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // Task completed successfully
                            // ...
                            Log.e(TAG, "onSuccess: barcode analyze");
                            if (barcodes.isEmpty()) {
                                Log.v(TAG, "No barcode has been detected");
                            }else {
                                AnalyzeResult<List<String>> result = null;
                                List<String> values = new ArrayList<>();
                                for (Barcode barcode: barcodes) {
                                    values.add(barcode.getRawValue());
                                }
                                result = new MLQRCodeAnalyzeResult<List<String>>(bitmap, values, barcodes);
                                if (result != null && !result.getResult().isEmpty()) {
                                    listener.onSuccess(result);
                                } else {
                                    listener.onFailure(null);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                            Log.e(TAG, "onFailure: barcode analyze");
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            mediaImage.close();
                            imageProxy.close();
                        }
                    });

        }
    }

    public void analyze(Bitmap bitmap, QRResultListener listener) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<List<Barcode>> results = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        // Task completed successfully
                        // ...
                        Log.e(TAG, "onSuccess: barcode analyze");
                        if (barcodes.isEmpty()) {
                            Log.v(TAG, "No barcode has been detected");
                        }else {
                            AnalyzeResult<List<String>> result = null;
                            List<String> values = new ArrayList<>();
                            for (Barcode barcode: barcodes) {
                                values.add(barcode.getRawValue());
                            }
                            result = new MLQRCodeAnalyzeResult<List<String>>(bitmap, values, barcodes);
                            if (result != null && !result.getResult().isEmpty()) {
                                listener.onSuccess(result);
                            } else {
                                listener.onFailure(null);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        Log.e(TAG, "onFailure: barcode analyze");
                    }
                });
    }

    /**
     * 二维码分析结果
     *
     * @param <T>
     */
    public static class MLQRCodeAnalyzeResult<T> extends AnalyzeResult<T> {

        /**
         * 二维码的位置点信息
         */
        private List<Mat> points;

        private Rect rect;

        public MLQRCodeAnalyzeResult(Bitmap bitmap, T result) {
            super(bitmap, result);
        }

        public MLQRCodeAnalyzeResult(Bitmap bitmap, T result, List<Barcode> barcodes) {
            super(bitmap, result);

            for (Barcode barcode: barcodes) {
                if (barcode.getBoundingBox() != null) {
                    rect = barcode.getBoundingBox();
                    Log.v(
                            TAG,
                            String.format(
                                    "Detected barcode's bounding box: %s", barcode.getBoundingBox().flattenToString()));
                }
                if (barcode.getCornerPoints() != null) {
                    Log.v(
                            TAG,
                            String.format(
                                    "Expected corner point size is 4, get %d", barcode.getCornerPoints().length));
                    for (Point point : barcode.getCornerPoints()) {
                        Log.v(
                                TAG,
                                String.format("Corner point is located at: x = %d, y = %d", point.x, point.y));

                    }
                    Mat mat = null;
                    for(int i = 0; i < barcode.getCornerPoints().length; i++){
                        if(i%4 == 0){
                            // 将Point对象转换为Mat对象
                            mat = new Mat(4, 4, CvType.CV_32SC2);
                        }
                        mat.put(i%4, i%4, barcode.getCornerPoints()[i].x, barcode.getCornerPoints()[i].y);
                        if((i+1)%4 == 0){
                            if(points == null){
                                points = new ArrayList<>();
                            }
                            points.add(mat);
                        }
                    }
                }
            }


        }

        /**
         * 获取二维码的位置点信息
         *
         * @return
         */
        public List<Mat> getPoints() {
            return points;
        }

        @Deprecated
        public void setPoints(List<Mat> points) {
            this.points = points;
        }

        public Rect getBoundingBox(){
            if(rect != null){
                return rect;
            }
            return null;
        }
    }

}
