package com.civiceye.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.civiceye.app.R;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiAnalyzingActivity extends AppCompatActivity {
    private String imagePath;
    private double latitude = 0, longitude = 0;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_analyzing);
        imagePath = getIntent().getStringExtra("image_path");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        animateChecks();
        getLocationAndDetect();
    }

    private void animateChecks() {
        int[] checks = {R.id.chk1, R.id.chk2, R.id.chk3, R.id.chk4, R.id.chk5};
        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < checks.length; i++) {
            int id = checks[i];
            handler.postDelayed(() -> {
                View v = findViewById(id);
                if (v != null) v.animate().alpha(1f).setDuration(300).start();
            }, 500 + (i * 700L));
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndDetect() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CancellationTokenSource cts = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                    callAIBackend();
                })
                .addOnFailureListener(e -> {
                    Log.e("CivicEyeAI", "Location failed: " + e.getMessage());
                    callAIBackend();
                });
        } else {
            callAIBackend();
        }
    }

    private void callAIBackend() {
        String base64Image = encodeImage(imagePath);
        if (base64Image.isEmpty()) {
            showError("Image processing failed");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("image_base64", base64Image);
        body.put("latitude", latitude);
        body.put("longitude", longitude);

        Log.d("CivicEyeAI", "Calling AI detect... Body size: " + base64Image.length() + " chars");

        ApiClient.getClient().create(ApiService.class).detectIssue(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String resStr = response.body().string();
                        Log.d("CivicEyeAI", "AI Response: " + resStr);
                        JSONObject json = new JSONObject(resStr);
                        
                        Intent intent = new Intent(AiAnalyzingActivity.this, PreviewActivity.class);
                        intent.putExtra("image_path", imagePath);
                        intent.putExtra("issue_type", json.optString("issue_type", "Unknown"));
                        intent.putExtra("severity", json.optString("severity", "Medium"));
                        intent.putExtra("description", json.optString("description", ""));
                        intent.putExtra("department", json.optString("department", ""));
                        intent.putExtra("address", json.optString("address", ""));
                        intent.putExtra("confidence", json.optInt("confidence", 0));
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Empty";
                        Log.e("CivicEyeAI", "AI Error " + response.code() + ": " + errorBody);
                        showError("Backend Error (" + response.code() + "). Check laptop terminal logs.");
                    }
                } catch (Exception e) {
                    Log.e("CivicEyeAI", "Parse error", e);
                    showError("AI Data error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("CivicEyeAI", "Network failure", t);
                showError("Network Error: " + t.getMessage());
            }
        });
    }

    private String encodeImage(String path) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap == null) return "";

            // Scale down to max 800px (Smaller payload to avoid 502 errors)
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 800 || height > 800) {
                float ratio = (float) width / (float) height;
                if (width > height) {
                    width = 800;
                    height = (int) (width / ratio);
                } else {
                    height = 800;
                    width = (int) (height * ratio);
                }
            }
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Lowered quality to 60 to significantly reduce string size
            scaled.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e("CivicEyeAI", "Encoding failed", e);
            return "";
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }
}
