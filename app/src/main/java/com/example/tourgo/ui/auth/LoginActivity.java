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
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.ui.main.MainActivity;
import com.example.tourgo.utils.ApiErrorMapper;
import com.example.tourgo.utils.SessionManager;
import com.example.tourgo.remote.SupabaseClient;
import com.example.tourgo.databinding.ActivityLoginBinding;
import com.example.tourgo.interfaces.ApiCallback;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutLoginRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        if (session.getEmail() != null) {
            binding.etLoginEmail.setText(session.getEmail());
            binding.cbLoginRemember.setChecked(session.isRememberMe());
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
            Editable emailEditable = binding.etLoginEmail.getText();
            Editable passEditable = binding.etLoginPassword.getText();
            
            String email = emailEditable != null ? emailEditable.toString().trim() : "";
            String password = passEditable != null ? passEditable.toString() : "";

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilLoginEmail.setError(getString(R.string.err_email_invalid));
                return;
            }
            if (password.isEmpty()) {
                binding.tilLoginPassword.setError(getString(R.string.err_password_empty));
                return;
            }

            binding.btnLogin.setVisibility(android.view.View.INVISIBLE);
            binding.pbLoginLoading.setVisibility(android.view.View.VISIBLE);

            SupabaseClient.login(email, password, new ApiCallback() {
                @Override
                public void onSuccess(String responseData) {
                    runOnUiThread(() -> {
                        binding.btnLogin.setVisibility(android.view.View.VISIBLE);
                        binding.pbLoginLoading.setVisibility(android.view.View.GONE);

                        try {
                            JSONObject json = new JSONObject(responseData);
                            String accessToken = json.getString("access_token");
                            String refreshToken = json.getString("refresh_token");
                            JSONObject user = json.getJSONObject("user");
                            String userId = user.getString("id");
                            String userEmail = user.getString("email");
                            
                            // Lấy tên từ metadata (kiểm tra cả user_metadata và raw_user_meta_data)
                            String userName = "";
                            String userRole = "USER";
                            if (user.has("user_metadata")) {
                                JSONObject meta = user.getJSONObject("user_metadata");
                                userName = meta.optString("name", "");
                                userRole = meta.optString("role", "USER");
                            }
                            if (userName.isEmpty() && user.has("raw_user_meta_data")) {
                                JSONObject rawMeta = user.getJSONObject("raw_user_meta_data");
                                userName = rawMeta.optString("name", "");
                                if (userRole.equals("USER")) {
                                    userRole = rawMeta.optString("role", "USER");
                                }
                            }

                            session.saveSession(userEmail, userId, accessToken, refreshToken, userName, userRole, binding.cbLoginRemember.isChecked());

                            Toast.makeText(LoginActivity.this, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "Lỗi xử lý phản hồi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(ApiErrorCode code, String raw) {
                    runOnUiThread(() -> {
                        binding.btnLogin.setVisibility(View.VISIBLE);
                        binding.pbLoginLoading.setVisibility(View.GONE);
                        if (code == ApiErrorCode.INVALID_CREDENTIALS) {
                            binding.tilLoginPassword.setError(getString(R.string.err_invalid_credentials));
                        } else {
                            Toast.makeText(LoginActivity.this, ApiErrorMapper.messageOf(LoginActivity.this, code), Toast.LENGTH_SHORT).show();
                        }
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
                    binding.tilLoginEmail.setError(getString(R.string.err_email_empty));
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tilLoginEmail.setError(getString(R.string.err_email_invalid));
                } else {
                    binding.tilLoginEmail.setError(null);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void validatePassword() {
        binding.etLoginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    binding.tilLoginPassword.setError(getString(R.string.err_password_empty));
                } else {
                    binding.tilLoginPassword.setError(null);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
}
