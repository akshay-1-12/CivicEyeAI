package com.civiceye.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.civiceye.app.R;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;
import com.civiceye.app.utils.BottomNavHelper;
import com.civiceye.app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private SessionManager session;
    private TextView tvUserRole, tvHello;
    private View citizenContainer, adminContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        session = new SessionManager(this);
        
        tvHello = findViewById(R.id.tvHello);
        tvUserRole = findViewById(R.id.tvUserRole);
        citizenContainer = findViewById(R.id.citizenContainer);
        adminContainer = findViewById(R.id.adminContainer);

        setupDashboardByRole();
        setupClickListeners();

        // Setup Bottom Navigation
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_home);
        BottomNavHelper.setup(this, nav);

        // For Admin: Hide the "+" (Report) button from the bottom bar
        if (session.isAdmin()) {
            Menu menu = nav.getMenu();
            menu.findItem(R.id.nav_add).setVisible(false);
        }
    }

    private void setupDashboardByRole() {
        tvHello.setText(getString(R.string.hello_user, session.getName()));

        if (session.isAdmin()) {
            // ADMIN UI
            tvUserRole.setText(R.string.admin_dashboard);
            tvUserRole.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_admin));
            tvUserRole.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            
            adminContainer.setVisibility(View.VISIBLE);
            citizenContainer.setVisibility(View.GONE);
            fetchAdminStats(); 
        } else {
            // CITIZEN UI
            tvUserRole.setText(R.string.citizen_dashboard);
            tvUserRole.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_citizen));
            tvUserRole.setTextColor(ContextCompat.getColor(this, R.color.primary));
            
            adminContainer.setVisibility(View.GONE);
            citizenContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        // --- SHARED ACTIONS ---
        findViewById(R.id.ivBell).setOnClickListener(v ->
                startActivity(new Intent(this, UpdatesActivity.class)));

        findViewById(R.id.cardNearby).setOnClickListener(v ->
                startActivity(new Intent(this, MapsActivity.class)));

        // --- CITIZEN ONLY ACTIONS ---
        findViewById(R.id.cardReport).setOnClickListener(v ->
                startActivity(new Intent(this, CaptureActivity.class)));

        findViewById(R.id.cardMyComplaints).setOnClickListener(v ->
                startActivity(new Intent(this, MyComplaintsActivity.class)));

        findViewById(R.id.cardHow).setOnClickListener(v ->
                startActivity(new Intent(this, HowItWorksActivity.class)));
        
        // --- ADMIN ONLY ACTIONS ---
        findViewById(R.id.cardAllComplaints).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyComplaintsActivity.class);
            intent.putExtra("is_admin_view", true);
            startActivity(intent);
        });

        findViewById(R.id.cardAdminStats).setOnClickListener(v ->
                startActivity(new Intent(this, StatisticsActivity.class)));

        findViewById(R.id.cardUserManagement).setOnClickListener(v ->
                Toast.makeText(this, "Department Directory coming soon", Toast.LENGTH_SHORT).show());
    }

    private void fetchAdminStats() {
        ApiClient.getClient().create(ApiService.class).getAllIssues().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONArray arr = new JSONArray(response.body().string());
                        int total = arr.length();
                        int pending = 0;
                        int resolved = 0;

                        for (int i = 0; i < total; i++) {
                            String status = arr.getJSONObject(i).optString("status", "Pending");
                            if ("Resolved".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status)) {
                                resolved++;
                            } else {
                                pending++;
                            }
                        }

                        ((TextView) findViewById(R.id.tvTotalReports)).setText(String.valueOf(total));
                        ((TextView) findViewById(R.id.tvPendingCount)).setText(String.valueOf(pending));
                        ((TextView) findViewById(R.id.tvResolvedSummary)).setText(String.valueOf(resolved));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session.isAdmin()) fetchAdminStats();
    }
}
