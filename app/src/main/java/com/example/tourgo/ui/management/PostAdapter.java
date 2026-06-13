package com.example.tourgo.ui.management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.models.Tour;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Tour> posts;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onEdit(Tour post);
        void onDelete(Tour post, int position);
        void onViewReviews(Tour post);
        void onItemClick(Tour post);
    }

    public PostAdapter(List<Tour> posts, OnPostActionListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tour post = posts.get(position);
        holder.tvName.setText(post.getName());
        holder.tvLocation.setText(post.getLocation());
        holder.tvPrice.setText(post.getPriceString(holder.itemView.getContext()));
        holder.tvStatus.setText(post.getStatus());

        if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(post.getImageUrls().get(0)).into(holder.ivThumb);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(post));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(post, position));
        holder.btnReviews.setOnClickListener(v -> listener.onViewReviews(post));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(post));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvLocation, tvPrice, tvStatus;
        ImageButton btnEdit, btnDelete, btnReviews;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnReviews = itemView.findViewById(R.id.btnReviews);
        }
    }
}
