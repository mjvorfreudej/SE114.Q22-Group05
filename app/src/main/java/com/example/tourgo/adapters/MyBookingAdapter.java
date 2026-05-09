package com.example.tourgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;

import java.util.List;

public class MyBookingAdapter extends RecyclerView.Adapter<MyBookingAdapter.VH> {

    public static class Item {
        public final String hotelName;
        public final String dateRange;
        public final int imageRes;

        public Item(String hotelName, String dateRange, int imageRes) {
            this.hotelName = hotelName;
            this.dateRange = dateRange;
            this.imageRes = imageRes;
        }
    }

    private final List<Item> items;

    public MyBookingAdapter(List<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_booking, parent, false);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (lp != null) {
            int gutter = parent.getResources().getDimensionPixelSize(R.dimen.gutter_space);
            lp.setMarginEnd(gutter);
            view.setLayoutParams(lp);
        }
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Item item = items.get(position);
        holder.image.setImageResource(item.imageRes);
        holder.name.setText(item.hotelName);
        holder.dates.setText(item.dateRange);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView dates;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivBookingImage);
            name = itemView.findViewById(R.id.tvBookingHotelName);
            dates = itemView.findViewById(R.id.tvBookingDates);
        }
    }
}
