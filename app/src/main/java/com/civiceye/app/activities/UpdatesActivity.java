package com.civiceye.app.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.civiceye.app.R;
import com.civiceye.app.adapters.ComplaintAdapter;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;
import com.civiceye.app.models.Complaint;
import com.civiceye.app.utils.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatesActivity extends AppCompatActivity {
    private RecyclerView rvUpdates;
    private ComplaintAdapter adapter;
    private List<Complaint> updatesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvUpdates = findViewById(R.id.rvUpdates);
        rvUpdates.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(updatesList, this);
        rvUpdates.setAdapter(adapter);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_updates);
        BottomNavHelper.setup(this, nav);

        fetchGlobalUpdates();
    }

    private void fetchGlobalUpdates() {
        ApiClient.getClient().create(ApiService.class).getAllIssues().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<Complaint>>(){}.getType();
                        List<Complaint> fetched = gson.fromJson(json, type);
                        
                        if (fetched != null) {
                            updatesList.clear();
                            updatesList.addAll(fetched);
                            adapter.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(UpdatesActivity.this, "Failed to load updates", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
