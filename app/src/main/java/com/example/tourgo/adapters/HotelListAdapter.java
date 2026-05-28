package com.example.tourgo.adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelListAdapter extends RecyclerView.Adapter<HotelListAdapter.HotelViewHolder> {

    private final List<Hotel> originalList;
    private List<Hotel> filteredList;
    private SessionManager session;

    public HotelListAdapter(List<Hotel> list) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
    }

    public void filter(String query) {
        filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Hotel item : originalList) {
                if (item.getName().toLowerCase().contains(filterPattern) ||
                    item.getAddress().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel_list, parent, false);
        session = new SessionManager(parent.getContext());
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel item = filteredList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvLocation.setText(item.getAddress());
        
        // Hiển thị giá tiền dựa trên cài đặt tiền tệ trong Session
        String formattedPrice = item.formatPrice(holder.itemView.getContext(), item.getPricePerNight());
        holder.tvPrice.setText(holder.itemView.getContext().getString(R.string.price_per_night_format, formattedPrice));

        holder.tvDescription.setText(item.getDescription());
        holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getRating()));

        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            ImageLoader.loadThumbnail(holder.imgHotel, item.getImageUrls().get(0));
        } else {
            holder.imgHotel.setImageResource(item.getImageResId() != 0 ? item.getImageResId() : R.drawable.hotel_1);
        }

        updateHeartIcon(holder.btnFavorite, item.isFavorite());

        // Sự kiện click vào item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("hotel_object", item);
            v.getContext().startActivity(intent);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (!session.isLoggedIn()) {
                Toast.makeText(v.getContext(), R.string.err_login_required, Toast.LENGTH_SHORT).show();
                return;
            }

            final boolean oldState = item.isFavorite();
            final boolean newState = !oldState;
            
            item.setFavorite(newState);
            updateHeartIcon(holder.btnFavorite, newState);

            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
            }).start();

            String token = session.getAccessToken();
            String userId = session.getUserId();

            if (newState) {
                Favorite favorite = new Favorite(userId, null, item.getId());
                FavoriteRepository.getInstance().addFavorite(v.getContext(), favorite, new DataCallback<Void>() {
                    @Override public void onSuccess(Void data) {}
                    @Override public void onError(ApiErrorCode code, String msg) {
                        holder.btnFavorite.post(() -> {
                            item.setFavorite(false);
                            updateHeartIcon(holder.btnFavorite, false);
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
                            holder.btnFavorite.post(() -> {
                                item.setFavorite(true);
                                updateHeartIcon(holder.btnFavorite, true);
                            });
                        }
                    });
                }
            }
        });
    }

    private void updateHeartIcon(ImageView imgHeart, boolean isFavorite) {
        imgHeart.setImageResource(isFavorite ? R.drawable.ic_heart_fullfilled : R.drawable.ic_heart_filled);
        int color = ContextCompat.getColor(imgHeart.getContext(),
                isFavorite ? R.color.red : android.R.color.white);
        imgHeart.setImageTintList(ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHotel, btnFavorite;
        TextView tvName, tvLocation, tvPrice, tvAmenities, tvRating, tvDescription;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHotel = itemView.findViewById(R.id.imgHotelList);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            tvName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvHotelLocation);
            tvPrice = itemView.findViewById(R.id.tvHotelPrice);
            tvAmenities = itemView.findViewById(R.id.tvHotelAmenities);
            tvRating = itemView.findViewById(R.id.tvHotelRating);
            tvDescription = itemView.findViewById(R.id.tvHotelDescription);
        }
    }

    public void setData(List<Hotel> newList) {
        if (newList == null) return;
        originalList.clear();
        originalList.addAll(newList);
        filteredList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }
}
