package com.example.tourgo.ui.business;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.ui.business.BusinessMockData.Listing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** RecyclerView adapter for the My Listings screen — cards with status, bulk-select & kebab menu. */
public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.VH> {

    public interface Listener {
        void onEdit(Listing l);
        void onView(Listing l);
        void onDelete(Listing l);
        void onSelectionChanged(int count);
    }

    private final Context ctx;
    private final Listener listener;
    private final List<Listing> items = new ArrayList<>();
    private final Set<Integer> selected = new HashSet<>();

    public ListingAdapter(Context ctx, Listener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    public void submit(List<Listing> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    public int selectedCount() {
        return selected.size();
    }

    public void clearSelection() {
        selected.clear();
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(0);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_biz_listing, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Listing l = items.get(position);
        h.photo.setImageResource(BusinessMockData.photo(l.photoIndex));
        h.name.setText(l.name);
        h.loc.setText(l.loc);

        // Category chip (reuses the Admin console's category styling)
        AdminUi.catChip(ctx, h.catChip, h.catDot, h.catLabel, l.cat);
        // Status pill
        BizUi.status(ctx, h.statusPill, h.statusDot, h.status, l.status);

        // Price + per-unit
        String unit = ctx.getString("hotel".equals(l.cat) ? R.string.biz_per_night : R.string.biz_per_person);
        h.price.setText(priceSpan("$" + l.price, unit));

        // Rating + bookings
        boolean hasRating = l.hasRating();
        h.starIcon.setVisibility(hasRating ? View.VISIBLE : View.GONE);
        h.rating.setVisibility(hasRating ? View.VISIBLE : View.GONE);
        if (hasRating) h.rating.setText(String.format(java.util.Locale.US, "%.1f", l.rating));
        h.bookings.setText(ctx.getString(R.string.biz_bookings_count, l.bookings));

        // Selection visuals
        boolean sel = selected.contains(l.id);
        h.card.setBackgroundResource(sel
                ? R.drawable.bg_biz_listing_card_selected : R.drawable.bg_biz_listing_card);
        h.check.setBackgroundTintList(ColorStateList.valueOf(sel
                ? ContextCompat.getColor(ctx, R.color.adm_gray_900) : 0xD9FFFFFF));
        h.checkIcon.setVisibility(sel ? View.VISIBLE : View.INVISIBLE);

        h.check.setOnClickListener(v -> {
            if (selected.contains(l.id)) selected.remove(l.id);
            else selected.add(l.id);
            notifyItemChanged(h.getBindingAdapterPosition());
            if (listener != null) listener.onSelectionChanged(selected.size());
        });

        h.card.setOnClickListener(v -> {
            if (listener != null) listener.onView(l);
        });

        h.menu.setOnClickListener(this::popup);
        h.menu.setTag(l);
    }

    private void popup(View anchor) {
        Listing l = (Listing) anchor.getTag();
        PopupMenu menu = new PopupMenu(ctx, anchor);
        menu.getMenu().add(0, 1, 0, R.string.biz_action_edit);
        menu.getMenu().add(0, 2, 1, R.string.biz_action_view);
        menu.getMenu().add(0, 3, 2, R.string.biz_action_duplicate);
        menu.getMenu().add(0, 4, 3, R.string.biz_action_delete);
        menu.setOnMenuItemClickListener(item -> {
            if (listener == null) return false;
            switch (item.getItemId()) {
                case 1: listener.onEdit(l); return true;
                case 2: listener.onView(l); return true;
                case 4: listener.onDelete(l); return true;
                default: return true;
            }
        });
        menu.show();
    }

    private SpannableStringBuilder priceSpan(String price, String unit) {
        SpannableStringBuilder sb = new SpannableStringBuilder(price);
        int start = sb.length();
        sb.append(unit);
        sb.setSpan(new AbsoluteSizeSpan(10, true), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.adm_gray_500)),
                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final View card, check, catChip, catDot, statusPill, statusDot;
        final ImageView photo, checkIcon, menu, starIcon;
        final TextView name, loc, catLabel, status, price, rating, bookings;

        VH(@NonNull View v) {
            super(v);
            card = v.findViewById(R.id.bizListingCard);
            check = v.findViewById(R.id.bizListingCheck);
            checkIcon = v.findViewById(R.id.bizListingCheckIcon);
            photo = v.findViewById(R.id.bizListingPhoto);
            menu = v.findViewById(R.id.bizListingMenu);
            starIcon = v.findViewById(R.id.bizListingStarIcon);
            name = v.findViewById(R.id.bizListingName);
            loc = v.findViewById(R.id.bizListingLoc);
            catChip = v.findViewById(R.id.bizListingCatChip);
            catDot = v.findViewById(R.id.bizListingCatDot);
            catLabel = v.findViewById(R.id.bizListingCatLabel);
            statusPill = v.findViewById(R.id.bizListingStatusPill);
            statusDot = v.findViewById(R.id.bizListingStatusDot);
            status = v.findViewById(R.id.bizListingStatus);
            price = v.findViewById(R.id.bizListingPrice);
            rating = v.findViewById(R.id.bizListingRating);
            bookings = v.findViewById(R.id.bizListingBookings);
        }
    }
}
