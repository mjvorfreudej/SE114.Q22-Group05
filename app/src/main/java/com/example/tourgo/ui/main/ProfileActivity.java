package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tourgo.R;
import com.example.tourgo.data.AppFakeData;
import com.example.tourgo.models.User;
import com.example.tourgo.ui.auth.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvProfilePhone, tvProfileDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileDescription = findViewById(R.id.tvProfileDescription);

        User user = AppFakeData.getUser();
        if (user != null) {
            tvProfileName.setText(user.getName());
            tvProfileEmail.setText(user.getEmail());
            tvProfilePhone.setText(user.getPhone());
            // tvProfileDescription.setText(user.getDescription()); // Field might not exist in current User model
        }

        setupLogout();
    }

    private void setupLogout() {
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}