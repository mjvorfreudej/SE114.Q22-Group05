package com.example.tourgo.ui.auth;

import android.content.Intent;
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
import com.example.tourgo.interfaces.ApiCallback;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.RegisterRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.AuthData;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.databinding.ActivityRegisterBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutRegisterRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        binding.tvRegisterSignIn.setOnClickListener(v -> finish());

        setupValidation();

        binding.btnRegister.setOnClickListener(v -> {
            if (!validateAllFields()) return;

            Editable nameEditable = binding.etRegisterName.getText();
            Editable emailEditable = binding.etRegisterEmail.getText();
            Editable passEditable = binding.etRegisterPassword.getText();

            if (nameEditable == null || emailEditable == null || passEditable == null) return;

            String name = nameEditable.toString().trim();
            String email = emailEditable.toString().trim();
            String password = passEditable.toString();

            setLoading(true);

            RegisterRequest request = new RegisterRequest(name, email, password);
            RetrofitClient.getInstance(this)
                    .getAuthApi()
                    .register(request)
                    .enqueue(new Callback<ApiResponse<AuthData>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                            runOnUiThread(() -> {
                                setLoading(false);

                                if (response.isSuccessful() && response.body() != null) {
                                    ApiResponse<AuthData> apiResponse = response.body();

                                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()
                                            && apiResponse.getData() != null) {

                                        Toast.makeText(RegisterActivity.this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    } else {
                                        ApiError error = ErrorHandler.parseError(response);
                                        ErrorHandler.showError(RegisterActivity.this, error);
                                    }
                                } else {
                                    ApiError error = ErrorHandler.parseError(response);
                                    ErrorHandler.showError(RegisterActivity.this, error);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                ApiError error = ErrorHandler.parseError(t);
                                ErrorHandler.showError(RegisterActivity.this, error);
                            });
                        }
                    });

        });
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.progressBarRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setText(loading ? "" : getString(R.string.login_sign_up));
    }

    private void setupValidation() {
        binding.etRegisterName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { binding.tilRegisterName.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.etRegisterEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { binding.tilRegisterEmail.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.etRegisterPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { binding.tilRegisterPassword.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.etRegisterConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { binding.tilRegisterConfirmPassword.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateAllFields() {
        Editable nameEdit = binding.etRegisterName.getText();
        Editable emailEdit = binding.etRegisterEmail.getText();
        Editable passEdit = binding.etRegisterPassword.getText();
        Editable confirmEdit = binding.etRegisterConfirmPassword.getText();

        String name = nameEdit != null ? nameEdit.toString().trim() : "";
        String email = emailEdit != null ? emailEdit.toString().trim() : "";
        String password = passEdit != null ? passEdit.toString() : "";
        String confirm = confirmEdit != null ? confirmEdit.toString() : "";

        if (name.isEmpty()) {
            binding.tilRegisterName.setError(getString(R.string.err_name_empty));
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegisterEmail.setError(getString(R.string.err_email_invalid));
            return false;
        }
        if (password.length() < 6) {
            binding.tilRegisterPassword.setError(getString(R.string.err_password_too_short));
            return false;
        }
        if (!confirm.equals(password)) {
            binding.tilRegisterConfirmPassword.setError(getString(R.string.err_password_mismatch));
            return false;
        }
        return true;
    }
}
