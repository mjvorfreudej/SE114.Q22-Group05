package com.example.tourgo.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.models.HotelItem;
import com.example.tourgo.ui.main.DetailActivity;

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

        updateHeartIcon(holder.imgHeart, item.isFavorite());

        holder.imgHeart.setOnClickListener(v -> {
            item.setFavorite(!item.isFavorite());
            animateHeart(holder.imgHeart);
            updateHeartIcon(holder.imgHeart, item.isFavorite());
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("hotel_item", item);
            v.getContext().startActivity(intent);
        });
    }

    private void updateHeartIcon(ImageView imgHeart, boolean isFavorite) {
        if (isFavorite) {
            imgHeart.setColorFilter(ContextCompat.getColor(imgHeart.getContext(), android.R.color.holo_red_dark));
        } else {
            imgHeart.setColorFilter(ContextCompat.getColor(imgHeart.getContext(), android.R.color.white));
        }
    }

    private void animateHeart(View view) {
        view.setScaleX(0.7f);
        view.setScaleY(0.7f);
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction(() -> view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start())
                .start();
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    static class PopularViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHotel, imgHeart;
        TextView tvName, tvPrice, tvRating;

        public PopularViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHotel = itemView.findViewById(R.id.imgPopularHotel);
            imgHeart = itemView.findViewById(R.id.imgHeart);
            tvName = itemView.findViewById(R.id.tvPopularName);
            tvPrice = itemView.findViewById(R.id.tvPopularPrice);
            tvRating = itemView.findViewById(R.id.tvPopularRating);
        }
    }
}
