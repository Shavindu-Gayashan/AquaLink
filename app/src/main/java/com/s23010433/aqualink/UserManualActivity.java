package com.s23010433.aqualink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class UserManualActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_manual);

        // Hook up back button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(UserManualActivity.this, DashboardActivity.class));
            finish();
        });

        // Hook up menu button
        ImageView btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(UserManualActivity.this, MenuActivity.class));
        });
    }
}
