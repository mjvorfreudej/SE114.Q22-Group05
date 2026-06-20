package com.example.tourgo.ui.notification;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the date-grouped notification feed (design NotifCenter body):
 * a flat list of group headers + notification rows. Mirrors the prototype's
 * {@code buildGroups(items, 'date')} — Today / Yesterday / Earlier, in order,
 * with the bottom divider suppressed on each group's last row.
 *
 * Role-aware: the {@link NotificationMockData.Role} fixes the category palette
 * used for the disc + tag colours. Business/Admin rows additionally show a
 * category tag pill before the quick actions (design NotifRow, "center" variant,
 * comfortable density); the Traveler center omits it, matching traveler.html.
 */
public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnNotificationClick {
        void onClick(NotificationItem item);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    /** One visual line in the list: either a group header or a notification. */
    private static class Row {
        final boolean header;
        @StringRes final int headerLabel;
        final NotificationItem item;
        boolean lastInGroup;

        Row(@StringRes int headerLabel) {
            this.header = true;
            this.headerLabel = headerLabel;
            this.item = null;
        }

        Row(NotificationItem item) {
            this.header = false;
            this.headerLabel = 0;
            this.item = item;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnNotificationClick listener;
    private final NotificationMockData.Role role;
    /** Category tag pill is part of the Business/Admin center only (design parity). */
    private final boolean showCategoryTag;

    public NotificationAdapter(OnNotificationClick listener, NotificationMockData.Role role) {
        this.listener = listener;
        this.role = role;
        this.showCategoryTag = role != NotificationMockData.Role.TRAVELER;
    }

    /** Rebuild the flat row list from an already-filtered set of notifications. */
    public void setItems(List<NotificationItem> filtered) {
        rows.clear();
        for (NotificationItem.Group group : NotificationItem.Group.values()) {
            List<NotificationItem> inGroup = new ArrayList<>();
            for (NotificationItem n : filtered) {
                if (n.group == group) inGroup.add(n);
            }
            if (inGroup.isEmpty()) continue;
            rows.add(new Row(NotificationMockData.groupLabel(group)));
            for (int i = 0; i < inGroup.size(); i++) {
                Row r = new Row(inGroup.get(i));
                r.lastInGroup = (i == inGroup.size() - 1);
                rows.add(r);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).header ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(inf.inflate(R.layout.item_notification_header, parent, false));
        }
        return new ItemVH(inf.inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).label.setText(row.headerLabel);
        } else {
            ((ItemVH) holder).bind(row.item, row.lastInGroup);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    // ── View holders ─────────────────────────────────────────────────────────
    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView label;
        HeaderVH(@NonNull View v) {
            super(v);
            label = (TextView) v;
            label.setTypeface(NotifFonts.get(v.getContext(), NotifFonts.Weight.BOLD));
        }
    }

    class ItemVH extends RecyclerView.ViewHolder {
        final View iconDisc;
        final ImageView icon;
        final TextView title, when, body;
        final LinearLayout actions;
        final View unreadDot, divider;

        ItemVH(@NonNull View v) {
            super(v);
            iconDisc = v.findViewById(R.id.notifIconDisc);
            icon = v.findViewById(R.id.ivNotifIcon);
            title = v.findViewById(R.id.tvNotifTitle);
            when = v.findViewById(R.id.tvNotifWhen);
            body = v.findViewById(R.id.tvNotifBody);
            actions = v.findViewById(R.id.notifActions);
            unreadDot = v.findViewById(R.id.notifUnreadDot);
            divider = v.findViewById(R.id.notifDivider);
        }

        void bind(NotificationItem n, boolean lastInGroup) {
            Context ctx = itemView.getContext();
            NotificationMockData.Category cat = NotificationMockData.category(role, n.cat);

            // Category disc: tinted background + tinted glyph.
            iconDisc.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, cat.bgColor)));
            icon.setImageResource(n.iconRes);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(ContextCompat.getColor(ctx, cat.solidColor)));

            // Title weight tracks read state (read = 600, unread = 700).
            title.setText(n.title);
            title.setTypeface(NotifFonts.get(ctx, n.read ? NotifFonts.Weight.SEMIBOLD : NotifFonts.Weight.BOLD));
            when.setText(n.when);
            when.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.MEDIUM));
            body.setText(n.body);
            body.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.REGULAR));

            // Unread tint + dot.
            itemView.setBackgroundColor(n.read ? Color.TRANSPARENT
                    : ContextCompat.getColor(ctx, R.color.notif_unread_tint));
            unreadDot.setVisibility(n.read ? View.GONE : View.VISIBLE);
            divider.setVisibility(lastInGroup ? View.GONE : View.VISIBLE);

            bindTagAndActions(n, cat);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(n);
            });
        }

        /**
         * Builds the bottom row: an optional category tag pill (Business/Admin)
         * followed by the quick-action pills. The row is shown when it carries a
         * tag or at least one action — design NotifRow's
         * {@code (showActs || (!inPop && !compact))} condition.
         */
        private void bindTagAndActions(NotificationItem n, NotificationMockData.Category cat) {
            actions.removeAllViews();
            Context ctx = actions.getContext();

            if (showCategoryTag) {
                actions.addView(buildCategoryTag(ctx, cat));
            }

            boolean hasActions = n.actions != null && !n.actions.isEmpty();
            if (hasActions) {
                for (int i = 0; i < n.actions.size(); i++) {
                    NotificationItem.QuickAction a = n.actions.get(i);
                    actions.addView(buildActionPill(ctx, a, actions.getChildCount() > 0, n));
                }
            }

            actions.setVisibility(actions.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        }

        private View buildCategoryTag(Context ctx, NotificationMockData.Category cat) {
            LinearLayout tag = new LinearLayout(ctx);
            tag.setOrientation(LinearLayout.HORIZONTAL);
            tag.setGravity(Gravity.CENTER);
            tag.setBackgroundResource(R.drawable.bg_adm_pill);
            tag.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, cat.bgColor)));
            tag.setPadding(dp(ctx, 12), 0, dp(ctx, 12), 0);

            View dot = new View(ctx);
            dot.setBackgroundResource(R.drawable.bg_adm_circle);
            dot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, cat.solidColor)));
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(ctx, 4), dp(ctx, 4));
            dlp.setMarginEnd(dp(ctx, 4));
            dot.setLayoutParams(dlp);
            tag.addView(dot);

            TextView label = new TextView(ctx);
            label.setText(cat.labelRes);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
            label.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.BOLD));
            label.setIncludeFontPadding(false);
            label.setTextColor(ContextCompat.getColor(ctx, cat.inkColor));
            tag.addView(label);

            // Match the action-pill height (36dp) so the tag and quick actions sit
            // on one even row instead of a short tag beside a taller button.
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            dp(ctx, 36));
            lp.gravity = Gravity.CENTER_VERTICAL;
            tag.setLayoutParams(lp);

            return tag;
        }

        private TextView buildActionPill(Context ctx, NotificationItem.QuickAction a,
                                         boolean leadingGap, NotificationItem n) {
            TextView b = new TextView(ctx);
            b.setText(a.label);
            b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            b.setTypeface(NotifFonts.get(ctx, NotifFonts.Weight.BOLD));
            b.setIncludeFontPadding(false);
            b.setGravity(Gravity.CENTER);
            int padH = dp(ctx, 12), padV = dp(ctx, 8);
            b.setPadding(padH, padV, padH, padV);

            if (a.primary) {
                b.setBackgroundResource(R.drawable.bg_adm_pill);
                b.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.adm_gray_900)));
                b.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            } else {
                b.setBackgroundResource(R.drawable.bg_notif_action_secondary);
                b.setTextColor(ContextCompat.getColor(ctx, R.color.adm_gray_700));
            }

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            dp(ctx, 36));
            lp.gravity = Gravity.CENTER_VERTICAL;
            if (leadingGap) lp.setMarginStart(dp(ctx, 7));
            b.setLayoutParams(lp);

            // Tapping a quick action marks the notification read (design behaviour).
            b.setOnClickListener(v -> {
                if (listener != null) listener.onClick(n);
            });
            return b;
        }
    }

    private static int dp(Context ctx, float v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }
}
