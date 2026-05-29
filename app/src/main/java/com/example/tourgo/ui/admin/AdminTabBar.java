package com.example.tourgo.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.tourgo.R;

import java.util.List;

/** Segmented pill tab strip used by Moderation / Businesses / Users (mirrors TopTabs). */
public final class AdminTabBar {

    public static final int NO_COUNT = Integer.MIN_VALUE;

    public interface OnTabSelected {
        void onTab(String id);
    }

    public static class Tab {
        public final String id;
        public final String label;
        public final int count;

        public Tab(String id, String label, int count) {
            this.id = id;
            this.label = label;
            this.count = count;
        }
    }

    private AdminTabBar() {}

    public static void build(LinearLayout container, List<Tab> tabs, String active, OnTabSelected cb) {
        container.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(container.getContext());
        for (Tab t : tabs) {
            View v = inf.inflate(R.layout.item_admin_tab, container, false);
            v.setTag(t.id);
            ((TextView) v.findViewById(R.id.admTabLabel)).setText(t.label);
            TextView count = v.findViewById(R.id.admTabCount);
            if (t.count != NO_COUNT) {
                count.setText(String.valueOf(t.count));
                count.setVisibility(View.VISIBLE);
            }
            v.setOnClickListener(view -> {
                style(container, t.id);
                if (cb != null) cb.onTab(t.id);
            });
            container.addView(v);
        }
        style(container, active);
    }

    private static void style(LinearLayout container, String selectedId) {
        Context ctx = container.getContext();
        int dark = ContextCompat.getColor(ctx, R.color.adm_gray_900);
        int idleBg = ContextCompat.getColor(ctx, R.color.adm_gray_100);
        int white = ContextCompat.getColor(ctx, R.color.white);
        int idleLabel = ContextCompat.getColor(ctx, R.color.adm_gray_700);
        int idleCount = ContextCompat.getColor(ctx, R.color.adm_gray_500);
        int overlay = 0x33FFFFFF; // translucent white for selected count chip

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            boolean sel = selectedId.equals(v.getTag());
            v.setBackgroundTintList(ColorStateList.valueOf(sel ? dark : idleBg));
            TextView label = v.findViewById(R.id.admTabLabel);
            TextView count = v.findViewById(R.id.admTabCount);
            label.setTextColor(sel ? white : idleLabel);
            count.setBackgroundTintList(ColorStateList.valueOf(sel ? overlay : white));
            count.setTextColor(sel ? white : idleCount);
        }
    }
}
