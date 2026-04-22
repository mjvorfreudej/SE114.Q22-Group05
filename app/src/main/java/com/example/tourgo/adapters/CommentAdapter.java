package com.example.tourgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.models.Comment;

import java.util.Locale;

public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.CommentViewHolder> {

    public CommentAdapter() {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.getUserName().equals(newItem.getUserName()) && 
                       oldItem.getDate().equals(newItem.getDate());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.getContent().equals(newItem.getContent()) && 
                       oldItem.getRating() == newItem.getRating();
            }
        });
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
        holder.tvUserRating.setText(String.format(Locale.US, "★ %.1f", comment.getRating()));
        
        holder.layoutCommentImages.removeAllViews();
        if (comment.hasImages()) {
            holder.scrollCommentImages.setVisibility(View.VISIBLE);
            for (Integer imageRes : comment.getImages()) {
                ImageView iv = new ImageView(holder.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(160, 160);
                params.setMargins(0, 0, 12, 0);
                iv.setLayoutParams(params);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setImageResource(imageRes);
                iv.setClipToOutline(true);
                iv.setBackgroundResource(R.drawable.bg_amenity_card);
                holder.layoutCommentImages.addView(iv);
            }
        } else {
            holder.scrollCommentImages.setVisibility(View.GONE);
        }
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvCommentDate, tvCommentContent, tvUserRating;
        LinearLayout layoutCommentImages;
        View scrollCommentImages;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvUserRating = itemView.findViewById(R.id.tvUserRating);
            layoutCommentImages = itemView.findViewById(R.id.layoutCommentImages);
            scrollCommentImages = itemView.findViewById(R.id.scrollCommentImages);
        }
    }
}
