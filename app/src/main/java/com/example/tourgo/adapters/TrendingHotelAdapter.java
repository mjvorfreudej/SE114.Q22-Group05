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

public class TrendingHotelAdapter extends RecyclerView.Adapter<TrendingHotelAdapter.TrendingViewHolder> {

    private final List<HotelItem> hotelList;

    public TrendingHotelAdapter(List<HotelItem> hotelList) {
        this.hotelList = hotelList;
    }

    @NonNull
    @Override
    public TrendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trending_hotel, parent, false);
        return new TrendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingViewHolder holder, int position) {
        HotelItem item = hotelList.get(position);

        holder.imgHotel.setImageResource(item.getImageResId());
        holder.tvRating.setText(String.format(Locale.US, "★ %.1f", item.getRating()));
        holder.imgFavorite.setVisibility(item.isFavorite() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    static class TrendingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHotel, imgFavorite;
        TextView tvRating;

        public TrendingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHotel = itemView.findViewById(R.id.imgTrendingHotel);
            imgFavorite = itemView.findViewById(R.id.imgTrendingFavorite);
            tvRating = itemView.findViewById(R.id.tvTrendingRating);
        }
    }
}