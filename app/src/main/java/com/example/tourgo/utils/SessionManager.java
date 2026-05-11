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
     */
    public void saveSession(String email, String userId, String accessToken, String refreshToken, String name, boolean rememberMe) {
        sharedPreferences.edit()
                .putString("email", email)
                .putString("user_id", userId)
                .putString("access_token", accessToken)
                .putString("refresh_token", refreshToken)
                .putString("user_name", name)
                .putBoolean("isLoggedIn", true)
                .putBoolean("remember_me", rememberMe)
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

    public String getUserName() {
        return sharedPreferences.getString("user_name", null);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    public boolean isRememberMe() {
        return sharedPreferences.getBoolean("remember_me", false);
    }

    public void setCurrency(String currency) {
        sharedPreferences.edit().putString("currency", currency).apply();
    }

    public String getCurrency() {
        // Mặc định là VND nếu chưa chọn
        return sharedPreferences.getString("currency", "VND");
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Lấy 2 chữ cuối của tên (thường là Họ và Tên ở VN, hoặc Tên chính)
     */
    public String getShortName() {
        String fullName = getUserName();
        if (fullName == null || fullName.trim().isEmpty()) return "User";
        
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length <= 2) {
            return fullName;
        } else {
            // Lấy 2 từ cuối
            return parts[parts.length - 2] + " " + parts[parts.length - 1];
        }
    }
}
