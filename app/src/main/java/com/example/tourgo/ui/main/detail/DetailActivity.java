package com.example.tourgo.ui.main.detail;
import com.example.tourgo.ui.main.booking.BookingActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.CameraUpdateFactory;
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
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.HotelService;
import com.example.tourgo.remote.service.TourService;
import com.example.tourgo.remote.service.ReviewService;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.remote.service.BookingService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private Hotel hotel;
    private Tour tour;
    private boolean isTourMode = false;
    
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
        
        Serializable data = getIntent().getSerializableExtra("hotel_object");
        if (data instanceof Tour) {
            tour = (Tour) data;
            isTourMode = true;
        } else if (data instanceof Hotel) {
            hotel = (Hotel) data;
            isTourMode = false;
        }

        setupImagePicker();

        if (tour != null || hotel != null) {
            setupUI();
        } else {
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnFavoriteDetail.setOnClickListener(v -> toggleFavorite());

        binding.btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            if (isTourMode) {
                intent.putExtra("tour_item", tour);
            } else {
                intent.putExtra("hotel_item", hotel);
            }
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
        loadReviews();
        checkReviewPermission();
        setupReviewForm();
        setupAmenities();
        setupMap();
        updateRatingSummaryUI();
        refreshSummary();

        if (isTourMode) {
            binding.tvDetailName.setText(tour.getName());
            String loc = tour.getDestination() != null ? tour.getDestination() : tour.getLocation();
            binding.tvDetailLocation.setText(formatShortLocation(loc));
            
            String formattedPrice = tour.formatPrice(this, tour.getPrice());
            binding.tvDetailPrice.setText(formattedPrice);
            binding.tvDetailDescription.setText(tour.getDescription());
            updateHeartIcon(tour.isFavorite());
            
            // Adjust labels for Tour
            binding.tvPriceLabel.setText(R.string.tour_price_label);
        } else {
            binding.tvDetailName.setText(hotel.getName());
            binding.tvDetailLocation.setText(formatShortLocation(hotel.getAddress()));
            
            String formattedPrice = hotel.formatPrice(this, hotel.getPricePerNight());
            binding.tvDetailPrice.setText(getString(R.string.price_per_night_format, formattedPrice));
            binding.tvDetailDescription.setText(hotel.getDescription());
            updateHeartIcon(hotel.isFavorite());
            binding.tvPriceLabel.setText(R.string.detail_staying_price_label);
        }
    }

    private String formatShortLocation(String location) {
        if (location == null || location.isEmpty()) return "";
        String[] parts = location.split(",");
        if (parts.length >= 2) {
            // Lấy 2 vế cuối cùng (thường là Tỉnh/Thành phố và Quốc gia)
            return parts[parts.length - 2].trim() + ", " + parts[parts.length - 1].trim();
        }
        return location.trim();
    }

    private void setupAmenities() {
        LinearLayout container = binding.layoutAmenities;
        container.removeAllViews();

        String amenitiesStr = isTourMode ? tour.getAmenities() : hotel.getAmenities();
        if (amenitiesStr != null && !amenitiesStr.isEmpty()) {
            String[] amenities = amenitiesStr.split(",");
            LayoutInflater inflater = LayoutInflater.from(this);
            for (String amenity : amenities) {
                amenity = amenity.trim();
                if (amenity.isEmpty()) continue;

                View row = inflater.inflate(R.layout.item_amenity, container, false);
                ImageView icon = row.findViewById(R.id.imgAmenity);
                TextView label = row.findViewById(R.id.tvAmenityLabel);

                int iconRes = getAmenityIcon(amenity);
                icon.setImageResource(iconRes);
                label.setText(amenity);
                container.addView(row);
            }
        }

        if (isTourMode && tour.getDuration() != null && !tour.getDuration().isEmpty()) {
            // Check if duration is already in amenities to avoid duplication
            boolean alreadyAdded = false;
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                TextView tv = child.findViewById(R.id.tvAmenityLabel);
                if (tv != null && tv.getText().toString().equalsIgnoreCase(tour.getDuration())) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                addDurationAmenity(container, tour.getDuration());
            }
        }
    }

    private int getAmenityIcon(String amenity) {
        if (amenity == null) return R.drawable.ic_check_circle;
        String lower = amenity.toLowerCase().trim();

        if (lower.contains("wifi") || lower.contains("mạng") || lower.contains("internet")) return R.drawable.ic_wifi;
        if (lower.contains("pool") || lower.contains("hồ bơi") || lower.contains("bể bơi")) return R.drawable.ic_pool;
        if (lower.contains("parking") || lower.contains("đỗ xe") || lower.contains("gửi xe") || lower.contains("garage")) return R.drawable.ic_garage;
        if (lower.contains("work") || lower.contains("gym") || lower.contains("làm việc") || lower.contains("văn phòng")) return R.drawable.ic_workplace;
        if (lower.contains("tour") || lower.contains("guide") || lower.contains("hướng dẫn") || lower.contains("desk")) return R.drawable.ic_tour;
        if (lower.contains("transport") || lower.contains("shuttle") || lower.contains("xe đưa đón") || lower.contains("di chuyển")) return R.drawable.ic_garage;
        if (lower.contains("meal") || lower.contains("ăn") || lower.contains("breakfast") || lower.contains("nhà hàng")) return R.drawable.ic_workplace;
        if (lower.contains("ticket") || lower.contains("vé") || lower.contains("tham quan")) return R.drawable.ic_check_circle;
        if (lower.contains("time") || lower.contains("thời gian") || lower.contains("giờ") || lower.contains("duration")) return R.drawable.ic_time;
        if (lower.contains("security") || lower.contains("an toàn") || lower.contains("bảo vệ")) return R.drawable.ic_shield;
        if (lower.contains("building") || lower.contains("phòng") || lower.contains("tòa nhà")) return R.drawable.ic_building;
        if (lower.contains("user") || lower.contains("khách") || lower.contains("người")) return R.drawable.ic_users;

        return R.drawable.ic_check_circle;
    }

    private void addDurationAmenity(LinearLayout container, String duration) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_amenity, container, false);
        ImageView icon = row.findViewById(R.id.imgAmenity);
        TextView label = row.findViewById(R.id.tvAmenityLabel);
        icon.setImageResource(R.drawable.ic_time);
        label.setText(duration);
        container.addView(row);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment == null) return;

        mapFragment.getMapAsync(googleMap -> {
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            LatLng target = resolveLatLng();
            String title = isTourMode ? tour.getName() : hotel.getName();
            googleMap.addMarker(new MarkerOptions().position(target).title(title));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15f));

            googleMap.setOnMapClickListener(latLng -> {
                String address = isTourMode ? tour.getDestination() : hotel.getAddress();
                Uri gmmIntentUri = Uri.parse("geo:" + target.latitude + "," + target.longitude + "?q=" + Uri.encode(address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, 
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address)));
                    startActivity(webIntent);
                }
            });
        });
    }

    private LatLng resolveLatLng() {

        String addressStr = isTourMode
                ? tour.getDestination()
                : hotel.getAddress();

        Log.d("MAP_DEBUG", "Address = " + addressStr);

        if (!TextUtils.isEmpty(addressStr) && Geocoder.isPresent()) {
            try {
                Geocoder geocoder = new Geocoder(this);

                List<Address> result =
                        geocoder.getFromLocationName(addressStr, 1);

                if (result != null && !result.isEmpty()) {
                    Address a = result.get(0);
                    Log.d("MAP_DEBUG", "Found: " + a.getLatitude() + "," + a.getLongitude());
                    return new LatLng(a.getLatitude(), a.getLongitude());
                }

            } catch (Exception e) {
                Log.e("MAP_DEBUG", e.toString());
            }
        }

        return new LatLng(21.0285,105.8542);
    }
    private void loadReviews() {
        String id = isTourMode ? tour.getId() : hotel.getId();
        DataCallback<List<Review>> callback = new DataCallback<List<Review>>() {
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
            @Override public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> { allReviews = new ArrayList<>(); myReview = null; reviewAdapter.submitList(new ArrayList<>()); });
            }
        };

        if (isTourMode) ReviewService.getReviewsByTourId(this, id, callback);
        else ReviewService.getReviewsByHotelId(this, id, callback);
    }

    private Review findMyReview(List<Review> reviews) {
        if (reviews == null || session == null || session.getUserId() == null) return null;
        String currentUserId = session.getUserId();
        for (Review review : reviews) {
            if (currentUserId.equals(review.getUserId())) return review;
        }
        return null;
    }

    private void bindReviewForm() {
        if (myReview == null) {
            binding.ratingBarReview.setRating(5);
            binding.edtReviewText.setText("");
            binding.btnSubmitReview.setText(R.string.review_submit_button);
        } else {
            binding.ratingBarReview.setRating(myReview.getRating());
            binding.edtReviewText.setText(myReview.getContent());
            binding.btnSubmitReview.setText(R.string.review_update_button);
        }
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

        final boolean currentState = isTourMode ? tour.isFavorite() : hotel.isFavorite();
        final boolean newState = !currentState;
        
        if (isTourMode) tour.setFavorite(newState);
        else hotel.setFavorite(newState);
        
        updateHeartIcon(newState);
        animateHeart(binding.btnFavoriteDetail);

        String userId = session.getUserId();
        String id = isTourMode ? tour.getId() : hotel.getId();

        if (newState) {
            Favorite favorite = isTourMode ? new Favorite(userId, id, null) : new Favorite(userId, null, id);
            FavoriteRepository.getInstance().addFavorite(this, favorite, new DataCallback<Favorite>() {
                @Override public void onSuccess(Favorite data) {}
                @Override public void onError(ApiErrorCode code, String msg) {
                    runOnUiThread(() -> {
                        if (isTourMode) tour.setFavorite(currentState); else hotel.setFavorite(currentState);
                        updateHeartIcon(currentState);
                        Toast.makeText(DetailActivity.this, getString(R.string.err_prefix, msg), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            String favoriteId = isTourMode ? FavoriteRepository.getInstance().findFavoriteIdByTourId(id) : FavoriteRepository.getInstance().findFavoriteIdByHotelId(id);
            if (favoriteId != null) {
                FavoriteRepository.getInstance().removeFavorite(this, favoriteId, new DataCallback<Void>() {
                    @Override public void onSuccess(Void data) {}
                    @Override public void onError(ApiErrorCode code, String msg) {
                        runOnUiThread(() -> {
                            if (isTourMode) tour.setFavorite(currentState); else hotel.setFavorite(currentState);
                            updateHeartIcon(currentState);
                            Toast.makeText(DetailActivity.this, getString(R.string.err_prefix, msg), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        }
    }

    private void updateRatingSummaryUI() {
        float rating = isTourMode ? tour.getRating() : hotel.getRating();
        int count = isTourMode ? tour.getReviewCount() : hotel.getReviewCount();
        binding.tvHeaderRating.setText(String.format(Locale.getDefault(), "★ %.1f (%d %s)",
                rating, count, getString(R.string.reviews_count_label)));
        binding.tvBigRating.setText(String.format(Locale.getDefault(), "%.1f", rating));
        binding.tvReviewCount.setText(String.format(Locale.getDefault(), "%d %s",
                count, getString(R.string.reviews_count_label).toLowerCase()));
    }

    private void refreshSummary() {
        String id = isTourMode ? tour.getId() : hotel.getId();
        if (isTourMode) {
            TourService.getTourDetail(this, id, new DataCallback<Tour>() {
                @Override public void onSuccess(Tour fresh) {
                    runOnUiThread(() -> { if (fresh != null) { fresh.setFavorite(tour.isFavorite()); tour = fresh; updateRatingSummaryUI(); setupAmenities(); } });
                }
                @Override public void onError(ApiErrorCode code, String msg) {}
            });
        } else {
            HotelService.getHotelDetail(this, id, new DataCallback<Hotel>() {
                @Override public void onSuccess(Hotel fresh) {
                    runOnUiThread(() -> { if (fresh != null) { fresh.setFavorite(hotel.isFavorite()); hotel = fresh; updateRatingSummaryUI(); setupAmenities(); } });
                }
                @Override public void onError(ApiErrorCode code, String msg) {}
            });
        }
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
            if ("all".equals(type)) filtered.add(review);
            else if ("photos".equals(type) && review.hasImages()) filtered.add(review);
            else {
                try {
                    int rating = Integer.parseInt(type);
                    if ((int) review.getRating() == rating) filtered.add(review);
                } catch (NumberFormatException ignored) {}
            }
        }
        sortCurrentUserReviewFirst(filtered);
        reviewAdapter.submitList(filtered);
    }

    private void setupGallery() {
        List<String> imageUrls = isTourMode ? tour.getImageUrls() : hotel.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = new ArrayList<>();
            imageUrls.add("android.resource://" + getPackageName() + "/" + (isTourMode ? R.drawable.banner_travel : R.drawable.hotel_1));
        }
        final List<String> images = imageUrls;
        binding.vpGallery.setAdapter(new GalleryAdapter(images));
        setupDots(images.size());
        binding.vpGallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int pos) {
                super.onPageSelected(pos); updateDots(pos);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
        sliderRunnable = () -> binding.vpGallery.setCurrentItem((binding.vpGallery.getCurrentItem() + 1) % images.size(), true);
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

    private void setupReviewForm() {
        binding.btnPickReviewImages.setOnClickListener(v -> openImagePicker());
        binding.btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
            Intent data = result.getData();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
            } else if (data.getData() != null) selectedImageUris.add(data.getData());
            updateSelectedImagesText();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImagesLauncher.launch(Intent.createChooser(intent, getString(R.string.review_pick_images_title)));
    }

    private void updateSelectedImagesText() {
        int count = selectedImageUris.size();
        if (count == 0) binding.tvSelectedReviewImages.setVisibility(View.GONE);
        else {
            binding.tvSelectedReviewImages.setVisibility(View.VISIBLE);
            binding.tvSelectedReviewImages.setText(getString(R.string.review_selected_images_count, count));
        }
    }

    private void checkReviewPermission() {
        String id = isTourMode ? tour.getId() : hotel.getId();
        if (id == null || !session.isLoggedIn()) {
            binding.layoutWriteReview.setVisibility(View.GONE);
            binding.tvReviewPermissionMessage.setVisibility(View.VISIBLE);
            return;
        }

        DataCallback<Boolean> callback = new DataCallback<Boolean>() {
            @Override public void onSuccess(Boolean hasBooked) {
                runOnUiThread(() -> {
                    binding.layoutWriteReview.setVisibility(hasBooked ? View.VISIBLE : View.GONE);
                    binding.tvReviewPermissionMessage.setVisibility(hasBooked ? View.GONE : View.VISIBLE);
                });
            }
            @Override public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> { binding.layoutWriteReview.setVisibility(View.GONE); binding.tvReviewPermissionMessage.setVisibility(View.VISIBLE); });
            }
        };

        if (isTourMode) BookingService.hasBookedTour(this, id, callback);
        else BookingService.hasBookedHotel(this, id, callback);
    }

    private void submitReview() {
        String id = isTourMode ? tour.getId() : hotel.getId();
        if (id == null || !session.isLoggedIn()) return;

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

        if (myReview != null && myReview.getId() != null) {
            ReviewService.updateReview(this, myReview.getId(), isTourMode ? "tour" : "hotel", reviewText, stars, new DataCallback<Void>() {
                @Override public void onSuccess(Void data) { uploadSelectedReviewImages(myReview.getId(), R.string.review_update_success); }
                @Override public void onError(ApiErrorCode code, String msg) { runOnUiThread(() -> { binding.btnSubmitReview.setEnabled(true); Toast.makeText(DetailActivity.this, R.string.review_update_error, Toast.LENGTH_SHORT).show(); }); }
            });
            return;
        }

        ReviewService.createReview(this, isTourMode ? null : id, isTourMode ? id : null, reviewText, stars, new DataCallback<String>() {
            @Override public void onSuccess(String reviewId) { uploadSelectedReviewImages(reviewId, R.string.review_submit_success); }
            @Override public void onError(ApiErrorCode code, String msg) { runOnUiThread(() -> { binding.btnSubmitReview.setEnabled(true); Toast.makeText(DetailActivity.this, R.string.review_submit_error, Toast.LENGTH_SHORT).show(); }); }
        });
    }

    private void uploadSelectedReviewImages(String reviewId, int successMessageRes) {
        if (selectedImageUris.isEmpty()) { runOnUiThread(() -> completeReviewSave(successMessageRes)); return; }
        final int total = selectedImageUris.size();
        final int[] completed = {0};
        final boolean[] hasError = {false};
        final List<String> uploadedUrls = new ArrayList<>();

        for (Uri imageUri : selectedImageUris) {
            ReviewService.uploadReviewImage(this, imageUri, reviewId, new DataCallback<String>() {
                @Override public void onSuccess(String imageUrl) {
                    synchronized (uploadedUrls) { uploadedUrls.add(imageUrl); }
                    markImageUploadDone(completed, hasError, total, successMessageRes, reviewId, uploadedUrls);
                }
                @Override public void onError(ApiErrorCode code, String msg) { hasError[0] = true; markImageUploadDone(completed, hasError, total, successMessageRes, reviewId, uploadedUrls); }
            });
        }
    }

    private void markImageUploadDone(int[] completed, boolean[] hasError, int total, int successMessageRes, String reviewId, List<String> uploadedUrls) {
        synchronized (completed) { completed[0]++; if (completed[0] < total) return; }
        if (!uploadedUrls.isEmpty()) {
            ReviewService.saveReviewImages(this, isTourMode ? "tour" : "hotel", reviewId, uploadedUrls, new DataCallback<Void>() {
                @Override public void onSuccess(Void data) { runOnUiThread(() -> completeReviewSave(hasError[0] ? R.string.review_image_upload_partial_error : successMessageRes)); }
                @Override public void onError(ApiErrorCode code, String message) { runOnUiThread(() -> completeReviewSave(R.string.review_image_upload_partial_error)); }
            });
        } else runOnUiThread(() -> completeReviewSave(hasError[0] ? R.string.review_image_upload_partial_error : successMessageRes));
    }

    private void completeReviewSave(int messageRes) {
        binding.btnSubmitReview.setEnabled(true);
        selectedImageUris.clear();
        updateSelectedImagesText();
        Toast.makeText(DetailActivity.this, messageRes, Toast.LENGTH_SHORT).show();
        loadReviews();
        refreshSummary();
    }

    private void setupComments() {
        String currentUserId = session != null ? session.getUserId() : null;
        reviewAdapter = new ReviewAdapter(currentUserId, new ReviewAdapter.ReviewActionListener() {
            @Override public void onDelete(Review review) { deleteReview(review); }
            @Override public void onEdit(Review review) {}
        });
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(reviewAdapter);
    }

    private void deleteReview(Review review) {
        if (review == null || review.getId() == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.review_delete_title)
                .setMessage(R.string.review_delete_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_review, (dialog, which) -> {
                    ReviewService.deleteReview(this, review.getId(), isTourMode ? "tour" : "hotel", new DataCallback<Void>() {
                        @Override public void onSuccess(Void data) { runOnUiThread(() -> { Toast.makeText(DetailActivity.this, R.string.review_delete_success, Toast.LENGTH_SHORT).show(); loadReviews(); refreshSummary(); checkReviewPermission(); }); }
                        @Override public void onError(ApiErrorCode code, String msg) { runOnUiThread(() -> Toast.makeText(DetailActivity.this, R.string.review_delete_error, Toast.LENGTH_SHORT).show()); }
                    });
                }).show();
    }

    @Override protected void onPause() { super.onPause(); sliderHandler.removeCallbacks(sliderRunnable); }
    @Override protected void onResume() { super.onResume(); if (sliderRunnable != null) sliderHandler.postDelayed(sliderRunnable, 3000); }
}
