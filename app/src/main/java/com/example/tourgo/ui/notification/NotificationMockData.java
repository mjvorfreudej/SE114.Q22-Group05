package com.example.tourgo.ui.notification;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.example.tourgo.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Role-keyed UI configuration for the notification surfaces (bell → popover →
 * center), shared across Traveler, Business and Admin.
 *
 * <p>The actual notification rows are no longer seeded locally — every surface is
 * served by the live backend feed through
 * {@link com.example.tourgo.remote.service.NotificationService}
 * ({@code GET /api/notifications}). What remains here is purely presentation:
 * the per-role category palettes ({@code NOTIF_CATS}), the filter chips
 * ({@code NOTIF_FILTERS}) and the date-group labels — all keyed by {@link Role},
 * matching the design's role-parameterised model. The class keeps its historical
 * name so the many {@code NotificationMockData.Role/Category/Filter} references
 * across the UI stay stable.
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

    @StringRes
    public static int groupLabel(NotificationItem.Group group) {
        switch (group) {
            case TODAY:     return R.string.notif_group_today;
            case YESTERDAY: return R.string.notif_group_yesterday;
            default:        return R.string.notif_group_earlier;
        }
    }
}
