package com.example.tourgo.ui.notification;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.tourgo.R;

import java.util.List;

/**
 * Bell-anchored notification popover (design: TravelerBell dropdown) — the first
 * step of the two-stage notifications flow. Tapping the home bell opens this
 * compact panel of the most recent notifications; its "View all notifications"
 * footer then opens the full {@link NotificationsActivity}.
 *
 * Like the rest of the notifications surface there is no API yet, so the popover
 * seeds its own local copy of {@link NotificationMockData} with optimistic
 * mark-as-read updates, mirroring the prototype's per-surface state.
 */
public final class NotificationPopover {

    /** Most recent rows shown in the compact panel; the rest live in the full center. */
    private static final int MAX_ROWS = 5;
    /** Count-badge / panel sizing. */
    private static final int MAX_WIDTH_DP = 360;
    private static final int SIDE_MARGIN_DP = 12;

    private NotificationPopover() {}

    /** Build and show the popover anchored below {@code anchor} (the bell button). */
    public static void show(View anchor) {
        Context ctx = anchor.getContext();
        LayoutInflater inf = LayoutInflater.from(ctx);
        View content = inf.inflate(R.layout.popup_notifications, null, false);

        final List<NotificationItem> items = NotificationMockData.seed(ctx);

        TextView headerTitle = content.findViewById(R.id.tvNotifPopHeaderTitle);
        TextView unread = content.findViewById(R.id.tvNotifPopUnread);
        TextView markAll = content.findViewById(R.id.btnNotifPopMarkAll);
        TextView viewAllLabel = content.findViewById(R.id.tvNotifPopViewAll);
        View viewAll = content.findViewById(R.id.btnNotifPopViewAll);
        TextView empty = content.findViewById(R.id.tvNotifPopEmpty);
        LinearLayout list = content.findViewById(R.id.notifPopList);

        // Static labels through NotifFonts so the panel follows the active language.
        headerTitle.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.BOLD));
        unread.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.MEDIUM));
        markAll.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.SEMIBOLD));
        viewAllLabel.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.BOLD));
        empty.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.MEDIUM));

        int width = popoverWidth(ctx);
        final PopupWindow popup = new PopupWindow(content, width,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // Transparent backing drawable enables outside-touch dismiss; elevation draws the shadow.
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(ctx, 16));

        // First render.
        bindUnread(ctx, unread, markAll, items);
        renderList(ctx, list, empty, items, popup);

        markAll.setOnClickListener(v -> {
            for (NotificationItem n : items) n.read = true;
            Toast.makeText(ctx, R.string.notif_toast_marked_all, Toast.LENGTH_SHORT).show();
            bindUnread(ctx, unread, markAll, items);
            renderList(ctx, list, empty, items, popup);
        });

        View.OnClickListener openCenter = v -> {
            popup.dismiss();
            ctx.startActivity(new Intent(ctx, NotificationsActivity.class));
        };
        viewAll.setOnClickListener(openCenter);

        // Drop below the bell, right edge aligned to the anchor.
        popup.showAsDropDown(anchor, 0, dp(ctx, 6), Gravity.END);
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private static void bindUnread(Context ctx, TextView unread, TextView markAll,
                                   List<NotificationItem> items) {
        int count = 0;
        for (NotificationItem n : items) if (!n.read) count++;
        if (count > 0) {
            unread.setText(ctx.getString(R.string.notif_unread, count));
            unread.setVisibility(View.VISIBLE);
            markAll.setVisibility(View.VISIBLE);
        } else {
            unread.setVisibility(View.GONE);
            markAll.setVisibility(View.GONE);
        }
    }

    // ── Recent feed ─────────────────────────────────────────────────────────
    private static void renderList(Context ctx, LinearLayout list, TextView empty,
                                   List<NotificationItem> items, PopupWindow popup) {
        list.removeAllViews();
        if (items.isEmpty()) {
            list.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            return;
        }
        empty.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);

        LayoutInflater inf = LayoutInflater.from(ctx);
        int rows = Math.min(MAX_ROWS, items.size());
        for (int i = 0; i < rows; i++) {
            NotificationItem n = items.get(i);
            View row = inf.inflate(R.layout.item_notif_popover, list, false);
            bindRow(ctx, row, n, i == rows - 1);
            // Tapping a row opens the full center (the popover holds no detail view).
            row.setOnClickListener(v -> {
                popup.dismiss();
                ctx.startActivity(new Intent(ctx, NotificationsActivity.class));
            });
            list.addView(row);
        }
    }

    private static void bindRow(Context ctx, View row, NotificationItem n, boolean last) {
        View disc = row.findViewById(R.id.notifPopIconDisc);
        ImageView icon = row.findViewById(R.id.ivNotifPopIcon);
        TextView title = row.findViewById(R.id.tvNotifPopTitle);
        TextView when = row.findViewById(R.id.tvNotifPopWhen);
        TextView body = row.findViewById(R.id.tvNotifPopBody);
        View dot = row.findViewById(R.id.notifPopUnreadDot);
        View divider = row.findViewById(R.id.notifPopDivider);

        NotificationMockData.Category cat = NotificationMockData.category(n.cat);
        disc.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, cat.bgColor)));
        icon.setImageResource(n.iconRes);
        ImageViewCompat.setImageTintList(icon,
                ColorStateList.valueOf(ContextCompat.getColor(ctx, cat.solidColor)));

        title.setText(n.title);
        title.setTypeface(NotifFonts.get(ctx, n.read ? NotifFonts.Weight.SEMIBOLD : NotifFonts.Weight.BOLD));
        when.setText(n.when);
        when.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.MEDIUM));
        body.setText(n.body);
        body.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.REGULAR));

        row.setBackgroundColor(n.read ? Color.TRANSPARENT
                : ContextCompat.getColor(ctx, R.color.notif_unread_tint));
        dot.setVisibility(n.read ? View.GONE : View.VISIBLE);
        divider.setVisibility(last ? View.GONE : View.VISIBLE);
    }

    // ── Sizing ──────────────────────────────────────────────────────────────
    private static int popoverWidth(Context ctx) {
        int screen = ctx.getResources().getDisplayMetrics().widthPixels;
        int max = dp(ctx, MAX_WIDTH_DP);
        int fit = screen - dp(ctx, SIDE_MARGIN_DP) * 2;
        return Math.min(max, fit);
    }

    private static int dp(Context ctx, float v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }
}
