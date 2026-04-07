package com.example.Tour_Booking_App;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.Tour_Booking_App.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutLoginRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        session = new SessionManager(this);
        if(session.isLoggedIn()){
            Intent intent = new Intent(LoginActivity.this, TempActivity.class); //TODO: Change to HomeActivity
            startActivity(intent);
            finish();
        }

        validateEmail();
        validatePassword();

        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPassActivity.class);
            startActivity(intent);
        });

        binding.tvLoginSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> {
            // TODO: Wait for DB
            String token = "";  //from DB
            String userID = ""; //from DB
            Boolean result = false; //from DB

            if(result){
                Intent intent = new Intent(LoginActivity.this, TempActivity.class); //TODO: Change to HomeActivity
                startActivity(intent);
                finish();
            }

            if(binding.cbLoginRemember.isChecked() && result){
                session.saveSession(token, userID);
            }
        });
    }

    private void validateEmail() {
        binding.etLoginEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
                        || s.toString().trim().isEmpty()) {
                    binding.tilLoginEmail.setError("Invalid email address");
                } else {
                    binding.tilLoginEmail.setError(null);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void validatePassword() {
        binding.etLoginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    binding.tilLoginPassword.setError("Password cannot be empty");
                } else {
                    binding.tilLoginPassword.setError(null);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
}