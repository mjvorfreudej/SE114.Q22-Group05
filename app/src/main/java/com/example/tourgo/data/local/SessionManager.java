package com.example.tourgo.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {
    private static final String PREF_NAME = "TourGoSession";

    // Auth keys
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_EXPIRES_AT = "expires_at";

    // User keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    // App settings
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_CURRENCY = "currency";
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
    public void saveSession(String accessToken, String refreshToken, long expiresAt) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .apply();
    }

    public void saveSession(String email, String userId, String accessToken, String refreshToken, String userName, boolean rememberMe) {
        sharedPreferences.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putBoolean(KEY_REMEMBER_ME, rememberMe)
                .apply();
    }

    public void saveUserInfo(String userId, String email, String name) {
        sharedPreferences.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, name)
                .apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public long getExpiresAt() {
        return sharedPreferences.getLong(KEY_EXPIRES_AT, 0);
    }

    public boolean isTokenExpired() {
        long expiresAt = getExpiresAt();
        return expiresAt == 0 || (System.currentTimeMillis() / 1000) >= expiresAt;
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null && !isTokenExpired();
    }

    public boolean isRememberMe() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void setRememberMe(boolean rememberMe) {
        sharedPreferences.edit().putBoolean(KEY_REMEMBER_ME, rememberMe).apply();
    }

    public void setCurrency(String currency) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply();
    }

    public String getCurrency() {
        // Mặc định là VND nếu chưa chọn
        return sharedPreferences.getString(KEY_CURRENCY, "VND");
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
