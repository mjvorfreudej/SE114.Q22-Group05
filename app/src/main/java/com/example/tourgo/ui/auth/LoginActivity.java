package com.example.tourgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast; // Thêm Toast

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

        // NOTE: Không nên đăng nhập trực tiếp khi có sesion sẽ gây lỗi nếu đăng xuất
        if (session.isLoggedIn()) {
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

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilLoginEmail.setError(getString(R.string.err_email_invalid));
                return;
            }
            if (password.isEmpty()) {
                binding.tilLoginPassword.setError(getString(R.string.err_password_empty));
                return;
            }

            // Hiển thị loading
            binding.btnLogin.setVisibility(android.view.View.INVISIBLE);
            binding.pbLoginLoading.setVisibility(android.view.View.VISIBLE);

            // DÙNG FAKE DATA ĐỂ TEST NHANH
            // TODO: Dùng xong nhớ xóa
            if (email.equals("admin@gmail.com") && password.equals("123456")) {
                session.saveUser(email, password);
                Toast.makeText(LoginActivity.this, "Đăng nhập giả thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                return;
            }

            SupabaseClient.login(email, password, new ApiCallback() {
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
                        Toast.makeText(LoginActivity.this, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                }

                @Override
                public void onError(ApiErrorCode code, String raw) {
                    runOnUiThread(() -> {
                        binding.btnLogin.setVisibility(View.VISIBLE);
                        binding.pbLoginLoading.setVisibility(View.GONE);
                        if (code == ApiErrorCode.INVALID_CREDENTIALS) {
                            binding.tilLoginPassword.setError(
                                    getString(R.string.err_invalid_credentials));
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    ApiErrorMapper.messageOf(LoginActivity.this, code),
                                    Toast.LENGTH_LONG).show();
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
                    binding.tilLoginPassword.setError(getString(R.string.err_password_empty));
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