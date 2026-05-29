package com.example.tourgo.ui.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.tourgo.R;
import com.google.android.material.button.MaterialButton;

/** Small UI helpers shared across the Admin Console screens. */
public final class AdminUi {

    private AdminUi() {}

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    /** Renders an initials avatar into a TextView with a per-name pastel hue (matches the
     *  prototype's MiniAvatar oklch hue trick). */
    public static void avatar(TextView tv, String name) {
        if (name == null || name.isEmpty()) name = "?";
        StringBuilder initials = new StringBuilder();
        String[] parts = name.trim().split("\\s+");
        for (int i = 0; i < parts.length && initials.length() < 2; i++) {
            if (!parts[i].isEmpty()) initials.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        int hue = ((name.charAt(0) * 19 + name.charAt(name.length() - 1) * 7) % 360 + 360) % 360;
        int color = Color.HSVToColor(new float[]{hue, 0.42f, 0.80f});
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        tv.setBackground(g);
        tv.setText(initials.toString());
        tv.setTextColor(Color.WHITE);
    }

    /** Centered confirm dialog with a danger/primary variant — mirrors the prototype's Confirm. */
    public static void confirm(Context ctx, CharSequence title, CharSequence message,
                               CharSequence confirmLabel, boolean danger, Runnable onConfirm) {
        Dialog d = new Dialog(ctx);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_admin_confirm);
        Window window = d.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int margin = dp(ctx, 28);
            int width = ctx.getResources().getDisplayMetrics().widthPixels - 2 * margin;
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        FrameLayout iconWrap = d.findViewById(R.id.admIconWrap);
        ImageView icon = d.findViewById(R.id.admIcon);
        iconWrap.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(ctx, danger ? R.color.adm_red_100 : R.color.adm_blue_50)));
        icon.setImageResource(danger ? R.drawable.ic_alert_triangle : R.drawable.ic_help_circle);
        icon.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(ctx, danger ? R.color.adm_red_500 : R.color.adm_blue_500)));

        ((TextView) d.findViewById(R.id.admTitle)).setText(title);
        ((TextView) d.findViewById(R.id.admMessage)).setText(message);

        MaterialButton cancel = d.findViewById(R.id.admCancel);
        MaterialButton confirm = d.findViewById(R.id.admConfirm);
        confirm.setText(confirmLabel);
        confirm.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(ctx, danger ? R.color.adm_red_500 : R.color.adm_gray_900)));
        cancel.setOnClickListener(v -> d.dismiss());
        confirm.setOnClickListener(v -> {
            d.dismiss();
            if (onConfirm != null) onConfirm.run();
        });
        d.show();
    }

    /** Toggles a View's visibility helper. */
    public static void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /** Styles a category chip (hotel / tour / special) with its tint + label. */
    public static void catChip(Context ctx, View chip, View dot, TextView label, String kind) {
        int bg, ink, solid;
        CharSequence text;
        switch (kind) {
            case "tour":
                bg = R.color.adm_teal_100; ink = R.color.adm_teal_ink; solid = R.color.adm_teal_500;
                text = ctx.getString(R.string.home_category_tour);
                break;
            case "special":
                bg = R.color.adm_purple_100; ink = R.color.adm_purple_ink; solid = R.color.adm_purple_500;
                text = "Premium";
                break;
            case "hotel":
            default:
                bg = R.color.adm_amber_100; ink = R.color.adm_orange_ink; solid = R.color.adm_amber_500;
                text = ctx.getString(R.string.home_category_hotel);
                break;
        }
        chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, bg)));
        dot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, solid)));
        label.setTextColor(ContextCompat.getColor(ctx, ink));
        label.setText(text);
    }
}
