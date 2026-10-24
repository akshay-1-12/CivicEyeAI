package com.civiceye.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.civiceye.app.R;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPhone, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        try {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        } catch (Exception e) {
            // ignore if btnBack doesn't exist in your layout
        }

        btnRegister.setOnClickListener(v -> performRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty()) { etName.setError("Enter name"); return; }
        if (email.isEmpty()) { etEmail.setError("Enter email"); return; }
        if (password.length() < 6) { etPassword.setError("Min 6 characters"); return; }

        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("password", password);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.registerUser(data);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Registered! Please Login.", Toast.LENGTH_LONG).show();
                    finish(); // Go back to login screen
                } else {
                    Toast.makeText(RegisterActivity.this, "Email already exists or invalid data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}