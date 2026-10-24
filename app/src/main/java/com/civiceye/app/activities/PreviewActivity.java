package com.civiceye.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.civiceye.app.R;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreviewActivity extends AppCompatActivity {

    private String imagePath, issueType, severity, description, department, address;
    private int confidence;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        imagePath   = getIntent().getStringExtra("image_path");
        issueType   = getIntent().getStringExtra("issue_type");
        severity    = getIntent().getStringExtra("severity");
        description = getIntent().getStringExtra("description");
        department  = getIntent().getStringExtra("department");
        address     = getIntent().getStringExtra("address");
        confidence  = getIntent().getIntExtra("confidence", 0);
        latitude    = getIntent().getDoubleExtra("latitude", 0);
        longitude   = getIntent().getDoubleExtra("longitude", 0);

        ImageView ivThumb = findViewById(R.id.ivThumb);
        if (imagePath != null) ivThumb.setImageURI(Uri.fromFile(new File(imagePath)));

        ((TextView) findViewById(R.id.tvIssueType)).setText(issueType + " (AI " + confidence + "%)");
        ((TextView) findViewById(R.id.tvSeverity)).setText("🔴 " + severity);
        ((TextView) findViewById(R.id.tvDescription)).setText(description);
        ((TextView) findViewById(R.id.tvDepartment)).setText(department);
        ((TextView) findViewById(R.id.tvLocation)).setText(address);

        Button btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> submitComplaint());
    }

    private void submitComplaint() {
        String base64Image = encodeImage(imagePath);
        int userId = new com.civiceye.app.utils.SessionManager(this).getUserId();

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("issue_type", issueType);
        body.put("ai_confidence", confidence);
        body.put("description", description);
        body.put("latitude", latitude);
        body.put("longitude", longitude);
        body.put("address", address);
        body.put("severity", severity);
        body.put("department", department);
        body.put("image_base64", base64Image);

        findViewById(R.id.btnSubmit).setEnabled(false);
        Log.d("CivicEyeAI", "Submitting report for user: " + userId);

        ApiClient.getClient().create(ApiService.class).submitReport(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String resStr = response.body() != null ? response.body().string() : "";
                    Log.d("CivicEyeAI", "Submit Response: " + resStr);

                    if (response.isSuccessful() && !resStr.isEmpty()) {
                        JSONObject json = new JSONObject(resStr);
                        Intent intent = new Intent(PreviewActivity.this, SuccessActivity.class);
                        intent.putExtra("complaint_id", json.optString("complaint_code", ""));
                        intent.putExtra("official", json.optString("official_notified", ""));
                        intent.putExtra("email_sent", json.optBoolean("email_sent", false));
                        // Passing extra info for contact searching
                        intent.putExtra("department", department);
                        intent.putExtra("address", address);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PreviewActivity.this, "Submit failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        findViewById(R.id.btnSubmit).setEnabled(true);
                    }
                } catch (Exception e) {
                    Log.e("CivicEyeAI", "Submit parse error", e);
                    Toast.makeText(PreviewActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    findViewById(R.id.btnSubmit).setEnabled(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("CivicEyeAI", "Submit network failure", t);
                Toast.makeText(PreviewActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                findViewById(R.id.btnSubmit).setEnabled(true);
            }
        });
    }

    private String encodeImage(String path) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap == null) return "";
            
            // Scaling for optimization
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 1200) {
                float ratio = (float) width / height;
                width = 1200;
                height = (int) (width / ratio);
            }
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }
}
