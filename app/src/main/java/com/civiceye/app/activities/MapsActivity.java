package com.civiceye.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.civiceye.app.R;
import com.civiceye.app.api.ApiClient;
import com.civiceye.app.api.ApiService;
import com.civiceye.app.utils.BottomNavHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_map);
        BottomNavHelper.setup(this, nav);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        
        fetchMapMarkers();
    }

    private void fetchMapMarkers() {
        ApiClient.getClient().create(ApiService.class).getAllIssues().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String resStr = response.body().string();
                        Log.d("CivicEyeMap", "Data: " + resStr);
                        JSONArray arr = new JSONArray(resStr);
                        
                        mMap.clear(); // Clear existing markers
                        
                        if (arr.length() == 0) {
                            Toast.makeText(MapsActivity.this, "No issues reported yet", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            double lat = obj.optDouble("latitude", 0);
                            double lng = obj.optDouble("longitude", 0);
                            String type = obj.optString("issue_type", "Unknown");
                            String status = obj.optString("status", "Pending");
                            
                            if (lat != 0 && lng != 0) {
                                LatLng loc = new LatLng(lat, lng);
                                
                                // Color markers by status
                                float markerColor = BitmapDescriptorFactory.HUE_RED; // Default Pending
                                if (status.toLowerCase().contains("resolved")) {
                                    markerColor = BitmapDescriptorFactory.HUE_GREEN;
                                } else if (status.toLowerCase().contains("progress")) {
                                    markerColor = BitmapDescriptorFactory.HUE_ORANGE;
                                }

                                mMap.addMarker(new MarkerOptions()
                                        .position(loc)
                                        .title(type)
                                        .snippet("Status: " + status)
                                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                                        
                                // Center camera on the latest reported issue
                                if (i == 0) {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 12));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("CivicEyeMap", "Error parsing map markers", e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(MapsActivity.this, "Network Error: Map data unavailable", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
