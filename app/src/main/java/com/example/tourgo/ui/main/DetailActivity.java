package com.example.tourgo.ui.main;

import static com.example.tourgo.remote.HotelReviewService.deleteHotelReview;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import com.example.tourgo.remote.HotelReviewService;
import com.example.tourgo.utils.SessionManager;
import com.example.tourgo.remote.BookingService;
import com.example.tourgo.remote.HotelReviewService;

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
    private Comment myReview = null;
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
        loadHotelReviews();
        checkReviewPermission();
        setupReviewForm();
        updateRatingSummary();
        
        binding.tvDetailName.setText(hotel.getName());
        binding.tvDetailLocation.setText(hotel.getAddress());
        
        // SỬA TẠI ĐÂY: Truyền 'this' để định dạng đúng VND/USD theo cài đặt Profile
        String formattedPrice = hotel.formatPrice(this, hotel.getPricePerNight());
        binding.tvDetailPrice.setText(getString(R.string.price_per_night_format, formattedPrice));
        
        binding.tvDetailDescription.setText(hotel.getDescription());

        updateHeartIcon(hotel.isFavorite());
    }

    private void loadHotelReviews() {
        if (hotel == null || hotel.getId() == null) return;

        HotelReviewService.getReviewsByHotelId(
                hotel.getId(),
                session.getAccessToken(),
                new DataCallback<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> data) {
                        runOnUiThread(() -> {
                            allComments = data != null ? data : new ArrayList<>();
                            sortCurrentUserReviewFirst(allComments);

                            myReview = findMyReview(allComments);
                            bindReviewForm();

                            commentAdapter.submitList(new ArrayList<>(allComments));
                        });
                    }

                    @Override
                    public void onError(ApiErrorCode code, String rawMessage) {
                        runOnUiThread(() -> {
                            allComments = new ArrayList<>();
                            myReview = null;
                            commentAdapter.submitList(new ArrayList<>());
                        });
                    }
                }
        );
    }

    private Comment findMyReview(List<Comment> comments) {
        if (comments == null || session == null || session.getUserId() == null) return null;

        String currentUserId = session.getUserId();

        for (Comment comment : comments) {
            if (currentUserId.equals(comment.getUserId())) {
                return comment;
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

    private void sortCurrentUserReviewFirst(List<Comment> comments) {
        if (session == null || session.getUserId() == null || comments == null) return;

        String currentUserId = session.getUserId();

        comments.sort((c1, c2) -> {
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
        List<Comment> filtered = new ArrayList<>();

        for (Comment comment : allComments) {
            if ("all".equals(type)) {
                filtered.add(comment);
            } else if ("photos".equals(type) && comment.hasImages()) {
                filtered.add(comment);
            } else {
                try {
                    int rating = Integer.parseInt(type);
                    if ((int) comment.getRating() == rating) {
                        filtered.add(comment);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        sortCurrentUserReviewFirst(filtered);
        commentAdapter.submitList(filtered);
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

    private void setupReviewForm() {
        binding.btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void checkReviewPermission() {
        if (hotel == null || hotel.getId() == null || !session.isLoggedIn()) {
            binding.layoutWriteReview.setVisibility(View.GONE);
            binding.tvReviewPermissionMessage.setVisibility(View.VISIBLE);
            return;
        }

        BookingService.hasBookedHotel(
                session.getUserId(),
                hotel.getId(),
                session.getAccessToken(),
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
            HotelReviewService.updateHotelReview(
                    myReview.getId(),
                    stars,
                    reviewText,
                    session.getAccessToken(),
                    new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            runOnUiThread(() -> {
                                binding.btnSubmitReview.setEnabled(true);
                                Toast.makeText(DetailActivity.this, R.string.review_update_success, Toast.LENGTH_SHORT).show();
                                loadHotelReviews();
                            });
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

        HotelReviewService.createHotelReview(
                hotel.getId(),
                session.getUserId(),
                stars,
                reviewText,
                session.getAccessToken(),
                new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            binding.btnSubmitReview.setEnabled(true);
                            Toast.makeText(DetailActivity.this, R.string.review_submit_success, Toast.LENGTH_SHORT).show();
                            loadHotelReviews();
                        });
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

    private void setupComments() {
        String currentUserId = session != null ? session.getUserId() : null;

        commentAdapter = new CommentAdapter(currentUserId, new CommentAdapter.ReviewActionListener() {
            @Override
            public void onDelete(Comment comment) {
                deleteReview(comment);
            }

            @Override
            public void onEdit(Comment comment) {}
        });

        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);
    }

    private void deleteReview(Comment comment) {
        if (comment == null || comment.getId() == null || comment.getId().isEmpty()) {
            Toast.makeText(this, R.string.review_delete_error, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.review_delete_title)
                .setMessage(R.string.review_delete_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.review_action_delete, (dialog, which) -> {
                    HotelReviewService.deleteHotelReview(
                            comment.getId(),
                            session.getAccessToken(),
                            new DataCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(DetailActivity.this, R.string.review_delete_success,
                                                Toast.LENGTH_SHORT).show();
                                        loadHotelReviews();
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

        myReview = null;
        bindReviewForm();
        loadHotelReviews();
        checkReviewPermission();
    }

    private void updateReview(String reviewId, int stars, String reviewText) {
        HotelReviewService.updateHotelReview(
                reviewId,
                stars,
                reviewText,
                session.getAccessToken(),
                new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, R.string.review_update_success,
                                    Toast.LENGTH_SHORT).show();
                            loadHotelReviews();
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

    private void showEditReviewDialog(Comment comment) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, 0);

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setRating(comment.getRating());

        EditText edtReview = new EditText(this);
        edtReview.setMinLines(4);
        edtReview.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        edtReview.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edtReview.setText(comment.getContent());

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

                    updateReview(comment.getId(), stars, reviewText);
                })
                .show();
    }
}
