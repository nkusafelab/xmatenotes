package com.example.xmatenotes.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xmatenotes.R;

public class DetailMessage extends AppCompatActivity {

    private TextView detailTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_message);

        detailTextView = findViewById(R.id.detailTextView);

        String detailInfo = getIntent().getStringExtra("detailInfo");

        detailTextView.setText(detailInfo);
    }
}