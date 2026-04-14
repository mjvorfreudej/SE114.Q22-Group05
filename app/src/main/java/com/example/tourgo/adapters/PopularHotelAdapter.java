package com.example.tourgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.models.HotelItem;

import java.util.List;
import java.util.Locale;

public class PopularHotelAdapter extends RecyclerView.Adapter<PopularHotelAdapter.PopularViewHolder> {

    private final List<HotelItem> hotelList;

    public PopularHotelAdapter(List<HotelItem> hotelList) {
        this.hotelList = hotelList;
    }

    @NonNull
    @Override
    public PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_hotel, parent, false);
        return new PopularViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularViewHolder holder, int position) {
        HotelItem item = hotelList.get(position);

        holder.imgHotel.setImageResource(item.getImageResId());
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPrice());
        holder.tvRating.setText(String.format(Locale.US, "★ %.1f", item.getRating()));
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    static class PopularViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHotel;
        TextView tvName, tvPrice, tvRating;

        public PopularViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHotel = itemView.findViewById(R.id.imgPopularHotel);
            tvName = itemView.findViewById(R.id.tvPopularName);
            tvPrice = itemView.findViewById(R.id.tvPopularPrice);
            tvRating = itemView.findViewById(R.id.tvPopularRating);
        }
    }
}
