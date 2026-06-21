package com.example.tourgo.remote.service;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.NotificationDto;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.notification.NotificationItem;
import com.example.tourgo.ui.notification.NotificationMockData;
import com.example.tourgo.ui.notification.NotificationStore;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Live data source behind the notification surfaces (bell badge → popover → full
 * center). Replaces the local-only {@link NotificationMockData} flow for the
 * Traveler and Business consoles by reading from {@code GET /api/notifications}
 * and writing read-state back to the server.
 *
 * <p>Role gating: ADMIN has no server-side feed wired yet, so it keeps using the
 * local mock seed + {@link NotificationStore} read-state. Callers don't need to
 * know — they always go through this service, which routes by role.
 */
public final class NotificationService {

    private NotificationService() {}

    private static boolean isApiBacked(NotificationMockData.Role role) {
        return role == NotificationMockData.Role.TRAVELER
                || role == NotificationMockData.Role.BUSINESS;
    }

    // ── Load ──────────────────────────────────────────────────────────────────
    public static void load(Context ctx, NotificationMockData.Role role,
                            DataCallback<List<NotificationItem>> callback) {
        if (!isApiBacked(role)) {
            callback.onSuccess(NotificationMockData.seed(ctx, role));
            return;
        }
        final Context app = ctx.getApplicationContext();
        RetrofitClient.getInstance(app)
                .getNotificationApi()
                .getNotifications(role.name())
                .enqueue(new Callback<ApiResponse<List<NotificationDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<NotificationDto>>> call,
                                           Response<ApiResponse<List<NotificationDto>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && Boolean.TRUE.equals(response.body().getSuccess())) {
                            callback.onSuccess(map(app, response.body().getData()));
                        } else {
                            ApiError e = ErrorHandler.parseError(response);
                            callback.onError(e.getCode(), e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<NotificationDto>>> call, Throwable t) {
                        ApiError e = ErrorHandler.parseError(t);
                        callback.onError(e.getCode(), e.getMessage());
                    }
                });
    }

    /** Convenience: load and report the unread count (drives the home bell badge). */
    public static void unreadCount(Context ctx, NotificationMockData.Role role,
                                   DataCallback<Integer> callback) {
        load(ctx, role, new DataCallback<List<NotificationItem>>() {
            @Override
            public void onSuccess(List<NotificationItem> items) {
                int unread = 0;
                for (NotificationItem n : items) if (!n.read) unread++;
                callback.onSuccess(unread);
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                callback.onError(code, rawMessage);
            }
        });
    }

    // ── Mark read ───────────────────────────────────────────────────────────────
    public static void markRead(Context ctx, NotificationMockData.Role role, String id,
                                @Nullable DataCallback<Void> callback) {
        if (!isApiBacked(role)) {
            NotificationStore.markRead(ctx, role, id);
            if (callback != null) callback.onSuccess(null);
            return;
        }
        RetrofitClient.getInstance(ctx.getApplicationContext())
                .getNotificationApi()
                .markAsRead(id)
                .enqueue(voidCallback(callback));
    }

    public static void markAllRead(Context ctx, NotificationMockData.Role role,
                                   List<String> ids, @Nullable DataCallback<Void> callback) {
        if (!isApiBacked(role)) {
            NotificationStore.markAllRead(ctx, role, ids);
            if (callback != null) callback.onSuccess(null);
            return;
        }
        RetrofitClient.getInstance(ctx.getApplicationContext())
                .getNotificationApi()
                .markAllAsRead(role.name())
                .enqueue(voidCallback(callback));
    }

    private static Callback<ApiResponse<Void>> voidCallback(@Nullable DataCallback<Void> callback) {
        return new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (callback == null) return;
                if (response.isSuccessful() && response.body() != null
                        && Boolean.TRUE.equals(response.body().getSuccess())) {
                    callback.onSuccess(null);
                } else {
                    ApiError e = ErrorHandler.parseError(response);
                    callback.onError(e.getCode(), e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (callback == null) return;
                ApiError e = ErrorHandler.parseError(t);
                callback.onError(e.getCode(), e.getMessage());
            }
        };
    }

    // ── Mapping (DTO → UI model) ─────────────────────────────────────────────────
    private static List<NotificationItem> map(Context ctx, List<NotificationDto> dtos) {
        List<NotificationItem> out = new ArrayList<>();
        if (dtos == null) return out;
        for (NotificationDto d : dtos) {
            if (d == null || d.getId() == null) continue;
            out.add(new NotificationItem(
                    d.getId(),
                    d.getCategory(),
                    iconFor(d.getIcon()),
                    d.getTitle(),
                    d.getBody(),
                    relativeWhen(ctx, d.getCreatedAt()),
                    groupOf(d.getCreatedAt()),
                    d.isRead(),
                    Collections.<NotificationItem.QuickAction>emptyList()));
        }
        return out;
    }

    /** Map a logical icon key (set by the backend) to a bundled drawable. */
    @DrawableRes
    private static int iconFor(@Nullable String key) {
        if (key == null) return R.drawable.ic_bell_20;
        switch (key) {
            case "calendar":        return R.drawable.ic_calendar;
            case "check_circle":    return R.drawable.ic_check_circle;
            case "dollar":          return R.drawable.ic_dollar;
            case "alert_triangle":  return R.drawable.ic_alert_triangle;
            case "star":            return R.drawable.ic_star;
            case "reply":           return R.drawable.ic_reply;
            case "shield_check":    return R.drawable.ic_shield_check;
            case "tag":             return R.drawable.ic_tag;
            case "percent":         return R.drawable.ic_percent;
            case "time":            return R.drawable.ic_time;
            case "settings":        return R.drawable.ic_settings;
            case "rocket":          return R.drawable.ic_rocket;
            default:                return R.drawable.ic_bell_20;
        }
    }

    // ── Time helpers ─────────────────────────────────────────────────────────────
    @Nullable
    private static Instant parseInstant(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try {
            return OffsetDateTime.parse(iso).toInstant();
        } catch (Exception ignored) {
            try {
                return Instant.parse(iso);
            } catch (Exception ignored2) {
                return null;
            }
        }
    }

    /** Compact relative timestamp ("Vừa xong", "5 phút", "2 giờ", "3 ngày", or a date). */
    private static String relativeWhen(Context ctx, @Nullable String iso) {
        Instant when = parseInstant(iso);
        if (when == null) return "";
        long mins = ChronoUnit.MINUTES.between(when, Instant.now());
        if (mins < 1) return ctx.getString(R.string.notif_when_now);
        if (mins < 60) return ctx.getString(R.string.notif_when_minutes, mins);
        long hours = mins / 60;
        if (hours < 24) return ctx.getString(R.string.notif_when_hours, hours);
        long days = hours / 24;
        if (days < 7) return ctx.getString(R.string.notif_when_days, days);
        return java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT)
                .format(java.util.Date.from(when));
    }

    /** Bucket a notification into Today / Yesterday / Earlier by local date. */
    private static NotificationItem.Group groupOf(@Nullable String iso) {
        Instant when = parseInstant(iso);
        if (when == null) return NotificationItem.Group.EARLIER;
        LocalDate date = when.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        if (date.isEqual(today)) return NotificationItem.Group.TODAY;
        if (date.isEqual(today.minusDays(1))) return NotificationItem.Group.YESTERDAY;
        return NotificationItem.Group.EARLIER;
    }
}
