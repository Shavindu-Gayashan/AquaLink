package com.s23010433.aqualink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AboutActivity extends AppCompatActivity {

    private ImageView btnBack, btnMenu;
    private MaterialButton btnTechnicalSupport, btnUserManual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        // Initialize views
        btnBack = findViewById(R.id.btn_back);
        btnMenu = findViewById(R.id.btn_menu);
        btnTechnicalSupport = findViewById(R.id.btnTechnicalSupport);
        btnUserManual = findViewById(R.id.btnUserManual);

        // Set click listeners
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(AboutActivity.this, MenuActivity.class));
            finish();
        });

        btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(AboutActivity.this, MenuActivity.class));
        });

        btnTechnicalSupport.setOnClickListener(v -> {
            startActivity(new Intent(AboutActivity.this, TechnicalSupportActivity.class));
        });

        btnUserManual.setOnClickListener(v -> {
            startActivity(new Intent(AboutActivity.this, UserManualActivity.class));
        });
    }
}
