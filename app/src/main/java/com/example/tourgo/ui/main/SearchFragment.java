package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelSearchAdapter;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.databinding.ActivitySearchBinding;
import com.example.tourgo.databinding.LayoutFilterBottomSheetBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.remote.service.HotelService;
import com.example.tourgo.remote.service.TourService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private ActivitySearchBinding binding;
    private PopularHotelAdapter recentViewedAdapter;
    private HotelSearchAdapter searchHotelAdapter;
    private TourAdapter searchTourAdapter;
    private SessionManager session;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY_MS = 500; 

    // Filter State
    private int selectedPropertyTypeId = R.id.chipTours; 
    private int selectedQuickPriceId = -1;
    private String minPriceStr = "";
    private String maxPriceStr = "";
    private final List<Integer> selectedAmenityIds = new ArrayList<>();
    private int selectedRatingId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivitySearchBinding.inflate(inflater, container, false);
        session = new SessionManager(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyTopInset(view);

        binding.btnBackSearch.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.btnFilterSearch.setOnClickListener(v -> showFilterBottomSheet());
        
        setupRecentSearches();
        setupRecentViewed();
        setupSearchLogic();
        loadRecentHotels();
    }

    private void applyTopInset(View root) {
        final int basePaddingTop = root.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setupSearchLogic() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                if (query.isEmpty()) {
                    showRecentContent();
                    return;
                }

                searchRunnable = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }
        });
    }

    private void showRecentContent() {
        if (binding == null) return;
        binding.layoutRecentContent.setVisibility(View.VISIBLE);
        binding.layoutSearchResults.setVisibility(View.GONE);
    }

    private void performSearch(String query) {
        if (binding == null) return;
        binding.layoutRecentContent.setVisibility(View.GONE);
        binding.layoutSearchResults.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
        showLoading(true);

        Double min = parseDouble(minPriceStr);
        Double max = parseDouble(maxPriceStr);
        Double rating = getRatingValue(selectedRatingId);

        if (selectedPropertyTypeId == R.id.chipHotels) {
            searchHotels(query, min, max, rating);
        } else {
            searchTours(query, min, max, rating);
        }
    }

    private void searchHotels(String query, Double min, Double max, Double rating) {
        HotelService.searchHotelsAdvanced(requireContext(), query, null, min, max, rating, "rating", "desc", new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    if (data != null && !data.isEmpty()) displayHotelResults(data);
                    else showNoResults();
                });
            }
            @Override public void onError(ApiErrorCode code, String msg) {
                requireActivity().runOnUiThread(() -> { showLoading(false); showNoResults(); });
            }
        });
    }

    private void searchTours(String query, Double min, Double max, Double rating) {
        TourService.searchToursAdvanced(requireContext(), query, null, min, max, rating, "rating", "desc", new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    if (data != null && !data.isEmpty()) displayTourResults(data);
                    else showNoResults();
                });
            }
            @Override public void onError(ApiErrorCode code, String msg) {
                requireActivity().runOnUiThread(() -> { showLoading(false); showNoResults(); });
            }
        });
    }

    private void showFilterBottomSheet() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
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
                    sheet.etMinPrice.setText("0"); sheet.etMaxPrice.setText("500000");
                } else if (id == R.id.chipPrice500to2M) {
                    sheet.etMinPrice.setText("500000"); sheet.etMaxPrice.setText("2000000");
                } else if (id == R.id.chipPrice2Mto5M) {
                    sheet.etMinPrice.setText("2000000"); sheet.etMaxPrice.setText("5000000");
                } else if (id == R.id.chipPriceOver5M) {
                    sheet.etMinPrice.setText("5000000"); sheet.etMaxPrice.setText("");
                }
            });

            sheet.btnCloseFilter.setOnClickListener(v -> dialog.dismiss());
            sheet.btnCancelFilter.setOnClickListener(v -> {
                resetFilters();
                dialog.dismiss();
                performSearch(binding.etSearchQuery.getText().toString());
            });

            sheet.btnApplyFilter.setOnClickListener(v -> {
                saveFilterState(sheet);
                dialog.dismiss();
                performSearch(binding.etSearchQuery.getText().toString());
            });

            dialog.show();
        } catch (Exception e) {
            Log.e("SearchFragment", "Error showing BottomSheet", e);
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
        minPriceStr = ""; maxPriceStr = "";
        selectedAmenityIds.clear();
        selectedRatingId = -1;
    }

    private void displayHotelResults(List<Hotel> hotels) {
        if (binding == null) return;
        if (searchHotelAdapter == null) {
            searchHotelAdapter = new HotelSearchAdapter(hotels);
            binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        } else searchHotelAdapter.setData(hotels);
        binding.rvSearchResults.setAdapter(searchHotelAdapter);
        binding.rvSearchResults.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
    }

    private void displayTourResults(List<Tour> tours) {
        if (binding == null) return;
        if (searchTourAdapter == null) {
            searchTourAdapter = new TourAdapter(tours);
            searchTourAdapter.setOnTourClickListener(this::navigateToDetail);
            binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        } else searchTourAdapter.setData(tours);
        binding.rvSearchResults.setAdapter(searchTourAdapter);
        binding.rvSearchResults.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
    }

    private void navigateToDetail(Tour tour) {
        Intent intent = new Intent(requireContext(), DetailActivity.class);
        intent.putExtra("hotel_object", tour); // Dùng chung key với Hotel
        startActivity(intent);
    }

    private void showNoResults() {
        if (binding == null) return;
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.tvNoResults.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        if (binding == null) return;
        binding.pbSearchLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.rvSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
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

    private void setupRecentSearches() {
        binding.llRecentSearches.removeAllViews();
        String[] cities = {"Hạ Long", "Đà Nẵng", "Phú Quốc"};
        for (String city : cities) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_recent_search, binding.llRecentSearches, false);
            ((TextView)itemView.findViewById(R.id.tvRecentCity)).setText(city);
            itemView.setOnClickListener(v -> binding.etSearchQuery.setText(city));
            binding.llRecentSearches.addView(itemView);
        }
        binding.btnClearRecent.setOnClickListener(v -> binding.llRecentSearches.removeAllViews());
    }

    private void setupRecentViewed() {
        recentViewedAdapter = new PopularHotelAdapter(new ArrayList<>());
        binding.rvRecentViewed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecentViewed.setAdapter(recentViewedAdapter);
    }

    private void loadRecentHotels() {
        String userId = session.getUserId();
        String token = session.getAccessToken();
        HotelRepository.getInstance().loadHotels(getContext(), userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        recentViewedAdapter.setData(data.size() > 5 ? data.subList(0, 5) : data);
                    }
                });
            }
            @Override public void onError(ApiErrorCode code, String msg) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchHandler != null && searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }
}
