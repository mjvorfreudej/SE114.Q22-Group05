package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tourgo.R;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Tour;
import com.example.tourgo.remote.TourService;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PendingTourActivity extends AppCompatActivity {

    private RecyclerView rvPendingTours;
    private TourAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private SessionManager session;
    private List<Tour> tourList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_tour);

        session = new SessionManager(this);
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();

        loadPendingTours();
    }

    private void initViews() {
        rvPendingTours = findViewById(R.id.rvPendingTours);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvPendingTours.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TourAdapter(tourList);
        adapter.setPendingList(true);
        rvPendingTours.setAdapter(adapter);

        adapter.setOnTourClickListener(tour -> {
            // Chuyển đến màn hình chi tiết (nếu cần xem lại thông tin đã gửi)
            // Hoặc hiện Toast thông báo
            Toast.makeText(this, "Tour này đang trong quá trình kiểm duyệt", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadPendingTours);
        swipeRefresh.setColorSchemeResources(R.color.light_blue);
    }

    private void loadPendingTours() {
        if (!session.isLoggedIn()) {
            Toast.makeText(this, R.string.err_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        layoutEmpty.setVisibility(View.GONE);

        TourService.getPendingTours(session.getUserId(), session.getAccessToken(), new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    tourList.clear();
                    tourList.addAll(data);
                    adapter.notifyDataSetChanged();

                    if (data.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(PendingTourActivity.this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
