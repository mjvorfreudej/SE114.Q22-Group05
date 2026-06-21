package com.example.tourgo.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.models.response.Review;
import com.example.tourgo.ui.main.detail.ImageDetailActivity;
import com.example.tourgo.utils.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewAdapter extends ListAdapter<Review, ReviewAdapter.CommentViewHolder> {

    public interface ReviewActionListener {
        void onEdit(Review review);
        void onDelete(Review review);
    }

    private String currentUserId;
    private ReviewActionListener listener;

    public ReviewAdapter(String currentUserId, ReviewActionListener listener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Review> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Review>() {
                @Override
                public boolean areItemsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
                    return oldItem.getContent().equals(newItem.getContent())
                            && oldItem.getRating() == newItem.getRating()
                            && (oldItem.getImageUrls() != null ? oldItem.getImageUrls().equals(newItem.getImageUrls()) : newItem.getImageUrls() == null);
                }
            };

    public ReviewAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Review review = getItem(position);
        holder.tvUserName.setText(review.getUserName());
        holder.tvCommentDate.setText(formatReviewDate(review.getDate()));
        holder.tvCommentContent.setText(review.getContent());
        holder.tvUserRating.setText(String.format(Locale.getDefault(), "★ %.1f", review.getRating()));
        
        holder.layoutCommentImages.removeAllViews();
        if (review.hasImages()) {
            holder.scrollCommentImages.setVisibility(View.VISIBLE);
            float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
            int size = (int) (100 * density);
            int margin = (int) (8 * density);
            
            for (int i = 0; i < review.getImageUrls().size(); i++) {
                String imageUrl = review.getImageUrls().get(i);
                final int finalI = i;
                ImageView iv = new ImageView(holder.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(0, 0, margin, 0);
                iv.setLayoutParams(params);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoader.loadThumbnail(iv, imageUrl);
                iv.setClipToOutline(true);
                iv.setBackgroundResource(R.drawable.bg_amenity_card);
                
                iv.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), ImageDetailActivity.class);
                    intent.putStringArrayListExtra(ImageDetailActivity.EXTRA_IMAGES, new ArrayList<>(review.getImageUrls()));
                    intent.putExtra(ImageDetailActivity.EXTRA_POSITION, finalI);
                    v.getContext().startActivity(intent);
                });

                holder.layoutCommentImages.addView(iv);
            }
        } else {
            holder.scrollCommentImages.setVisibility(View.GONE);
        }

        // Kiểm tra quyền chủ sở hữu để hiện nút menu
        boolean isOwner = currentUserId != null && review.getUserId() != null && currentUserId.equals(review.getUserId());
        holder.btnReviewMenu.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        if (isOwner) {
            holder.btnReviewMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.menu_review_options);
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete_review) {
                        if (listener != null) {
                            listener.onDelete(review);
                        }
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }

    private String formatReviewDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";
        try {
            Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})[T ](\\d{2}:\\d{2}:\\d{2})");
            Matcher matcher = pattern.matcher(rawDate);
            if (matcher.find()) {
                String cleanDate = matcher.group(1) + " " + matcher.group(2);
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                if (rawDate.contains("Z") || rawDate.contains("+")) {
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date date = inputFormat.parse(cleanDate);
                if (date != null) {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    return outputFormat.format(date);
                }
            }
            if (rawDate.matches("\\d+")) {
                long ts = Long.parseLong(rawDate);
                if (ts < 10000000000L) ts *= 1000;
                return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(ts));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rawDate;
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvCommentDate, tvCommentContent, tvUserRating;
        LinearLayout layoutCommentImages;
        View scrollCommentImages;
        ImageButton btnReviewMenu;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvUserRating = itemView.findViewById(R.id.tvUserRating);
            layoutCommentImages = itemView.findViewById(R.id.layoutCommentImages);
            scrollCommentImages = itemView.findViewById(R.id.scrollCommentImages);
            btnReviewMenu = itemView.findViewById(R.id.btnReviewMenu);
        }
    }
}
