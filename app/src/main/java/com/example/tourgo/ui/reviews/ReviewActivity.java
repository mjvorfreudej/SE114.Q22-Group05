package com.example.tourgo.ui.reviews;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tourgo.databinding.ActivityReviewsBinding;
import com.example.tourgo.models.Review;
import com.example.tourgo.remote.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    private ActivityReviewsBinding binding;
    private ReviewAdapter adapter;
    private final List<Review> reviewList = new ArrayList<>();
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postId = getIntent().getStringExtra("POST_ID");
        String postName = getIntent().getStringExtra("POST_NAME");

        if (postName != null) {
            binding.toolbar.setTitle("Reviews for " + postName);
        }

        setupRecyclerView();
        setupListeners();
        loadReviews();
    }

    private void setupRecyclerView() {
        adapter = new ReviewAdapter(reviewList);
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadReviews() {
        RetrofitClient.getService().getPostReviews(postId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Review>> call, @NonNull Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviewList.clear();
                    reviewList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateUI();
                } else {
                    loadMockData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Review>> call, @NonNull Throwable t) {
                loadMockData();
            }
        });
    }

    private void updateUI() {
        if (reviewList.isEmpty()) {
            binding.tvNoReviews.setVisibility(View.VISIBLE);
            binding.rvReviews.setVisibility(View.GONE);
        } else {
            binding.tvNoReviews.setVisibility(View.GONE);
            binding.rvReviews.setVisibility(View.VISIBLE);
            
            // Tính trung bình cộng rating
            float total = 0;
            for (Review r : reviewList) total += r.getRating();
            float avg = total / reviewList.size();
            
            binding.tvAverageRating.setText(String.format(Locale.US, "%.1f", avg));
            binding.ratingBar.setRating(avg);
            String totalReviewsText = "Based on " + reviewList.size() + " reviews";
            binding.tvTotalReviews.setText(totalReviewsText);
        }
    }

    private void loadMockData() {
        reviewList.add(new Review("John Doe", 5f, "Amazing experience!", "Aug 20, 2025"));
        reviewList.add(new Review("Maria Smith", 4f, "Good service, but a bit crowded.", "Aug 18, 2025"));
        reviewList.add(new Review("Alex Wong", 5f, "Highly recommend this tour.", "Aug 15, 2025"));
        adapter.notifyDataSetChanged();
        updateUI();
    }
}
