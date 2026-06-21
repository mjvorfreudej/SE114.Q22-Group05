package com.example.tourgo.ui.notification;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Persists which notifications the user has read, per
 * {@link NotificationMockData.Role}, in SharedPreferences.
 *
 * There is no notifications REST API yet, so this is what makes read-state
 * survive leaving and reopening a surface, and what keeps the three surfaces in
 * agreement: the home bell badge, the popover and the full center all seed
 * through {@link NotificationMockData#seed}, which applies this store. Without it
 * each surface recomputed "unread" from the raw seed and reset on every open.
 */
public final class NotificationStore {

    private static final String PREF = "tourgo_notif_read";

    private NotificationStore() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static String key(NotificationMockData.Role role) {
        return "read_" + role.name();
    }

    /** Ids the user has marked read for this role (a defensive copy, safe to mutate). */
    public static Set<String> readIds(Context ctx, NotificationMockData.Role role) {
        return new HashSet<>(prefs(ctx).getStringSet(key(role), Collections.<String>emptySet()));
    }

    /** Mark one notification read and persist it. */
    public static void markRead(Context ctx, NotificationMockData.Role role, String id) {
        Set<String> ids = readIds(ctx, role);
        if (ids.add(id)) {
            // A fresh Set instance must be stored for the change to be persisted.
            prefs(ctx).edit().putStringSet(key(role), ids).apply();
        }
    }

    /** Mark a batch of notifications read and persist them. */
    public static void markAllRead(Context ctx, NotificationMockData.Role role,
                                   Collection<String> idsToAdd) {
        Set<String> ids = readIds(ctx, role);
        if (ids.addAll(idsToAdd)) {
            prefs(ctx).edit().putStringSet(key(role), ids).apply();
        }
    }
}
