package com.example.tourgo.ui.main;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tourgo.R;
import com.example.tourgo.adapters.CommentAdapter;
import com.example.tourgo.adapters.GalleryAdapter;
import com.example.tourgo.databinding.ActivityDetailBinding;
import com.example.tourgo.models.Comment;
import com.example.tourgo.models.HotelItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private HotelItem item;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private CommentAdapter commentAdapter;
    private List<Comment> allComments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        item = (HotelItem) getIntent().getSerializableExtra("hotel_item");

        if (item != null) {
            // Mock gallery images
            if (item.getGalleryImages().size() <= 1) {
                item.setGalleryImages(Arrays.asList(item.getImageResId(), R.drawable.hotel_2, R.drawable.hotel_3, R.drawable.hotel_4));
            }
            
            // Mock comments with images and varying ratings
            if (item.getComments().isEmpty()) {
                List<Comment> mockComments = new ArrayList<>();
                mockComments.add(new Comment("Alex Johnson", null, "Amazing place! The view from the balcony was breathtaking.", 5.0f, "Today", Arrays.asList(R.drawable.hotel_1, R.drawable.hotel_2)));
                mockComments.add(new Comment("Maria Garcia", null, "Very clean and professional staff. Highly recommend for families.", 4.5f, "2 days ago"));
                mockComments.add(new Comment("David Chen", null, "Good location but a bit noisy at night.", 3.0f, "1 week ago"));
                mockComments.add(new Comment("Emma Wilson", null, "Perfect stay! Loved the pool area.", 5.0f, "3 days ago", Arrays.asList(R.drawable.hotel_3)));
                mockComments.add(new Comment("Lucas Brown", null, "It was okay, but the breakfast could be better.", 2.0f, "2 weeks ago"));
                mockComments.add(new Comment("Sophie Taylor", null, "Not worth the price. Small rooms.", 1.5f, "1 month ago"));
                item.setComments(mockComments);
            }

            allComments.addAll(item.getComments());
            setupUI();
        }

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnFavoriteDetail.setOnClickListener(v -> {
            item.setFavorite(!item.isFavorite());
            animateHeart(binding.btnFavoriteDetail);
            updateHeartIcon(item.isFavorite());
        });

        binding.btnBookNow.setOnClickListener(v -> {
            Toast.makeText(this, "Booking: " + item.getName(), Toast.LENGTH_SHORT).show();
        });

        setupFilters();
    }

    private void setupUI() {
        setupGallery();
        setupComments();
        updateRatingSummary();
        
        binding.tvDetailName.setText(item.getName());
        binding.tvDetailLocation.setText(item.getLocation() != null && !item.getLocation().isEmpty() ? item.getLocation() : "Location details");
        binding.tvDetailPrice.setText(item.getPrice());
        
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            binding.tvDetailDescription.setText(item.getDescription());
        }

        updateHeartIcon(item.isFavorite());
    }

    private void updateRatingSummary() {
        if (allComments.isEmpty()) return;

        double sum = 0;
        for (Comment c : allComments) {
            sum += c.getRating();
        }

        double average = sum / allComments.size();
        
        binding.tvHeaderRating.setText(String.format(Locale.US, "★ %.1f (%d Reviews)", average, allComments.size()));
        binding.tvBigRating.setText(String.format(Locale.US, "%.1f", average));
        binding.tvReviewCount.setText(String.format(Locale.US, "%d reviews", allComments.size()));
    }

    private void setupFilters() {
        binding.cgFilterRating.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                filterComments("all");
            } else if (checkedId == R.id.chipWithPhotos) {
                filterComments("photos");
            } else if (checkedId == R.id.chip5) {
                filterComments("5");
            } else if (checkedId == R.id.chip4) {
                filterComments("4");
            } else if (checkedId == R.id.chip3) {
                filterComments("3");
            } else if (checkedId == R.id.chip2) {
                filterComments("2");
            } else if (checkedId == R.id.chip1) {
                filterComments("1");
            }
        });
    }

    private void filterComments(String type) {
        List<Comment> filtered;
        switch (type) {
            case "5":
            case "4":
            case "3":
            case "2":
            case "1":
                int stars = Integer.parseInt(type);
                filtered = allComments.stream().filter(c -> Math.floor(c.getRating()) == stars).collect(Collectors.toList());
                break;
            case "photos":
                filtered = allComments.stream().filter(Comment::hasImages).collect(Collectors.toList());
                break;
            default:
                filtered = new ArrayList<>(allComments);
                break;
        }
        
        // Use submitList for smooth DiffUtil animation
        commentAdapter.submitList(filtered);
    }

    private void setupGallery() {
        GalleryAdapter galleryAdapter = new GalleryAdapter(item.getGalleryImages());
        binding.vpGallery.setAdapter(galleryAdapter);
        setupDots(item.getGalleryImages().size());
        
        binding.vpGallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        sliderRunnable = () -> {
            int nextItem = (binding.vpGallery.getCurrentItem() + 1) % item.getGalleryImages().size();
            binding.vpGallery.setCurrentItem(nextItem);
        };
    }

    private void setupDots(int count) {
        binding.layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.dot_inactive);
            binding.layoutDots.addView(dot);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < binding.layoutDots.getChildCount(); i++) {
            ((ImageView) binding.layoutDots.getChildAt(i)).setImageResource(i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void setupComments() {
        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);
        commentAdapter.submitList(new ArrayList<>(allComments));
    }

    private void updateHeartIcon(boolean isFavorite) {
        int color = isFavorite ? ContextCompat.getColor(this, android.R.color.holo_red_dark) : ContextCompat.getColor(this, android.R.color.white);
        binding.btnFavoriteDetail.setImageTintList(ColorStateList.valueOf(color));
    }

    private void animateHeart(View view) {
        view.setScaleX(0.7f); view.setScaleY(0.7f);
        view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction(() -> view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()).start();
    }

    @Override
    protected void onPause() { super.onPause(); sliderHandler.removeCallbacks(sliderRunnable); }
    @Override
    protected void onResume() { super.onResume(); sliderHandler.postDelayed(sliderRunnable, 3000); }
}
