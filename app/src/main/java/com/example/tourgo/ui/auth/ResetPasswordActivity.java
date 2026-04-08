package com.example.tourgo.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.remote.SupabaseClient;
import com.example.tourgo.databinding.ActivityResetPasswordBinding;
import com.example.tourgo.interfaces.AuthCallback;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutResetPasswordRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        accessToken = extractToken(getIntent());

        if (accessToken == null) {
            binding.tvResetError.setVisibility(View.VISIBLE);
            binding.tvResetError.setText("Liên kết không hợp lệ hoặc đã hết hạn.");


            return;
        }

        validatePassword();
        validateConfirmPassword();

        binding.btnResetPassword.setOnClickListener(v -> {
            String password = binding.etResetPassword.getText().toString();
            String confirm  = binding.etResetConfirmPassword.getText().toString();

            if (password.length() < 6) {
                binding.tilResetPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }
            if (!password.equals(confirm)) {
                binding.tilResetConfirmPassword.setError("Mật khẩu không khớp");
                return;
            }
            submitNewPassword(password);
        });
    }

    private String extractToken(Intent intent) {
        if (intent == null || intent.getData() == null) return null;
        Uri uri = intent.getData();

        String token = uri.getQueryParameter("access_token");
        if (token != null) return token;

        String fragment = uri.getFragment();
        if (fragment != null) {
            for (String part : fragment.split("&")) {
                if (part.startsWith("access_token=")) {
                    return part.substring("access_token=".length());
                }
            }
        }
        return null;
    }

    private void validatePassword() {
        binding.etResetPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                binding.tilResetPassword.setError(
                        s.toString().length() < 6 ? "Mật khẩu phải có ít nhất 6 ký tự" : null
                );
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void validateConfirmPassword() {
        binding.etResetConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String pass = binding.etResetPassword.getText().toString();
                binding.tilResetConfirmPassword.setError(
                        !s.toString().equals(pass) ? "Mật khẩu không khớp" : null
                );
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void submitNewPassword(String newPassword) {
        setLoading(true);

        SupabaseClient.updatePassword(accessToken, newPassword, new AuthCallback() {
            @Override
            public void onSuccess(String responseData) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    setLoading(false);
                    binding.tvResetError.setVisibility(View.VISIBLE);
                    binding.tvResetError.setText("Lỗi: " + errorMessage);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnResetPassword.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}