package com.civiceye.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.civiceye.app.R;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;
import com.civiceye.app.utils.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // If already logged in, skip to home
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(v -> performLogin());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) { etEmail.setError("Enter email"); return; }
        if (password.isEmpty()) { etPassword.setError("Enter password"); return; }

        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.loginUser(data);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String resStr = response.body().string();
                        JSONObject json = new JSONObject(resStr);
                        String name = json.optString("name", email.split("@")[0]);
                        int userId = json.optInt("user_id", 0);
                        // Backend should return role. If not, we default to citizen for now.
                        String role = json.optString("role", "citizen");

                        sessionManager.saveUserId(userId);
                        sessionManager.saveUser(name, email, role);
                        
                        Toast.makeText(LoginActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Error reading response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
