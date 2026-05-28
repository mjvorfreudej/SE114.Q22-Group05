package com.example.tourgo.ui.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.tourgo.R;
import com.example.tourgo.adapters.ReviewAdapter;
import com.example.tourgo.adapters.GalleryAdapter;
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.databinding.ActivityDetailBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Review;
import com.example.tourgo.models.response.Favorite;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.remote.service.HotelService;
import com.example.tourgo.remote.service.ReviewService;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.remote.service.BookingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private Hotel hotel;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private ReviewAdapter reviewAdapter;
    private List<Review> allReviews = new ArrayList<>();
    private Review myReview = null;
    private SessionManager session;
    private ActivityResultLauncher<Intent> pickImagesLauncher;
    private final List<Uri> selectedImageUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyStatusBarInset();

        session = new SessionManager(this);
        hotel = (Hotel) getIntent().getSerializableExtra("hotel_object");
        setupImagePicker();

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

    private void applyStatusBarInset() {
        View actions = binding.layoutHeaderActions;
        final int actionsBaseTop = actions.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(actions, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), actionsBaseTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        View bottomBar = binding.layoutBottomBar;
        final int bottomBaseBottom = bottomBar.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(bottomBar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomBaseBottom + bars.bottom);
            return insets;
        });

        ViewCompat.requestApplyInsets(actions);
        ViewCompat.requestApplyInsets(bottomBar);
    }

    private void setupUI() {
        setupGallery();
        setupComments();
        loadHotelReviews();
        checkReviewPermission();
        setupReviewForm();
        setupAmenities();
        setupMap();
        updateRatingSummary();
        refreshHotelSummary();

        binding.tvDetailName.setText(hotel.getName());
        binding.tvDetailLocation.setText(hotel.getAddress());

        // SỬA TẠI ĐÂY: Truyền 'this' để định dạng đúng VND/USD theo cài đặt Profile
        String formattedPrice = hotel.formatPrice(this, hotel.getPricePerNight());
        binding.tvDetailPrice.setText(getString(R.string.price_per_night_format, formattedPrice));

        binding.tvDetailDescription.setText(hotel.getDescription());

        updateHeartIcon(hotel.isFavorite());
    }

    private void setupAmenities() {
        LinearLayout container = binding.layoutAmenities;
        container.removeAllViews();

        int[][] items = new int[][] {
                {R.drawable.ic_wifi, R.string.amenity_wifi},
                {R.drawable.ic_garage, R.string.amenity_parking},
                {R.drawable.ic_pool, R.string.amenity_pool},
                {R.drawable.ic_workplace, R.string.amenity_workplace},
                {R.drawable.ic_tour, R.string.amenity_tour_desk}
        };

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int[] item : items) {
            View row = inflater.inflate(R.layout.item_amenity, container, false);
            ImageView icon = row.findViewById(R.id.imgAmenity);
            TextView label = row.findViewById(R.id.tvAmenityLabel);
            icon.setImageResource(item[0]);
            label.setText(item[1]);
            container.addView(row);
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment == null) return;

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(false);

                LatLng target = resolveHotelLatLng();
                String title = hotel != null && !TextUtils.isEmpty(hotel.getName()) ? hotel.getName() : "Location";
                googleMap.addMarker(new MarkerOptions().position(target).title(title));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 14f));
            }
        });
    }

    private LatLng resolveHotelLatLng() {
        if (hotel != null && hotel.hasCoordinates()) {
            return new LatLng(hotel.getLatitude(), hotel.getLongitude());
        }

        if (hotel != null && !TextUtils.isEmpty(hotel.getAddress()) && Geocoder.isPresent()) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> matches = geocoder.getFromLocationName(hotel.getAddress(), 1);
                if (matches != null && !matches.isEmpty()) {
                    Address a = matches.get(0);
                    return new LatLng(a.getLatitude(), a.getLongitude());
                }
            } catch (IOException e) {
                Log.w("DetailActivity", "Geocoder failed: " + e.getMessage());
            }
        }

        // Fallback: Hanoi city center
        return new LatLng(21.0285, 105.8542);
    }

    private void loadHotelReviews() {
        if (hotel == null || hotel.getId() == null) return;

        ReviewService.getReviewsByHotelId(
                this,
                hotel.getId(),
                new DataCallback<List<Review>>() {
                    @Override
                    public void onSuccess(List<Review> data) {
                        runOnUiThread(() -> {
                            allReviews = data != null ? data : new ArrayList<>();
                            sortCurrentUserReviewFirst(allReviews);

                            myReview = findMyReview(allReviews);
                            bindReviewForm();

                            reviewAdapter.submitList(new ArrayList<>(allReviews));
                        });
                    }

                    @Override
                    public void onError(ApiErrorCode code, String rawMessage) {
                        runOnUiThread(() -> {
                            allReviews = new ArrayList<>();
                            myReview = null;
                            reviewAdapter.submitList(new ArrayList<>());
                        });
                    }
                }
        );
    }

    private Review findMyReview(List<Review> reviews) {
        if (reviews == null || session == null || session.getUserId() == null) return null;

        String currentUserId = session.getUserId();

        for (Review review : reviews) {
            if (currentUserId.equals(review.getUserId())) {
                return review;
            }
        }

        return null;
    }

    private void bindReviewForm() {
        if (myReview == null) {
            binding.ratingBarReview.setRating(5);
            binding.edtReviewText.setText("");
            binding.btnSubmitReview.setText(R.string.review_submit_button);
            return;
        }

        binding.ratingBarReview.setRating(myReview.getRating());
        binding.edtReviewText.setText(myReview.getContent());
        binding.btnSubmitReview.setText(R.string.review_update_button);
    }

    private void sortCurrentUserReviewFirst(List<Review> reviews) {
        if (session == null || session.getUserId() == null || reviews == null) return;

        String currentUserId = session.getUserId();

        reviews.sort((c1, c2) -> {
            boolean c1IsMine = currentUserId.equals(c1.getUserId());
            boolean c2IsMine = currentUserId.equals(c2.getUserId());

            if (c1IsMine && !c2IsMine) return -1;
            if (!c1IsMine && c2IsMine) return 1;
            return 0;
        });
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
            FavoriteRepository.getInstance().addFavorite(this, favorite, new DataCallback<Void>() {
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
            String favoriteId = FavoriteRepository.getInstance().findFavoriteIdByHotelId(hotelId);
            if (favoriteId != null) {
                FavoriteRepository.getInstance().removeFavorite(this, favoriteId, new DataCallback<Void>() {
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
    }

    private void updateRatingSummary() {
        if (hotel == null) return;
        updateRatingSummary(hotel.getRating(), hotel.getReviewCount());
    }

    private void updateRatingSummary(float rating, int reviewCount) {
        binding.tvHeaderRating.setText(String.format(Locale.getDefault(), "★ %.1f (%d %s)",
                rating, reviewCount, getString(R.string.reviews_count_label)));
        binding.tvBigRating.setText(String.format(Locale.getDefault(), "%.1f", rating));
        binding.tvReviewCount.setText(String.format(Locale.getDefault(), "%d %s",
                reviewCount, getString(R.string.reviews_count_label).toLowerCase()));
    }

    private void refreshHotelSummary() {
        if (hotel == null || hotel.getId() == null) return;

        HotelService.getHotelDetail(this, hotel.getId(), new DataCallback<Hotel>() {
            @Override
            public void onSuccess(Hotel freshHotel) {
                runOnUiThread(() -> {
                    if (freshHotel == null) return;
                    freshHotel.setFavorite(hotel.isFavorite());
                    hotel = freshHotel;
                    updateRatingSummary();
                });
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                Log.e("DetailActivity", "Can not refresh hotel summary: " + rawMessage);
            }
        });
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
        List<Review> filtered = new ArrayList<>();

        for (Review review : allReviews) {
            if ("all".equals(type)) {
                filtered.add(review);
            } else if ("photos".equals(type) && review.hasImages()) {
                filtered.add(review);
            } else {
                try {
                    int rating = Integer.parseInt(type);
                    if ((int) review.getRating() == rating) {
                        filtered.add(review);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        sortCurrentUserReviewFirst(filtered);
        reviewAdapter.submitList(filtered);
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

    private void updateHeartIcon(boolean isFavorite) {
        binding.btnFavoriteDetail.setImageResource(isFavorite ? R.drawable.ic_heart_fullfilled : R.drawable.ic_heart_outline_18);
        int color = ContextCompat.getColor(this, isFavorite ? R.color.red : android.R.color.white);
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

    private void setupReviewForm() {
        binding.btnPickReviewImages.setOnClickListener(v -> openImagePicker());
        binding.btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        selectedImageUris.add(data.getData());
                    }

                    updateSelectedImagesText();
                }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImagesLauncher.launch(Intent.createChooser(intent, getString(R.string.review_pick_images_title)));
    }

    private void updateSelectedImagesText() {
        int count = selectedImageUris.size();
        if (count == 0) {
            binding.tvSelectedReviewImages.setVisibility(View.GONE);
            return;
        }

        binding.tvSelectedReviewImages.setVisibility(View.VISIBLE);
        binding.tvSelectedReviewImages.setText(getString(R.string.review_selected_images_count, count));
    }

    private void checkReviewPermission() {
        if (hotel == null || hotel.getId() == null || !session.isLoggedIn()) {
            binding.layoutWriteReview.setVisibility(View.GONE);
            binding.tvReviewPermissionMessage.setVisibility(View.VISIBLE);
            return;
        }

        BookingService.hasBookedHotel(
                this,
                hotel.getId(),
                new DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean hasBooked) {
                        runOnUiThread(() -> {
                            binding.layoutWriteReview.setVisibility(hasBooked ? View.VISIBLE : View.GONE);
                            binding.tvReviewPermissionMessage.setVisibility(hasBooked ? View.GONE : View.VISIBLE);
                        });
                    }

                    @Override
                    public void onError(ApiErrorCode code, String rawMessage) {
                        runOnUiThread(() -> {
                            binding.layoutWriteReview.setVisibility(View.GONE);
                            binding.tvReviewPermissionMessage.setVisibility(View.VISIBLE);
                        });
                    }
                }
        );
    }

    private void submitReview() {
        if (hotel == null || hotel.getId() == null || !session.isLoggedIn()) return;

        int stars = (int) binding.ratingBarReview.getRating();
        String reviewText = binding.edtReviewText.getText().toString().trim();

        if (stars < 1 || stars > 5) {
            Toast.makeText(this, R.string.review_rating_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (reviewText.isEmpty()) {
            Toast.makeText(this, R.string.review_text_required, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSubmitReview.setEnabled(false);

        if (myReview != null && myReview.getId() != null && !myReview.getId().isEmpty()) {
            ReviewService.updateReview(
                    this,
                    myReview.getId(),
                    "hotel",
                    reviewText,
                    stars,
                    new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            uploadSelectedReviewImages(myReview.getId(), R.string.review_update_success);
                        }

                        @Override
                        public void onError(ApiErrorCode code, String rawMessage) {
                            runOnUiThread(() -> {
                                binding.btnSubmitReview.setEnabled(true);
                                Toast.makeText(DetailActivity.this, R.string.review_update_error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
            );
            return;
        }

        ReviewService.createReview(
                this,
                hotel.getId(),
                null,
                reviewText,
                stars,
                new DataCallback<String>() {
                    @Override
                    public void onSuccess(String reviewId) {
                        uploadSelectedReviewImages(reviewId, R.string.review_submit_success);
                    }

                    @Override
                    public void onError(ApiErrorCode code, String rawMessage) {
                        runOnUiThread(() -> {
                            binding.btnSubmitReview.setEnabled(true);
                            Toast.makeText(DetailActivity.this, R.string.review_submit_error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void uploadSelectedReviewImages(String reviewId, int successMessageRes) {
        if (selectedImageUris.isEmpty()) {
            runOnUiThread(() -> completeReviewSave(successMessageRes));
            return;
        }

        final int total = selectedImageUris.size();
        final int[] completed = {0};
        final boolean[] hasError = {false};
        final List<String> uploadedUrls = new ArrayList<>();
        List<Uri> imagesToUpload = new ArrayList<>(selectedImageUris);

        for (Uri imageUri : imagesToUpload) {
            ReviewService.uploadReviewImage(
                    this,
                    imageUri,
                    reviewId,
                    new DataCallback<String>() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            synchronized (uploadedUrls) {
                                uploadedUrls.add(imageUrl);
                            }
                            markImageUploadDone(completed, hasError, total, successMessageRes, reviewId, uploadedUrls);
                        }

                        @Override
                        public void onError(ApiErrorCode code, String rawMessage) {
                            hasError[0] = true;
                            markImageUploadDone(completed, hasError, total, successMessageRes, reviewId, uploadedUrls);
                        }
                    }
            );
        }
    }

    private void markImageUploadDone(int[] completed, boolean[] hasError, int total, int successMessageRes, String reviewId, List<String> uploadedUrls) {
        synchronized (completed) {
            completed[0]++;
            if (completed[0] < total) return;
        }

        if (!uploadedUrls.isEmpty()) {
            ReviewService.saveReviewImages(
                    this,
                    "hotel",
                    reviewId,
                    uploadedUrls,
                    new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            runOnUiThread(() -> {
                                int messageRes = hasError[0] ? R.string.review_image_upload_partial_error : successMessageRes;
                                completeReviewSave(messageRes);
                            });
                        }

                        @Override
                        public void onError(ApiErrorCode code, String message) {
                            runOnUiThread(() -> completeReviewSave(R.string.review_image_upload_partial_error));
                        }
                    }
            );
        } else {
            runOnUiThread(() -> {
                int messageRes = hasError[0] ? R.string.review_image_upload_partial_error : successMessageRes;
                completeReviewSave(messageRes);
            });
        }
    }

    private void completeReviewSave(int messageRes) {
        binding.btnSubmitReview.setEnabled(true);
        selectedImageUris.clear();
        updateSelectedImagesText();
        Toast.makeText(DetailActivity.this, messageRes, Toast.LENGTH_SHORT).show();
        loadHotelReviews();
        refreshHotelSummary();
    }

    private void setupComments() {
        String currentUserId = session != null ? session.getUserId() : null;

        reviewAdapter = new ReviewAdapter(currentUserId, new ReviewAdapter.ReviewActionListener() {
            @Override
            public void onDelete(Review review) {
                deleteReview(review);
            }

            @Override
            public void onEdit(Review review) {}
        });

        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(reviewAdapter);
    }

    private void deleteReview(Review review) {
        if (review == null || review.getId() == null || review.getId().isEmpty()) {
            Toast.makeText(this, R.string.review_delete_error, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.review_delete_title)
                .setMessage(R.string.review_delete_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_review, (dialog, which) -> {
                    ReviewService.deleteReview(
                            DetailActivity.this,
                            review.getId(),
                            "hotel",
                            new DataCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(DetailActivity.this, R.string.review_delete_success,
                                                Toast.LENGTH_SHORT).show();
                                        loadHotelReviews();
                                        refreshHotelSummary();
                                        checkReviewPermission();
                                    });
                                }

                                @Override
                                public void onError(ApiErrorCode code, String rawMessage) {
                                    runOnUiThread(() ->
                                            Toast.makeText(DetailActivity.this, R.string.review_delete_error,
                                                    Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                    );
                })
                .show();
    }

    private void updateReview(String reviewId, int stars, String reviewText) {
        ReviewService.updateReview(
                this,
                reviewId,
                "hotel",
                reviewText,
                stars,
                new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, R.string.review_update_success,
                                    Toast.LENGTH_SHORT).show();
                            loadHotelReviews();
                            refreshHotelSummary();
                        });
                    }

                    @Override
                    public void onError(ApiErrorCode code, String rawMessage) {
                        runOnUiThread(() ->
                                Toast.makeText(DetailActivity.this, R.string.review_update_error,
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                }
        );
    }

    private void showEditReviewDialog(Review review) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, 0);

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setRating(review.getRating());

        EditText edtReview = new EditText(this);
        edtReview.setMinLines(4);
        edtReview.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        edtReview.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edtReview.setText(review.getContent());

        container.addView(ratingBar);
        container.addView(edtReview);

        new AlertDialog.Builder(this)
                .setTitle(R.string.review_edit_title)
                .setView(container)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.review_update_button, (dialog, which) -> {
                    int stars = (int) ratingBar.getRating();
                    String reviewText = edtReview.getText().toString().trim();

                    if (reviewText.isEmpty()) {
                        Toast.makeText(this, R.string.review_text_required, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateReview(review.getId(), stars, reviewText);
                })
                .show();
    }
}
