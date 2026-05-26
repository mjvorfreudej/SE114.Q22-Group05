package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tourgo.R;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.databinding.ActivitySearchBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.remote.service.HotelService;
import com.example.tourgo.remote.service.TourService;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private ActivitySearchBinding binding;
    private PopularHotelAdapter recentViewedAdapter;
    private PopularHotelAdapter searchHotelAdapter;
    private TourAdapter searchTourAdapter;
    private SessionManager session;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY_MS = 500; // Debounce delay

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
        
        binding.btnBackSearch.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onBackPressed();
            }
        });
        
        setupRecentSearches();
        setupRecentViewed();
        setupSearchLogic();
        loadRecentHotels();
    }

    private void setupSearchLogic() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Cancel previous search request
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (query.isEmpty()) {
                    // Show recent searches and recent viewed
                    showRecentContent();
                    return;
                }

                // Debounce search - wait 500ms after user stops typing
                searchRunnable = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showRecentContent() {
        if (binding == null) return;
        binding.layoutRecentContent.setVisibility(View.VISIBLE);
        binding.layoutSearchResults.setVisibility(View.GONE);
    }

    private void performSearch(String query) {
        if (binding == null) return;

        // Hide recent content, show search results area
        binding.layoutRecentContent.setVisibility(View.GONE);
        binding.layoutSearchResults.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);

        // Show loading state
        showLoading(true);

        // Search both hotels and tours
        searchHotels(query);
        searchTours(query);
    }

    private void searchHotels(String query) {
        HotelService.searchHotels(requireContext(), query, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    if (data != null && !data.isEmpty()) {
                        displayHotelResults(data);
                    } else {
                        showNoHotelResults();
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                        getString(R.string.err_prefix, msg),
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void searchTours(String query) {
        TourService.searchTours(requireContext(), query, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    if (data != null && !data.isEmpty()) {
                        displayTourResults(data);
                    } else {
                        showNoTourResults();
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                        getString(R.string.err_prefix, msg),
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayHotelResults(List<Hotel> hotels) {
        if (binding == null) return;

        if (searchHotelAdapter == null) {
            searchHotelAdapter = new PopularHotelAdapter(hotels);
            binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvSearchResults.setAdapter(searchHotelAdapter);
        } else {
            searchHotelAdapter.setData(hotels);
        }

        binding.rvSearchResults.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
    }

    private void displayTourResults(List<Tour> tours) {
        if (binding == null) return;

        if (searchTourAdapter == null) {
            searchTourAdapter = new TourAdapter(tours);
            binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvSearchResults.setAdapter(searchTourAdapter);
        } else {
            searchTourAdapter.setData(tours);
        }

        binding.rvSearchResults.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
    }

    private void showNoHotelResults() {
        if (binding == null) return;
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.tvNoResults.setVisibility(View.VISIBLE);
    }

    private void showNoTourResults() {
        if (binding == null) return;
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.tvNoResults.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        if (binding == null) return;
        binding.pbSearchLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.rvSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
    }

    private void setupRecentSearches() {
        binding.llRecentSearches.removeAllViews();
        addRecentSearchItem("Phuket", "Dominic Hotel, Luxury Royale Hotel...");
        addRecentSearchItem("Pattaya City", "Hilton Bandung, Namin Hotel...");
        binding.btnClearRecent.setOnClickListener(v -> binding.llRecentSearches.removeAllViews());
    }

    private void addRecentSearchItem(String city, String details) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_recent_search, binding.llRecentSearches, false);
        TextView tvCity = itemView.findViewById(R.id.tvRecentCity);
        TextView tvDetails = itemView.findViewById(R.id.tvRecentHotelDetails);
        tvCity.setText(city);
        tvDetails.setText(details);
        binding.llRecentSearches.addView(itemView);
    }

    private void setupRecentViewed() {
        recentViewedAdapter = new PopularHotelAdapter(new ArrayList<>());
        binding.rvRecentViewed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecentViewed.setAdapter(recentViewedAdapter);
    }

    private void loadRecentHotels() {
        String userId = session.getUserId();
        String token = session.getAccessToken();

        // Sử dụng dữ liệu thật từ Repository thay vì AppFakeData
        HotelRepository.getInstance().loadHotels(getContext(), userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (binding == null) return;
                getActivity().runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        // Lấy 5 khách sạn đầu tiên làm "Vừa xem"
                        recentViewedAdapter.setData(data.size() > 5 ? data.subList(0, 5) : data);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recentViewedAdapter != null) recentViewedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up handler callbacks
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}
