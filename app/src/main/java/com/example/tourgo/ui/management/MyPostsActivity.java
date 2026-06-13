package com.example.tourgo.ui.management;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tourgo.databinding.ActivityMyPostsBinding;
import com.example.tourgo.models.Tour;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.listing.AddListingActivity;
import com.example.tourgo.ui.reviews.ReviewActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsActivity extends AppCompatActivity implements PostAdapter.OnPostActionListener {

    private ActivityMyPostsBinding binding;
    private PostAdapter adapter;
    private List<Tour> postList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupListeners();
        loadMyPosts();
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(postList, this);
        binding.rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyPosts.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.swipeRefresh.setOnRefreshListener(this::loadMyPosts);
        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddListingActivity.class));
        });
    }

    private void loadMyPosts() {
        binding.swipeRefresh.setRefreshing(true);
        // Giả sử token lấy từ SessionManager hoặc tương tự
        String token = "Bearer your_token_here"; 

        RetrofitClient.getService().getMyPosts(token).enqueue(new Callback<List<Tour>>() {
            @Override
            public void onResponse(Call<List<Tour>> call, Response<List<Tour>> response) {
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    postList.clear();
                    postList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    Toast.makeText(MyPostsActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Tour>> call, Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(MyPostsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        binding.emptyState.setVisibility(postList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(Tour post) {
        // Chuyển sang AddListingActivity với chế độ Edit
        Intent intent = new Intent(this, AddListingActivity.class);
        intent.putExtra("POST_ID", post.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Tour post, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to delete this listing?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePostApi(post, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePostApi(Tour post, int position) {
        String token = "Bearer your_token_here";
        RetrofitClient.getService().deletePost(token, post.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    postList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateEmptyState();
                    Toast.makeText(MyPostsActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MyPostsActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewReviews(Tour post) {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra("POST_ID", post.getId());
        intent.putExtra("POST_NAME", post.getName());
        startActivity(intent);
    }

    @Override
    public void onItemClick(Tour post) {
        // Xem chi tiết (nếu có)
    }
}
