package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.data.TourRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Tour;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TourlistActivity extends AppCompatActivity {

    private RecyclerView recyclerTours;
    private TourAdapter adapter;
    private ProgressBar progressBar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_list);

        session = new SessionManager(this);
        progressBar = findViewById(R.id.pbTourList);

        recyclerTours = findViewById(R.id.recyclerTours);
        recyclerTours.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new TourAdapter(new ArrayList<>());
        adapter.setOnTourClickListener(tour -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra(BookingActivity.EXTRA_TOUR, tour);
            startActivity(intent);
        });
        recyclerTours.setAdapter(adapter);

        loadTours();
    }

    private void loadTours() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String userId = session.getUserId();
        String token = session.getAccessToken();

        TourRepository.getInstance().loadTours(userId, token, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (data != null) {
                        adapter.setData(data);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(TourlistActivity.this, R.string.err_network, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}