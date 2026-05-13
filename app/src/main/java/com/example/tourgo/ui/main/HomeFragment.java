package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tourgo.R;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.data.TourRepository;
import com.example.tourgo.databinding.FragmentHomeBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.models.Tour;
import com.example.tourgo.utils.ImageLoader;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int OFFERS_LIMIT = 8;

    private FragmentHomeBinding binding;
    private PopularHotelAdapter offersHotelAdapter;
    private TourAdapter offersTourAdapter;
    private SessionManager session;

    private List<Hotel> cachedHotels = new ArrayList<>();
    private List<Tour> cachedTours = new ArrayList<>();

    private enum OfferFilter { ALL, HOTEL, TOUR }
    private OfferFilter currentFilter = OfferFilter.ALL;

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

        applyStatusBarInset();
        updateUserName();
        setupClickListeners();
        loadOffers();
    }

    private void applyStatusBarInset() {
        if (binding == null) return;
        View header = binding.homeHeader;
        int basePaddingTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(header);
    }

    private void updateUserName() {
        if (binding == null || session == null) return;
        if (session.isLoggedIn()) {
            binding.tvHomeUserName.setText(session.getShortName());
        } else {
            binding.tvHomeUserName.setText(R.string.home_guest_name);
        }
    }

    private void setupClickListeners() {
        binding.categoryHotel.setOnClickListener(v -> openHotelScreen());
        binding.categoryTour.setOnClickListener(v -> openTourScreen());

        binding.cardSpecialDeal.setOnClickListener(v -> openHotelScreen());
        binding.btnDealBookNow.setOnClickListener(v -> openHotelScreen());

        binding.cardGuideHotel.setOnClickListener(v -> openHotelScreen());
        binding.cardGuideTour.setOnClickListener(v -> openTourScreen());

        binding.tvOffersSeeAll.setOnClickListener(v -> {
            if (currentFilter == OfferFilter.TOUR) {
                openTourScreen();
            } else {
                openHotelScreen();
            }
        });

        binding.chipOfferAll.setOnClickListener(v -> setFilter(OfferFilter.ALL));
        binding.chipOfferHotel.setOnClickListener(v -> setFilter(OfferFilter.HOTEL));
        binding.chipOfferTour.setOnClickListener(v -> setFilter(OfferFilter.TOUR));
    }

    private void openHotelScreen() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).switchToHotelScreen();
        }
    }

    private void openTourScreen() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).switchToTourScreen();
        }
    }

    private void setFilter(OfferFilter filter) {
        currentFilter = filter;
        applyChipStyles();
        refreshOffersList();
    }

    private void applyChipStyles() {
        styleChip(binding.chipOfferAll, currentFilter == OfferFilter.ALL);
        styleChip(binding.chipOfferHotel, currentFilter == OfferFilter.HOTEL);
        styleChip(binding.chipOfferTour, currentFilter == OfferFilter.TOUR);
    }

    private void styleChip(android.widget.TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_action_gradient : R.drawable.bg_offer_chip);
        chip.setTextColor(getResources().getColor(selected ? R.color.white : R.color.dark_gray, null));
    }

    private void loadOffers() {
        if (binding == null) return;
        binding.progressBarHome.setVisibility(View.VISIBLE);

        String userId = session.getUserId();
        String token = session.getAccessToken();

        HotelRepository.getInstance().loadHotels(userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (binding == null || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (data != null) {
                        cachedHotels = data;
                        preloadHotelImages(data);
                    }
                    refreshOffersList();
                    maybeHideProgress();
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (binding == null || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    maybeHideProgress();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), R.string.err_network, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        TourRepository.getInstance().loadTours(userId, token, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                if (binding == null || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (data != null) {
                        cachedTours = data;
                        preloadTourImages(data);
                    }
                    refreshOffersList();
                    maybeHideProgress();
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (binding == null || getActivity() == null) return;
                getActivity().runOnUiThread(this::handleError);
            }

            private void handleError() {
                maybeHideProgress();
            }
        });
    }

    private void maybeHideProgress() {
        if (binding == null) return;
        binding.progressBarHome.setVisibility(View.GONE);
    }

    private void refreshOffersList() {
        if (binding == null) return;

        if (currentFilter == OfferFilter.TOUR) {
            if (offersTourAdapter == null) {
                offersTourAdapter = new TourAdapter(new ArrayList<>());
                offersTourAdapter.setOnTourClickListener(tour -> {
                    Intent intent = new Intent(getContext(), BookingActivity.class);
                    intent.putExtra(BookingActivity.EXTRA_TOUR, tour);
                    startActivity(intent);
                });
            }
            binding.rvHomeOffers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.rvHomeOffers.setAdapter(offersTourAdapter);
            offersTourAdapter.setData(limit(cachedTours, OFFERS_LIMIT));
        } else {
            if (offersHotelAdapter == null) {
                offersHotelAdapter = new PopularHotelAdapter(new ArrayList<>());
            }
            binding.rvHomeOffers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.rvHomeOffers.setAdapter(offersHotelAdapter);
            offersHotelAdapter.setData(limit(cachedHotels, OFFERS_LIMIT));
        }
    }

    private <T> List<T> limit(List<T> source, int max) {
        if (source == null) return new ArrayList<>();
        return source.size() > max ? source.subList(0, max) : source;
    }

    private void preloadHotelImages(List<Hotel> hotels) {
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
        updateUserName();
        applyChipStyles();
        if (offersHotelAdapter != null) offersHotelAdapter.notifyDataSetChanged();
        if (offersTourAdapter != null) offersTourAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
