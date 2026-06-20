package com.example.tourgo.ui.main.search;
import com.example.tourgo.ui.main.detail.DetailActivity;
import com.example.tourgo.ui.main.home.MainActivity;

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
import android.view.inputmethod.EditorInfo;
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
import com.example.tourgo.data.repository.TourRepository;
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
        
        setupCategorySelector();
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

    private void setupCategorySelector() {
        // Mặc định là Tour
        binding.etSearchQuery.setHint(R.string.search_hint_tour);
        
        binding.cgSearchCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int id = checkedIds.get(0);
            if (id == R.id.chipSearchHotels) {
                selectedPropertyTypeId = R.id.chipHotels;
                binding.etSearchQuery.setHint(R.string.search_hint_hotel);
            } else {
                selectedPropertyTypeId = R.id.chipTours;
                binding.etSearchQuery.setHint(R.string.search_hint_tour);
            }
            
            String query = binding.etSearchQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });
    }

    private void setupSearchLogic() {
        // Xử lý khi nhấn nút Search trên bàn phím
        binding.etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearchQuery.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                    // Ẩn bàn phím sau khi search
                    v.clearFocus();
                }
                return true;
            }
            return false;
        });

        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showRecentContent();
                }
            }
        });
    }

    private void showRecentContent() {
        if (binding == null) return;
        binding.layoutRecentContent.setVisibility(View.VISIBLE);
        binding.layoutSearchResults.setVisibility(View.GONE);
        setupRecentSearches(); // Refresh recent searches list
        loadRecentHotels(); // Refresh recent viewed list
    }

    private void performSearch(String query) {
        if (binding == null) return;
        
        boolean isTour = (selectedPropertyTypeId == R.id.chipTours);
        
        // Chỉ lưu vào lịch sử khi thực hiện tìm kiếm (nhấn nút Search)
        session.addRecentSearch(query, isTour);
        
        binding.layoutRecentContent.setVisibility(View.GONE);
        binding.layoutSearchResults.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
        
        // Cập nhật tiêu đề kết quả
        String type = isTour ? "Tour" : "Khách sạn";
        binding.tvResultsHeader.setText(getString(R.string.search_results_for_format, type, query));

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
                // Sync main category selector when resetting filter
                binding.cgSearchCategory.check(R.id.chipSearchTours);
                performSearch(binding.etSearchQuery.getText().toString());
            });

            sheet.btnApplyFilter.setOnClickListener(v -> {
                saveFilterState(sheet);
                dialog.dismiss();
                // Sync main category selector when applying filter
                if (selectedPropertyTypeId == R.id.chipHotels) {
                    binding.cgSearchCategory.check(R.id.chipSearchHotels);
                } else {
                    binding.cgSearchCategory.check(R.id.chipSearchTours);
                }
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
        List<SessionManager.RecentSearchItem> recentSearches = session.getRecentSearches();
        
        if (recentSearches.isEmpty()) {
            binding.tvRecentSearchTitle.setVisibility(View.GONE);
            binding.btnClearRecent.setVisibility(View.GONE);
        } else {
            binding.tvRecentSearchTitle.setVisibility(View.VISIBLE);
            binding.btnClearRecent.setVisibility(View.VISIBLE);
            
            // Chỉ hiển thị tối đa 4 lịch sử tìm kiếm gần nhất
            int count = 0;
            for (SessionManager.RecentSearchItem item : recentSearches) {
                if (count >= 4) break;
                
                View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_recent_search, binding.llRecentSearches, false);
                ((TextView)itemView.findViewById(R.id.tvRecentCity)).setText(item.query);
                
                String typeStr = item.isTour ? "Tour" : "Khách sạn";
                ((TextView)itemView.findViewById(R.id.tvRecentType)).setText("Tìm kiếm trong: " + typeStr);
                
                itemView.setOnClickListener(v -> {
                    // Cập nhật chip lựa chọn trên UI trước khi search
                    if (item.isTour) {
                        binding.cgSearchCategory.check(R.id.chipSearchTours);
                        selectedPropertyTypeId = R.id.chipTours;
                    } else {
                        binding.cgSearchCategory.check(R.id.chipSearchHotels);
                        selectedPropertyTypeId = R.id.chipHotels;
                    }
                    
                    binding.etSearchQuery.setText(item.query);
                    performSearch(item.query); // Tìm kiếm ngay khi click vào lịch sử
                });
                binding.llRecentSearches.addView(itemView);
                count++;
            }
        }
        
        binding.btnClearRecent.setOnClickListener(v -> {
            session.clearRecentSearches();
            binding.llRecentSearches.removeAllViews();
            binding.tvRecentSearchTitle.setVisibility(View.GONE);
            binding.btnClearRecent.setVisibility(View.GONE);
        });
    }

    private void setupRecentViewed() {
        recentViewedAdapter = new PopularHotelAdapter(new ArrayList<>());
        binding.rvRecentViewed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecentViewed.setAdapter(recentViewedAdapter);
    }

    private void loadRecentHotels() {
        String userId = session.getUserId();
        String token = session.getAccessToken();
        
        // Ensure hotels and tours are loaded in repositories first
        HotelRepository.getInstance().loadHotels(getContext(), userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> hotels) {
                TourRepository.getInstance().loadTours(getContext(), userId, token, new DataCallback<List<Tour>>() {
                    @Override
                    public void onSuccess(List<Tour> tours) {
                        resolveRecentlyViewedItems();
                    }
                    @Override public void onError(ApiErrorCode code, String msg) { resolveRecentlyViewedItems(); }
                });
            }
            @Override public void onError(ApiErrorCode code, String msg) {}
        });
    }

    private void resolveRecentlyViewedItems() {
        List<SessionManager.RecentlyViewedItem> recentItems = session.getRecentlyViewed();
        List<Hotel> displayList = new ArrayList<>();

        for (SessionManager.RecentlyViewedItem item : recentItems) {
            if (item.isTour) {
                Tour t = TourRepository.getInstance().findTourById(item.id);
                if (t != null) displayList.add(t.toHotel());
            } else {
                Hotel h = HotelRepository.getInstance().findHotelById(item.id);
                if (h != null) displayList.add(h);
            }
        }

        if (binding == null) return;
        requireActivity().runOnUiThread(() -> {
            if (!displayList.isEmpty()) {
                binding.tvRecentViewedTitle.setVisibility(View.VISIBLE);
                binding.rvRecentViewed.setVisibility(View.VISIBLE);
                recentViewedAdapter.setData(displayList);
            } else {
                binding.tvRecentViewedTitle.setVisibility(View.GONE);
                binding.rvRecentViewed.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchHandler != null && searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }
}
