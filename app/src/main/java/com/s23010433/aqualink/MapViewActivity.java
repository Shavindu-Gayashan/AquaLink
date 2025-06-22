package com.s23010433.aqualink;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Marker> markers = new ArrayList<>();
    private static final String TAG = "MapViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);

        // Hook up menu button if needed
        ImageView btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> {
            finish(); // Return to previous activity
        });

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error: Map Fragment not found!");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls and compass
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Fetch water tank data from Firebase
        fetchWaterTankData();

        // Move camera to a good starting position
        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 8f));

        // Add a listener to keep info windows visible when camera moves
        mMap.setOnCameraMoveListener(this::showAllInfoWindows);
        mMap.setOnCameraIdleListener(this::showAllInfoWindows);
    }

    private void fetchWaterTankData() {
        DatabaseReference tankRef = FirebaseDatabase.getInstance().getReference("water_tank");

        tankRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Data received from Firebase");

                // Clear existing markers
                for (Marker marker : markers) {
                    marker.remove();
                }
                markers.clear();

                // Extract location data
                if (snapshot.exists()) {
                    String locationStr = snapshot.child("location").getValue(String.class);
                    String tankName = snapshot.child("tank_name").getValue(String.class);
                    String tankId = snapshot.child("tank_id").getValue(String.class);
                    Long waterLevel = snapshot.child("water_level").getValue(Long.class);

                    if (locationStr != null && !locationStr.isEmpty()) {
                        try {
                            // Parse location string to get latitude and longitude
                            // Assuming format is "latitude,longitude"
                            String[] coordinates = locationStr.split(",");
                            if (coordinates.length == 2) {
                                double lat = Double.parseDouble(coordinates[0].trim());
                                double lng = Double.parseDouble(coordinates[1].trim());

                                LatLng position = new LatLng(lat, lng);

                                // Create marker title with tank info
                                String title = tankName != null ? tankName : "Unknown Tank";
                                String snippet = "ID: " + (tankId != null ? tankId : "Unknown") +
                                        ", Water Level: " + (waterLevel != null ? waterLevel + "%" : "Unknown");

                                // Add marker to map
                                addMarker(position, title, snippet);
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing location coordinates: " + e.getMessage());
                        }
                    } else {
                        // If no location found in Firebase, use default marker in Sri Lanka
                        Log.w(TAG, "No valid location found in Firebase, using default");
                        LatLng defaultLocation = new LatLng(6.883125102122275, 79.8865742341049);
                        addMarker(defaultLocation, "Default Tank", "No location data available");
                    }
                } else {
                    Log.w(TAG, "No water tank data found in Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching data from Firebase: " + error.getMessage());
            }
        });
    }

    private void addMarker(LatLng position, String title, String snippet) {
        MarkerOptions options = new MarkerOptions()
                .position(position)
                .title(title)
                .snippet(snippet);

        Marker marker = mMap.addMarker(options);
        if (marker != null) {
            markers.add(marker);
            marker.showInfoWindow();

            // Move camera to the marker if it's the first one
            if (markers.size() == 1) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 10f));
            }
        }
    }

    private void showAllInfoWindows() {
        // Make all info windows visible again
        for (Marker marker : markers) {
            marker.showInfoWindow();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh info windows when activity comes to foreground
        if (mMap != null) {
            showAllInfoWindows();
        }
    }
}