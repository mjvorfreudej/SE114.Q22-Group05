package com.example.tourgo.adapters;

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
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Favorite;
import com.example.tourgo.models.Tour;
import com.example.tourgo.remote.FavoriteService;
import com.example.tourgo.utils.ImageLoader;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    public interface OnTourClickListener {
        void onTourClick(Tour tour);
    }

    private final List<Tour> originalList;
    private List<Tour> filteredList;
    private SessionManager session;
    private OnTourClickListener onTourClickListener;

    public TourAdapter(List<Tour> list) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
    }

    public void setOnTourClickListener(OnTourClickListener listener) {
        this.onTourClickListener = listener;
    }

    public void filter(String query) {
        filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Tour item : originalList) {
                if (item.getName().toLowerCase().contains(filterPattern) || 
                    (item.getLocation() != null && item.getLocation().toLowerCase().contains(filterPattern))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour, parent, false);
        session = new SessionManager(parent.getContext());
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour item = filteredList.get(position);

        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            ImageLoader.loadThumbnail(holder.imgTour, item.getImageUrls().get(0));
        } else {
            holder.imgTour.setImageResource(item.getImageResId() != 0 ? item.getImageResId() : R.drawable.hotel_1);
        }

        holder.tvName.setText(item.getName());
        holder.tvLocation.setText(item.getLocation());
        
        // Hiển thị giá tiền đa ngôn ngữ cho Tour
        holder.tvPrice.setText(item.getPriceString());
        
        holder.tvDuration.setText(item.getDuration());
        holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getRating()));

        updateHeartIcon(holder.btnFavorite, item.isFavorite());

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
                Favorite favorite = new Favorite(userId, item.getId(), null);
                FavoriteService.addFavorite(favorite, token, new DataCallback<Void>() {
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
                FavoriteService.removeFavoriteTour(userId, item.getId(), token, new DataCallback<Void>() {
                    @Override public void onSuccess(Void data) {}
                    @Override public void onError(ApiErrorCode code, String msg) {
                        holder.btnFavorite.post(() -> {
                            item.setFavorite(true);
                            updateHeartIcon(holder.btnFavorite, true);
                            Toast.makeText(v.getContext(), v.getContext().getString(R.string.err_prefix, msg), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (onTourClickListener != null) {
                onTourClickListener.onTourClick(item);
            }
        });
    }

    private void updateHeartIcon(ImageView imgHeart, boolean isFavorite) {
        int color = isFavorite ? ContextCompat.getColor(imgHeart.getContext(), android.R.color.holo_red_dark) 
                              : ContextCompat.getColor(imgHeart.getContext(), android.R.color.white);
        imgHeart.setImageTintList(ColorStateList.valueOf(color));
        
        if (isFavorite) {
            imgHeart.setBackgroundResource(R.drawable.bg_circle_white);
        } else {
            imgHeart.setBackgroundResource(R.drawable.bg_circle_white_alpha);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTour, btnFavorite;
        TextView tvName, tvLocation, tvPrice, tvRating, tvDuration;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTour = itemView.findViewById(R.id.imgTour);
            btnFavorite = itemView.findViewById(R.id.btnFavoriteTour);
            tvName = itemView.findViewById(R.id.tvTourName);
            tvLocation = itemView.findViewById(R.id.tvTourLocation);
            tvPrice = itemView.findViewById(R.id.tvTourPrice);
            tvRating = itemView.findViewById(R.id.tvTourRating);
            tvDuration = itemView.findViewById(R.id.tvTourDuration);
        }
    }

    public void setData(List<Tour> newList) {
        originalList.clear();
        originalList.addAll(newList);
        filteredList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }
}
