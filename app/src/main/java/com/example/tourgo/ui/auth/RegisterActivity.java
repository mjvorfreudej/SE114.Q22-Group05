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

import com.example.tourgo.remote.SupabaseClient;
import com.example.tourgo.databinding.ActivityRegisterBinding;
import com.example.tourgo.interfaces.AuthCallback;

import org.json.JSONArray;
import org.json.JSONObject;

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

            SupabaseClient.register(email, password, name, new AuthCallback() {
                @Override
                public void onSuccess(String responseData) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        try {
                            JSONObject json = new JSONObject(responseData);
                            JSONArray identities = json.optJSONArray("identities");
                            
                            if (identities != null && identities.length() == 0) {
                                binding.tilRegisterEmail.setError("Email này đã được đăng ký");
                                binding.etRegisterEmail.requestFocus();
                                return;
                            }

                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        if (errorMessage.toLowerCase().contains("mạng") || errorMessage.toLowerCase().contains("connect")) {
                            Toast.makeText(RegisterActivity.this, "Lỗi kết nối mạng, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                        } else if (errorMessage.contains("đã được đăng ký") || errorMessage.contains("already exists")) {
                            binding.tilRegisterEmail.setError("Email này đã được đăng ký");
                            binding.etRegisterEmail.requestFocus();
                        } else {
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.progressBarRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setText(loading ? "" : "Sign Up");
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
            binding.tilRegisterName.setError("Tên không được để trống");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegisterEmail.setError("Email không hợp lệ");
            return false;
        }
        if (password.length() < 6) {
            binding.tilRegisterPassword.setError("Mật khẩu phải từ 6 ký tự");
            return false;
        }
        if (!confirm.equals(password)) {
            binding.tilRegisterConfirmPassword.setError("Mật khẩu không khớp");
            return false;
        }
        return true;
    }
}
