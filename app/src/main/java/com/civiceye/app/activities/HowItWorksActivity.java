package com.civiceye.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.civiceye.app.R;

public class HowItWorksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_it_works);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnStartReporting).setOnClickListener(v -> {
            startActivity(new Intent(this, CaptureActivity.class));
            finish();
        });
    }
}
