package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tourgo.R;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.adapters.TrendingHotelAdapter;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.databinding.FragmentHomeBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.models.response.User;
import com.example.tourgo.utils.ImageLoader;
import com.example.tourgo.data.local.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PopularHotelAdapter popularAdapter;
    private TrendingHotelAdapter trendingAdapter;
    private TourAdapter tourAdapter;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        session = new SessionManager(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateGreeting();

        setupRecyclerViews();
        showLoading(true);
        loadData();
        loadTours();

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnFind.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                String dest = binding.tvSearchDestination.getText().toString();
                String date = binding.tvCheckInOut.getText().toString();
                String guest = binding.tvGuest.getText().toString();
                
                // Chuyển sang HotelListFragment với tiêu đề tìm kiếm
                ((MainActivity) getActivity()).switchToHotelList("Search Results", dest, date, guest);
            }
        });
        
        binding.layoutSearchDestination.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToSearch();
            }
        });

        // Chuyển sang xem tất cả các mục tương ứng
        binding.tvSeeAllHotels.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToHotelList("Most Popular", "", "", "");
            }
        });

        binding.tvSeeAllTrending.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToHotelList("Trending Hotels", "", "", "");
            }
        });

        binding.tvSeeAllTours.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToSearch();
            }
        });
    }

    private void updateGreeting() {
        if (binding == null || session == null) return;

        if (session.isLoggedIn()) {
            // Lấy user từ UserRepository (cache)
            User cachedUser = UserRepository.getInstance().getCachedUser();

            if (cachedUser != null && cachedUser.getName() != null) {
                // Dùng tên từ UserRepository nếu có
                String firstName = cachedUser.getName().split(" ")[0];
                binding.tvGreeting.setText(getString(R.string.main_greeting, firstName));
            } else {
                // Fallback về SessionManager nếu chưa có cache
                binding.tvGreeting.setText(getString(R.string.main_greeting, session.getShortName()));
            }
        } else {
            binding.tvGreeting.setText(R.string.main_greeting_default);
        }
    }

    private void setupRecyclerViews() {
        popularAdapter = new PopularHotelAdapter(new ArrayList<>());
        binding.rvPopularHotels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopularHotels.setAdapter(popularAdapter);

        trendingAdapter = new TrendingHotelAdapter(new ArrayList<>());
        binding.rvTrendingHotels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTrendingHotels.setAdapter(trendingAdapter);

        tourAdapter = new TourAdapter(new ArrayList<>());
        tourAdapter.setOnTourClickListener(tour -> {
            Intent intent = new Intent(getContext(), BookingActivity.class);
            intent.putExtra(BookingActivity.EXTRA_TOUR, tour);
            startActivity(intent);
        });
        binding.rvPopularTours.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPopularTours.setAdapter(tourAdapter);
    }

    private void showLoading(boolean show) {
        if (binding == null) return;
        binding.progressBarHome.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.layoutHomeBody.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void loadData() {
        String userId = session.getUserId();
        String token = session.getAccessToken();

        HotelRepository.getInstance().loadHotels(getContext(), userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (binding == null) return;
                showLoading(false);
                if (data != null && !data.isEmpty()) {
                    popularAdapter.setData(data.size() > 5 ? data.subList(0, 5) : data);
                    if (data.size() > 5) {
                        trendingAdapter.setData(data.subList(5, Math.min(data.size(), 10)));
                    } else {
                        trendingAdapter.setData(new ArrayList<>(data));
                    }
                    preloadImages(data);
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (binding == null) return;
                showLoading(false);
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.err_network, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadTours() {
        binding.progressBarTours.setVisibility(View.VISIBLE);
        
        String userId = session.getUserId();
        String token = session.getAccessToken();
        
        TourRepository.getInstance().loadTours(requireContext(), userId, token, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                if (binding == null || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    binding.progressBarTours.setVisibility(View.GONE);
                    if (data != null && !data.isEmpty()) {
                        tourAdapter.setData(data.size() > 5 ? data.subList(0, 5) : data);
                        preloadTourImages(data);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (binding == null || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    binding.progressBarTours.setVisibility(View.GONE);
                });
            }
        });
    }

    private void preloadImages(List<Hotel> hotels) {
        List<String> urls = new ArrayList<>();
        for (Hotel hotel : hotels) {
            if (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty()) {
                urls.add(hotel.getImageUrls().get(0));
            }
        }
        ImageLoader.preload(getContext(), urls);
    }

    private void preloadTourImages(List<Tour> tours) {
        List<String> urls = new ArrayList<>();
        for (Tour tour : tours) {
            if (tour.getImageUrls() != null && !tour.getImageUrls().isEmpty()) {
                urls.add(tour.getImageUrls().get(0));
            }
        }
        ImageLoader.preload(getContext(), urls);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGreeting();
        if (popularAdapter != null) popularAdapter.notifyDataSetChanged();
        if (trendingAdapter != null) trendingAdapter.notifyDataSetChanged();
        if (tourAdapter != null) tourAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
