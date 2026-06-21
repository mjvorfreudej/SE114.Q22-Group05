package com.example.tourgo.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.ui.admin.AdminMockData.PendingListing;
import com.example.tourgo.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class PendingListingAdapter extends RecyclerView.Adapter<PendingListingAdapter.VH> {

    public interface Listener {
        void onOpen(PendingListing item);
    }

    private final List<PendingListing> items = new ArrayList<>();
    private final Listener listener;

    public PendingListingAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PendingListing> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_pending_listing, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PendingListing it = items.get(position);
        Context ctx = h.itemView.getContext();

        if (it.imageUrl != null && !it.imageUrl.isEmpty()) {
            ImageLoader.loadThumbnail(h.photo, it.imageUrl);
        } else {
            h.photo.setImageResource(it.photoRes);
        }
        AdminUi.catChip(ctx, h.cat, h.catDot, h.catLabel, it.cat);
        h.revision.setVisibility("revision".equals(it.status) ? View.VISIBLE : View.GONE);
        h.name.setText(it.name);
        boolean hasBusiness = it.business != null && !it.business.isEmpty();
        h.sub.setText(hasBusiness ? it.business + " · " + it.city : it.city);
        h.submitted.setText(ctx.getString(R.string.adm_submitted, it.date));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(it);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView photo;
        final LinearLayout cat;
        final View catDot;
        final TextView catLabel, revision, name, sub, submitted;

        VH(@NonNull View v) {
            super(v);
            photo = v.findViewById(R.id.admPlPhoto);
            cat = v.findViewById(R.id.admPlCat);
            catDot = v.findViewById(R.id.admPlCatDot);
            catLabel = v.findViewById(R.id.admPlCatLabel);
            revision = v.findViewById(R.id.admPlRevision);
            name = v.findViewById(R.id.admPlName);
            sub = v.findViewById(R.id.admPlSub);
            submitted = v.findViewById(R.id.admPlSubmitted);
        }
    }
}
