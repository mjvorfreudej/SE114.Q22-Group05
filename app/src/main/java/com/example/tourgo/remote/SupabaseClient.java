package com.example.tourgo.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.tourgo.BuildConfig;
import com.example.tourgo.R;
import com.example.tourgo.TourGoApp;
import com.example.tourgo.interfaces.ApiCallback;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.data.local.SessionManager;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SupabaseClient {
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final Object refreshLock = new Object();
    private static final AtomicBoolean loggedOut = new AtomicBoolean(false);

    /**
     * Synchronously refreshes the access token using the stored refresh_token.
     * Returns the new access token on success, or null on failure (caller should forceLogout).
     * <p>
     * Concurrency-safe: if multiple callers race, only one network round-trip occurs;
     * subsequent callers observe the already-rotated token via the staleAccessToken check.
     */
    public static String refreshToken(String staleAccessToken) {
        synchronized (refreshLock) {
            Context ctx = TourGoApp.getAppContext();
            if (ctx == null) return null;
            SessionManager session = new SessionManager(ctx);

            String currentAccess = session.getAccessToken();
            if (currentAccess != null && !currentAccess.equals(staleAccessToken)) {
                return currentAccess;
            }

            String refresh = session.getRefreshToken();
            if (refresh == null || refresh.isEmpty()) return null;

            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("refresh_token", refresh);
                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/auth/v1/token?grant_type=refresh_token")
                        .addHeader("apikey", ANON_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                try (Response response = SupabaseConfig.client.newCall(request).execute()) {
                    ResponseBody respBody = response.body();
                    String text = respBody != null ? respBody.string() : "";
                    if (!response.isSuccessful() || text.isEmpty()) return null;

                    JSONObject json = new JSONObject(text);
                    String newAccess = json.optString("access_token", "");
                    String newRefresh = json.optString("refresh_token", refresh);
                    if (newAccess.isEmpty()) return null;

                    session.saveSession(
                            session.getEmail(),
                            session.getUserId(),
                            newAccess,
                            newRefresh,
                            session.getUserName(),
                            session.isRememberMe()
                    );
                    return newAccess;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Wipes the session and bounces the user to LoginActivity with a toast.
     * Idempotent — only fires once per process even if many in-flight calls fail at once.
     */
    public static void forceLogout() {
        if (!loggedOut.compareAndSet(false, true)) return;

        Context ctx = TourGoApp.getAppContext();
        if (ctx == null) return;

        new SessionManager(ctx).clear();

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(ctx, R.string.err_unauthorized, Toast.LENGTH_LONG).show());

        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
    }
}
