package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tourgo.R;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.databinding.ActivitySearchBinding;
import com.example.tourgo.databinding.LayoutFilterBottomSheetBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.TourService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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

    private int currentSearchVersion = 0;
    
    // Filter State
    private int selectedPropertyTypeId = -1;
    private int selectedQuickPriceId = -1;
    private String minPrice = "";
    private String maxPrice = "";
    private final List<Integer> selectedAmenityIds = new ArrayList<>();
    private int selectedRatingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        setupSearchInput();
        setupRecentContent();
    }

    private void initViews() {
        searchAdapter = new TourAdapter(new ArrayList<>());
        searchAdapter.setOnTourClickListener(this::navigateToDetail);
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(searchAdapter);

        binding.btnBackSearch.setOnClickListener(v -> finish());
        
        binding.btnFilterSearch.setOnClickListener(v -> {
            showFilterBottomSheet();
        });
    }

    private void navigateToDetail(Tour tour) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("hotel_object", tour); 
        startActivity(intent);
    }

    private void showFilterBottomSheet() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            LayoutFilterBottomSheetBinding sheet = LayoutFilterBottomSheetBinding.inflate(getLayoutInflater());
            dialog.setContentView(sheet.getRoot());

            View bottomSheetInternal = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetInternal);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }

            restoreFilterState(sheet);

            // Xử lý khi chọn các mốc giá nhanh
            sheet.cgQuickPrice.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                int id = checkedIds.get(0);
                selectedQuickPriceId = id;
                if (id == R.id.chipPriceUnder500) {
                    sheet.etMinPrice.setText("0");
                    sheet.etMaxPrice.setText("500000");
                } else if (id == R.id.chipPrice500to2M) {
                    sheet.etMinPrice.setText("500000");
                    sheet.etMaxPrice.setText("2000000");
                } else if (id == R.id.chipPrice2Mto5M) {
                    sheet.etMinPrice.setText("2000000");
                    sheet.etMaxPrice.setText("5000000");
                } else if (id == R.id.chipPriceOver5M) {
                    sheet.etMinPrice.setText("5000000");
                    sheet.etMaxPrice.setText("");
                }
            });

            // Khi người dùng tự nhập -> bỏ chọn mốc giá nhanh
            TextWatcher manualPriceWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (sheet.etMinPrice.hasFocus() || sheet.etMaxPrice.hasFocus()) {
                        sheet.cgQuickPrice.clearCheck();
                        selectedQuickPriceId = -1;
                    }
                }
            };
            sheet.etMinPrice.addTextChangedListener(manualPriceWatcher);
            sheet.etMaxPrice.addTextChangedListener(manualPriceWatcher);

            sheet.btnCloseFilter.setOnClickListener(v -> dialog.dismiss());
            sheet.btnCancelFilter.setOnClickListener(v -> {
                resetFilters();
                dialog.dismiss();
                triggerSearch(binding.etSearchQuery.getText().toString());
            });

            sheet.btnApplyFilter.setOnClickListener(v -> {
                saveFilterState(sheet);
                dialog.dismiss();
                triggerSearch(binding.etSearchQuery.getText().toString());
            });

            dialog.show();
        } catch (Exception e) {
            Log.e("SearchActivity", "Error showing BottomSheet", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreFilterState(LayoutFilterBottomSheetBinding sheet) {
        if (selectedPropertyTypeId != -1) sheet.cgPropertyType.check(selectedPropertyTypeId);
        if (selectedQuickPriceId != -1) sheet.cgQuickPrice.check(selectedQuickPriceId);
        sheet.etMinPrice.setText(minPrice);
        sheet.etMaxPrice.setText(maxPrice);
        for (int id : selectedAmenityIds) sheet.cgAmenities.check(id);
        if (selectedRatingId != -1) sheet.cgRating.check(selectedRatingId);
    }

    private void saveFilterState(LayoutFilterBottomSheetBinding sheet) {
        selectedPropertyTypeId = sheet.cgPropertyType.getCheckedChipId();
        selectedQuickPriceId = sheet.cgQuickPrice.getCheckedChipId();
        minPrice = sheet.etMinPrice.getText().toString();
        maxPrice = sheet.etMaxPrice.getText().toString();
        selectedRatingId = sheet.cgRating.getCheckedChipId();
        selectedAmenityIds.clear();
        selectedAmenityIds.addAll(sheet.cgAmenities.getCheckedChipIds());
    }

    private void resetFilters() {
        selectedPropertyTypeId = -1;
        selectedQuickPriceId = -1;
        minPrice = "";
        maxPrice = "";
        selectedAmenityIds.clear();
        selectedRatingId = -1;
    }

    private void setupSearchInput() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                cancelPendingCallbacks();
                if (query.isEmpty()) {
                    currentSearchVersion++;
                    showRecentContent();
                    return;
                }
                showSearchMode();
                debounceRunnable = () -> triggerSearch(query);
                handler.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
            }
        });
    }

    private void triggerSearch(String query) {
        if (query.isEmpty()) return;
        final int version = ++currentSearchVersion;
        timeoutRunnable = () -> {
            if (version != currentSearchVersion) return;
            showNoResults(getString(R.string.search_timeout));
        };
        handler.postDelayed(timeoutRunnable, SEARCH_TIMEOUT_MS);

        TourService.searchTours(this, query, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                runOnUiThread(() -> {
                    cancelTimeoutCallback();
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
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    cancelTimeoutCallback();
                    if (version != currentSearchVersion) return;
                    showNoResults(getString(R.string.search_no_results));
                });
            }
        });
    }

    private void setupRecentContent() {
        String[] recent = {"Hà Nội", "Hạ Long", "Đà Nẵng", "Phú Quốc"};
        binding.llRecentSearches.removeAllViews();
        for (String city : recent) {
            TextView tv = new TextView(this);
            tv.setText(city);
            tv.setPadding(0, 16, 0, 16);
            tv.setTextColor(getColor(R.color.black));
            tv.setOnClickListener(v -> binding.etSearchQuery.setText(city));
            binding.llRecentSearches.addView(tv);
        }
        binding.btnClearRecent.setOnClickListener(v -> binding.llRecentSearches.removeAllViews());
    }

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

    private void cancelPendingCallbacks() {
        if (debounceRunnable != null) handler.removeCallbacks(debounceRunnable);
        cancelTimeoutCallback();
    }

    private void cancelTimeoutCallback() {
        if (timeoutRunnable != null) handler.removeCallbacks(timeoutRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPendingCallbacks();
        binding = null;
    }
}
