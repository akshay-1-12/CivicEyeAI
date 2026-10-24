package com.civiceye.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.civiceye.app.R;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;
import com.civiceye.app.models.Complaint;
import com.civiceye.app.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplaintDetailActivity extends AppCompatActivity {

    private Complaint complaint;
    private SessionManager session;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_detail);

        complaint = (Complaint) getIntent().getSerializableExtra("complaint");
        session = new SessionManager(this);

        if (complaint == null) {
            Toast.makeText(this, R.string.error_loading_details, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupAdminPanel();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        btnBack.setImageResource(R.drawable.ic_back);

        ImageView ivImage = findViewById(R.id.ivDetailImage);
        TextView tvType = findViewById(R.id.tvDetailType);
        tvStatus = findViewById(R.id.tvDetailStatus);
        TextView tvCode = findViewById(R.id.tvDetailCode);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvSeverity = findViewById(R.id.tvDetailSeverity);
        TextView tvDept = findViewById(R.id.tvDetailDept);

        tvType.setText(complaint.getIssueType());
        tvStatus.setText(complaint.getStatus());
        
        // Use formatted string resource instead of concatenation
        tvCode.setText(getString(R.string.complaint_code_format, complaint.getComplaintCode()));

        tvLocation.setText(complaint.getAddress());
        tvDescription.setText(complaint.getDescription());
        tvSeverity.setText(complaint.getSeverity());
        tvDept.setText(complaint.getDepartment());

        updateStatusUI(complaint.getStatus());

        String imageUrl = complaint.getImageUrl();
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = ApiClient.BASE_URL + (imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl);
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .into(ivImage);
    }

    private void setupAdminPanel() {
        View adminPanel = findViewById(R.id.adminActionPanel);
        if (session.isAdmin()) {
            adminPanel.setVisibility(View.VISIBLE);
            
            findViewById(R.id.btnMarkInProgress).setOnClickListener(v -> updateStatus("In Progress"));
            findViewById(R.id.btnMarkResolved).setOnClickListener(v -> updateStatus("Resolved"));
        } else {
            adminPanel.setVisibility(View.GONE);
        }
    }

    private void updateStatus(String newStatus) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("report_id", complaint.getId());
        body.put("status", newStatus);

        apiService.updateStatus(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ComplaintDetailActivity.this, 
                            getString(R.string.status_updated_msg, newStatus), Toast.LENGTH_SHORT).show();
                    complaint.setStatus(newStatus);
                    if (tvStatus != null) {
                        tvStatus.setText(newStatus);
                        updateStatusUI(newStatus);
                    }
                } else {
                    Toast.makeText(ComplaintDetailActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(ComplaintDetailActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatusUI(String status) {
        if (status == null || tvStatus == null) return;
        String s = status.toLowerCase();
        if (s.contains("pending")) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            tvStatus.setTextColor(getColor(R.color.pendingText));
        } else if (s.contains("progress")) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_inprogress);
            tvStatus.setTextColor(getColor(R.color.primary));
        } else if (s.contains("resolved")) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_resolved);
            tvStatus.setTextColor(getColor(R.color.white));
        }
    }
}
