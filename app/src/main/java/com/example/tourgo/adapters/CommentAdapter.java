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
import com.example.tourgo.models.response.Comment;
import com.example.tourgo.utils.ImageLoader;

import java.util.Locale;

public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.CommentViewHolder> {

    public interface ReviewActionListener {
        void onEdit(Comment comment);
        void onDelete(Comment comment);
    }

    private String currentUserId;
    private ReviewActionListener listener;

    public CommentAdapter(String currentUserId, ReviewActionListener listener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Comment>() {
                @Override
                public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                    return oldItem.getContent().equals(newItem.getContent())
                            && oldItem.getRating() == newItem.getRating()
                            && oldItem.getImageUrls().equals(newItem.getImageUrls());
                }
            };

    public CommentAdapter() {
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
        Comment comment = getItem(position);
        holder.tvUserName.setText(comment.getUserName());
        holder.tvCommentDate.setText(comment.getDate());
        holder.tvCommentContent.setText(comment.getContent());
        // Sử dụng Locale mặc định để đồng bộ với định dạng hệ thống
        holder.tvUserRating.setText(String.format(Locale.getDefault(), "★ %.1f", comment.getRating()));
        
        holder.layoutCommentImages.removeAllViews();
        if (comment.hasImages()) {
            holder.scrollCommentImages.setVisibility(View.VISIBLE);
            for (String imageUrl : comment.getImageUrls()) {
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

        boolean isOwner = currentUserId != null && currentUserId.equals(comment.getUserId());

        holder.btnReviewMenu.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        holder.btnReviewMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.menu_review_options);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_delete_review) {
                    if (listener != null) {
                        listener.onDelete(comment);
                    }
                    return true;
                }

                return false;
            });

            popup.show();
        });
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
