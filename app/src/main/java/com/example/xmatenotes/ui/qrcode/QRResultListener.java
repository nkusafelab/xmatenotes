package com.example.xmatenotes.ui.qrcode;

import com.king.mlkit.vision.camera.AnalyzeResult;

import java.util.List;

public interface QRResultListener {

    public void onSuccess(AnalyzeResult<List<String>> result);
    public void onFailure(Exception e);
}
