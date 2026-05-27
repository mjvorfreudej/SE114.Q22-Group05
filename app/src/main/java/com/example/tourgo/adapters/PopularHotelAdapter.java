package com.example.tourgo.adapters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Favorite;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.ui.main.DetailActivity;
import com.example.tourgo.utils.ImageLoader;
import com.example.tourgo.data.local.SessionManager;

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
            ImageLoader.loadThumbnail(holder.imgHotel, item.getImageUrls().get(0));
        } else {
            holder.imgHotel.setImageResource(item.getImageResId() != 0 ? item.getImageResId() : R.drawable.hotel_1);
        }

        holder.tvName.setText(item.getName());
        
        // Hiển thị giá tiền đa ngôn ngữ
        String formattedPrice = item.formatPrice(item.getPricePerNight());
        holder.tvPrice.setText(holder.itemView.getContext().getString(R.string.price_per_night_format, formattedPrice));
        
        holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getRating()));

        updateHeartIcon(holder.imgHeart, item.isFavorite());

        holder.imgHeart.setOnClickListener(v -> {
            if (!session.isLoggedIn()) {
                Toast.makeText(v.getContext(), R.string.err_login_required, Toast.LENGTH_SHORT).show();
                return;
            }

            final boolean oldState = item.isFavorite();
            final boolean newState = !oldState;
            
            item.setFavorite(newState);
            updateHeartIcon(holder.imgHeart, newState);
            animateHeart(holder.imgHeart);

            String token = session.getAccessToken();
            String userId = session.getUserId();

            if (newState) {
                Favorite favorite = new Favorite(userId, null, item.getId());
                FavoriteRepository.getInstance().addFavorite(v.getContext(), favorite, new DataCallback<Void>() {
                    @Override public void onSuccess(Void data) {}
                    @Override public void onError(ApiErrorCode code, String msg) {
                        holder.imgHeart.post(() -> {
                            item.setFavorite(false);
                            updateHeartIcon(holder.imgHeart, false);
                            Toast.makeText(v.getContext(), v.getContext().getString(R.string.err_prefix, msg), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                String favoriteId = FavoriteRepository.getInstance().findFavoriteIdByHotelId(item.getId());
                if (favoriteId != null) {
                    FavoriteRepository.getInstance().removeFavorite(v.getContext(), favoriteId, new DataCallback<Void>() {
                        @Override public void onSuccess(Void data) {}
                        @Override public void onError(ApiErrorCode code, String msg) {
                            holder.imgHeart.post(() -> {
                                item.setFavorite(true);
                                updateHeartIcon(holder.imgHeart, true);
                                Toast.makeText(v.getContext(), v.getContext().getString(R.string.err_prefix, msg), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("hotel_object", item);
            v.getContext().startActivity(intent);
        });
    }

    private void updateHeartIcon(ImageView imgHeart, boolean isFavorite) {
        imgHeart.setImageResource(isFavorite ? R.drawable.ic_heart_fullfilled : R.drawable.ic_heart_filled);
        int color = ContextCompat.getColor(imgHeart.getContext(),
                isFavorite ? R.color.red : android.R.color.white);
        imgHeart.setImageTintList(ColorStateList.valueOf(color));
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
