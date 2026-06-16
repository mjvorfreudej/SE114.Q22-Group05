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

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.ui.notification.NotificationItem;
import com.example.tourgo.ui.notification.NotificationMockData;
import com.example.tourgo.ui.notification.NotificationPopover;

/** Admin › Home dashboard — KPI tiles, critical alert, quick actions, recent activity. */
public class AdminHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        setupBell(v);

        // Critical alert
        ((TextView) v.findViewById(R.id.admAlertTitle)).setText(getString(R.string.adm_alert_title, 23));
        ((TextView) v.findViewById(R.id.admAlertSub)).setText(getString(R.string.adm_alert_sub, 17, 6));
        v.findViewById(R.id.admBtnOpenMod).setOnClickListener(view -> goTab(AdminActivity.TAB_MODERATION));

        // KPI tiles (label, value, icon, accent ink, soft bg, optional +delta)
        stat(v, R.id.admStat1, R.drawable.ic_users, R.color.adm_blue_700, R.color.adm_blue_50, "12.4k", R.string.adm_stat_users, "+312");
        stat(v, R.id.admStat2, R.drawable.ic_building, R.color.adm_teal_ink, R.color.adm_teal_50, "184", R.string.adm_stat_biz, "+8");
        stat(v, R.id.admStat3, R.drawable.ic_map, R.color.adm_purple_ink, R.color.adm_purple_50, "1268", R.string.adm_stat_listings, "+34");
        stat(v, R.id.admStat4, R.drawable.ic_gavel, R.color.adm_orange_ink, R.color.adm_orange_50, "17", R.string.adm_stat_pending, null);
        stat(v, R.id.admStat5, R.drawable.ic_flag, R.color.adm_red_700, R.color.adm_red_100, "6", R.string.adm_stat_reports, null);
        stat(v, R.id.admStat6, R.drawable.ic_shield_x, R.color.adm_red_700, R.color.adm_red_100, "3", R.string.adm_stat_flagged, null);

        // Quick actions (cat hotel=amber, special=purple, tour=teal)
        action(v, R.id.admActListings, R.drawable.ic_gavel, R.color.adm_amber_100, R.color.adm_amber_500,
                R.string.adm_qa_review_listings, getString(R.string.adm_qa_review_listings_sub, 17), 17, AdminActivity.TAB_MODERATION);
        action(v, R.id.admActReports, R.drawable.ic_flag, R.color.adm_purple_100, R.color.adm_purple_500,
                R.string.adm_qa_review_reports, getString(R.string.adm_qa_review_reports_sub, 6), 6, AdminActivity.TAB_MODERATION);
        action(v, R.id.admActUsers, R.drawable.ic_users, R.color.adm_teal_100, R.color.adm_teal_500,
                R.string.adm_qa_browse_users, getString(R.string.adm_qa_browse_users_sub, "12,438"), 0, AdminActivity.TAB_USERS);

        // Recent activity
        LinearLayout list = v.findViewById(R.id.admActivityList);
        LayoutInflater inf = LayoutInflater.from(requireContext());
        addActivity(inf, list, R.drawable.ic_building, R.color.adm_purple_100, R.color.adm_purple_500,
                span("Skyline Marina", " registered as a new business", true), "Awaiting approval · 7 listings", "2m", false);
        addActivity(inf, list, R.drawable.ic_map, R.color.adm_amber_100, R.color.adm_amber_500,
                span("The Grand Orchid Resort", " submitted a new listing", true), "Hotel · Bangkok, Thailand", "24m", false);
        addActivity(inf, list, R.drawable.ic_flag, R.color.adm_red_100, R.color.adm_red_500,
                spanMid("New report on ", "guest_jay_88", " for toxic language"), "Filed by Cedar Cove LLC", "1h", false);
        addActivity(inf, list, R.drawable.ic_map, R.color.adm_teal_100, R.color.adm_teal_500,
                span("Riverbend Tours Co.", " submitted a new listing", true), "Tour · Kyoto, Japan", "3h", false);
        addActivity(inf, list, R.drawable.ic_users, R.color.adm_purple_100, R.color.adm_purple_500,
                new SpannableStringBuilder("14 new traveler accounts created"), "Last 24 hours", "6h", true);
    }

    private void goTab(int tab) {
        if (getActivity() instanceof AdminActivity) ((AdminActivity) getActivity()).goToTab(tab);
    }

    /**
     * Header bell → Admin notification popover (design index.html, role="admin").
     * The red count badge shows the initial unread tally; the popover and full center
     * hold their own optimistic state, mirroring the prototype (no notifications API yet).
     */
    private void setupBell(View root) {
        View bell = root.findViewById(R.id.admBellBtn);
        TextView badge = root.findViewById(R.id.admBellBadge);

        int unread = 0;
        for (NotificationItem n : NotificationMockData.seed(requireContext(), NotificationMockData.Role.ADMIN)) {
            if (!n.read) unread++;
        }
        if (unread > 0) {
            badge.setText(unread > 9 ? "9+" : String.valueOf(unread));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }

        bell.setOnClickListener(v -> NotificationPopover.show(v, NotificationMockData.Role.ADMIN));
    }

    private void stat(View root, int id, int iconRes, @ColorRes int accent, @ColorRes int soft,
                      String value, int labelRes, @Nullable String delta) {
        View tile = root.findViewById(id);
        tile.findViewById(R.id.admStatIconWrap).setBackgroundTintList(ColorStateList.valueOf(color(soft)));
        ImageView icon = tile.findViewById(R.id.admStatIcon);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(color(accent)));
        ((TextView) tile.findViewById(R.id.admStatValue)).setText(value);
        ((TextView) tile.findViewById(R.id.admStatLabel)).setText(labelRes);
        TextView d = tile.findViewById(R.id.admStatDelta);
        if (delta != null) {
            d.setText(delta);
            d.setVisibility(View.VISIBLE);
        }
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

    private static SpannableStringBuilder spanMid(String before, String bold, String after) {
        SpannableStringBuilder sb = new SpannableStringBuilder(before);
        int start = sb.length();
        sb.append(bold);
        sb.setSpan(new StyleSpan(Typeface.BOLD), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(after);
        return sb;
    }
}
