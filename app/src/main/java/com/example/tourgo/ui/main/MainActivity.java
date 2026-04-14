package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.adapters.TrendingHotelAdapter;
import com.example.tourgo.data.AppFakeData;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRecyclerViews();
        setupNavigation();
    }

    private void setupNavigation() {
        findViewById(R.id.navTours).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TourlistActivity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        findViewById(R.id.btnFind).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HotelListAcitivity.class));
        });

        findViewById(R.id.tvSeeAllHotels).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HotelListAcitivity.class));
        });

        findViewById(R.id.tvSeeAllTours).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TourlistActivity.class));
        });
    }

    private void setupRecyclerViews() {
        // Popular Hotels
        RecyclerView rvPopular = findViewById(R.id.rvPopularHotels);
        rvPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPopular.setAdapter(new PopularHotelAdapter(AppFakeData.getPopularHotelItems()));

        // Trending Hotels
        RecyclerView rvTrending = findViewById(R.id.rvTrendingHotels);
        rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTrending.setAdapter(new TrendingHotelAdapter(AppFakeData.getTrendingHotelItems()));
    }
}