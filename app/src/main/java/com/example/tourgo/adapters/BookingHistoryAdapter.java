package com.example.tourgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Vertical list of past bookings with a coloured status badge. */
public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(Item item, int position);
    }

    /** Flat display model so the adapter doesn't depend on how data is joined. */
    public static class Item {
        public final String title;
        public final String dateText;
        public final String priceText;
        public final String status;
        public final String imageUrl;
        public final String bookingId;
        public final String confirmationNumber;
        public final double totalAmount;
        public final String guestInfo;

        public Item(String title, String dateText, String priceText, String status, String imageUrl) {
            this(title, dateText, priceText, status, imageUrl, null, null, 0.0, null);
        }

        public Item(String title, String dateText, String priceText, String status, String imageUrl,
                    String bookingId, String confirmationNumber, double totalAmount, String guestInfo) {
            this.title = title;
            this.dateText = dateText;
            this.priceText = priceText;
            this.status = status;
            this.imageUrl = imageUrl;
            this.bookingId = bookingId;
            this.confirmationNumber = confirmationNumber;
            this.totalAmount = totalAmount;
            this.guestInfo = guestInfo;
        }
    }

    private final List<Item> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Item> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_history, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Item item = items.get(position);
        holder.title.setText(item.title);
        holder.date.setText(item.dateText);
        holder.price.setText(item.priceText);
        holder.status.setText(item.status);

        applyStatusColors(holder, item.status);

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            ImageLoader.loadThumbnail(holder.image, item.imageUrl);
        } else {
            holder.image.setImageResource(R.drawable.hotel_1);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    private void applyStatusColors(VH holder, String status) {
        int bgColor;
        int textColor;
        if ("COMPLETED".equalsIgnoreCase(status)) {
            // Completed trip = Green
            bgColor = R.color.adm_green_100;
            textColor = R.color.adm_green_700;
        } else if ("PAID".equalsIgnoreCase(status)) {
            // Just paid = Blue (Confirmed)
            bgColor = R.color.adm_blue_50;
            textColor = R.color.adm_blue_700;
        } else {
            // Pending or other = Amber
            bgColor = R.color.adm_amber_100;
            textColor = R.color.adm_amber_700;
        }
        holder.status.setBackgroundTintList(
                ContextCompat.getColorStateList(holder.itemView.getContext(), bgColor));
        holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), textColor));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView date;
        final TextView price;
        final TextView status;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivBookingHistoryImage);
            title = itemView.findViewById(R.id.tvBookingHistoryTitle);
            date = itemView.findViewById(R.id.tvBookingHistoryDate);
            price = itemView.findViewById(R.id.tvBookingHistoryPrice);
            status = itemView.findViewById(R.id.tvBookingHistoryStatus);
        }
    }
}
