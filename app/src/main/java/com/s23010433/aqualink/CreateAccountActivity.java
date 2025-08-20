package com.s23010433.aqualink;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText txtName, txtEmail, txtPassword;
    private CheckBox checkTerms;
    private MaterialButton btnCreate;
    private TextView btnLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Bind UI elements
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        checkTerms = findViewById(R.id.check_terms);
        btnCreate = findViewById(R.id.btnCreate);
        btnLogin = findViewById(R.id.btnLogin);

        // Create account button logic
        btnCreate.setOnClickListener(v -> registerUser());

        // Go to Login page
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    private void registerUser() {
        String name = txtName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        // Validations
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkTerms.isChecked()) {
            Toast.makeText(this, "You must agree to the terms", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save user info to Firebase Database
                        String userId = user.getUid();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("id", userId);
                        userData.put("name", name);
                        userData.put("email", email);

                        mDatabase.child(userId).setValue(userData)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                                        // Update user profile
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(task2 -> {
                                                    // Go to Dashboard or login
                                                    startActivity(new Intent(this, DashboardActivity.class));
                                                    finish();
                                                });
                                    } else {
                                        Toast.makeText(this, "Failed to save user data: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

}
