package com.s23010433.aqualink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText txtEmail, txtPassword;
    private Button btnLogin;
    private TextView btnCreateAnAccount, btnForgotPassword;
    private ImageButton btnLoginWithGoogle, btnLoginWithFacebook;
    private FirebaseAuth mAuth;


    @Override
    protected void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, redirect to dashboard
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        //Bind views
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAnAccount = findViewById(R.id.btnCreateAnAccount);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnLoginWithGoogle = findViewById(R.id.btnloginWithGoogle);
        btnLoginWithFacebook = findViewById(R.id.btnLoginWithFacebook);

        //Login Button
        btnLogin.setOnClickListener(v -> loginUser());

        //Navigate to Create Account Page
        btnCreateAnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        //Google Login Button
        btnLoginWithGoogle.setOnClickListener(v-> {
            Toast.makeText(this, "Google login coming soon!", Toast.LENGTH_SHORT).show();
        });

        //Facebook Login Button
        btnLoginWithFacebook.setOnClickListener(v-> {
            Toast.makeText(this, "Facebook login coming soon!", Toast.LENGTH_SHORT).show();
        });

        //Forgot Password Button
        btnForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

    }

    private void loginUser(){
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Login Successfill!" , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
