package com.example.tourgo.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;

import java.util.List;

public class CreateTourImageAdapter extends RecyclerView.Adapter<CreateTourImageAdapter.ImageVH> {

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    private final List<Uri> images;
    private final OnRemoveClickListener listener;

    public CreateTourImageAdapter(List<Uri> images, OnRemoveClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_create_tour_image, parent, false);
        return new ImageVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageVH holder, int position) {
        Uri imageUri = images.get(position);
        
        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .centerCrop()
                .placeholder(R.drawable.bg_listing_card)
                .into(holder.imgPreview);

        holder.tvCover.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

        holder.btnRemoveImage.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onRemove(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }

    static class ImageVH extends RecyclerView.ViewHolder {
        ImageView imgPreview;
        TextView tvCover;
        TextView btnRemoveImage;

        public ImageVH(@NonNull View itemView) {
            super(itemView);
            imgPreview = itemView.findViewById(R.id.imgPreview);
            tvCover = itemView.findViewById(R.id.tvCover);
            btnRemoveImage = itemView.findViewById(R.id.btnRemoveImage);
        }
    }
}
