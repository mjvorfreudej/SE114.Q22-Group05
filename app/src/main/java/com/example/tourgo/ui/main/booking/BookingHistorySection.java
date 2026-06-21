package com.example.tourgo.ui.main.booking;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.BookingHistoryAdapter;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Booking;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.BookingService;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Wires the "Booking history" section.
 * UI Logic Mapping:
 * - Tab PAID: Shows backend status "COMPLETED" (which means paid).
 * - Tab COMPLETED: Shows backend status "FINISHED" or "DONE" (trip ended).
 * - Displays max 2 items by default, toggles expand/collapse via "View All".
 * - PENDING items are strictly excluded from the PAID tab.
 */
public class BookingHistorySection {

    private static final String UI_STATUS_PAID = "PAID";
    private static final String UI_STATUS_COMPLETED = "COMPLETED";

    private final Context context;
    private final SessionManager session;

    private RecyclerView recyclerView;
    private ChipGroup filterChips;
    private TextView emptyView;
    private TextView viewAllBtn;
    private ProgressBar progressBar;
    private BookingHistoryAdapter adapter;

    private final List<BookingHistoryAdapter.Item> allItems = new ArrayList<>();
    private String currentTab = UI_STATUS_PAID;
    private boolean isExpanded = false;

    public BookingHistorySection(Context context) {
        this.context = context;
        this.session = new SessionManager(context);
    }

    public void bind(View root) {
        recyclerView = root.findViewById(R.id.rvMyBookings);
        filterChips = root.findViewById(R.id.chipGroupBookingFilter);
        emptyView = root.findViewById(R.id.tvBookingEmpty);
        viewAllBtn = root.findViewById(R.id.tvBookingViewAll);
        progressBar = root.findViewById(R.id.progressBookings);
        if (recyclerView == null) return;

        adapter = new BookingHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        if (filterChips != null) {
            filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
                currentTab = checkedIds.contains(R.id.chipBookingCompleted)
                        ? UI_STATUS_COMPLETED : UI_STATUS_PAID;
                isExpanded = false; // Collapse when switching tabs
                applyFilter();
            });
        }

        if (viewAllBtn != null) {
            viewAllBtn.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                applyFilter();
            });
        }

        load();
    }

    private void load() {
        if (!session.isLoggedIn()) {
            showEmpty(true);
            return;
        }
        setLoading(true);

        String userId = session.getUserId();
        String token = session.getAccessToken();

        TourRepository.getInstance().loadTours(context, userId, token, new DataCallback<List<Tour>>() {
            @Override public void onSuccess(List<Tour> data) { loadHotelsThenBookings(userId, token); }
            @Override public void onError(ApiErrorCode code, String msg) { loadHotelsThenBookings(userId, token); }
        });
    }

    private void loadHotelsThenBookings(String userId, String token) {
        HotelRepository.getInstance().loadHotels(context, userId, token, new DataCallback<List<Hotel>>() {
            @Override public void onSuccess(List<Hotel> data) { fetchBookings(); }
            @Override public void onError(ApiErrorCode code, String msg) { fetchBookings(); }
        });
    }

    private void fetchBookings() {
        BookingService.getMyBookings(context, new DataCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                setLoading(false);
                allItems.clear();
                if (bookings != null) {
                    for (Booking booking : bookings) {
                        allItems.add(toItem(booking));
                    }
                }
                applyFilter();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                setLoading(false);
                allItems.clear();
                applyFilter();
            }
        });
    }

    private BookingHistoryAdapter.Item toItem(Booking booking) {
        String title = null;
        String priceText = "";
        String imageUrl = null;

        if (booking.getTourId() != null) {
            Tour tour = TourRepository.getInstance().findTourById(booking.getTourId());
            if (tour != null) {
                title = tour.getName();
                priceText = tour.getPriceString(context);
                List<String> urls = tour.getImageUrls();
                if (urls != null && !urls.isEmpty()) imageUrl = urls.get(0);
            }
        } else if (booking.getHotelId() != null) {
            Hotel hotel = HotelRepository.getInstance().findHotelById(booking.getHotelId());
            if (hotel != null) {
                title = hotel.getName();
                priceText = hotel.getPriceString(context);
                List<String> urls = hotel.getImageUrls();
                if (urls != null && !urls.isEmpty()) imageUrl = urls.get(0);
            }
        }

        if (title == null || title.isEmpty()) {
            title = context.getString(R.string.booking_history_title);
        }

        // Map Backend Status to UI Status
        String backendStatus = booking.getStatus() != null ? booking.getStatus().toUpperCase() : "PENDING";
        String uiStatus = backendStatus;

        if (backendStatus.equals("COMPLETED")) {
            uiStatus = UI_STATUS_PAID; // Backend COMPLETED means UI PAID
        } else if (backendStatus.equals("FINISHED") || backendStatus.equals("DONE")) {
            uiStatus = UI_STATUS_COMPLETED; // Trip ended
        }

        return new BookingHistoryAdapter.Item(
                title,
                formatDate(booking.getBookingDate()),
                priceText,
                uiStatus,
                imageUrl
        );
    }

    private void applyFilter() {
        List<BookingHistoryAdapter.Item> filtered = new ArrayList<>();
        for (BookingHistoryAdapter.Item item : allItems) {
            // Strictly filter by mapped UI status. 
            // PENDING items will be ignored as they don't match PAID or COMPLETED tabs.
            if (currentTab.equals(item.status)) {
                filtered.add(item);
            }
        }

        // Toggle logic: show max 2 or show all
        if (filtered.size() > 2) {
            if (viewAllBtn != null) {
                viewAllBtn.setVisibility(View.VISIBLE);
                viewAllBtn.setText(isExpanded ? "Thu gọn" : "Xem tất cả");
            }
            if (!isExpanded) {
                filtered = filtered.subList(0, 2);
            }
        } else {
            if (viewAllBtn != null) viewAllBtn.setVisibility(View.GONE);
        }

        if (adapter != null) adapter.setData(filtered);
        showEmpty(filtered.isEmpty());
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading && emptyView != null) emptyView.setVisibility(View.GONE);
        if (loading && viewAllBtn != null) viewAllBtn.setVisibility(View.GONE);
    }

    private void showEmpty(boolean empty) {
        if (emptyView != null) emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private static String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        return raw.length() >= 10 ? raw.substring(0, 10) : raw;
    }
}
