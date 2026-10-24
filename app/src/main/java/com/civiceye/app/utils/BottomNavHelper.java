package com.civiceye.app.utils;

import android.app.Activity;
import android.content.Intent;

import com.civiceye.app.R;
import com.civiceye.app.activities.CaptureActivity;
import com.civiceye.app.activities.HomeActivity;
import com.civiceye.app.activities.MapsActivity;
import com.civiceye.app.activities.ProfileActivity;
import com.civiceye.app.activities.UpdatesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavHelper {

    public static void setup(Activity activity, BottomNavigationView nav) {
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home && !(activity instanceof HomeActivity)) {
                activity.startActivity(new Intent(activity, HomeActivity.class));
                activity.finish();
                return true;
            } else if (id == R.id.nav_map && !(activity instanceof MapsActivity)) {
                activity.startActivity(new Intent(activity, MapsActivity.class));
                return true;
            } else if (id == R.id.nav_add) {
                activity.startActivity(new Intent(activity, CaptureActivity.class));
                return true;
            } else if (id == R.id.nav_updates && !(activity instanceof UpdatesActivity)) {
                activity.startActivity(new Intent(activity, UpdatesActivity.class));
                return true;
            } else if (id == R.id.nav_profile && !(activity instanceof ProfileActivity)) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
                return true;
            }

            return true;
        });
    }
}