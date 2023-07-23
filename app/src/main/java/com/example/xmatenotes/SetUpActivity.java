package com.example.xmatenotes;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xmatenotes.App.XApp;

public class SetUpActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("设置");
        //actionBar.setTitle(ApplicationResources.getLocalVersionName(this));
        actionBar.setDisplayHomeAsUpEnabled(true);

        editText = (EditText) findViewById(R.id.editTextTextPersonName);
        button = (Button) findViewById(R.id.button2);

        if("00:00:00:00:00:00".equals(XApp.mBTMac)){
            Toast.makeText(SetUpActivity.this, "未连接蓝牙", Toast.LENGTH_SHORT).show();
        } else {
            String name = XApp.penMacManager.getNameByMac(XApp.mBTMac);
            if(name != null){
                editText.setText(name);
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.button2:
                        if("00:00:00:00:00:00".equals(XApp.mBTMac)){
                            Toast.makeText(SetUpActivity.this, "未连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            String s = String.valueOf(editText.getText());
                            SharedPreferences.Editor editor = getSharedPreferences(XApp.peopleSharedPreferences, MODE_PRIVATE).edit();
                            editor.putString(XApp.mBTMac, s);
                            editor.apply();
                            Toast.makeText(SetUpActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}