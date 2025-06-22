package com.s23010433.aqualink;

import android.util.Log;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class DashboardActivity extends AppCompatActivity {

    private View settingsAuto, settingsManual, settingsCustom, icon_connection_state;
    private TextView btnModeAuto, btnModeManual, btnModeCustom, txt_water_level, txt_motor_state, txt_connection_state, txt_selected_mode;

    private String currentMode = "auto"; // Default mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Get layout references
        settingsAuto = findViewById(R.id.settings_auto);
        settingsManual = findViewById(R.id.settings_manual);
        settingsCustom = findViewById(R.id.settings_custom);

        // Get button references
        btnModeAuto = findViewById(R.id.btn_mode_auto);
        btnModeManual = findViewById(R.id.btn_mode_manual);
        btnModeCustom = findViewById(R.id.btn_mode_custom);

        // Set onClick listeners
        btnModeAuto.setOnClickListener(v -> {
            updateModeInDatabase("auto");
            showMode("auto");
        });
        btnModeManual.setOnClickListener(v -> {
            updateModeInDatabase("manual");
            showMode("manual");
        });
        btnModeCustom.setOnClickListener(v -> {
            updateModeInDatabase("custom");
            showMode("custom");
        });

        // Database
        txt_water_level = findViewById(R.id.txt_water_level);
        txt_motor_state = findViewById(R.id.txt_motor_state);
        icon_connection_state = findViewById(R.id.icon_connection_state);
        txt_connection_state = findViewById(R.id.txt_connection_state);
        txt_selected_mode = findViewById(R.id.txt_selected_mode);

        DatabaseReference tankRef = FirebaseDatabase.getInstance().getReference("water_tank");
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        // Water tank data listener
        tankRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("Firebase", "Database connected and data received");
                if (snapshot.exists()) {
                    Long level = snapshot.child("water_level").getValue(Long.class);
                    String motor = snapshot.child("motor_state").getValue(String.class);
                    String mode = snapshot.child("selected_mode").getValue(String.class);

                    if (level != null)
                        txt_water_level.setText(level + "%");

                    if (motor != null)
                        txt_motor_state.setText(motor);

                    if (mode != null && !mode.equalsIgnoreCase(currentMode)) {
                        currentMode = mode.toLowerCase();
                        updateModeDisplay();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database connection failed: " + error.getMessage());
                // Optionally handle error
            }
        });

        // Connection state listener
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    txt_connection_state.setText(getString(R.string.lbl_connection_state_connected));
                    txt_connection_state.setTextColor(getResources().getColor(R.color.green));
                    icon_connection_state.setBackgroundResource(R.drawable.circle_green);
                } else {
                    txt_connection_state.setText(getString(R.string.lbl_connection_state_disconnected));
                    txt_connection_state.setTextColor(getResources().getColor(R.color.red));
                    icon_connection_state.setBackgroundResource(R.drawable.circle_red);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                txt_connection_state.setText(getString(R.string.lbl_connection_state_connecting));
                txt_connection_state.setTextColor(getResources().getColor(R.color.orange));
                icon_connection_state.setBackgroundResource(R.drawable.circle_orange);
            }
        });
    }

    private void showMode(String mode) {
        // Hide all settings layouts
        settingsAuto.setVisibility(View.GONE);
        settingsManual.setVisibility(View.GONE);
        settingsCustom.setVisibility(View.GONE);

        // Show selected layout
        switch (mode) {
            case "auto":
                settingsAuto.setVisibility(View.VISIBLE);
                break;
            case "manual":
                settingsManual.setVisibility(View.VISIBLE);
                break;
            case "custom":
                settingsCustom.setVisibility(View.VISIBLE);
                break;
        }

        // Optional: change background to show active selection
        highlightSelectedButton(mode);
    }

    private void highlightSelectedButton(String mode) {
        // Reset all to default background
        btnModeAuto.setBackgroundResource(R.drawable.rounded_background_white);
        btnModeManual.setBackgroundResource(R.drawable.rounded_background_white);
        btnModeCustom.setBackgroundResource(R.drawable.rounded_background_white);

        // Highlight selected one
        switch (mode) {
            case "auto":
                btnModeAuto.setBackgroundResource(R.drawable.rounded_background_selected);
                break;
            case "manual":
                btnModeManual.setBackgroundResource(R.drawable.rounded_background_selected);
                break;
            case "custom":
                btnModeCustom.setBackgroundResource(R.drawable.rounded_background_selected);
                break;
        }
    }

    // Method to update the UI based on the current mode
    private void updateModeDisplay() {
        // Update the mode text display
        String displayMode = currentMode.substring(0, 1).toUpperCase() + currentMode.substring(1);
        txt_selected_mode.setText(displayMode);

        // Show the corresponding settings layout
        showMode(currentMode);
    }

    // Method to update the mode in Firebase
    private void updateModeInDatabase(String mode) {
        currentMode = mode;
        updateModeDisplay();

        DatabaseReference modeRef = FirebaseDatabase.getInstance().getReference("water_tank/selected_mode");
        modeRef.setValue(mode)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Mode updated successfully"))
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to update mode: " + e.getMessage());
                });
    }
}