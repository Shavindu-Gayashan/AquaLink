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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {
    ImageView btnMenu, btnHome;
    TextView username;
    MaterialButton btnMapView, btnTechnicalSupport, btnUserSettings,
            btnAppSettings, btnManageDevices, btnCustomization,
            btnUserManual, btnAbout, btnLogout;

    // Firebase references
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        // Initialize views
        btnMenu = findViewById(R.id.btn_menu);
        btnHome = findViewById(R.id.btn_home);
        username = findViewById(R.id.username);

        // Initialize buttons
        btnMapView = findViewById(R.id.btnMapView);
        btnTechnicalSupport = findViewById(R.id.btnTechnicalSupport);
        btnUserSettings = findViewById(R.id.btnUserSettings);
        btnManageDevices = findViewById(R.id.btnManageDevices);
        btnCustomization = findViewById(R.id.btnCustomization);
        btnUserManual = findViewById(R.id.btnUserManual);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set username if logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String displayName = dataSnapshot.child("name").getValue(String.class);
                        String profilePicture = dataSnapshot.child("selectedProfilePicture").getValue(String.class);

                        username.setText(displayName != null ? displayName : "User");

                        // Load profile picture if exists
                        if (profilePicture != null) {
                            ImageView profileImageView = findViewById(R.id.profileImageView);
                            if (profileImageView != null) {
                                int resID = getResources().getIdentifier(profilePicture, "drawable", getPackageName());
                                if (resID != 0) {
                                    profileImageView.setImageResource(resID);
                                }
                            }
                        }
                    } else {
                        username.setText("User");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MenuActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Button click listeners
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, DashboardActivity.class));
            finish();
        });

        btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, DashboardActivity.class));
            finish();
        });

        btnMapView.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, MapViewActivity.class));
        });

        btnTechnicalSupport.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, TechnicalSupportActivity.class));
        });

        btnUserSettings.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, UserSettingsActivity.class));
        });

        btnManageDevices.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, ManageDeviceActivity.class));
        });

        btnCustomization.setOnClickListener(v -> {
            Toast.makeText(MenuActivity.this, "Customization coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MenuActivity.this, CustomizationActivity.class));
        });

        btnUserManual.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, UserManualActivity.class));
        });

        btnAbout.setOnClickListener(v -> {
            startActivity(new Intent(MenuActivity.this, AboutActivity.class));
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
