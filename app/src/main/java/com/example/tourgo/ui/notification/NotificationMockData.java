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
 * In-memory sample data for the notification surfaces, mirroring the TourGo
 * design-system prototypes:
 *   · Traveler  → ui_kits/notifications/traveler.html
 *   · Business  → ui_kits/notifications/index.html ({@code role="business"})
 *   · Admin     → ui_kits/notifications/index.html ({@code role="admin"})
 *
 * There is no notifications REST API in this app yet, so every surface is wired
 * to local state with optimistic mark-as-read updates — exactly as the HTML
 * prototypes behave. Record content (titles, bodies, timestamps, action labels)
 * is sourced from localized string resources (values/ + values-en/), so the whole
 * screen follows the app's active language.
 *
 * Everything here is keyed by {@link Role}: category palettes ({@code NOTIF_CATS}),
 * filter chips ({@code NOTIF_FILTERS}) and seed data ({@code NOTIF_DATA}) all differ
 * per role, matching the prototype's role-parameterised data model.
 */
public final class NotificationMockData {

    private NotificationMockData() {}

    /** The three surfaces that share this notification module. */
    public enum Role { TRAVELER, BUSINESS, ADMIN }

    // ── Category visual tokens (design NOTIF_CATS) ───────────────────────────
    // bg = disc background, solid = disc icon tint + category-dot, ink = category
    // tag-pill text (Business/Admin center only). The adm_* palette already carries
    // the exact hex values from colors_and_type.css.
    public static class Category {
        public final String key;
        @StringRes public final int labelRes;
        @ColorRes public final int bgColor;
        @ColorRes public final int solidColor;
        @ColorRes public final int inkColor;

        Category(String key, @StringRes int labelRes,
                 @ColorRes int bgColor, @ColorRes int solidColor, @ColorRes int inkColor) {
            this.key = key;
            this.labelRes = labelRes;
            this.bgColor = bgColor;
            this.solidColor = solidColor;
            this.inkColor = inkColor;
        }
    }

    private static final Map<String, Category> CATS_TRAVELER = buildTravelerCats();
    private static final Map<String, Category> CATS_BUSINESS = buildBusinessCats();
    private static final Map<String, Category> CATS_ADMIN = buildAdminCats();

    private static Map<String, Category> buildTravelerCats() {
        Map<String, Category> m = new LinkedHashMap<>();
        m.put("bookings", new Category("bookings", R.string.notif_cat_bookings, R.color.adm_blue_50,  R.color.adm_blue_500,  R.color.adm_blue_700));
        m.put("payments", new Category("payments", R.string.notif_cat_payments, R.color.adm_green_100, R.color.adm_green_500, R.color.adm_green_700));
        m.put("offers",   new Category("offers",   R.string.notif_cat_offers,   R.color.adm_amber_100, R.color.adm_amber_500, R.color.adm_amber_700));
        m.put("trips",    new Category("trips",    R.string.notif_cat_trips,    R.color.adm_teal_50,   R.color.adm_teal_500,  R.color.adm_teal_ink));
        m.put("account",  new Category("account",  R.string.notif_cat_account,  R.color.adm_gray_100,  R.color.adm_gray_500,  R.color.adm_gray_700));
        return Collections.unmodifiableMap(m);
    }

    private static Map<String, Category> buildBusinessCats() {
        Map<String, Category> m = new LinkedHashMap<>();
        m.put("orders",     new Category("orders",     R.string.notif_cat_orders,     R.color.adm_blue_50,   R.color.adm_blue_500,  R.color.adm_blue_700));
        m.put("operations", new Category("operations", R.string.notif_cat_operations, R.color.adm_teal_50,   R.color.adm_teal_500,  R.color.adm_teal_ink));
        m.put("support",    new Category("support",    R.string.notif_cat_support,    R.color.adm_purple_bg, R.color.adm_purple_500, R.color.adm_purple_ink));
        m.put("promo",      new Category("promo",      R.string.notif_cat_promotions, R.color.adm_amber_100, R.color.adm_amber_500, R.color.adm_amber_700));
        return Collections.unmodifiableMap(m);
    }

    private static Map<String, Category> buildAdminCats() {
        Map<String, Category> m = new LinkedHashMap<>();
        m.put("system",     new Category("system",     R.string.notif_cat_system,     R.color.adm_red_100,   R.color.adm_red_500,   R.color.adm_red_700));
        m.put("approvals",  new Category("approvals",  R.string.notif_cat_approvals,  R.color.adm_blue_50,   R.color.adm_blue_500,  R.color.adm_blue_700));
        m.put("reports",    new Category("reports",    R.string.notif_cat_reports,    R.color.adm_amber_100, R.color.adm_amber_500, R.color.adm_amber_700));
        m.put("milestones", new Category("milestones", R.string.notif_cat_milestones, R.color.adm_green_100, R.color.adm_green_500, R.color.adm_green_700));
        return Collections.unmodifiableMap(m);
    }

    /** Category palette for a role (insertion-ordered, matching the prototype). */
    public static Map<String, Category> cats(Role role) {
        switch (role) {
            case BUSINESS: return CATS_BUSINESS;
            case ADMIN:    return CATS_ADMIN;
            default:       return CATS_TRAVELER;
        }
    }

    /** Resolve a category for a role, falling back to that role's first entry. */
    public static Category category(Role role, String key) {
        Map<String, Category> m = cats(role);
        Category c = m.get(key);
        return c != null ? c : m.values().iterator().next();
    }

    // ── Filters (design NOTIF_FILTERS) ───────────────────────────────────────
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

    private static final List<Filter> FILTERS_TRAVELER = Arrays.asList(
            new Filter("all",      R.string.notif_filter_all,    null),
            new Filter("unread",   R.string.notif_filter_unread, null),
            new Filter("bookings", R.string.notif_cat_bookings,  "bookings"),
            new Filter("payments", R.string.notif_cat_payments,  "payments"),
            new Filter("offers",   R.string.notif_cat_offers,    "offers"),
            new Filter("trips",    R.string.notif_cat_trips,     "trips"),
            new Filter("account",  R.string.notif_cat_account,   "account")
    );

    private static final List<Filter> FILTERS_BUSINESS = Arrays.asList(
            new Filter("all",        R.string.notif_filter_all,     null),
            new Filter("unread",     R.string.notif_filter_unread,  null),
            new Filter("orders",     R.string.notif_cat_orders,     "orders"),
            new Filter("operations", R.string.notif_cat_operations, "operations"),
            new Filter("support",    R.string.notif_cat_support,    "support"),
            new Filter("promo",      R.string.notif_cat_promotions, "promo")
    );

    private static final List<Filter> FILTERS_ADMIN = Arrays.asList(
            new Filter("all",        R.string.notif_filter_all,     null),
            new Filter("unread",     R.string.notif_filter_unread,  null),
            new Filter("system",     R.string.notif_cat_system,     "system"),
            new Filter("approvals",  R.string.notif_cat_approvals,  "approvals"),
            new Filter("reports",    R.string.notif_cat_reports,    "reports"),
            new Filter("milestones", R.string.notif_cat_milestones, "milestones")
    );

    public static List<Filter> filters(Role role) {
        switch (role) {
            case BUSINESS: return FILTERS_BUSINESS;
            case ADMIN:    return FILTERS_ADMIN;
            default:       return FILTERS_TRAVELER;
        }
    }

    // ── Seed data (design NOTIF_DATA) ────────────────────────────────────────
    // Text is resolved from string resources so it matches the active language.
    public static List<NotificationItem> seed(Context ctx, Role role) {
        switch (role) {
            case BUSINESS: return seedBusiness(ctx);
            case ADMIN:    return seedAdmin(ctx);
            default:       return seedTraveler(ctx);
        }
    }

    private static List<NotificationItem> seedTraveler(Context ctx) {
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

    private static List<NotificationItem> seedBusiness(Context ctx) {
        List<NotificationItem> list = new ArrayList<>();

        list.add(new NotificationItem("b1", "orders", R.drawable.ic_calendar,
                ctx.getString(R.string.notif_b1_title),
                ctx.getString(R.string.notif_b1_body),
                ctx.getString(R.string.notif_b1_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_b1_action), true))));

        list.add(new NotificationItem("b2", "orders", R.drawable.ic_dollar,
                ctx.getString(R.string.notif_b2_title),
                ctx.getString(R.string.notif_b2_body),
                ctx.getString(R.string.notif_b2_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_b2_action), false))));

        list.add(new NotificationItem("b3", "operations", R.drawable.ic_alert_triangle,
                ctx.getString(R.string.notif_b3_title),
                ctx.getString(R.string.notif_b3_body),
                ctx.getString(R.string.notif_b3_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_b3_action), true))));

        list.add(new NotificationItem("b4", "operations", R.drawable.ic_time,
                ctx.getString(R.string.notif_b4_title),
                ctx.getString(R.string.notif_b4_body),
                ctx.getString(R.string.notif_b4_when), NotificationItem.Group.TODAY, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_b4_action), false))));

        list.add(new NotificationItem("b5", "support", R.drawable.ic_reply,
                ctx.getString(R.string.notif_b5_title),
                ctx.getString(R.string.notif_b5_body),
                ctx.getString(R.string.notif_b5_when), NotificationItem.Group.TODAY, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_b5_action), false))));

        list.add(new NotificationItem("b6", "support", R.drawable.ic_star,
                ctx.getString(R.string.notif_b6_title),
                ctx.getString(R.string.notif_b6_body),
                ctx.getString(R.string.notif_b6_when), NotificationItem.Group.YESTERDAY, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_b6_action), true))));

        list.add(new NotificationItem("b7", "promo", R.drawable.ic_rocket,
                ctx.getString(R.string.notif_b7_title),
                ctx.getString(R.string.notif_b7_body),
                ctx.getString(R.string.notif_b7_when), NotificationItem.Group.YESTERDAY, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_b7_action), false))));

        list.add(new NotificationItem("b8", "promo", R.drawable.ic_settings,
                ctx.getString(R.string.notif_b8_title),
                ctx.getString(R.string.notif_b8_body),
                ctx.getString(R.string.notif_b8_when), NotificationItem.Group.EARLIER, true,
                Collections.<NotificationItem.QuickAction>emptyList()));

        return list;
    }

    private static List<NotificationItem> seedAdmin(Context ctx) {
        List<NotificationItem> list = new ArrayList<>();

        list.add(new NotificationItem("a1", "approvals", R.drawable.ic_shield_check,
                ctx.getString(R.string.notif_a1_title),
                ctx.getString(R.string.notif_a1_body),
                ctx.getString(R.string.notif_a1_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_a1_action), true))));

        list.add(new NotificationItem("a2", "approvals", R.drawable.ic_dollar,
                ctx.getString(R.string.notif_a2_title),
                ctx.getString(R.string.notif_a2_body),
                ctx.getString(R.string.notif_a2_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_a2_action), true))));

        list.add(new NotificationItem("a3", "system", R.drawable.ic_alert_triangle,
                ctx.getString(R.string.notif_a3_title),
                ctx.getString(R.string.notif_a3_body),
                ctx.getString(R.string.notif_a3_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_a3_action), false))));

        list.add(new NotificationItem("a4", "reports", R.drawable.ic_flag,
                ctx.getString(R.string.notif_a4_title),
                ctx.getString(R.string.notif_a4_body),
                ctx.getString(R.string.notif_a4_when), NotificationItem.Group.TODAY, false,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_a4_action), true))));

        list.add(new NotificationItem("a5", "reports", R.drawable.ic_user_x,
                ctx.getString(R.string.notif_a5_title),
                ctx.getString(R.string.notif_a5_body),
                ctx.getString(R.string.notif_a5_when), NotificationItem.Group.TODAY, true,
                Arrays.asList(new NotificationItem.QuickAction(ctx.getString(R.string.notif_a5_action), true))));

        list.add(new NotificationItem("a6", "milestones", R.drawable.ic_flame,
                ctx.getString(R.string.notif_a6_title),
                ctx.getString(R.string.notif_a6_body),
                ctx.getString(R.string.notif_a6_when), NotificationItem.Group.YESTERDAY, true,
                Collections.<NotificationItem.QuickAction>emptyList()));

        list.add(new NotificationItem("a7", "milestones", R.drawable.ic_users,
                ctx.getString(R.string.notif_a7_title),
                ctx.getString(R.string.notif_a7_body),
                ctx.getString(R.string.notif_a7_when), NotificationItem.Group.YESTERDAY, true,
                Collections.<NotificationItem.QuickAction>emptyList()));

        list.add(new NotificationItem("a8", "system", R.drawable.ic_settings,
                ctx.getString(R.string.notif_a8_title),
                ctx.getString(R.string.notif_a8_body),
                ctx.getString(R.string.notif_a8_when), NotificationItem.Group.EARLIER, true,
                Collections.<NotificationItem.QuickAction>emptyList()));

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
