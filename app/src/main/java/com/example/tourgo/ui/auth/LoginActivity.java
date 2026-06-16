package com.example.tourgo.ui.auth;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.BuildConfig;
import com.example.tourgo.R;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.LoginRequest;
import com.example.tourgo.models.request.SocialLoginRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.AuthData;
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.admin.AdminActivity;
import com.example.tourgo.ui.business.BusinessActivity;
import com.example.tourgo.ui.main.home.MainActivity;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager session;

    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googleLauncher;

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

        // Register the Google sign-in launcher up front — registerForActivityResult
        // must run before the Activity reaches STARTED.
        setupGoogleSignIn();

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

        binding.tvForgotPassword.setOnClickListener(v ->
                new ForgotPasswordDialog().show(getSupportFragmentManager(), "forgot_password"));

        binding.tvLoginSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        binding.btnLogin.setOnClickListener(v -> doEmailLogin());

        // Social login
        binding.ivLoginGoogle.setOnClickListener(v -> startGoogleSignIn());
        binding.ivLoginFacebook.setOnClickListener(v -> startFacebookLogin());

        // Handle a Facebook OAuth redirect that launched / resumed this screen.
        handleOAuthRedirect(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOAuthRedirect(intent);
    }

    // ── Email / password login ────────────────────────────────────────────────
    private void doEmailLogin() {
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

        setLoading(true);
        RetrofitClient.getInstance(this)
                .getAuthApi()
                .login(new LoginRequest(email, password))
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (isAuthSuccess(response)) {
                                onAuthData(response.body().getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                ErrorHandler.showError(LoginActivity.this, error, binding.tilLoginPassword);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            ErrorHandler.showError(LoginActivity.this, ErrorHandler.parseError(t));
                        });
                    }
                });
    }

    // ── Google sign-in ────────────────────────────────────────────────────────
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Request an ID token (audience = our Web client ID) so the backend
                // can verify it with Supabase signInWithIdToken.
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        String idToken = account != null ? account.getIdToken() : null;
                        if (idToken != null && !idToken.isEmpty()) {
                            socialLogin("google", idToken);
                        } else {
                            setLoading(false);
                            Toast.makeText(this, R.string.login_social_failed, Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        setLoading(false);
                        // 12501 = user cancelled the chooser — stay silent.
                        if (e.getStatusCode() != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                            Toast.makeText(this, R.string.login_social_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startGoogleSignIn() {
        setLoading(true);
        // Sign out first so the account chooser always appears (instead of silently
        // reusing the last account).
        googleClient.signOut().addOnCompleteListener(this,
                t -> googleLauncher.launch(googleClient.getSignInIntent()));
    }

    // ── Shared social-login call ──────────────────────────────────────────────
    private void socialLogin(String provider, String token) {
        setLoading(true);
        RetrofitClient.getInstance(this)
                .getAuthApi()
                .socialLogin(new SocialLoginRequest(provider, token))
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (isAuthSuccess(response)) {
                                onAuthData(response.body().getData());
                            } else {
                                ErrorHandler.showError(LoginActivity.this, ErrorHandler.parseError(response));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            ErrorHandler.showError(LoginActivity.this, ErrorHandler.parseError(t));
                        });
                    }
                });
    }

    // ── Post-login handling (shared by email + social) ────────────────────────
    private boolean isAuthSuccess(Response<ApiResponse<AuthData>> response) {
        return response.isSuccessful() && response.body() != null
                && Boolean.TRUE.equals(response.body().getSuccess())
                && response.body().getData() != null;
    }

    private void onAuthData(AuthData authData) {
        session.saveSession(
                authData.getSession().getAccess_token(),
                authData.getSession().getRefresh_token(),
                authData.getSession().getExpires_at());
        session.saveUserInfo(
                authData.getUser().getId(),
                authData.getUser().getEmail(),
                authData.getUser().getName());
        proceedAfterSession();
    }

    /**
     * Facebook web-OAuth: the Supabase redirect carries only session tokens, so
     * user details (and the email used for admin/business routing) come from the
     * follow-up /me fetch in {@link #proceedAfterSession()}.
     */
    private void onSessionTokens(String accessToken, String refreshToken, long expiresAt) {
        setLoading(true);
        session.saveSession(accessToken, refreshToken, expiresAt);
        proceedAfterSession();
    }

    private void proceedAfterSession() {
        session.setRememberMe(binding.cbLoginRemember.isChecked());
        // Fetch + cache full user info; proceed home either way.
        UserRepository.getInstance().getCurrentUser(this, true, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    session.saveUserInfo(user.getId(), user.getEmail(), user.getName());
                }
                loginDone();
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                loginDone();
            }
        });
    }

    // ── Facebook (Supabase web OAuth via Custom Tab + deep link) ──────────────
    private void startFacebookLogin() {
        // Open Supabase's authorize endpoint; it redirects back to
        // tourgo://login-callback with the session tokens in the URL fragment.
        String url = BuildConfig.SUPABASE_URL
                + "/auth/v1/authorize?provider=facebook&redirect_to="
                + Uri.encode("tourgo://login-callback");
        Uri uri = Uri.parse(url);
        try {
            new CustomTabsIntent.Builder().build().launchUrl(this, uri);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, R.string.login_social_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleOAuthRedirect(Intent intent) {
        if (intent == null || intent.getData() == null) return;
        Uri data = intent.getData();
        if (!"tourgo".equals(data.getScheme()) || !"login-callback".equals(data.getHost())) return;

        // Consume the redirect so it isn't reprocessed on rotation / re-delivery.
        intent.setData(null);

        java.util.Map<String, String> params = parseUrlParams(data.getFragment());
        String error = params.get("error_description");
        if (error == null) error = params.get("error");
        if (error == null) error = data.getQueryParameter("error_description");

        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            return;
        }

        String accessToken = params.get("access_token");
        String refreshToken = params.get("refresh_token");
        if (accessToken == null || refreshToken == null) {
            Toast.makeText(this, R.string.login_social_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        long expiresAt = parseLongOr(params.get("expires_at"), 0);
        if (expiresAt == 0) {
            expiresAt = System.currentTimeMillis() / 1000 + parseLongOr(params.get("expires_in"), 3600);
        }
        onSessionTokens(accessToken, refreshToken, expiresAt);
    }

    private java.util.Map<String, String> parseUrlParams(String raw) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            if (i > 0) {
                map.put(Uri.decode(pair.substring(0, i)), Uri.decode(pair.substring(i + 1)));
            }
        }
        return map;
    }

    private long parseLongOr(String s, long def) {
        if (s == null) return def;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void loginDone() {
        Toast.makeText(this, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show();
        goToHome();
    }

    private void setLoading(boolean loading) {
        binding.pbLoginLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
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
