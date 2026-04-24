package com.example.tourgo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to normal SharedPreferences if encryption fails
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * Lưu session sau khi login thành công.
     * Lưu access_token và refresh_token thay vì mật khẩu.
     */
    public void saveSession(String email, String userId, String accessToken, String refreshToken) {
        sharedPreferences.edit()
                .putString("email", email)
                .putString("user_id", userId)
                .putString("access_token", accessToken)
                .putString("refresh_token", refreshToken)
                .putBoolean("isLoggedIn", true)
                .apply();
    }

    public String getEmail() {
        return sharedPreferences.getString("email", null);
    }

    public String getUserId() {
        return sharedPreferences.getString("user_id", null);
    }

    public String getAccessToken() {
        return sharedPreferences.getString("access_token", null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString("refresh_token", null);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
