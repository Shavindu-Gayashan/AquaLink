package com.s23010433.aqualink;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.Toast;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserSettingsActivity extends AppCompatActivity {

    private ImageView btnMenu, btnBack, currentProfilePicture;
    private ImageView profilePic1, profilePic2, profilePic3, profilePic4, profilePic5, profilePic6;
    private TextInputEditText txtDisplayName;
    private MaterialButton btnSaveChanges;

    private int selectedProfilePicture = R.drawable.profile_picture_1; // Default
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_settings);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Initialize Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize views
        initializeViews();

        // Load current user data
        loadCurrentUserData();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        btnMenu = findViewById(R.id.btn_menu);
        btnBack = findViewById(R.id.btn_back);
        currentProfilePicture = findViewById(R.id.currentProfilePicture);

        txtDisplayName = findViewById(R.id.txtDisplayName);

        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        // Profile picture options
        profilePic1 = findViewById(R.id.profilePic1);
        profilePic2 = findViewById(R.id.profilePic2);
        profilePic3 = findViewById(R.id.profilePic3);
        profilePic4 = findViewById(R.id.profilePic4);
        profilePic5 = findViewById(R.id.profilePic5);
        profilePic6 = findViewById(R.id.profilePic6);
    }

    private void loadCurrentUserData() {
        if (currentUser != null) {
            // Load display name from database
            databaseReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String displayName = dataSnapshot.child("name").getValue(String.class);
                        String profilePicture = dataSnapshot.child("selectedProfilePicture").getValue(String.class);

                        if (displayName != null) {
                            txtDisplayName.setText(displayName);
                        }

                        if (profilePicture != null) {
                            int resID = getResources().getIdentifier(profilePicture, "drawable", getPackageName());
                            if (resID != 0) {
                                currentProfilePicture.setImageResource(resID);
                                selectedProfilePicture = resID;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(UserSettingsActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setClickListeners() {
        // Menu button
        btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(UserSettingsActivity.this, MenuActivity.class));
            finish();
        });

        // Back button
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(UserSettingsActivity.this, DashboardActivity.class));
            finish();
        });

        // Profile picture selection
        View.OnClickListener profilePictureClickListener = v -> {
            // Reset all profile pictures to normal state
            resetProfilePictureSelection();

            // Highlight selected picture and update current display
            v.setBackgroundResource(R.drawable.circle_background); // You might want a selected state drawable

            if (v.getId() == R.id.profilePic1) {
                selectedProfilePicture = R.drawable.profile_picture_1;
            } else if (v.getId() == R.id.profilePic2) {
                selectedProfilePicture = R.drawable.profile_picture_2;
            } else if (v.getId() == R.id.profilePic3) {
                selectedProfilePicture = R.drawable.profile_picture_3;
            } else if (v.getId() == R.id.profilePic4) {
                selectedProfilePicture = R.drawable.profile_picture_4;
            } else if (v.getId() == R.id.profilePic5) {
                selectedProfilePicture = R.drawable.profile_picture_5;
            } else if (v.getId() == R.id.profilePic6) {
                selectedProfilePicture = R.drawable.profile_picture_6;
            }

            currentProfilePicture.setImageResource(selectedProfilePicture);
            Toast.makeText(this, "Profile picture selected", Toast.LENGTH_SHORT).show();
        };

        profilePic1.setOnClickListener(profilePictureClickListener);
        profilePic2.setOnClickListener(profilePictureClickListener);
        profilePic3.setOnClickListener(profilePictureClickListener);
        profilePic4.setOnClickListener(profilePictureClickListener);
        profilePic5.setOnClickListener(profilePictureClickListener);
        profilePic6.setOnClickListener(profilePictureClickListener);

        // Save changes button
        btnSaveChanges.setOnClickListener(v -> saveUserChanges());
    }

    private void resetProfilePictureSelection() {
        profilePic1.setBackgroundResource(R.drawable.circle_background);
        profilePic2.setBackgroundResource(R.drawable.circle_background);
        profilePic3.setBackgroundResource(R.drawable.circle_background);
        profilePic4.setBackgroundResource(R.drawable.circle_background);
        profilePic5.setBackgroundResource(R.drawable.circle_background);
        profilePic6.setBackgroundResource(R.drawable.circle_background);
    }

    private void saveUserChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String newDisplayName = txtDisplayName.getText().toString().trim();

        // Validate input
        if (newDisplayName.isEmpty()) {
            txtDisplayName.setError("Display name cannot be empty");
            return;
        }

        // Update display name in Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update user data in database
                        String profilePictureName = getResources().getResourceEntryName(selectedProfilePicture);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("name", newDisplayName);
                        updates.put("selectedProfilePicture", profilePictureName);

                        databaseReference.child(currentUser.getUid()).updateChildren(updates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(UserSettingsActivity.this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UserSettingsActivity.this, "Failed to update settings", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(UserSettingsActivity.this, "Failed to update settings", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
