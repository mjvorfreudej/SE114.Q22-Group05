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
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Favorite;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.utils.ImageLoader;
import com.example.tourgo.data.local.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    public interface OnTourClickListener {
        void onTourClick(Tour tour);
    }

    public static final int VIEW_TYPE_VERTICAL = 0;
    public static final int VIEW_TYPE_HORIZONTAL = 1;

    private final List<Tour> originalList;
    private List<Tour> filteredList;
    private SessionManager session;
    private OnTourClickListener onTourClickListener;
    private int viewType = VIEW_TYPE_VERTICAL;

    public TourAdapter(List<Tour> list) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
    }

    public TourAdapter(List<Tour> list, int viewType) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
        this.viewType = viewType;
    }

    public void setOnTourClickListener(OnTourClickListener listener) {
        this.onTourClickListener = listener;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
        notifyDataSetChanged();
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

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == VIEW_TYPE_HORIZONTAL) ? R.layout.item_popular_tour : R.layout.item_tour;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
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
        
        if (holder.tvLocation != null) {
            holder.tvLocation.setText(item.getLocation());
        }
        
        // Giá tour
        holder.tvPrice.setText(item.getPriceString(holder.itemView.getContext()));
        
        // Thời gian tour
        if (holder.tvDuration != null) {
            String duration = item.getDuration();
            if (viewType == VIEW_TYPE_HORIZONTAL) {
                holder.tvDuration.setText(duration != null && !duration.isEmpty() ? "• " + duration : "");
            } else {
                holder.tvDuration.setText(duration);
            }
        }
        
        // Đánh giá và Lượt đánh giá
        if (holder.tvRating != null) {
            if (viewType == VIEW_TYPE_HORIZONTAL) {
                // Hiển thị dạng: ★ 4.8 (120) cho trang chủ
                holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f (%d)", 
                        item.getRating(), item.getReviewCount()));
            } else {
                holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getRating()));
            }
        }

        if (holder.btnFavorite != null) {
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

                String userId = session.getUserId();

                if (newState) {
                    Favorite favorite = new Favorite(userId, item.getId(), null);
                    FavoriteRepository.getInstance().addFavorite(v.getContext(), favorite, new DataCallback<Favorite>() {
                        @Override public void onSuccess(Favorite data) {
                            // Cập nhật trạng thái đã được Repository thực hiện vào cache
                        }
                        @Override public void onError(ApiErrorCode code, String msg) {
                            holder.btnFavorite.post(() -> {
                                item.setFavorite(false);
                                updateHeartIcon(holder.btnFavorite, false);
                                Toast.makeText(v.getContext(), v.getContext().getString(R.string.err_prefix, msg), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    String favoriteId = FavoriteRepository.getInstance().findFavoriteIdByTourId(item.getId());
                    if (favoriteId != null) {
                        FavoriteRepository.getInstance().removeFavorite(v.getContext(), favoriteId, new DataCallback<Void>() {
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
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (onTourClickListener != null) {
                onTourClickListener.onTourClick(item);
            }
        });
    }

    private void updateHeartIcon(ImageView imgHeart, boolean isFavorite) {
        if (imgHeart == null) return;

        imgHeart.setImageResource(isFavorite ? R.drawable.ic_heart_fullfilled : R.drawable.ic_heart_outline_18);
        int color = ContextCompat.getColor(imgHeart.getContext(),
                isFavorite ? R.color.red : android.R.color.white);
        imgHeart.setImageTintList(ColorStateList.valueOf(color));

        if (viewType == VIEW_TYPE_HORIZONTAL) {
            imgHeart.setBackgroundResource(R.drawable.bg_input_glass);
        } else {
            imgHeart.setBackgroundResource(isFavorite
                    ? R.drawable.bg_circle_white
                    : R.drawable.bg_circle_white_alpha);
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
