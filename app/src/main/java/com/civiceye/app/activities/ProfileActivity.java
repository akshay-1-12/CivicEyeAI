package com.civiceye.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.civiceye.app.R;
import com.civiceye.app.utils.BottomNavHelper;
import com.civiceye.app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SessionManager session = new SessionManager(this);

        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);

        tvName.setText(session.getName());
        tvEmail.setText(session.getEmail());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_profile);
        BottomNavHelper.setup(this, nav);
    }
}
