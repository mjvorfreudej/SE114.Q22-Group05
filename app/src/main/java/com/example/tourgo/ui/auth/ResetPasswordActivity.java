package com.example.tourgo.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.remote.SupabaseClient;
import com.example.tourgo.databinding.ActivityResetPasswordBinding;
import com.example.tourgo.interfaces.AuthCallback;
import com.example.tourgo.utils.ApiErrorMapper;

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
            Toast.makeText(this, R.string.err_invalid_token, Toast.LENGTH_LONG).show();
            binding.btnResetPassword.setEnabled(false);
            return;
        }

        setupTextWatchers();

        binding.btnResetPassword.setOnClickListener(v -> {
            String password = binding.etResetPassword.getText().toString();
            String confirm  = binding.etResetConfirmPassword.getText().toString();

            boolean hasError = false;
            if (password.length() < 6) {
                binding.tilResetPassword.setError(getText(R.string.err_password_too_short));
                hasError = true;
            }
            if (!password.equals(confirm)) {
                binding.tilResetConfirmPassword.setError(getText(R.string.err_password_mismatch));
                hasError = true;
            }

            if (!hasError) {
                submitNewPassword(password);
            }
        });
    }

    private void setupTextWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilResetPassword.setError(null);
                binding.tilResetConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etResetPassword.addTextChangedListener(clearErrorWatcher);
        binding.etResetConfirmPassword.addTextChangedListener(clearErrorWatcher);
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

    private void submitNewPassword(String newPassword) {
        setLoading(true);

        SupabaseClient.updatePassword(accessToken, newPassword, new AuthCallback() {
            @Override
            public void onSuccess(String responseData) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ResetPasswordActivity.this, R.string.msg_reset_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(ApiErrorCode code, String raw) {
                runOnUiThread(() -> {
                    setLoading(false);
                    switch (code) {
                        case NETWORK:
                            Toast.makeText(ResetPasswordActivity.this, getString(R.string.err_network), Toast.LENGTH_SHORT).show();
                            break;
                        case PASSWORD_SAME_AS_OLD:
                            binding.tilResetPassword.setError(getString(R.string.err_password_same_as_old));
                            binding.etResetPassword.requestFocus();
                            break;
                        case INVALID_TOKEN:
                            Toast.makeText(ResetPasswordActivity.this, R.string.err_invalid_token, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(ResetPasswordActivity.this, ApiErrorMapper.messageOf(ResetPasswordActivity.this, code), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnResetPassword.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            binding.tilResetPassword.setError(null);
            binding.tilResetConfirmPassword.setError(null);
        }
    }
}
