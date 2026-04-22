package com.example.tourgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.ui.main.MainActivity;
import com.example.tourgo.utils.SessionManager;
import com.example.tourgo.remote.SupabaseClient;
import com.example.tourgo.databinding.ActivityLoginBinding;
import com.example.tourgo.interfaces.AuthCallback;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutLoginRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

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

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilLoginEmail.setError("Vui lòng nhập email hợp lệ");
                return;
            }
            if (password.isEmpty()) {
                binding.tilLoginPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }

            // Hiển thị loading
            binding.btnLogin.setVisibility(android.view.View.INVISIBLE);
            binding.pbLoginLoading.setVisibility(android.view.View.VISIBLE);

            // DÙNG FAKE DATA ĐỂ TEST NHANH
            if (email.equals("admin@gmail.com") && password.equals("123456")) {
                session.saveUser(email, password);
                Toast.makeText(LoginActivity.this, "Đăng nhập giả thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                return;
            }

            SupabaseClient.login(email, password, new AuthCallback() {
                @Override
                public void onSuccess(String responseData) {
                    runOnUiThread(() -> {
                        binding.btnLogin.setVisibility(android.view.View.VISIBLE);
                        binding.pbLoginLoading.setVisibility(android.view.View.GONE);
                        if (binding.cbLoginRemember.isChecked()) {
                            session.saveUser(email, password);
                        } else {
                            session.clear();
                        }
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                }

                @Override
                public void onError(ApiErrorCode code, String errorMessage) {
                    runOnUiThread(() -> {
                        binding.btnLogin.setVisibility(android.view.View.VISIBLE);
                        binding.pbLoginLoading.setVisibility(android.view.View.GONE);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    private void validateEmail() {
        binding.etLoginEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (email.isEmpty()) {
                    binding.tilLoginEmail.setError("Email không được để trống");
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tilLoginEmail.setError("Định dạng email không hợp lệ");
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