package com.s23010433.aqualink;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class DashboardActivity extends AppCompatActivity {

    private View settingsAuto, settingsManual, settingsCustom, icon_connection_state;
    private TextView btnModeAuto, btnModeManual, btnModeCustom, txt_water_level, txt_motor_state, txt_connection_state, txt_selected_mode;
    ImageView btnMenu;
    private MaterialButton btnOnOffMotor, btnSaveAuto, btnSaveCustom, btnSaveManual;
    private ProgressBar progressBar;

    // Form input references
    private TextInputEditText txtAutoOnLevel, txtAutoOffLevel, txtCustomOnLevel, txtCustomOffLevel, txtSafetyTimeout;
    private SwitchMaterial toggleAutoOn, toggleAutoOff;

    private String currentMode = "auto"; // Default mode
    private Boolean isMotorOn = false; // Track motor state
    private Boolean previousMotorState = false; // Track previous motor state for change detection

    // Firebase references
    private DatabaseReference motorStateRef;
    private DatabaseReference requestTurnOnRef;
    private DatabaseReference requestTurnOffRef;
    private DatabaseReference settingsRef;
    private DatabaseReference lastSeenRef;

    // Safety timeout handler
    private Handler safetyTimeoutHandler = new Handler();
    private Runnable safetyTimeoutRunnable;
    private boolean isRequestPending = false;

    // IoT connectivity monitoring
    private Handler connectivityCheckHandler = new Handler();
    private Runnable connectivityCheckRunnable;
    private static final long DEVICE_OFFLINE_THRESHOLD = 60000; // 60 seconds
    private boolean isIoTDeviceOnline = false;

    // used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        //Progress Bar
        progressBar = findViewById(R.id.progressHorizontal);

        // Get layout references
        settingsAuto = findViewById(R.id.settings_auto);
        settingsManual = findViewById(R.id.settings_manual);
        settingsCustom = findViewById(R.id.settings_custom);

        // Get button references
        btnModeAuto = findViewById(R.id.btn_mode_auto);
        btnModeManual = findViewById(R.id.btn_mode_manual);
        btnModeCustom = findViewById(R.id.btn_mode_custom);
        btnMenu = findViewById(R.id.btn_menu);
        btnOnOffMotor = findViewById(R.id.btn_on_off_motor);
        btnSaveAuto = findViewById(R.id.btnSaveAuto);
        btnSaveCustom = findViewById(R.id.btnSaveCustom);
        btnSaveManual = findViewById(R.id.btnSaveManual);

        // Get form input references
        txtAutoOnLevel = findViewById(R.id.txtAutoOnLevel);
        txtAutoOffLevel = findViewById(R.id.txtAutoOffLevel);
        txtCustomOnLevel = findViewById(R.id.txtCustomOnLevel);
        txtCustomOffLevel = findViewById(R.id.txtCustomOffLevel);
        txtSafetyTimeout = findViewById(R.id.txtSafetyTimeout);
        toggleAutoOn = findViewById(R.id.toggleAutoOn);
        toggleAutoOff = findViewById(R.id.toggleAutoOff);

        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        motorStateRef = database.getReference("water_tank/motor_state");
        requestTurnOnRef = database.getReference("water_tank/isRequestToTurnOn");
        requestTurnOffRef = database.getReference("water_tank/isRequestToTurnOff");
        settingsRef = database.getReference("water_tank/settings");
        lastSeenRef = database.getReference("water_tank/last_seen");

        // Load settings from Firebase on app start
        loadSettingsFromFirebase();

        // Start IoT connectivity monitoring
        startIoTConnectivityMonitoring();

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
        btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, MenuActivity.class));
        });

        // Manual motor control button listener
        btnOnOffMotor.setOnClickListener(v -> onStartButtonClicked());

        // Save button listeners
        btnSaveAuto.setOnClickListener(v -> saveAutoSettings());
        btnSaveCustom.setOnClickListener(v -> saveCustomSettings());
        btnSaveManual.setOnClickListener(v -> saveManualSettings());

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

                    if (level != null) {
                        txt_water_level.setText(level + "%");
                        progressBar.setProgress(level.intValue());
                    }

                    if (motor != null){
                        previousMotorState = isMotorOn;
                        isMotorOn = motor.equalsIgnoreCase("ON");
                        txt_motor_state.setText(motor);
                        updateMotorButton();

                        // Cancel pending requests if motor state changed
                        if (!previousMotorState.equals(isMotorOn) && isRequestPending) {
                            cancelPendingRequests("Motor state changed to " + motor);
                        }
                    }

                    if (mode != null && !mode.equalsIgnoreCase(currentMode)) {
                        currentMode = mode;
                        updateModeDisplay();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database connection failed: " + error.getMessage());
            }
        });

        // Firebase connection state listener (for database connectivity)
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                updateConnectionStatus(connected != null && connected);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                updateConnectionStatus(false);
            }
        });

        // IoT device last seen timestamp listener
        lastSeenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long lastSeenTimestamp = snapshot.getValue(Long.class);
                if (lastSeenTimestamp != null) {
                    checkIoTDeviceStatus(lastSeenTimestamp);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Failed to monitor IoT device status: " + error.getMessage());
                isIoTDeviceOnline = false;
                updateConnectionDisplay();
            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(count -> {
            if (count >= 2) {
                Intent intent = new Intent(DashboardActivity.this, TechnicalSupportActivity.class);
                startActivity(intent);
                Toast.makeText(DashboardActivity.this, "Shake Detected! Navigating to Technical Support", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Start periodic IoT connectivity monitoring
    private void startIoTConnectivityMonitoring() {
        connectivityCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // Check every 10 seconds
                lastSeenRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Long lastSeenTimestamp = task.getResult().getValue(Long.class);
                        if (lastSeenTimestamp != null) {
                            checkIoTDeviceStatus(lastSeenTimestamp);
                        }
                    } else {
                        isIoTDeviceOnline = false;
                        updateConnectionDisplay();
                    }
                });

                // Schedule next check
                connectivityCheckHandler.postDelayed(this, 10000); // Check every 10 seconds
            }
        };

        // Start the periodic check
        connectivityCheckHandler.post(connectivityCheckRunnable);
    }

    // Check if IoT device is online based on last seen timestamp
    private void checkIoTDeviceStatus(long lastSeenTimestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastSeenTimestamp;

        boolean wasOnline = isIoTDeviceOnline;
        isIoTDeviceOnline = timeDifference <= DEVICE_OFFLINE_THRESHOLD;

        // Log status change
        if (wasOnline != isIoTDeviceOnline) {
            Log.d("IoT", "Device status changed: " + (isIoTDeviceOnline ? "ONLINE" : "OFFLINE"));
            Log.d("IoT", "Last seen: " + (timeDifference / 1000) + " seconds ago");
        }

        updateConnectionDisplay();
    }

    // Update connection display based on both database and IoT device status
    private void updateConnectionDisplay() {
        // This method will be called by updateConnectionStatus() for database connectivity
        // and by checkIoTDeviceStatus() for IoT device connectivity

        // The display will show the combined status
        if (isIoTDeviceOnline) {
            txt_connection_state.setText("Connected (Tank Online)");
            txt_connection_state.setTextColor(getResources().getColor(R.color.green));
            icon_connection_state.setBackgroundResource(R.drawable.circle_green);
        } else {
            txt_connection_state.setText("Connected (Tank Offline)");
            txt_connection_state.setTextColor(getResources().getColor(R.color.orange));
            icon_connection_state.setBackgroundResource(R.drawable.circle_orange);
        }
    }

    // Update connection status (for database connectivity)
    private void updateConnectionStatus(boolean isDatabaseConnected) {
        if (!isDatabaseConnected) {
            txt_connection_state.setText("Disconnected");
            txt_connection_state.setTextColor(getResources().getColor(R.color.red));
            icon_connection_state.setBackgroundResource(R.drawable.circle_red);
        } else {
            // When database is connected, show IoT device status
            updateConnectionDisplay();
        }
    }

    // Load settings from Firebase on app startup
    private void loadSettingsFromFirebase() {
        settingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String autoOnLevel = snapshot.child("auto_on_level").getValue(String.class);
                    String autoOffLevel = snapshot.child("auto_off_level").getValue(String.class);
                    String customOnLevel = snapshot.child("custom_on_level").getValue(String.class);
                    String customOffLevel = snapshot.child("custom_off_level").getValue(String.class);
                    String safetyTimeout = snapshot.child("safety_timeout").getValue(String.class);
                    Boolean enableAutoOn = snapshot.child("enable_auto_on_trigger").getValue(Boolean.class);
                    Boolean enableAutoOff = snapshot.child("enable_auto_off_trigger").getValue(Boolean.class);

                    if (autoOnLevel != null) txtAutoOnLevel.setText(autoOnLevel);
                    if (autoOffLevel != null) txtAutoOffLevel.setText(autoOffLevel);
                    if (customOnLevel != null) txtCustomOnLevel.setText(customOnLevel);
                    if (customOffLevel != null) txtCustomOffLevel.setText(customOffLevel);
                    if (safetyTimeout != null) txtSafetyTimeout.setText(safetyTimeout);
                    if (enableAutoOn != null) toggleAutoOn.setChecked(enableAutoOn);
                    if (enableAutoOff != null) toggleAutoOff.setChecked(enableAutoOff);

                    Log.d("Firebase", "Settings loaded successfully");
                } else {
                    Log.d("Firebase", "No settings found, using defaults");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Failed to load settings: " + error.getMessage());
                Toast.makeText(DashboardActivity.this, "Failed to load settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save Auto Mode settings to Firebase
    private void saveAutoSettings() {
        String autoOnLevel = txtAutoOnLevel.getText().toString().trim();
        String autoOffLevel = txtAutoOffLevel.getText().toString().trim();

        if (autoOnLevel.isEmpty() || autoOffLevel.isEmpty()) {
            Toast.makeText(this, "Please fill all auto mode fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int onLevel = Integer.parseInt(autoOnLevel);
            int offLevel = Integer.parseInt(autoOffLevel);

            if (onLevel < 0 || onLevel > 100 || offLevel < 0 || offLevel > 100) {
                Toast.makeText(this, "Water levels must be between 0-100%", Toast.LENGTH_SHORT).show();
                return;
            }

            if (onLevel >= offLevel) {
                Toast.makeText(this, "Turn ON level must be less than Turn OFF level", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Firebase
            settingsRef.child("auto_on_level").setValue(autoOnLevel);
            settingsRef.child("auto_off_level").setValue(autoOffLevel);

            Log.d("Firebase", "Auto settings saved: ON=" + autoOnLevel + "%, OFF=" + autoOffLevel + "%");
            Toast.makeText(this, "Auto mode settings saved successfully", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    // Save Custom Mode settings to Firebase
    private void saveCustomSettings() {
        String customOnLevel = txtCustomOnLevel.getText().toString().trim();
        String customOffLevel = txtCustomOffLevel.getText().toString().trim();
        boolean enableAutoOn = toggleAutoOn.isChecked();
        boolean enableAutoOff = toggleAutoOff.isChecked();

        if (customOnLevel.isEmpty() || customOffLevel.isEmpty()) {
            Toast.makeText(this, "Please fill all custom mode fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int onLevel = Integer.parseInt(customOnLevel);
            int offLevel = Integer.parseInt(customOffLevel);

            if (onLevel < 0 || onLevel > 100 || offLevel < 0 || offLevel > 100) {
                Toast.makeText(this, "Water levels must be between 0-100%", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enableAutoOn && enableAutoOff && onLevel >= offLevel) {
                Toast.makeText(this, "When both triggers enabled, Turn ON level must be less than Turn OFF level", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Firebase
            settingsRef.child("custom_on_level").setValue(customOnLevel);
            settingsRef.child("custom_off_level").setValue(customOffLevel);
            settingsRef.child("enable_auto_on_trigger").setValue(enableAutoOn);
            settingsRef.child("enable_auto_off_trigger").setValue(enableAutoOff);

            Log.d("Firebase", "Custom settings saved: ON=" + customOnLevel + "%, OFF=" + customOffLevel + "%, AutoOn=" + enableAutoOn + ", AutoOff=" + enableAutoOff);
            Toast.makeText(this, "Custom mode settings saved successfully", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    // Save Manual Mode settings to Firebase
    private void saveManualSettings() {
        String safetyTimeout = txtSafetyTimeout.getText().toString().trim();

        if (safetyTimeout.isEmpty()) {
            Toast.makeText(this, "Please enter safety timeout value", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int timeout = Integer.parseInt(safetyTimeout);

            if (timeout < 1 || timeout > 60) {
                Toast.makeText(this, "Safety timeout must be between 1-60 seconds", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Firebase
            settingsRef.child("safety_timeout").setValue(safetyTimeout)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Manual settings saved: Safety Timeout=" + safetyTimeout + " seconds");
                        Toast.makeText(this, "Manual mode settings saved successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to save manual settings: " + e.getMessage());
                        Toast.makeText(this, "Failed to save manual settings", Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending timeout when activity is destroyed
        if (safetyTimeoutRunnable != null) {
            safetyTimeoutHandler.removeCallbacks(safetyTimeoutRunnable);
        }

        // Cancel IoT connectivity monitoring
        if (connectivityCheckRunnable != null) {
            connectivityCheckHandler.removeCallbacks(connectivityCheckRunnable);
        }
    }

    // Manual motor control method with safety timeout
    private void onStartButtonClicked() {
        // Check if IoT device is online before allowing manual control
        if (!isIoTDeviceOnline) {
            Toast.makeText(this, "Cannot control motor: Tank is offline", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cancel any existing timeout
        if (safetyTimeoutRunnable != null) {
            safetyTimeoutHandler.removeCallbacks(safetyTimeoutRunnable);
        }

        isRequestPending = true;

        if (isMotorOn) {
            // Request to turn off
            requestTurnOffRef.setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Turn OFF request sent successfully");
                        startSafetyTimeout("Turn OFF");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to send turn OFF request: " + e.getMessage());
                        isRequestPending = false;
                        Toast.makeText(this, "Failed to send turn OFF request", Toast.LENGTH_SHORT).show();
                    });

            requestTurnOnRef.setValue(false);
        } else {
            // Request to turn on
            requestTurnOnRef.setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Turn ON request sent successfully");
                        startSafetyTimeout("Turn ON");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to send turn ON request: " + e.getMessage());
                        isRequestPending = false;
                        Toast.makeText(this, "Failed to send turn ON request", Toast.LENGTH_SHORT).show();
                    });

            requestTurnOffRef.setValue(false);
        }
    }

    // Start safety timeout (uses saved timeout value or default 5 seconds)
    private void startSafetyTimeout(String requestType) {
        // Get timeout value from input field, default to 5 seconds if not available
        int timeoutSeconds = 5;
        String timeoutStr = txtSafetyTimeout.getText().toString().trim();
        if (!timeoutStr.isEmpty()) {
            try {
                timeoutSeconds = Integer.parseInt(timeoutStr);
                if (timeoutSeconds < 1 || timeoutSeconds > 60) {
                    timeoutSeconds = 5; // Use default if out of range
                }
            } catch (NumberFormatException e) {
                timeoutSeconds = 5; // Use default if parse fails
            }
        }

        // Make the variable final so it can be accessed from inner class
        final int finalTimeoutSeconds = timeoutSeconds;

        safetyTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                cancelPendingRequests("Safety timeout reached after " + finalTimeoutSeconds + " seconds");
            }
        };

        // Schedule timeout using the configured timeout value
        safetyTimeoutHandler.postDelayed(safetyTimeoutRunnable, finalTimeoutSeconds * 1000);
        Log.d("Firebase", "Safety timeout started for " + requestType + " request (" + finalTimeoutSeconds + " seconds)");
    }

    // Cancel pending requests and reset flags
    private void cancelPendingRequests(String reason) {
        if (isRequestPending) {
            Log.d("Firebase", "Cancelling pending requests: " + reason);

            // Reset both request flags
            requestTurnOnRef.setValue(false)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Turn ON request cancelled"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to cancel turn ON request: " + e.getMessage()));

            requestTurnOffRef.setValue(false)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Turn OFF request cancelled"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to cancel turn OFF request: " + e.getMessage()));

            // Cancel timeout and reset flags
            if (safetyTimeoutRunnable != null) {
                safetyTimeoutHandler.removeCallbacks(safetyTimeoutRunnable);
                safetyTimeoutRunnable = null;
            }

            isRequestPending = false;
            Toast.makeText(this, "Request cancelled: " + reason, Toast.LENGTH_SHORT).show();
        }
    }

    // Update button text based on motor state
    private void updateMotorButton() {
        if (isMotorOn) {
            btnOnOffMotor.setText(getString(R.string.btn_manual_turn_off));
            btnOnOffMotor.setBackgroundTintList(getResources().getColorStateList(R.color.red));
        } else {
            btnOnOffMotor.setText(getString(R.string.btn_manual_turn_on));
            btnOnOffMotor.setBackgroundTintList(getResources().getColorStateList(R.color.theme_blue));
        }
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
