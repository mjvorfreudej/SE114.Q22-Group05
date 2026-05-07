package com.example.tourgo.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Favorite;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.remote.FavoriteService;
import com.example.tourgo.ui.main.DetailActivity;
import com.example.tourgo.utils.SessionManager;

import java.util.List;
import java.util.Locale;

public class PopularHotelAdapter extends RecyclerView.Adapter<PopularHotelAdapter.PopularViewHolder> {

    private List<Hotel> hotelList;
    private SessionManager session;

    public PopularHotelAdapter(List<Hotel> hotelList) {
        this.hotelList = hotelList;
    }

    public void setData(List<Hotel> newList) {
        this.hotelList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_hotel, parent, false);
        session = new SessionManager(parent.getContext());
        return new PopularViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularViewHolder holder, int position) {
        Hotel item = hotelList.get(position);

        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrls().get(0))
                    .placeholder(R.drawable.hotel_1)
                    .centerCrop()
                    .into(holder.imgHotel);
        } else {
            holder.imgHotel.setImageResource(item.getImageResId() != 0 ? item.getImageResId() : R.drawable.hotel_1);
        }

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPriceString());
        holder.tvRating.setText(String.format(Locale.US, "★ %.1f", item.getRating()));

        updateHeartIcon(holder.imgHeart, item.isFavorite());

        holder.imgHeart.setOnClickListener(v -> {
            if (!session.isLoggedIn()) {
                Toast.makeText(v.getContext(), "Vui lòng đăng nhập để thực hiện", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean newState = !item.isFavorite();
            item.setFavorite(newState);
            animateHeart(holder.imgHeart);
            updateHeartIcon(holder.imgHeart, item.isFavorite());

            String token = session.getAccessToken();
            String userId = session.getUserId();

            if (newState) {
                Favorite favorite = new Favorite(userId, null, item.getId());
                FavoriteService.addFavorite(favorite, token, new DataCallback<>() {
                    @Override public void onSuccess(Void data) {}
                    @Override public void onError(ApiErrorCode code, String msg) {
                        item.setFavorite(false);
                        updateHeartIcon(holder.imgHeart, false);
                    }
                });
            } else {
                FavoriteService.removeFavoriteHotel(userId, item.getId(), token, new DataCallback<>() {
                    @Override public void onSuccess(Void data) {}
                    @Override public void onError(ApiErrorCode code, String msg) {
                        item.setFavorite(true);
                        updateHeartIcon(holder.imgHeart, true);
                    }
                });
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("hotel_object", item);
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
        return hotelList != null ? hotelList.size() : 0;
    }

    public static class PopularViewHolder extends RecyclerView.ViewHolder {
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
