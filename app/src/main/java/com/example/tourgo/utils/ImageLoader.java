package com.example.tourgo.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tourgo.R;

import java.util.List;

public final class ImageLoader {

    // Sized for list/carousel thumbnails on xxhdpi (~200dp wide cells).
    // Glide downsamples the network bytes to this size before allocating a
    // bitmap, so a 4000×3000 source costs ~960 KB instead of ~48 MB.
    private static final int THUMB_W = 600;
    private static final int THUMB_H = 400;

    private ImageLoader() {}

    public static void loadThumbnail(ImageView target, String url) {
        Glide.with(target.getContext())
                .load(url)
                .placeholder(R.drawable.hotel_1)
                .error(R.drawable.hotel_1)
                .override(THUMB_W, THUMB_H)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(target);
    }

    public static void preload(Context context, List<String> urls) {
        if (context == null || urls == null) return;
        for (String url : urls) {
            if (url == null || url.isEmpty()) continue;
            Glide.with(context)
                    .load(url)
                    .override(THUMB_W, THUMB_H)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .preload(THUMB_W, THUMB_H);
        }
    }
}