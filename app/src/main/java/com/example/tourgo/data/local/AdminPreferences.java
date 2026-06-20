package com.example.tourgo.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Local persistence for Admin Console settings that have no backend endpoint
 * (moderation policy, notification delivery preferences, the 2FA toggle). These
 * are device-local preferences, not secrets, so plain SharedPreferences is fine.
 *
 * <p>Used by {@link com.example.tourgo.ui.admin.AdminDetailActivity} so those
 * screens load their saved state and persist changes across sessions instead of
 * resetting to defaults every visit.
 */
public class AdminPreferences {

    private static final String FILE = "AdminSettings";

    // Moderation policy
    public static final String POLICY_AUTOHIDE = "policy_autohide";
    public static final String POLICY_PROFANITY = "policy_profanity";
    public static final String POLICY_PHOTO = "policy_photo";
    public static final String POLICY_GEO = "policy_geo";
    public static final String POLICY_HIDE_AT = "policy_hide_at";
    public static final String POLICY_SLA = "policy_sla";
    public static final String POLICY_TERMS = "policy_terms";

    // Notification delivery preferences
    public static final String NOTIF_PENDING = "notif_pending";
    public static final String NOTIF_REPORTED = "notif_reported";
    public static final String NOTIF_TEAM = "notif_team";
    public static final String NOTIF_SLA = "notif_sla";
    public static final String NOTIF_DIGEST = "notif_digest";
    public static final String NOTIF_WEEKLY = "notif_weekly";
    public static final String NOTIF_SECURITY = "notif_security";

    // Security
    public static final String TFA_ENABLED = "tfa_enabled";

    private final SharedPreferences sp;

    public AdminPreferences(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public boolean getBool(String key, boolean def) {
        return sp.getBoolean(key, def);
    }

    public void setBool(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public int getInt(String key, int def) {
        return sp.getInt(key, def);
    }

    public void setInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    /** An ordered list, persisted as a newline-joined string. */
    public List<String> getList(String key, List<String> def) {
        String raw = sp.getString(key, null);
        if (raw == null) return new ArrayList<>(def);
        List<String> out = new ArrayList<>();
        for (String s : raw.split("\n")) {
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    public void setList(String key, List<String> values) {
        sp.edit().putString(key, TextUtils.join("\n", values)).apply();
    }
}
