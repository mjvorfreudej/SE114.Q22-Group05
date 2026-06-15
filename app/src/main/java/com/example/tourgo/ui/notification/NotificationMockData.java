package com.example.tourgo.ui.notification;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.example.tourgo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory sample data for the Traveler notification center, mirroring the
 * TourGo design-system prototype (ui_kits/notifications/traveler.html). There is
 * no notifications REST API in this app yet, so the screen is wired to local
 * state with optimistic mark-as-read updates — exactly as the HTML prototype
 * behaves. Notification record content (titles, bodies, timestamps, action
 * labels) is sourced from localized string resources (values/ + values-en/),
 * so the whole screen follows the app's active language.
 */
public final class NotificationMockData {

    private NotificationMockData() {}

    // ── Category visual tokens (design TNOTIF_CATS) ──────────────────────────
    // bg = disc background, solid = disc icon tint. The app's adm_* palette
    // already carries the exact hex values from colors_and_type.css.
    public static class Category {
        public final String key;
        @StringRes public final int labelRes;
        @ColorRes public final int bgColor;
        @ColorRes public final int solidColor;

        Category(String key, @StringRes int labelRes, @ColorRes int bgColor, @ColorRes int solidColor) {
            this.key = key;
            this.labelRes = labelRes;
            this.bgColor = bgColor;
            this.solidColor = solidColor;
        }
    }

    public static final Map<String, Category> CATS = buildCats();

    private static Map<String, Category> buildCats() {
        Map<String, Category> m = new LinkedHashMap<>();
        m.put("bookings", new Category("bookings", R.string.notif_cat_bookings, R.color.adm_blue_50,  R.color.adm_blue_500));
        m.put("payments", new Category("payments", R.string.notif_cat_payments, R.color.adm_green_100, R.color.adm_green_500));
        m.put("offers",   new Category("offers",   R.string.notif_cat_offers,   R.color.adm_amber_100, R.color.adm_amber_500));
        m.put("trips",    new Category("trips",    R.string.notif_cat_trips,    R.color.adm_teal_50,   R.color.adm_teal_500));
        m.put("account",  new Category("account",  R.string.notif_cat_account,  R.color.adm_gray_100,  R.color.adm_gray_500));
        return Collections.unmodifiableMap(m);
    }

    public static Category category(String key) {
        Category c = CATS.get(key);
        return c != null ? c : CATS.get("account");
    }

    // ── Filters (design TNOTIF_FILTERS) ──────────────────────────────────────
    public static class Filter {
        public final String id;     // all | unread | <category key>
        @StringRes public final int labelRes;
        public final String cat;    // null for all/unread

        Filter(String id, @StringRes int labelRes, String cat) {
            this.id = id;
            this.labelRes = labelRes;
            this.cat = cat;
        }
    }

    public static final List<Filter> FILTERS = Arrays.asList(
            new Filter("all",      R.string.notif_filter_all,    null),
            new Filter("unread",   R.string.notif_filter_unread, null),
            new Filter("bookings", R.string.notif_cat_bookings,  "bookings"),
            new Filter("payments", R.string.notif_cat_payments,  "payments"),
            new Filter("offers",   R.string.notif_cat_offers,    "offers"),
            new Filter("trips",    R.string.notif_cat_trips,     "trips"),
            new Filter("account",  R.string.notif_cat_account,   "account")
    );

    // ── Seed data (design TNOTIF_DATA) ───────────────────────────────────────
    // Text is resolved from string resources so it matches the active language.
    public static List<NotificationItem> seed(Context ctx) {
        List<NotificationItem> list = new ArrayList<>();

        list.add(new NotificationItem("t1", "bookings", R.drawable.ic_check_circle,
                ctx.getString(R.string.notif_t1_title),
                ctx.getString(R.string.notif_t1_body),
                ctx.getString(R.string.notif_t1_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_t1_action), true))));

        list.add(new NotificationItem("t2", "payments", R.drawable.ic_dollar,
                ctx.getString(R.string.notif_t2_title),
                ctx.getString(R.string.notif_t2_body),
                ctx.getString(R.string.notif_t2_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_t2_action), false))));

        list.add(new NotificationItem("t3", "trips", R.drawable.ic_time,
                ctx.getString(R.string.notif_t3_title),
                ctx.getString(R.string.notif_t3_body),
                ctx.getString(R.string.notif_t3_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_t3_action), true))));

        list.add(new NotificationItem("t4", "offers", R.drawable.ic_percent,
                ctx.getString(R.string.notif_t4_title),
                ctx.getString(R.string.notif_t4_body),
                ctx.getString(R.string.notif_t4_when), NotificationItem.Group.TODAY, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_t4_action), true))));

        list.add(new NotificationItem("t5", "offers", R.drawable.ic_tag,
                ctx.getString(R.string.notif_t5_title),
                ctx.getString(R.string.notif_t5_body),
                ctx.getString(R.string.notif_t5_when), NotificationItem.Group.TODAY, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_t5_action), false))));

        list.add(new NotificationItem("t6", "trips", R.drawable.ic_star,
                ctx.getString(R.string.notif_t6_title),
                ctx.getString(R.string.notif_t6_body),
                ctx.getString(R.string.notif_t6_when), NotificationItem.Group.YESTERDAY, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_t6_action), true))));

        list.add(new NotificationItem("t7", "account", R.drawable.ic_shield_check,
                ctx.getString(R.string.notif_t7_title),
                ctx.getString(R.string.notif_t7_body),
                ctx.getString(R.string.notif_t7_when), NotificationItem.Group.YESTERDAY, true,
                Collections.<NotificationItem.QuickAction>emptyList()));

        list.add(new NotificationItem("t8", "account", R.drawable.ic_settings,
                ctx.getString(R.string.notif_t8_title),
                ctx.getString(R.string.notif_t8_body),
                ctx.getString(R.string.notif_t8_when), NotificationItem.Group.EARLIER, true,
                Collections.<NotificationItem.QuickAction>emptyList()));

        list.add(new NotificationItem("t9", "bookings", R.drawable.ic_calendar,
                ctx.getString(R.string.notif_t9_title),
                ctx.getString(R.string.notif_t9_body),
                ctx.getString(R.string.notif_t9_when), NotificationItem.Group.EARLIER, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_t9_action), false))));

        return list;
    }

    @StringRes
    public static int groupLabel(NotificationItem.Group group) {
        switch (group) {
            case TODAY:     return R.string.notif_group_today;
            case YESTERDAY: return R.string.notif_group_yesterday;
            default:        return R.string.notif_group_earlier;
        }
    }
}
