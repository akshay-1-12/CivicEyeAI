package com.civiceye.app.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.civiceye.app.R;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchStats();
    }

    private void fetchStats() {
        // Fetch all issues to calculate stats locally or from a dedicated endpoint if available
        // For now, we'll fetch all issues and count statuses to make it "working"
        ApiClient.getClient().create(ApiService.class).getAllIssues().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONArray arr = new JSONArray(response.body().string());
                        int resolved = 0;
                        int active = 0;
                        
                        for (int i = 0; i < arr.length(); i++) {
                            String status = arr.getJSONObject(i).optString("status", "").toLowerCase();
                            if (status.contains("resolved")) resolved++;
                            else active++;
                        }

                        ((TextView) findViewById(R.id.tvResolvedCount)).setText(String.valueOf(resolved));
                        ((TextView) findViewById(R.id.tvActiveCount)).setText(String.valueOf(active));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(StatisticsActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
