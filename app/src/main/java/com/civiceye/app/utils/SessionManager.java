package com.civiceye.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("CivicEyeSession", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUser(String name, String email, String role) {
        editor.putBoolean("logged_in", true);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("role", role);
        editor.apply();
    }

    public void saveUserId(int id) { editor.putInt("user_id", id).apply(); }
    public int getUserId() { return prefs.getInt("user_id", 0); }
    public String getRole() { return prefs.getString("role", "citizen"); }
    public boolean isLoggedIn() { return prefs.getBoolean("logged_in", false); }
    public String getName() { return prefs.getString("name", "Citizen"); }
    public String getEmail() { return prefs.getString("email", ""); }
    
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getRole()) || "official".equalsIgnoreCase(getRole());
    }
    
    public void logout() { editor.clear().apply(); }
}
