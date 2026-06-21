package com.example.tourgo.ui.business;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.example.tourgo.R;

/**
 * Small UI helpers shared across the Business Console screens — chiefly the status
 * pills, which appear in many flavours (booking · listing · calendar occupancy).
 * Avatars and the centered confirm dialog reuse {@link com.example.tourgo.ui.admin.AdminUi}.
 */
public final class BizUi {

    private BizUi() {}

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /** Colour triple for a status pill: rounded background + text + leading dot. */
    public static final class Pill {
        @ColorRes public final int bg, fg, dot;
        @StringRes public final int label;

        Pill(int bg, int fg, int dot, int label) {
            this.bg = bg; this.fg = fg; this.dot = dot; this.label = label;
        }
    }

    /** Resolves the colour/label set for any status key used across the kit. */
    public static Pill pill(String status) {
        switch (status) {
            case "upcoming":
                return new Pill(R.color.adm_blue_50, R.color.adm_blue_700, R.color.adm_blue_500, R.string.biz_book_upcoming);
            case "completed":
                return new Pill(R.color.adm_green_100, R.color.adm_green_700, R.color.adm_green_500, R.string.biz_book_completed);
            case "cancelled":
                return new Pill(R.color.adm_red_100, R.color.adm_red_700, R.color.adm_red_500, R.string.biz_book_cancelled);
            case "inactive":
                return new Pill(R.color.adm_gray_200, R.color.adm_gray_600, R.color.adm_gray_400, R.string.biz_status_inactive_label);
            case "draft":
                return new Pill(R.color.adm_blue_50, R.color.adm_blue_700, R.color.adm_blue_500, R.string.biz_status_draft_label);
            case "confirmed":
                return new Pill(R.color.adm_blue_50, R.color.adm_blue_700, R.color.adm_blue_500, R.string.biz_status_confirmed);
            case "pending":
                return new Pill(R.color.adm_amber_100, R.color.adm_amber_700, R.color.adm_amber_500, R.string.biz_status_pending);
            case "checked-in":
                return new Pill(R.color.adm_green_100, R.color.adm_green_700, R.color.adm_green_500, R.string.biz_status_checkedin);
            case "checked-out":
                return new Pill(R.color.adm_gray_200, R.color.adm_gray_600, R.color.adm_gray_400, R.string.biz_status_checkedout);
            case "active":
            default:
                return new Pill(R.color.adm_green_100, R.color.adm_green_700, R.color.adm_green_500, R.string.biz_status_active_label);
        }
    }

    /**
     * Styles a status pill: a rounded container (bg tinted), a small leading dot (oval,
     * tinted), and the label TextView (text + colour). Any element may be null.
     */
    public static void status(Context ctx, View pill, View dot, TextView label, String status) {
        Pill p = pill(status);
        if (pill != null) pill.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, p.bg)));
        if (dot != null) dot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, p.dot)));
        if (label != null) {
            label.setTextColor(ContextCompat.getColor(ctx, p.fg));
            label.setText(p.label);
        }
    }
}
