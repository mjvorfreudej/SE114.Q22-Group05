package com.example.tourgo.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SessionManager {
    private static final String PREF_NAME = "TourGoSession";

    // ── Admin access control (Email suffix + Predefined whitelist) ────────────
    /** Any account whose email ends with this domain is treated as an admin. */
    private static final String ADMIN_EMAIL_DOMAIN = "@tourgo.com";
    /** Extra admin accounts that don't use the company domain. Edit as needed.
     *  Whitelisted users register normally with their own password; their admin
     *  role is verified solely by their email at login. */
    private static final String[] ADMIN_EMAIL_WHITELIST = {
            "lamquyen290391@gmail.com",
//            "hinduck3206@gmail.com",
    };

    // ── Business access control (Email suffix + Predefined whitelist) ─────────
    /** Any account whose email ends with this domain is routed to the Business Console. */
    private static final String BUSINESS_EMAIL_DOMAIN = "@business.tourgo.com";
    /** Extra business (partner) accounts that don't use the partner domain. */
    private static final String[] BUSINESS_EMAIL_WHITELIST = {
            "business@tourgo.com",
            "partner@tourgo.com",
    };

    // Auth keys
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_EXPIRES_AT = "expires_at";

    // User keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";

    // App settings
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_CURRENCY = "currency";
    
    // Recent Search & Viewed keys
    private static final String KEY_RECENT_SEARCHES = "recent_searches_v2";
    private static final String KEY_RECENTLY_VIEWED = "recently_viewed";
    
    private SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public static class RecentlyViewedItem {
        public String id;
        public boolean isTour;
        public long timestamp;

        public RecentlyViewedItem(String id, boolean isTour) {
            this.id = id;
            this.isTour = isTour;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecentlyViewedItem that = (RecentlyViewedItem) o;
            return isTour == that.isTour && id.equals(that.id);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + (isTour ? 1 : 0);
            return result;
        }
    }

    public static class RecentSearchItem {
        public String query;
        public boolean isTour;

        public RecentSearchItem(String query, boolean isTour) {
            this.query = query;
            this.isTour = isTour;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecentSearchItem that = (RecentSearchItem) o;
            return isTour == that.isTour && query.equals(that.query);
        }

        @Override
        public int hashCode() {
            int result = query.hashCode();
            result = 31 * result + (isTour ? 1 : 0);
            return result;
        }
    }

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
        saveUserInfo(userId, email, name, null);
    }

    public void saveUserInfo(String userId, String email, String name, String role) {
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, name);
        if (role != null) {
            editor.putString(KEY_USER_ROLE, role);
        }
        editor.apply();
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

    public boolean isAdmin() {
        String role = sharedPreferences.getString(KEY_USER_ROLE, null);
        if ("admin".equalsIgnoreCase(role)) return true;

        String email = getEmail();
        if (email == null) return false;
        email = email.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty()) return false;

        for (String biz : BUSINESS_EMAIL_WHITELIST) {
            if (biz != null && email.equals(biz.trim().toLowerCase(Locale.ROOT))) return false;
        }
        if (email.endsWith(BUSINESS_EMAIL_DOMAIN)) return false;

        if (email.endsWith(ADMIN_EMAIL_DOMAIN)) return true;
        for (String admin : ADMIN_EMAIL_WHITELIST) {
            if (admin != null && email.equals(admin.trim().toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    public boolean isBusiness() {
        String role = sharedPreferences.getString(KEY_USER_ROLE, null);
        if ("business".equalsIgnoreCase(role)) return true;

        if (isAdmin()) return false;
        String email = getEmail();
        if (email == null) return false;
        email = email.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty()) return false;
        if (email.endsWith(BUSINESS_EMAIL_DOMAIN)) return true;
        for (String biz : BUSINESS_EMAIL_WHITELIST) {
            if (biz != null && email.equals(biz.trim().toLowerCase(Locale.ROOT))) return true;
        }
        return false;
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
        return sharedPreferences.getString(KEY_CURRENCY, "VND");
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Lưu danh sách các ngày bị chặn của một tháng cụ thể.
     */
    public void saveBlockedDates(String monthYearKey, java.util.Set<String> blockedDays) {
        sharedPreferences.edit().putStringSet("blocked_dates_" + monthYearKey, blockedDays).apply();
    }

    /**
     * Lấy danh sách các ngày bị chặn của một tháng cụ thể.
     */
    public java.util.Set<String> getBlockedDates(String monthYearKey) {
        java.util.Set<String> set = sharedPreferences.getStringSet("blocked_dates_" + monthYearKey, null);
        if (set == null) {
            return new java.util.HashSet<>();
        }
        return new java.util.HashSet<>(set);
    }


    public String getShortName() {
        String fullName = getUserName();
        if (fullName == null || fullName.trim().isEmpty()) return "User";
        
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length <= 2) {
            return fullName;
        } else {
            return parts[parts.length - 2] + " " + parts[parts.length - 1];
        }
    }

    // ── Recent Search Logic ──────────────────────────────────────────────────
    public void addRecentSearch(String query, boolean isTour) {
        if (query == null || query.trim().isEmpty()) return;
        List<RecentSearchItem> searches = getRecentSearches();
        RecentSearchItem newItem = new RecentSearchItem(query, isTour);
        searches.remove(newItem);
        searches.add(0, newItem);
        if (searches.size() > 10) searches = searches.subList(0, 10);
        sharedPreferences.edit().putString(KEY_RECENT_SEARCHES, gson.toJson(searches)).apply();
    }

    public List<RecentSearchItem> getRecentSearches() {
        String json = sharedPreferences.getString(KEY_RECENT_SEARCHES, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<RecentSearchItem>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void clearRecentSearches() {
        sharedPreferences.edit().remove(KEY_RECENT_SEARCHES).apply();
    }

    // ── Recently Viewed Logic ───────────────────────────────────────────────
    public void addRecentlyViewed(String id, boolean isTour) {
        if (id == null) return;
        List<RecentlyViewedItem> items = getRecentlyViewed();
        RecentlyViewedItem newItem = new RecentlyViewedItem(id, isTour);
        items.remove(newItem);
        items.add(0, newItem);
        if (items.size() > 10) items = items.subList(0, 10);
        sharedPreferences.edit().putString(KEY_RECENTLY_VIEWED, gson.toJson(items)).apply();
    }

    public List<RecentlyViewedItem> getRecentlyViewed() {
        String json = sharedPreferences.getString(KEY_RECENTLY_VIEWED, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<RecentlyViewedItem>>(){}.getType();
        return gson.fromJson(json, type);
    }
}
