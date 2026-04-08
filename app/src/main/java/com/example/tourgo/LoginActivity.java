package com.example.tourgo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.databinding.ActivityLoginBinding;

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
            binding.etLoginEmail.setText(session.getEmail());
            binding.etLoginPassword.setText(session.getPassword());
            binding.cbLoginRemember.setChecked(true);
        }

        validateEmail();
        validatePassword();

        binding.tvForgotPassword.setOnClickListener(v -> {
            new ForgotPasswordDialog().show(getSupportFragmentManager(), "forgot_password");
        });

        binding.tvLoginSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etLoginEmail.getText().toString().trim();
            String password = binding.etLoginPassword.getText().toString();

            if(email.isEmpty() || password.isEmpty()) {
                if(email.isEmpty()) {
                    binding.tilLoginEmail.setError("Địa chỉ email không được để trống");
                }
                if(password.isEmpty()) {
                    binding.tilLoginPassword.setError("Mật khẩu không được để trống");
                }
                return;
            }

            SupabaseClient.login(email, password, new AuthCallback() {
                @Override
                public void onSuccess(String responseData) {
                    if(binding.cbLoginRemember.isChecked()) {
                        session.saveUser(email, password);
                    }else {
                        session.clear();
                    }

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        binding.tilLoginEmail.setError("Sai email hoặc mật khẩu");
                    });
                }
            });
        });
    }

    private void validateEmail() {
        binding.etLoginEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
                        || s.toString().trim().isEmpty()) {
                    binding.tilLoginEmail.setError("Địa chỉ email không hợp lệ");
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
                    binding.tilLoginPassword.setError("Mật khẩu không được để trống");
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