package com.civiceye.app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.civiceye.app.R;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        findViewById(R.id.tvSkip).setOnClickListener(v -> goLogin());
        findViewById(R.id.btnNext).setOnClickListener(v -> goLogin());
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}