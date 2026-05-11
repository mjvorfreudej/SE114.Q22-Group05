package com.example.tourgo.ui.main;

import android.content.Intent;
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
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Comment;
import com.example.tourgo.models.Favorite;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.remote.FavoriteService;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private Hotel hotel;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private CommentAdapter commentAdapter;
    private List<Comment> allComments = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        session = new SessionManager(this);
        hotel = (Hotel) getIntent().getSerializableExtra("hotel_object");

        if (hotel != null) {
            setupUI();
        } else {
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnFavoriteDetail.setOnClickListener(v -> toggleFavorite());

        binding.btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("hotel_item", hotel);
            startActivity(intent);
        });

        setupFilters();
    }

    private void setupUI() {
        setupGallery();
        setupComments();
        updateRatingSummary();
        
        binding.tvDetailName.setText(hotel.getName());
        binding.tvDetailLocation.setText(hotel.getAddress());
        
        // SỬA TẠI ĐÂY: Truyền 'this' để định dạng đúng VND/USD theo cài đặt Profile
        String formattedPrice = hotel.formatPrice(this, hotel.getPricePerNight());
        binding.tvDetailPrice.setText(getString(R.string.price_per_night_format, formattedPrice));
        
        binding.tvDetailDescription.setText(hotel.getDescription());

        updateHeartIcon(hotel.isFavorite());
    }

    private void toggleFavorite() {
        if (!session.isLoggedIn()) {
            Toast.makeText(this, R.string.err_login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean currentState = hotel.isFavorite();
        final boolean newState = !currentState;
        
        hotel.setFavorite(newState);
        updateHeartIcon(newState);
        animateHeart(binding.btnFavoriteDetail);

        final String userId = session.getUserId();
        final String token = session.getAccessToken();
        final String hotelId = hotel.getId();

        if (newState) {
            Favorite favorite = new Favorite(userId, null, hotelId);
            FavoriteService.addFavorite(favorite, token, new DataCallback<Void>() {
                @Override public void onSuccess(Void data) {}
                @Override public void onError(ApiErrorCode code, String msg) {
                    runOnUiThread(() -> {
                        hotel.setFavorite(currentState);
                        updateHeartIcon(currentState);
                        Toast.makeText(DetailActivity.this, getString(R.string.err_prefix, msg), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            FavoriteService.removeFavoriteHotel(userId, hotelId, token, new DataCallback<Void>() {
                @Override public void onSuccess(Void data) {}
                @Override public void onError(ApiErrorCode code, String msg) {
                    runOnUiThread(() -> {
                        hotel.setFavorite(currentState);
                        updateHeartIcon(currentState);
                        Toast.makeText(DetailActivity.this, getString(R.string.err_prefix, msg), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void updateRatingSummary() {
        binding.tvHeaderRating.setText(String.format(Locale.getDefault(), "★ %.1f (%d %s)", 
                hotel.getRating(), hotel.getReviewCount(), getString(R.string.reviews_count_label)));
        binding.tvBigRating.setText(String.format(Locale.getDefault(), "%.1f", hotel.getRating()));
        binding.tvReviewCount.setText(String.format(Locale.getDefault(), "%d %s", 
                hotel.getReviewCount(), getString(R.string.reviews_count_label).toLowerCase()));
    }

    private void setupFilters() {
        binding.cgFilterRating.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) filterComments("all");
            else if (checkedId == R.id.chipWithPhotos) filterComments("photos");
            else if (checkedId == R.id.chip5) filterComments("5");
            else if (checkedId == R.id.chip4) filterComments("4");
            else if (checkedId == R.id.chip3) filterComments("3");
            else if (checkedId == R.id.chip2) filterComments("2");
            else if (checkedId == R.id.chip1) filterComments("1");
        });
    }

    private void filterComments(String type) {
        commentAdapter.submitList(new ArrayList<>(allComments));
    }

    private void setupGallery() {
        List<String> imageUrls = hotel.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = new ArrayList<>();
            imageUrls.add("android.resource://" + getPackageName() + "/" + R.drawable.hotel_1);
        }
        final List<String> images = imageUrls;
        
        GalleryAdapter galleryAdapter = new GalleryAdapter(images);
        binding.vpGallery.setAdapter(galleryAdapter);
        
        setupDots(images.size());
        
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
            int current = binding.vpGallery.getCurrentItem();
            int next = (current + 1) % images.size();
            binding.vpGallery.setCurrentItem(next, true);
        };
    }

    private void setupDots(int count) {
        binding.layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
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
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }
}
