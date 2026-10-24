package com.civiceye.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.civiceye.app.R;
import com.civiceye.app.utils.SessionManager;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            if (session.isLoggedIn()) {
                startActivity(new Intent(this, HomeActivity.class));
            } else {
                startActivity(new Intent(this, OnboardingActivity.class));
            }
            finish();
        }, 2200);
    }
}