package com.example.tourgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.tourgo.utils.ImageLoader;

import java.text.SimpleDateFormat;
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
                            && oldItem.getImageUrls().equals(newItem.getImageUrls());
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
        // Sử dụng Locale mặc định để đồng bộ với định dạng hệ thống
        holder.tvUserRating.setText(String.format(Locale.getDefault(), "★ %.1f", review.getRating()));
        
        holder.layoutCommentImages.removeAllViews();
        if (review.hasImages()) {
            holder.scrollCommentImages.setVisibility(View.VISIBLE);
            for (String imageUrl : review.getImageUrls()) {
                ImageView iv = new ImageView(holder.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(160, 160);
                params.setMargins(0, 0, 12, 0);
                iv.setLayoutParams(params);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoader.loadThumbnail(iv, imageUrl);
                iv.setClipToOutline(true);
                iv.setBackgroundResource(R.drawable.bg_amenity_card);
                holder.layoutCommentImages.addView(iv);
            }
        } else {
            holder.scrollCommentImages.setVisibility(View.GONE);
        }

        boolean isOwner = currentUserId != null && currentUserId.equals(review.getUserId());

        holder.btnReviewMenu.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        holder.btnReviewMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.menu_review_options);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_delete_review) {
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

    /**
     * Định dạng ngày giờ bình luận.
     * Xử lý triệt để các định dạng ISO 8601 (có mili giây, múi giờ) hoặc Timestamp.
     */
    private String formatReviewDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";
        
        try {
            // 1. Xử lý định dạng ISO 8601 bằng Regex để lấy chính xác phần cần thiết
            // Mẫu: 2025-02-18T09:15:35.336154Z hoặc 2025-02-18 09:15:35+07:00
            Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})[T ](\\d{2}:\\d{2}:\\d{2})");
            Matcher matcher = pattern.matcher(rawDate);
            
            if (matcher.find()) {
                String cleanDate = matcher.group(1) + " " + matcher.group(2);
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                
                // Giả định Server trả về UTC nếu có Z hoặc +
                if (rawDate.contains("Z") || rawDate.contains("+")) {
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                
                Date date = inputFormat.parse(cleanDate);
                if (date != null) {
                    // Định dạng hiển thị gọn gàng cho người dùng
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    return outputFormat.format(date);
                }
            }
            
            // 2. Xử lý nếu rawDate là một số (Unix Timestamp)
            if (rawDate.matches("\\d+")) {
                long ts = Long.parseLong(rawDate);
                // Nếu timestamp tính bằng giây (10 chữ số), đổi sang mili giây
                if (ts < 10000000000L) ts *= 1000;
                Date date = new Date(ts);
                return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 3. Fallback: Nếu mọi cách trên thất bại, thử cắt chuỗi cơ bản
        if (rawDate.length() >= 16) {
            try {
                // Giả định định dạng yyyy-MM-dd...
                String y = rawDate.substring(0, 4);
                String m = rawDate.substring(5, 7);
                String d = rawDate.substring(8, 10);
                String time = rawDate.substring(11, 16);
                return d + "/" + m + "/" + y + " " + time;
            } catch (Exception ignored) {}
        }

        return rawDate;
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvCommentDate, tvCommentContent, tvUserRating;
        LinearLayout layoutCommentImages;
        View scrollCommentImages;
        ImageView btnReviewMenu;

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
