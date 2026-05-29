package com.example.tourgo.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.ui.admin.AdminMockData.AdminUser;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.VH> {

    public interface Listener {
        void onOpen(AdminUser u);
    }

    private final List<AdminUser> items = new ArrayList<>();
    private final Listener listener;

    public AdminUserAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminUser> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminUser u = items.get(position);
        Context ctx = h.itemView.getContext();

        AdminUi.avatar(h.avatar, u.name);
        h.name.setText(u.name);
        h.email.setText(u.email);
        h.meta.setText(ctx.getString(R.string.adm_bookings_count, u.bookings)
                + " · " + ctx.getString(R.string.adm_joined, u.joined));

        if (u.reported > 0) {
            h.reported.setVisibility(View.VISIBLE);
            h.reportedN.setText(String.valueOf(u.reported));
        } else {
            h.reported.setVisibility(View.GONE);
        }

        applyStatus(ctx, h.status, u.status);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(u);
        });
    }

    static void applyStatus(Context ctx, TextView chip, String status) {
        int bg, fg, label;
        switch (status) {
            case "flagged":
                bg = R.color.adm_amber_100; fg = R.color.adm_amber_700; label = R.string.adm_status_flagged; break;
            case "suspended":
                bg = R.color.adm_red_100; fg = R.color.adm_red_700; label = R.string.adm_status_suspended; break;
            case "active":
            default:
                bg = R.color.adm_green_100; fg = R.color.adm_green_700; label = R.string.adm_status_active; break;
        }
        chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, bg)));
        chip.setTextColor(ContextCompat.getColor(ctx, fg));
        chip.setText(label);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView avatar, name, email, meta, status, reportedN;
        final LinearLayout reported;

        VH(@NonNull View v) {
            super(v);
            avatar = v.findViewById(R.id.admUserAvatar);
            name = v.findViewById(R.id.admUserName);
            email = v.findViewById(R.id.admUserEmail);
            meta = v.findViewById(R.id.admUserMeta);
            status = v.findViewById(R.id.admUserStatus);
            reported = v.findViewById(R.id.admUserReported);
            reportedN = v.findViewById(R.id.admUserReportedN);
        }
    }
}
