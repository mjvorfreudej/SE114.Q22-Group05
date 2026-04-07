package com.example.Tour_Booking_App;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {
    private static final String PREF_NAME = "secure_session";
    private SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        try {
            @Deprecated
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void saveSession(String token, String userID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("userID", userID);
        editor.putBoolean("is_login", true);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString("token", null);
    }

    public Boolean isLoggedIn() {
        return sharedPreferences.getBoolean("is_login", false);
    }

    public void clearSession() {
        sharedPreferences.edit().clear().apply();
    }
}
