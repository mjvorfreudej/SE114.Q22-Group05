package com.example.tourgo.ui.notification;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tourgo.R;
import com.example.tourgo.databinding.ActivityNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Traveler notification center — the canonical full-screen surface from
 * ui_kits/notifications/traveler.html. App bar (back · title + unread count ·
 * overflow) over a filter chip row over a date-grouped feed, with loading,
 * empty and unread states. The bell-anchored popover in the prototype is a
 * web hover pattern; on Android the bell opens this screen directly.
 *
 * No notifications API exists yet, so state is local + optimistic (mark-as-read
 * on tap, mark-all-read from the overflow menu), exactly as the prototype behaves.
 */
public class NotificationsActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClick {

    private static final int SKELETON_ROWS = 6;
    private static final long SIMULATED_LOAD_MS = 500L;
    /** Count-badge fill when a chip is selected: 20% white over the dark pill. */
    private static final int CHIP_COUNT_SELECTED_BG = 0x33FFFFFF;

    private ActivityNotificationsBinding binding;
    private NotificationAdapter adapter;
    private final List<NotificationItem> items = new ArrayList<>();
    private String currentFilter = "all";
    private boolean loading = true;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyInsets();
        applyLightStatusBar();
        applyStaticFonts();

        items.clear();
        items.addAll(NotificationMockData.seed(this));

        adapter = new NotificationAdapter(this);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);

        binding.btnNotifBack.setOnClickListener(v -> finish());
        binding.btnNotifMenu.setOnClickListener(this::showOverflowMenu);

        buildSkeleton();
        renderFilters();
        render();

        // Brief skeleton pass so the loading state is visible (no real API yet).
        handler.postDelayed(() -> {
            if (binding == null) return;
            loading = false;
            render();
        }, SIMULATED_LOAD_MS);
    }

    // ── System bars ──────────────────────────────────────────────────────────
    private void applyInsets() {
        final int appBarTop = binding.notifAppBar.getPaddingTop();
        final int listBottom = binding.rvNotifications.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.notifAppBar.setPadding(
                    binding.notifAppBar.getPaddingLeft(), appBarTop + bars.top,
                    binding.notifAppBar.getPaddingRight(), binding.notifAppBar.getPaddingBottom());
            binding.rvNotifications.setPadding(
                    binding.rvNotifications.getPaddingLeft(), binding.rvNotifications.getPaddingTop(),
                    binding.rvNotifications.getPaddingRight(), listBottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());
    }

    private void applyLightStatusBar() {
        // White page → dark status-bar icons.
        WindowInsetsControllerCompat c =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (c != null) c.setAppearanceLightStatusBars(true);
    }

    /**
     * The app-bar title, unread subtitle and empty-state texts are declared in XML, so they
     * fall back to the theme's default sans-serif and never pass through {@link NotifFonts}.
     * Route them through it here so the whole screen follows the active language: Urbanist in
     * English, system sans-serif in Vietnamese (Urbanist lacks Vietnamese glyphs). The locale
     * is fixed for the activity's lifetime — a language switch recreates it and re-runs this.
     */
    private void applyStaticFonts() {
        binding.tvNotifTitle.setTypeface(NotifFonts.get(this, NotifFonts.Weight.BOLD));
        binding.tvNotifUnread.setTypeface(NotifFonts.get(this, NotifFonts.Weight.MEDIUM));
        binding.tvEmptyTitle.setTypeface(NotifFonts.get(this, NotifFonts.Weight.SEMIBOLD));
        binding.tvEmptyMessage.setTypeface(NotifFonts.get(this, NotifFonts.Weight.REGULAR));
    }

    // ── State machine ─────────────────────────────────────────────────────────
    @Override
    public void onClick(NotificationItem item) {
        if (item.read) return;
        item.read = true;
        renderFilters();
        render();
    }

    private void markAllRead() {
        for (NotificationItem n : items) n.read = true;
        Toast.makeText(this, R.string.notif_toast_marked_all, Toast.LENGTH_SHORT).show();
        renderFilters();
        render();
    }

    private int unreadCount() {
        int u = 0;
        for (NotificationItem n : items) if (!n.read) u++;
        return u;
    }

    private List<NotificationItem> filtered() {
        List<NotificationItem> out = new ArrayList<>();
        for (NotificationItem n : items) {
            if ("all".equals(currentFilter)) {
                out.add(n);
            } else if ("unread".equals(currentFilter)) {
                if (!n.read) out.add(n);
            } else if (currentFilter.equals(n.cat)) {
                out.add(n);
            }
        }
        return out;
    }

    private void render() {
        if (binding == null) return;

        // Unread subtitle.
        int unread = unreadCount();
        if (unread > 0) {
            binding.tvNotifUnread.setText(getString(R.string.notif_unread, unread));
            binding.tvNotifUnread.setVisibility(View.VISIBLE);
        } else {
            binding.tvNotifUnread.setVisibility(View.GONE);
        }

        if (loading) {
            binding.skeletonContainer.setVisibility(View.VISIBLE);
            binding.rvNotifications.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.GONE);
            return;
        }
        binding.skeletonContainer.setVisibility(View.GONE);

        List<NotificationItem> list = filtered();
        if (list.isEmpty()) {
            showEmpty();
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvNotifications.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE);
            adapter.setItems(list);
        }
    }

    private void showEmpty() {
        boolean unreadFilter = "unread".equals(currentFilter);
        binding.ivEmptyIcon.setImageResource(unreadFilter ? R.drawable.ic_check_check : R.drawable.ic_inbox);
        binding.tvEmptyTitle.setText(unreadFilter ? R.string.notif_empty_unread_title : R.string.notif_empty_all_title);
        binding.tvEmptyMessage.setText(unreadFilter ? R.string.notif_empty_unread_msg : R.string.notif_empty_all_msg);
    }

    // ── Filter chips ───────────────────────────────────────────────────────────
    private void renderFilters() {
        LinearLayout row = binding.rowNotifFilters;
        row.removeAllViews();
        for (NotificationMockData.Filter f : NotificationMockData.FILTERS) {
            row.addView(buildChip(f, countFor(f), currentFilter.equals(f.id)));
        }
    }

    private int countFor(NotificationMockData.Filter f) {
        if ("all".equals(f.id)) return items.size();
        if ("unread".equals(f.id)) return unreadCount();
        int c = 0;
        for (NotificationItem n : items) if (f.cat != null && f.cat.equals(n.cat)) c++;
        return c;
    }

    private View buildChip(NotificationMockData.Filter f, int count, boolean selected) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackgroundResource(R.drawable.bg_adm_pill);
        chip.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, selected ? R.color.adm_gray_900 : R.color.adm_gray_100)));
        chip.setPadding(dp(13), dp(8), dp(13), dp(8));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(7));
        chip.setLayoutParams(lp);

        TextView label = new TextView(this);
        label.setText(f.labelRes);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        label.setTypeface(NotifFonts.get(this, NotifFonts.Weight.BOLD));
        label.setIncludeFontPadding(false);
        label.setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.adm_gray_600));
        chip.addView(label);

        if (count > 0) {
            TextView badge = new TextView(this);
            badge.setText(String.valueOf(count));
            badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
            badge.setTypeface(NotifFonts.get(this, NotifFonts.Weight.BOLD));
            badge.setIncludeFontPadding(false);
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dp(6), dp(2), dp(6), dp(2));
            badge.setBackgroundResource(R.drawable.bg_adm_pill);
            badge.setBackgroundTintList(ColorStateList.valueOf(
                    selected ? CHIP_COUNT_SELECTED_BG : Color.WHITE));
            badge.setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.adm_gray_500));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            blp.setMarginStart(dp(6));
            badge.setLayoutParams(blp);
            chip.addView(badge);
        }

        chip.setOnClickListener(v -> {
            if (currentFilter.equals(f.id)) return;
            currentFilter = f.id;
            renderFilters();
            render();
        });
        return chip;
    }

    // ── Overflow menu ───────────────────────────────────────────────────────────
    private void showOverflowMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        Menu m = menu.getMenu();
        final int idMarkAll = 1;
        final int idSettings = 2;
        if (unreadCount() > 0) {
            m.add(Menu.NONE, idMarkAll, Menu.NONE, R.string.notif_mark_all_read);
        }
        m.add(Menu.NONE, idSettings, Menu.NONE, R.string.notif_settings);
        menu.setOnMenuItemClickListener(mi -> {
            if (mi.getItemId() == idMarkAll) {
                markAllRead();
                return true;
            }
            // Settings: no destination yet — close silently (prototype parity).
            return true;
        });
        menu.show();
    }

    // ── Loading skeleton ──────────────────────────────────────────────────────────
    private void buildSkeleton() {
        LayoutInflater inf = getLayoutInflater();
        binding.skeletonContainer.removeAllViews();
        for (int i = 0; i < SKELETON_ROWS; i++) {
            binding.skeletonContainer.addView(
                    inf.inflate(R.layout.item_notification_skeleton, binding.skeletonContainer, false));
        }
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
        binding = null;
    }
}
