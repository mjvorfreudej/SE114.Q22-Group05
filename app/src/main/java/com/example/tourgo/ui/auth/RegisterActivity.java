package com.example.tourgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.remote.SupabaseClient;
import com.example.tourgo.databinding.ActivityRegisterBinding;
import com.example.tourgo.interfaces.AuthCallback;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    Boolean isValid = true;

    public abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public abstract void afterTextChanged(Editable s);
    }

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

        binding.tvRegisterSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        validateName();
        validateEmail();
        validatePassword();
        validateConfirmPassword();

        binding.btnRegister.setOnClickListener(v -> {
            if (checkEmptyInput() || !isValid) return;

            String name = binding.etRegisterName.getText().toString().trim();
            String email = binding.etRegisterEmail.getText().toString().trim();
            String password = binding.etRegisterPassword.getText().toString();

            SupabaseClient.register(email, password, name, new AuthCallback() {
                @Override
                public void onSuccess(String responseData) {
                    Intent itent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(itent);
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        binding.tilRegisterEmail.setError(errorMessage);
                    });
                }
            });
        });
    }

    private void validateName() {
        binding.etRegisterName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    binding.tilRegisterName.setError("Tên không được để trống");
                    isValid = false;
                } else {
                    binding.tilRegisterName.setError(null);
                    isValid = true;
                }
            }
        });
    }

    private void validateEmail() {
        binding.etRegisterEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    binding.tilRegisterEmail.setError("Địa chỉ email không hợp lệ");
                    isValid = false;
                } else {
                    binding.tilRegisterEmail.setError(null);
                    isValid = true;
                }
            }
        });
    }

    private void validatePassword() {
        binding.etRegisterPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    binding.tilRegisterPassword.setError("Mật khẩu không được để trống");
                    isValid = false;
                } else if (s.toString().length() < 6) {
                    binding.tilRegisterPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                    isValid = false;
                } else {
                    binding.tilRegisterPassword.setError(null);
                    isValid = true;
                }
            }
        });
    }

    private void validateConfirmPassword() {
        binding.etRegisterConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(binding.etRegisterPassword.getText().toString())) {
                    binding.tilRegisterConfirmPassword.setError("Mật khẩu không khớp");
                    isValid = false;
                } else {
                    binding.tilRegisterConfirmPassword.setError(null);
                    isValid = true;
                }
            }
        });
    }

    private Boolean checkEmptyInput() {
        Boolean isEmpty = false;
        if(binding.etRegisterName.getText().toString().trim().isEmpty()) {
            binding.tilRegisterName.setError("Tên không được để trống");
            isEmpty = true;
        }
        if(binding.etRegisterEmail.getText().toString().trim().isEmpty()) {
            binding.tilRegisterEmail.setError("Email không được để trống");
            isEmpty = true;
        }
        if(binding.etRegisterPassword.getText().toString().isEmpty()) {
            binding.tilRegisterPassword.setError("Mật khẩu không được để trống");
            isEmpty = true;
        }
        if(binding.etRegisterConfirmPassword.getText().toString().isEmpty()) {
            binding.tilRegisterConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
            isEmpty = true;
        }
        return isEmpty;
    }
}