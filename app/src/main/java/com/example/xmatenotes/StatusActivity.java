package com.example.xmatenotes;



import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.example.xmatenotes.app.XmateNotesApplication;
//import com.example.xmatenotes.logic.manager.AppDataBase;
import com.example.xmatenotes.logic.manager.RoleManager;
//import com.example.xmatenotes.logic.dao.RoleDao;
import com.example.xmatenotes.ui.BaseActivity;
import com.tqltech.tqlpencomm.PenCommAgent;
import com.tqltech.tqlpencomm.bean.PenStatus;


public class StatusActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = "StatusActivity";

    private LinearLayout ll_name; //角色

    private LinearLayout student_name; //学生编号

    private LinearLayout group_number;  //小组编号

    private LinearLayout group_tip; //小组组型

    private LinearLayout school; //学校

    private LinearLayout class_number; //班级

    private LinearLayout grade; //年级

    private TextView student1_name;

    private TextView tv_name;

    private TextView group1_number;

    private TextView group1_tip;

    private TextView school1;

    private TextView class1_number;

    private TextView grade1;

    private TextView tv_address;

    private PenStatus penStatus;

    private String mName;

    private String nowRole;

    private String nowStudent_number;

    private String nowGroup_number;

    private String nowGroup_tip;

    private String nowSchool;

    private String nowClass;

    private String nowGrade;

    private PenCommAgent penCommAgent;

    private Button button;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
//        RoleDao roleDao = AppDataBase.getDatabase(XmateNotesApplication.context).roleDao();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("当前状态");
        actionBar.setDisplayHomeAsUpEnabled(true);

        ll_name = findViewById(R.id.ll_name);

        ll_name.setOnClickListener(this);

        student_name = findViewById(R.id.student_name);

        student_name.setOnClickListener(this);

        group_number = findViewById(R.id.group_number);

        group_number.setOnClickListener(this);

        group_tip = findViewById(R.id.group_tip);

        group_tip.setOnClickListener(this);

        school = findViewById(R.id.school);

        school.setOnClickListener(this);

        class_number = findViewById(R.id.class_number);

        class_number.setOnClickListener(this);

        grade = findViewById(R.id.grade);

        grade.setOnClickListener(this);

        grade1 = findViewById(R.id.grade1);

        class1_number = findViewById(R.id.class1_number);

        school1 = findViewById(R.id.school1);

        group1_tip = findViewById(R.id.group1_tip);

        student1_name  = findViewById(R.id.student1_name);

        tv_name = findViewById(R.id.tv_name);

        group1_number = findViewById(R.id.group1_number);

        tv_address = findViewById(R.id.tv_address);

        penCommAgent = PenCommAgent.GetInstance(getApplication());

        penStatus = penCommAgent.getPenStatus();

        button = (Button) findViewById(R.id.button2);

        //加载各种布局

        if("00:00:00:00:00:00".equals(XmateNotesApplication.mBTMac)){
            Toast.makeText(StatusActivity.this, "未连接蓝牙", Toast.LENGTH_SHORT).show();

            tv_address.setText("");
            if(RoleManager.getRole()==null){
                tv_name.setText("");
                student1_name.setText("");
                group1_number.setText("");
                group1_tip.setText("");
                school1.setText("");
                class1_number.setText("");
                grade1.setText("");
            }
            else{
                tv_name.setText(RoleManager.getRole().getRoleName());
                nowRole = RoleManager.getRole().getRoleName();
                student1_name.setText(RoleManager.getRole().getStudentNumber());
                nowStudent_number = RoleManager.getRole().getStudentNumber();
                group1_number.setText(RoleManager.getRole().getGroupNumber());
                nowGroup_number = RoleManager.getRole().getGroupNumber();
                group1_tip.setText(RoleManager.getRole().getGroupTip());
                nowGroup_tip = RoleManager.getRole().getGroupTip();
                school1.setText(RoleManager.getRole().getSchool());
                nowSchool = RoleManager.getRole().getSchool();
                class1_number.setText(RoleManager.getRole().getClassNumber());
                nowClass = RoleManager.getRole().getClassNumber();
                grade1.setText(RoleManager.getRole().getGrade());
                nowGrade = RoleManager.getRole().getGrade();
            }
        }
        else{

            tv_address.setText(penStatus.mPenMac);

            if(RoleManager.getRole(XmateNotesApplication.mBTMac)==null){
                tv_name.setText("");
                student1_name.setText("");
                group1_number.setText("");
                group1_tip.setText("");
                school1.setText("");
                class1_number.setText("");
                grade1.setText("");
            }
            else{
                nowRole = RoleManager.getRole(XmateNotesApplication.mBTMac).getRoleName();
                tv_name.setText(RoleManager.getRole(XmateNotesApplication.mBTMac).getRoleName());
                nowStudent_number = RoleManager.getRole(XmateNotesApplication.mBTMac).getStudentNumber();
                student1_name.setText(RoleManager.getRole(XmateNotesApplication.mBTMac).getStudentNumber());//
                nowGroup_number = RoleManager.getRole(XmateNotesApplication.mBTMac).getGroupNumber();
                group1_number.setText(RoleManager.getRole(XmateNotesApplication.mBTMac).getGroupNumber());
                nowGroup_tip = RoleManager.getRole(XmateNotesApplication.mBTMac).getGroupTip();
                group1_tip.setText(RoleManager.getRole(XmateNotesApplication.mBTMac).getGroupTip());
                nowSchool = RoleManager.getRole(XmateNotesApplication.mBTMac).getSchool();
                school1.setText(RoleManager.getRole(XmateNotesApplication.mBTMac).getSchool());
                nowClass = RoleManager.getRole(XmateNotesApplication.mBTMac).getClassNumber();
                class1_number.setText(RoleManager.getRole(XmateNotesApplication.mBTMac).getClassNumber());
                nowGrade = RoleManager.getRole(XmateNotesApplication.mBTMac).getGrade();
                grade1.setText(RoleManager.getRole(XmateNotesApplication.mBTMac).getGrade());
            }

        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.button2:
                        if("00:00:00:00:00:00".equals(XmateNotesApplication.mBTMac)){
                            if(nowRole == null || nowStudent_number == null || nowGroup_number ==null || nowGroup_tip == null || nowSchool == null || nowClass == null || nowGrade == null || XmateNotesApplication.mBTMac == null){
                                Toast.makeText(StatusActivity.this, "绑定未完成", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(StatusActivity.this, "未连接蓝牙", Toast.LENGTH_SHORT).show();
                                XmateNotesApplication.role = RoleManager.createRole(nowRole,nowStudent_number,nowGroup_number,nowGroup_tip,nowSchool,nowClass,nowGrade,XmateNotesApplication.mBTMac);
                            }

                        } else {
                            if(nowRole == null || nowStudent_number == null || nowGroup_number ==null || nowGroup_tip == null || nowSchool == null || nowClass == null || nowGrade == null || XmateNotesApplication.mBTMac == null){
                                Toast.makeText(StatusActivity.this, "绑定未完成", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                XmateNotesApplication.role = RoleManager.createRole(nowRole,nowStudent_number,nowGroup_number,nowGroup_tip,nowSchool,nowClass,nowGrade,XmateNotesApplication.mBTMac);
                                RoleManager.saveRole(XmateNotesApplication.role);
                                Toast.makeText(StatusActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                            }

                        }
                        break;
                    default:
                }
            }
        });
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
            case R.id.student_name:
                showSetPenNameDialog1();
                break;
            case R.id.group_number:
                showSetPenNameDialog2();
                break;
            case R.id.group_tip:
                showSetPenNameDialog3();
                break;
            case R.id.school:
                showSetPenNameDialog4();
                break;
            case R.id.class_number:
                showSetPenNameDialog5();
                break;
            case R.id.grade:
                showSetPenNameDialog6();
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
//    private void updateStatus() {
//
//        tv_name.setText(XmateNotesApplication.mPenName);
//
//        if (!TextUtils.isEmpty(penStatus.mPenMac)) {
//            tv_address.setText(penStatus.mPenMac);
//        } else {
//            tv_address.setText("");
//        }
//    }



    /**
     * 设置笔名dialog
     */
    private void showSetPenNameDialog() {
        Dialog dialog = new Dialog(this, R.style.customDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_set_name, null);
        EditText et_pen_name = view.findViewById(R.id.et_pen_name);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_ok = view.findViewById(R.id.tv_ok);

        et_pen_name.setText(tv_name.getText().toString()); //在这里使得能够持久显示
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowRole = et_pen_name.getText().toString();
                tv_name.setText(nowRole);
                dialog.dismiss();
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

    private void showSetPenNameDialog1() {
        Dialog dialog = new Dialog(this, R.style.customDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.student_name, null);
        EditText et_pen_name = view.findViewById(R.id.et_pen_name);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_ok = view.findViewById(R.id.tv_ok);

        et_pen_name.setText(student1_name.getText().toString());
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowStudent_number = et_pen_name.getText().toString();
                student1_name.setText(nowStudent_number);
                dialog.dismiss();
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

    private void showSetPenNameDialog2(){
        Dialog dialog = new Dialog(this, R.style.customDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.group_number, null);
        EditText et_pen_name = view.findViewById(R.id.et_pen_name);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_ok = view.findViewById(R.id.tv_ok);

        et_pen_name.setText(group1_number.getText().toString()); //在这里使得能够持久显示
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowGroup_number = et_pen_name.getText().toString();
                group1_number.setText(nowGroup_number);
                dialog.dismiss();
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

    private void showSetPenNameDialog3(){
        Dialog dialog = new Dialog(this, R.style.customDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.group_tip, null);
        EditText et_pen_name = view.findViewById(R.id.et_pen_name);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_ok = view.findViewById(R.id.tv_ok);

        et_pen_name.setText(group1_tip.getText().toString()); //在这里使得能够持久显示
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowGroup_tip = et_pen_name.getText().toString();
                group1_tip.setText(nowGroup_tip);
                dialog.dismiss();
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

    private void showSetPenNameDialog4(){
        Dialog dialog = new Dialog(this, R.style.customDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.school, null);
        EditText et_pen_name = view.findViewById(R.id.et_pen_name);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_ok = view.findViewById(R.id.tv_ok);

        et_pen_name.setText(school1.getText().toString()); //在这里使得能够持久显示
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowSchool = et_pen_name.getText().toString();
                school1.setText(nowSchool);
                dialog.dismiss();
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

    private void showSetPenNameDialog5(){
        Dialog dialog = new Dialog(this, R.style.customDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.class_number, null);
        EditText et_pen_name = view.findViewById(R.id.et_pen_name);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_ok = view.findViewById(R.id.tv_ok);

        et_pen_name.setText(class1_number.getText().toString()); //在这里使得能够持久显示
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowClass = et_pen_name.getText().toString();
                class1_number.setText(nowClass);
                dialog.dismiss();
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

    private void showSetPenNameDialog6(){
        Dialog dialog = new Dialog(this, R.style.customDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.grade, null);
        EditText et_pen_name = view.findViewById(R.id.et_pen_name);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_ok = view.findViewById(R.id.tv_ok);

        et_pen_name.setText(grade1.getText().toString()); //在这里使得能够持久显示
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowGrade = et_pen_name.getText().toString();
                grade1.setText(nowGrade);
                dialog.dismiss();
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
