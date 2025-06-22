package com.s23010433.aqualink;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MenuActivity extends AppCompatActivity {
    ImageView btnMenu;
    TextView username;
    MaterialButton btnMapView, btnTechnicalSupport, btnUserSettings,
            btnAppSettings, btnManageDevices, btnCustomization,
            btnUserManual, btnAbout, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        // Initialize views
        btnMenu = findViewById(R.id.btn_menu);
        username = findViewById(R.id.username);

        // Initialize buttons
        btnMapView = findViewById(R.id.btnMapView);
        btnTechnicalSupport = findViewById(R.id.btnTechnicalSupport);
        btnUserSettings = findViewById(R.id.btnUserSettings);
        btnAppSettings = findViewById(R.id.btnAppSettings);
        btnManageDevices = findViewById(R.id.btnManageDevices);
        btnCustomization = findViewById(R.id.btnCustomization);
        btnUserManual = findViewById(R.id.btnUserManual);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);

        // Set username if logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            username.setText(displayName != null ? displayName : "User");
        }

        // Button click listeners
        btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, DashboardActivity.class));
            finish();
        });

        btnMapView.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "Map view coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, MapViewActivity.class));
        });

        btnTechnicalSupport.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "Technical support coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, TechnicalSupportActivity.class));
        });

        btnUserSettings.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "User settings coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, UserSettingsActivity.class));
        });

        btnAppSettings.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "App settings coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, AppSettingsActivity.class));
        });

        btnManageDevices.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "Manage devices coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, ManageDevicesActivity.class));
        });

        btnCustomization.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "Customization coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, CustomizationActivity.class));
        });

        btnUserManual.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "User manual coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, UserManualActivity.class));
        });

        btnAbout.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "About page coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, AboutActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();
            // Navigate to login screen
            Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

}
