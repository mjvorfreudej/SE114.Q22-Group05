package com.example.tourgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private final List<String> images;

    public GalleryAdapter(List<String> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        String imageUrl = images.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.hotel_1)
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgGalleryItem);
        }
    }
}
