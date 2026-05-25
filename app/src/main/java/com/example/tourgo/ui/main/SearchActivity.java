package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tourgo.R;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.databinding.ActivitySearchBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.TourService;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final long DEBOUNCE_DELAY_MS = 300;
    private static final long SEARCH_TIMEOUT_MS = 5000;

    private ActivitySearchBinding binding;
    private TourAdapter searchAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private Runnable timeoutRunnable;

    // Incremented on every new search; used to discard stale callbacks.
    private int currentSearchVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupSearchInput();
        setupRecentSearches();

        binding.btnBackSearch.setOnClickListener(v -> finish());
    }

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    private void setupRecyclerView() {
        searchAdapter = new TourAdapter(new ArrayList<>());
        searchAdapter.setOnTourClickListener(tour -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra(BookingActivity.EXTRA_TOUR, tour);
            startActivity(intent);
        });
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(searchAdapter);
    }

    private void setupSearchInput() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Cancel any pending debounce and timeout immediately.
                cancelPendingCallbacks();

                if (query.isEmpty()) {
                    // Invalidate any in-flight search so its callback is silently dropped.
                    currentSearchVersion++;
                    showRecentContent();
                    return;
                }

                // Immediately hide recent content and show the spinner — the UI
                // responds to the keystroke even before the debounce fires.
                showSearchMode();

                debounceRunnable = () -> triggerSearch(query);
                handler.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
            }
        });
    }

    private void setupRecentSearches() {
        String[] recent = {"Phuket", "Pattaya City", "Surat Thani"};
        for (String city : recent) {
            TextView tv = new TextView(this);
            tv.setText(city);
            tv.setPadding(0, 16, 0, 16);
            tv.setTextColor(getResources().getColor(R.color.black));
            binding.llRecentSearches.addView(tv);
        }
        binding.btnClearRecent.setOnClickListener(v -> binding.llRecentSearches.removeAllViews());
    }

    // -------------------------------------------------------------------------
    // Search logic
    // -------------------------------------------------------------------------

    private void triggerSearch(String query) {
        final int version = ++currentSearchVersion;

        // 5-second hard timeout — hides the spinner and shows an error message
        // if the network call hasn't resolved by then.
        timeoutRunnable = () -> {
            if (version != currentSearchVersion) return;
            showNoResults(getString(R.string.search_timeout));
        };
        handler.postDelayed(timeoutRunnable, SEARCH_TIMEOUT_MS);

        TourService.searchTours(query, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                runOnUiThread(() -> {
                    cancelTimeoutCallback();

                    // Stale response — a newer search has already been issued.
                    if (version != currentSearchVersion) return;

                    binding.pbSearchLoading.setVisibility(View.GONE);

                    if (data != null && !data.isEmpty()) {
                        binding.rvSearchResults.setVisibility(View.VISIBLE);
                        binding.tvNoResults.setVisibility(View.GONE);
                        searchAdapter.setData(data);
                    } else {
                        showNoResults(getString(R.string.search_no_results));
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                runOnUiThread(() -> {
                    cancelTimeoutCallback();

                    // Stale error — discard silently.
                    if (version != currentSearchVersion) return;

                    showNoResults(getString(R.string.search_no_results));
                });
            }
        });
    }

    // -------------------------------------------------------------------------
    // UI state helpers
    // -------------------------------------------------------------------------

    private void showSearchMode() {
        binding.layoutRecentContent.setVisibility(View.GONE);
        binding.layoutSearchResults.setVisibility(View.VISIBLE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.tvNoResults.setVisibility(View.GONE);
        binding.pbSearchLoading.setVisibility(View.VISIBLE);
    }

    private void showRecentContent() {
        binding.layoutRecentContent.setVisibility(View.VISIBLE);
        binding.layoutSearchResults.setVisibility(View.GONE);
        binding.pbSearchLoading.setVisibility(View.GONE);
    }

    private void showNoResults(String message) {
        binding.pbSearchLoading.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.tvNoResults.setText(message);
        binding.tvNoResults.setVisibility(View.VISIBLE);
    }

    // -------------------------------------------------------------------------
    // Handler cleanup
    // -------------------------------------------------------------------------

    private void cancelPendingCallbacks() {
        if (debounceRunnable != null) {
            handler.removeCallbacks(debounceRunnable);
            debounceRunnable = null;
        }
        cancelTimeoutCallback();
    }

    private void cancelTimeoutCallback() {
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPendingCallbacks();
        binding = null;
    }
}
