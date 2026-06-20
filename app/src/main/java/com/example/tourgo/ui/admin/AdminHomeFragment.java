package com.example.tourgo.ui.admin;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.AdminAccount;
import com.example.tourgo.models.response.AdminActivityItem;
import com.example.tourgo.models.response.AdminStats;
import com.example.tourgo.remote.service.AdminService;
import com.example.tourgo.ui.notification.NotificationMockData;
import com.example.tourgo.ui.notification.NotificationPopover;

import java.text.NumberFormat;
import java.util.List;

/** Admin › Home dashboard — KPI tiles, critical alert, quick actions, recent activity (live). */
public class AdminHomeFragment extends Fragment {

    private View root;
    /** Live user total, counted from the same list the Users directory shows. */
    private Integer userCount = null;
    private TextView bellBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        root = v;

        setupBell(v);

        v.findViewById(R.id.admBtnOpenMod).setOnClickListener(view -> goTab(AdminActivity.TAB_MODERATION));

        // KPI tile chrome (icon + label). Values fill in from GET /api/admin/stats.
        stat(v, R.id.admStat1, R.drawable.ic_users, R.color.adm_blue_700, R.color.adm_blue_50, "—", R.string.adm_stat_users);
        stat(v, R.id.admStat2, R.drawable.ic_building, R.color.adm_teal_ink, R.color.adm_teal_50, "—", R.string.adm_stat_biz);
        stat(v, R.id.admStat3, R.drawable.ic_map, R.color.adm_purple_ink, R.color.adm_purple_50, "—", R.string.adm_stat_listings);
        stat(v, R.id.admStat4, R.drawable.ic_gavel, R.color.adm_orange_ink, R.color.adm_orange_50, "—", R.string.adm_stat_pending);
        stat(v, R.id.admStat5, R.drawable.ic_flag, R.color.adm_red_700, R.color.adm_red_100, "—", R.string.adm_stat_reports);
        stat(v, R.id.admStat6, R.drawable.ic_shield_x, R.color.adm_red_700, R.color.adm_red_100, "—", R.string.adm_stat_flagged);

        // Quick actions chrome (subtitle + badge fill in from stats).
        action(v, R.id.admActListings, R.drawable.ic_gavel, R.color.adm_amber_100, R.color.adm_amber_500,
                R.string.adm_qa_review_listings, "", 0, AdminActivity.TAB_MODERATION);
        action(v, R.id.admActReports, R.drawable.ic_flag, R.color.adm_purple_100, R.color.adm_purple_500,
                R.string.adm_qa_review_reports, "", 0, AdminActivity.TAB_MODERATION);
        action(v, R.id.admActUsers, R.drawable.ic_users, R.color.adm_teal_100, R.color.adm_teal_500,
                R.string.adm_qa_browse_users, "", 0, AdminActivity.TAB_USERS);

        loadStats();
        loadActivity();
        loadUserCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Keep the dashboard in sync after approvals / suspensions elsewhere.
        loadStats();
        loadActivity();
        loadUserCount();
        // Reflect any notifications read since we were last shown.
        refreshBellBadge();
    }

    // ── Live data ─────────────────────────────────────────────────────────────
    private void loadStats() {
        AdminService.getStats(requireContext(), new DataCallback<AdminStats>() {
            @Override
            public void onSuccess(AdminStats s) {
                if (!isAdded() || root == null || s == null) return;

                stat(root, R.id.admStat1, R.drawable.ic_users, R.color.adm_blue_700, R.color.adm_blue_50, fmt(s.getUsers()), R.string.adm_stat_users);
                stat(root, R.id.admStat2, R.drawable.ic_building, R.color.adm_teal_ink, R.color.adm_teal_50, fmt(s.getBusinesses()), R.string.adm_stat_biz);
                stat(root, R.id.admStat3, R.drawable.ic_map, R.color.adm_purple_ink, R.color.adm_purple_50, fmt(s.getListings()), R.string.adm_stat_listings);
                stat(root, R.id.admStat4, R.drawable.ic_gavel, R.color.adm_orange_ink, R.color.adm_orange_50, fmt(s.getPendingListings()), R.string.adm_stat_pending);
                stat(root, R.id.admStat5, R.drawable.ic_flag, R.color.adm_red_700, R.color.adm_red_100, fmt(s.getReports()), R.string.adm_stat_reports);
                stat(root, R.id.admStat6, R.drawable.ic_shield_x, R.color.adm_red_700, R.color.adm_red_100, fmt(s.getFlaggedUsers()), R.string.adm_stat_flagged);

                ((TextView) root.findViewById(R.id.admAlertTitle))
                        .setText(getString(R.string.adm_alert_title, s.getQueueTotal()));
                ((TextView) root.findViewById(R.id.admAlertSub))
                        .setText(getString(R.string.adm_alert_sub, s.getPendingListings(), s.getReports()));

                action(root, R.id.admActListings, R.drawable.ic_gavel, R.color.adm_amber_100, R.color.adm_amber_500,
                        R.string.adm_qa_review_listings, getString(R.string.adm_qa_review_listings_sub, s.getPendingListings()),
                        s.getPendingListings(), AdminActivity.TAB_MODERATION);
                action(root, R.id.admActReports, R.drawable.ic_flag, R.color.adm_purple_100, R.color.adm_purple_500,
                        R.string.adm_qa_review_reports, getString(R.string.adm_qa_review_reports_sub, s.getReports()),
                        s.getReports(), AdminActivity.TAB_MODERATION);
                action(root, R.id.admActUsers, R.drawable.ic_users, R.color.adm_teal_100, R.color.adm_teal_500,
                        R.string.adm_qa_browse_users, getString(R.string.adm_qa_browse_users_sub, fmt(s.getUsers())),
                        0, AdminActivity.TAB_USERS);

                // The live count wins over the stats aggregate when it has arrived,
                // so the tile + subtitle always match the Users directory.
                applyUserCount();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * The dashboard's "total users" must match the Users directory exactly, so we
     * count the same live list the directory loads rather than trusting the stats
     * aggregate (which can lag behind or count a different subset).
     */
    private void loadUserCount() {
        AdminService.getUsers(requireContext(), new DataCallback<List<AdminAccount>>() {
            @Override
            public void onSuccess(List<AdminAccount> data) {
                if (!isAdded() || root == null) return;
                userCount = data != null ? data.size() : 0;
                applyUserCount();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Leave the stats-derived value in place on failure.
            }
        });
    }

    /** Push the live user count onto the KPI tile + "browse users" subtitle. */
    private void applyUserCount() {
        if (userCount == null || !isAdded() || root == null) return;
        String v = fmt(userCount);
        ((TextView) root.findViewById(R.id.admStat1).findViewById(R.id.admStatValue)).setText(v);
        ((TextView) root.findViewById(R.id.admActUsers).findViewById(R.id.admActSub))
                .setText(getString(R.string.adm_qa_browse_users_sub, v));
    }

    private void loadActivity() {
        AdminService.getActivity(requireContext(), new DataCallback<List<AdminActivityItem>>() {
            @Override
            public void onSuccess(List<AdminActivityItem> items) {
                if (!isAdded() || root == null) return;
                LinearLayout list = root.findViewById(R.id.admActivityList);
                list.removeAllViews();
                if (items == null || items.isEmpty()) return;
                LayoutInflater inf = LayoutInflater.from(requireContext());
                for (int i = 0; i < items.size(); i++) {
                    AdminActivityItem a = items.get(i);
                    int icon = iconFor(a.getKind());
                    int soft = softFor(a.getKind());
                    int accent = accentFor(a.getKind());
                    addActivity(inf, list, icon, soft, accent, title(a), a.getMeta(),
                            relativeTime(a.getCreatedAt()), i == items.size() - 1);
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Leave the recent-activity section empty on failure.
            }
        });
    }

    private SpannableStringBuilder title(AdminActivityItem a) {
        return span(a.getBold(), a.getRest(), true);
    }

    private int iconFor(String kind) {
        switch (kind) {
            case "report": return R.drawable.ic_flag;
            case "tour":   return R.drawable.ic_map;
            case "user":   return R.drawable.ic_users;
            case "business":
            default:       return R.drawable.ic_building;
        }
    }

    @ColorRes
    private int softFor(String kind) {
        switch (kind) {
            case "report": return R.color.adm_red_100;
            case "tour":   return R.color.adm_teal_100;
            case "business":
            case "user":
            default:       return R.color.adm_purple_100;
        }
    }

    @ColorRes
    private int accentFor(String kind) {
        switch (kind) {
            case "report": return R.color.adm_red_500;
            case "tour":   return R.color.adm_teal_500;
            case "business":
            case "user":
            default:       return R.color.adm_purple_500;
        }
    }

    private String fmt(int n) {
        return NumberFormat.getInstance().format(n);
    }

    /**
     * Short relative time ("2m", "3h", "Yesterday", "5d") from an ISO timestamp.
     * Falls back to the raw date portion (or empty) when parsing fails.
     */
    private String relativeTime(String iso) {
        if (iso == null || iso.length() < 19) return iso != null && iso.length() >= 10 ? iso.substring(0, 10) : "";
        try {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            long then = f.parse(iso.substring(0, 19)).getTime();
            long diff = System.currentTimeMillis() - then;
            if (diff < 0) diff = 0;
            long min = diff / 60000;
            if (min < 1) return "now";
            if (min < 60) return min + "m";
            long hr = min / 60;
            if (hr < 24) return hr + "h";
            long day = hr / 24;
            if (day == 1) return getString(R.string.adm_time_yesterday);
            return day + "d";
        } catch (Exception e) {
            return iso.length() >= 10 ? iso.substring(0, 10) : "";
        }
    }

    private void goTab(int tab) {
        if (getActivity() instanceof AdminActivity) ((AdminActivity) getActivity()).goToTab(tab);
    }

    /**
     * Header bell → Admin notification popover (design index.html, role="admin").
     * The badge mirrors the notification center's unread count and is recomputed
     * on resume so it drops as notifications are read (and the read-state persists).
     */
    private void setupBell(View root) {
        View bell = root.findViewById(R.id.admBellBtn);
        bellBadge = root.findViewById(R.id.admBellBadge);
        bell.setOnClickListener(v -> NotificationPopover.show(v, NotificationMockData.Role.ADMIN));
        refreshBellBadge();
    }

    /** Bell badge = current unread admin notifications (shared, persisted state). */
    private void refreshBellBadge() {
        if (bellBadge == null || !isAdded()) return;
        applyBellBadge(NotificationMockData.unreadCount(requireContext(), NotificationMockData.Role.ADMIN));
    }

    private void applyBellBadge(int count) {
        if (bellBadge == null) return;
        if (count > 0) {
            bellBadge.setText(count > 9 ? "9+" : String.valueOf(count));
            bellBadge.setVisibility(View.VISIBLE);
        } else {
            bellBadge.setVisibility(View.GONE);
        }
    }

    private void stat(View root, int id, int iconRes, @ColorRes int accent, @ColorRes int soft,
                      String value, int labelRes) {
        View tile = root.findViewById(id);
        tile.findViewById(R.id.admStatIconWrap).setBackgroundTintList(ColorStateList.valueOf(color(soft)));
        ImageView icon = tile.findViewById(R.id.admStatIcon);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(color(accent)));
        ((TextView) tile.findViewById(R.id.admStatValue)).setText(value);
        ((TextView) tile.findViewById(R.id.admStatLabel)).setText(labelRes);
        View delta = tile.findViewById(R.id.admStatDelta);
        if (delta != null) delta.setVisibility(View.GONE);
    }

    private void action(View root, int id, int iconRes, @ColorRes int soft, @ColorRes int accent,
                        int titleRes, CharSequence sub, int badge, int targetTab) {
        View row = root.findViewById(id);
        row.findViewById(R.id.admActIconWrap).setBackgroundTintList(ColorStateList.valueOf(color(soft)));
        ImageView icon = row.findViewById(R.id.admActIcon);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(color(accent)));
        ((TextView) row.findViewById(R.id.admActTitle)).setText(titleRes);
        ((TextView) row.findViewById(R.id.admActSub)).setText(sub);
        TextView badgeView = row.findViewById(R.id.admActBadge);
        if (badge > 0) {
            badgeView.setText(String.valueOf(badge));
            badgeView.setVisibility(View.VISIBLE);
        } else {
            badgeView.setVisibility(View.GONE);
        }
        row.setOnClickListener(view -> goTab(targetTab));
    }

    private void addActivity(LayoutInflater inf, LinearLayout list, int iconRes, @ColorRes int soft,
                             @ColorRes int accent, CharSequence title, String meta, String when, boolean last) {
        View row = inf.inflate(R.layout.item_admin_activity, list, false);
        row.findViewById(R.id.admActivityIconWrap).setBackgroundTintList(ColorStateList.valueOf(color(soft)));
        ImageView icon = row.findViewById(R.id.admActivityIcon);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(color(accent)));
        ((TextView) row.findViewById(R.id.admActivityTitle)).setText(title);
        ((TextView) row.findViewById(R.id.admActivityMeta)).setText(meta);
        ((TextView) row.findViewById(R.id.admActivityWhen)).setText(when);
        if (last) row.findViewById(R.id.admActivityDivider).setVisibility(View.GONE);
        list.addView(row);
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }

    private static SpannableStringBuilder span(String bold, String rest, boolean boldFirst) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (boldFirst) {
            sb.append(bold);
            sb.setSpan(new StyleSpan(Typeface.BOLD), 0, bold.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append(rest);
        }
        return sb;
    }
}
