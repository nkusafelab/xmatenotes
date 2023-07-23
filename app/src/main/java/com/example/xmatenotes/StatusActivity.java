package com.example.xmatenotes;



import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.example.xmatenotes.App.XApp;
import com.tqltech.tqlpencomm.PenCommAgent;
import com.tqltech.tqlpencomm.bean.PenStatus;


public class StatusActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = "StatusActivity";

    private LinearLayout ll_name;

    private TextView tv_name;

    private TextView tv_address;

    private PenStatus penStatus;

    private String mName;

    private PenCommAgent penCommAgent;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("当前状态");
        actionBar.setDisplayHomeAsUpEnabled(true);

        ll_name = findViewById(R.id.ll_name);

        ll_name.setOnClickListener(this);

        tv_name = findViewById(R.id.tv_name);
        tv_address = findViewById(R.id.tv_address);

        penCommAgent = PenCommAgent.GetInstance(getApplication());
        penStatus = penCommAgent.getPenStatus();

    }


    // 点击空白区域 自动隐藏软键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(null != this.getCurrentFocus()){
            /**
             * 点击空白位置 隐藏软键盘
             */
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                penCommAgent.getPenAllStatus();
//            }
//        }).start();
//
//        updateStatus();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_name:
                showSetPenNameDialog();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO Auto-generated method stub

        //android.R.id.home对应应用程序图标的id
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 状态显示
     */
    private void updateStatus() {

        tv_name.setText(XApp.mPenName);

        if (!TextUtils.isEmpty(penStatus.mPenMac)) {
            tv_address.setText(penStatus.mPenMac);
        } else {
            tv_address.setText("");
        }
    }


    /**
     * 设置笔名dialog
     */
    private void showSetPenNameDialog() {
        Dialog dialog = new Dialog(this, R.style.customDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_set_name, null);
        EditText et_pen_name = view.findViewById(R.id.et_pen_name);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_ok = view.findViewById(R.id.tv_ok);

        et_pen_name.setText(tv_name.getText().toString());
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mName = et_pen_name.getText().toString();
                if (mName.length() > 12) {
                    showToast("笔名长度不能超过12");
                    return;
                }
                penCommAgent.setPenName(mName);
                XApp.mPenName=mName;
                dialog.dismiss();
                updateStatus();
            }
        });

        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        //window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        dialog.show();
    }
}
