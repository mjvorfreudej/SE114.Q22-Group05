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
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.LoginRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.AuthData;
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.admin.AdminActivity;
import com.example.tourgo.ui.business.BusinessActivity;
import com.example.tourgo.ui.main.home.MainActivity;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.databinding.ActivityLoginBinding;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Auto-login: if a remembered session exists, route to the correct home
        // (admins -> AdminActivity, travelers -> MainActivity). Deferred with
        // post() so the Activity finishes initialising before finish() runs —
        // calling finish() during onCreate() crashes on API 35+ with
        // "Activity client record must not be null ... TopResumedActivityChangeItem".
        if (session.isLoggedIn() && session.isRememberMe()) {
            binding.getRoot().post(this::goToHome);
            return;
        }

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

            LoginRequest request = new LoginRequest(email, password);
            RetrofitClient.getInstance(this)
                    .getAuthApi()
                    .login(request)
                    .enqueue(new Callback<ApiResponse<AuthData>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                            runOnUiThread(() -> {
                                binding.btnLogin.setVisibility(android.view.View.VISIBLE);
                                binding.pbLoginLoading.setVisibility(View.GONE);

                                if (response.isSuccessful() && response.body() != null) {
                                    ApiResponse<AuthData> apiResponse = response.body();

                                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()
                                            && apiResponse.getData() != null) {

                                        AuthData authData = apiResponse.getData();

                                        session.saveSession(
                                                authData.getSession().getAccess_token(),
                                                authData.getSession().getRefresh_token(),
                                                authData.getSession().getExpires_at()
                                        );

                                        session.saveUserInfo(
                                                authData.getUser().getId(),
                                                authData.getUser().getEmail(),
                                                authData.getUser().getName(),
                                                authData.getUser().getRole()
                                        );

                                        session.setRememberMe(binding.cbLoginRemember.isChecked());

                                        // Gọi UserService để lấy thông tin đầy đủ và cache vào UserRepository
                                        UserRepository.getInstance().getCurrentUser(LoginActivity.this, true, new DataCallback<User>() {
                                            @Override
                                            public void onSuccess(User user) {
                                                if (user != null) {
                                                    session.saveUserInfo(
                                                            user.getId(),
                                                            user.getEmail(),
                                                            user.getName(),
                                                            user.getRole()
                                                    );
                                                }
                                                Toast.makeText(LoginActivity.this,
                                                        getString(R.string.msg_login_success),
                                                        Toast.LENGTH_SHORT).show();

                                                goToHome();
                                            }

                                            @Override
                                            public void onError(ApiErrorCode code, String message) {
                                                // Nếu lỗi khi lấy user info, vẫn cho login nhưng không có cache
                                                Toast.makeText(LoginActivity.this,
                                                        getString(R.string.msg_login_success),
                                                        Toast.LENGTH_SHORT).show();

                                                goToHome();
                                            }
                                        });

                                    } else {
                                        ApiError error = ErrorHandler.parseError(response);
                                        ErrorHandler.showError(LoginActivity.this, error, binding.tilLoginPassword);
                                    }
                                } else {
                                    ApiError error = ErrorHandler.parseError(response);
                                    ErrorHandler.showError(LoginActivity.this, error, binding.tilLoginPassword);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                            runOnUiThread(() -> {
                                binding.btnLogin.setVisibility(View.VISIBLE);
                                binding.pbLoginLoading.setVisibility(View.GONE);

                                ApiError error = ErrorHandler.parseError(t);
                                ErrorHandler.showError(LoginActivity.this, error);
                            });
                        }
                    });
        });
    }

    /**
     * Routes the user after a successful login / auto-login: admins land on the
     * Admin Console, business/partner accounts on the Business Console, everyone
     * else on the standard Traveler home.
     */
    private void goToHome() {
        Class<?> destination = session.isAdmin() ? AdminActivity.class
                : session.isBusiness() ? BusinessActivity.class
                : MainActivity.class;
        Intent intent = new Intent(LoginActivity.this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
