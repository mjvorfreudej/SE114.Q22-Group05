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
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.UpdatePasswordRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.databinding.ActivityResetPasswordBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        RetrofitClient.getInstance(this)
                .getAuthApi()
                .updatePassword(new UpdatePasswordRequest(newPassword))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<?> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                                runOnUiThread(() -> {
                                    setLoading(false);
                                    Toast.makeText(ResetPasswordActivity.this, R.string.msg_reset_success, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                ErrorHandler.showError(ResetPasswordActivity.this, error);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            ApiError error = ErrorHandler.parseError(t);
                            ErrorHandler.showError(ResetPasswordActivity.this, error);
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
