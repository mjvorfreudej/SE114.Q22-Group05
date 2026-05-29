package com.example.tourgo.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.ui.admin.AdminMockData.BizAccount;

import java.util.ArrayList;
import java.util.List;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.VH> {

    public interface Listener {
        void onAction(BizAccount biz, String action); // "suspend" | "reactivate"
    }

    public static final String ACTION_SUSPEND = "suspend";
    public static final String ACTION_REACTIVATE = "reactivate";
    public static final String ACTION_APPROVE = "approve";

    private final List<BizAccount> items = new ArrayList<>();
    private final Listener listener;

    public BusinessAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<BizAccount> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_business, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BizAccount b = items.get(position);
        Context ctx = h.itemView.getContext();
        String state = stateOf(b);
        boolean pending = "pending".equals(state);
        boolean suspended = "suspended".equals(state);

        h.itemView.setAlpha(suspended ? 0.85f : 1f);
        h.name.setText(b.name);
        h.owner.setText(b.owner + " · " + ctx.getString(R.string.adm_joined, b.joined));
        h.listings.setText(String.valueOf(b.listings));
        h.bookings.setText(String.valueOf(b.bookings));

        int wrapColor, iconColor, chipBg, chipFg, dot, label;
        if (pending) {
            wrapColor = R.color.adm_amber_100; iconColor = R.color.adm_amber_700;
            chipBg = R.color.adm_amber_100; chipFg = R.color.adm_amber_700; dot = R.color.adm_amber_500;
            label = R.string.adm_status_pending;
        } else if (suspended) {
            wrapColor = R.color.adm_red_100; iconColor = R.color.adm_red_700;
            chipBg = R.color.adm_red_100; chipFg = R.color.adm_red_700; dot = R.color.adm_red_500;
            label = R.string.adm_status_suspended;
        } else {
            wrapColor = R.color.adm_blue_50; iconColor = R.color.adm_blue_500;
            chipBg = R.color.adm_green_100; chipFg = R.color.adm_green_700; dot = R.color.adm_green_500;
            label = R.string.adm_status_active;
        }
        h.iconWrap.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, wrapColor)));
        h.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, iconColor)));
        h.statusChip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, chipBg)));
        h.statusDot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, dot)));
        h.statusLabel.setTextColor(ContextCompat.getColor(ctx, chipFg));
        h.statusLabel.setText(label);

        h.more.setOnClickListener(v -> showMenu(v, b));
    }

    /** Resolve the display state, preferring the explicit status over the legacy flag. */
    private static String stateOf(BizAccount b) {
        if (b.status != null && !b.status.isEmpty()) return b.status;
        return b.suspended ? "suspended" : "active";
    }

    private void showMenu(View anchor, BizAccount b) {
        Context ctx = anchor.getContext();
        String state = stateOf(b);

        // Primary action depends on the account state: pending → approve,
        // active → suspend, suspended → reactivate.
        final String actionLabel;
        final String action;
        if ("pending".equals(state)) {
            actionLabel = ctx.getString(R.string.adm_menu_approve);
            action = ACTION_APPROVE;
        } else if ("suspended".equals(state)) {
            actionLabel = ctx.getString(R.string.adm_reactivate);
            action = ACTION_REACTIVATE;
        } else {
            actionLabel = ctx.getString(R.string.adm_suspend);
            action = ACTION_SUSPEND;
        }

        PopupMenu menu = new PopupMenu(ctx, anchor);
        menu.getMenu().add(ctx.getString(R.string.adm_menu_view_profile));
        menu.getMenu().add(ctx.getString(R.string.adm_menu_view_listings));
        menu.getMenu().add(ctx.getString(R.string.adm_menu_view_reports));
        menu.getMenu().add(actionLabel);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getTitle() != null && item.getTitle().toString().equals(actionLabel)) {
                if (listener != null) listener.onAction(b, action);
            } else {
                Toast.makeText(ctx, item.getTitle(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        menu.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, owner, listings, bookings, statusLabel;
        final ImageView icon, more;
        final View iconWrap, statusDot;
        final LinearLayout statusChip;

        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.admBizName);
            owner = v.findViewById(R.id.admBizOwner);
            listings = v.findViewById(R.id.admBizListingsN);
            bookings = v.findViewById(R.id.admBizBookingsN);
            statusLabel = v.findViewById(R.id.admBizStatusLabel);
            icon = v.findViewById(R.id.admBizIcon);
            more = v.findViewById(R.id.admBizMore);
            iconWrap = v.findViewById(R.id.admBizIconWrap);
            statusDot = v.findViewById(R.id.admBizStatusDot);
            statusChip = v.findViewById(R.id.admBizStatus);
        }
    }
}
