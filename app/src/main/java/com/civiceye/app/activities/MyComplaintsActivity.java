package com.civiceye.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.civiceye.app.R;
import com.civiceye.app.adapters.ComplaintAdapter;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;
import com.civiceye.app.models.Complaint;
import com.civiceye.app.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyComplaintsActivity extends AppCompatActivity {
    private RecyclerView rvComplaints;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList = new ArrayList<>();
    private boolean isAdminView = false;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complaints);

        isAdminView = getIntent().getBooleanExtra("is_admin_view", false);
        tvTitle = findViewById(R.id.tvTitle);
        
        if (isAdminView) {
            tvTitle.setText("All Reports (Admin)");
        } else {
            tvTitle.setText("My Complaints");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvComplaints = findViewById(R.id.rvComplaints);
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(complaintList, this);
        rvComplaints.setAdapter(adapter);

        fetchComplaints();
    }

    private void fetchComplaints() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call;

        if (isAdminView) {
            // Admin sees everything
            call = apiService.getAllIssues();
        } else {
            // User sees only their own
            int userId = new SessionManager(this).getUserId();
            call = apiService.getMyComplaints(userId);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<Complaint>>(){}.getType();
                        List<Complaint> fetched = gson.fromJson(json, type);
                        
                        if (fetched != null) {
                            complaintList.clear();
                            complaintList.addAll(fetched);
                            adapter.notifyDataSetChanged();
                            
                            if (complaintList.isEmpty()) {
                                Toast.makeText(MyComplaintsActivity.this, "No complaints found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(MyComplaintsActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MyComplaintsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MyComplaintsActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
