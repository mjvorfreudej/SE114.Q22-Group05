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
import com.example.tourgo.adapters.HotelSearchAdapter;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.databinding.ActivitySearchBinding;
import com.example.tourgo.databinding.LayoutFilterBottomSheetBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.HotelService;
import com.example.tourgo.remote.service.TourService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final long DEBOUNCE_DELAY_MS = 300;
    private static final long SEARCH_TIMEOUT_MS = 5000;

    private ActivitySearchBinding binding;
    private TourAdapter tourAdapter;
    private HotelSearchAdapter hotelAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private Runnable timeoutRunnable;

    private int currentSearchVersion = 0;
    
    // Filter State
    private int selectedPropertyTypeId = R.id.chipTours; 
    private int selectedQuickPriceId = -1;
    private String minPriceStr = "";
    private String maxPriceStr = "";
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
        tourAdapter = new TourAdapter(new ArrayList<>());
        tourAdapter.setOnTourClickListener(tour -> navigateToDetail(tour, "tour"));
        
        hotelAdapter = new HotelSearchAdapter(new ArrayList<>());

        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(tourAdapter); 

        binding.btnBackSearch.setOnClickListener(v -> finish());
        
        binding.btnFilterSearch.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void navigateToDetail(Object obj, String type) {
        Intent intent = new Intent(this, DetailActivity.class);
        if ("tour".equals(type)) {
            intent.putExtra("hotel_object", (Tour)obj);
        } else {
            intent.putExtra("hotel_object", (Hotel)obj);
        }
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
        }
    }

    private void restoreFilterState(LayoutFilterBottomSheetBinding sheet) {
        if (selectedPropertyTypeId != -1) sheet.cgPropertyType.check(selectedPropertyTypeId);
        if (selectedQuickPriceId != -1) sheet.cgQuickPrice.check(selectedQuickPriceId);
        sheet.etMinPrice.setText(minPriceStr);
        sheet.etMaxPrice.setText(maxPriceStr);
        for (int id : selectedAmenityIds) sheet.cgAmenities.check(id);
        if (selectedRatingId != -1) sheet.cgRating.check(selectedRatingId);
    }

    private void saveFilterState(LayoutFilterBottomSheetBinding sheet) {
        selectedPropertyTypeId = sheet.cgPropertyType.getCheckedChipId();
        selectedQuickPriceId = sheet.cgQuickPrice.getCheckedChipId();
        minPriceStr = sheet.etMinPrice.getText().toString();
        maxPriceStr = sheet.etMaxPrice.getText().toString();
        selectedRatingId = sheet.cgRating.getCheckedChipId();
        selectedAmenityIds.clear();
        selectedAmenityIds.addAll(sheet.cgAmenities.getCheckedChipIds());
    }

    private void resetFilters() {
        selectedPropertyTypeId = R.id.chipTours;
        selectedQuickPriceId = -1;
        minPriceStr = "";
        maxPriceStr = "";
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
        final int version = ++currentSearchVersion;
        timeoutRunnable = () -> {
            if (version != currentSearchVersion) return;
            showNoResults(getString(R.string.search_no_results));
        };
        handler.postDelayed(timeoutRunnable, SEARCH_TIMEOUT_MS);

        Double min = parseDouble(minPriceStr);
        Double max = parseDouble(maxPriceStr);
        Double rating = getRatingValue(selectedRatingId);

        if (selectedPropertyTypeId == R.id.chipHotels) {
            HotelService.searchHotelsAdvanced(this, query, null, min, max, rating, "rating", "desc", new DataCallback<List<Hotel>>() {
                @Override public void onSuccess(List<Hotel> data) {
                    runOnUiThread(() -> {
                        if (version != currentSearchVersion) return;
                        cancelTimeoutCallback();
                        handleSearchResult(data, hotelAdapter);
                    });
                }
                @Override public void onError(ApiErrorCode code, String msg) {
                    runOnUiThread(() -> { if (version == currentSearchVersion) showNoResults(getString(R.string.search_no_results)); });
                }
            });
        } else {
            TourService.searchToursAdvanced(this, query, null, min, max, rating, "rating", "desc", new DataCallback<List<Tour>>() {
                @Override public void onSuccess(List<Tour> data) {
                    runOnUiThread(() -> {
                        if (version != currentSearchVersion) return;
                        cancelTimeoutCallback();
                        handleSearchResult(data, tourAdapter);
                    });
                }
                @Override public void onError(ApiErrorCode code, String msg) {
                    runOnUiThread(() -> { if (version == currentSearchVersion) showNoResults(getString(R.string.search_no_results)); });
                }
            });
        }
    }

    private void handleSearchResult(List<?> data, Object adapter) {
        binding.pbSearchLoading.setVisibility(View.GONE);
        if (data != null && !data.isEmpty()) {
            binding.rvSearchResults.setVisibility(View.VISIBLE);
            binding.tvNoResults.setVisibility(View.GONE);
            binding.rvSearchResults.setAdapter((androidx.recyclerview.widget.RecyclerView.Adapter) adapter);
            if (adapter instanceof TourAdapter) ((TourAdapter) adapter).setData((List<Tour>) data);
            else if (adapter instanceof HotelSearchAdapter) ((HotelSearchAdapter) adapter).setData((List<Hotel>) data);
        } else {
            showNoResults(getString(R.string.search_no_results));
        }
    }

    private Double parseDouble(String value) {
        try { return Double.parseDouble(value); } catch (Exception e) { return null; }
    }

    private Double getRatingValue(int chipId) {
        if (chipId == R.id.chipRating5) return 5.0;
        if (chipId == R.id.chipRating4) return 4.0;
        if (chipId == R.id.chipRating3) return 3.0;
        if (chipId == R.id.chipRating2) return 2.0;
        if (chipId == R.id.chipRating1) return 1.0;
        return null;
    }

    private void setupRecentContent() {
        String[] recent = {"Hà Nội", "Hạ Long", "Đà Nẵng", "Phú Quốc"};
        binding.llRecentSearches.removeAllViews();
        for (String city : recent) {
            TextView tv = new TextView(this);
            tv.setText(city);
            tv.setPadding(32, 24, 32, 24);
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
