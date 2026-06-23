package com.example.tourgo.ui.chat;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Per-room "last read" marker for chat, stored in SharedPreferences.
 *
 * <p>The backend has no chat read-tracking (chat_messages.is_read is never
 * maintained, getRooms returns no unread count), so the Detail-screen chat badge
 * derives its unread count locally: messages from the partner newer than the
 * stored marker are "unread". The marker advances whenever the user opens the
 * room ({@link com.example.tourgo.ui.chat.ChatActivity} calls {@link #markRead}).
 */
public final class ChatReadStore {

    private static final String PREF = "tourgo_chat_read";

    private ChatReadStore() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static String key(String roomId) {
        return "room_" + roomId;
    }

    /** Epoch millis of the newest message the user has seen in this room (0 if never). */
    public static long lastRead(Context ctx, String roomId) {
        if (roomId == null) return 0L;
        return prefs(ctx).getLong(key(roomId), 0L);
    }

    /** Advance the read marker for this room to {@code latestIso} (no-op if older). */
    public static void markRead(Context ctx, String roomId, String latestIso) {
        if (roomId == null) return;
        long t = parseIso(latestIso);
        if (t <= 0) return;
        if (t > lastRead(ctx, roomId)) {
            prefs(ctx).edit().putLong(key(roomId), t).apply();
        }
    }

    /** Parse an ISO-8601 timestamp to epoch millis; 0 on failure. */
    public static long parseIso(String iso) {
        if (iso == null || iso.isEmpty()) return 0L;
        try {
            return OffsetDateTime.parse(iso).toInstant().toEpochMilli();
        } catch (Exception ignored) {
            try {
                return Instant.parse(iso).toEpochMilli();
            } catch (Exception ignored2) {
                return 0L;
            }
        }
    }
}
