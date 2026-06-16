package com.example.tourgo.ui.main.booking;

import android.content.Context;
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
 * Wires the "Booking history" section (status tabs + list) shared by the
 * profile screen and fragment. Bookings come from the {@code bookings} table
 * via the API; tour/hotel name, price and image are joined from the cached
 * repositories. Tabs filter the list by status (PAID / COMPLETED).
 */
public class BookingHistorySection {

    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final Context context;
    private final SessionManager session;

    private RecyclerView recyclerView;
    private ChipGroup filterChips;
    private TextView emptyView;
    private ProgressBar progressBar;
    private BookingHistoryAdapter adapter;

    private final List<BookingHistoryAdapter.Item> allItems = new ArrayList<>();
    private String currentStatus = STATUS_PAID;

    public BookingHistorySection(Context context) {
        this.context = context;
        this.session = new SessionManager(context);
    }

    /** Find and wire the section views inside {@code root}, then load data. */
    public void bind(View root) {
        recyclerView = root.findViewById(R.id.rvMyBookings);
        filterChips = root.findViewById(R.id.chipGroupBookingFilter);
        emptyView = root.findViewById(R.id.tvBookingEmpty);
        progressBar = root.findViewById(R.id.progressBookings);
        if (recyclerView == null) return;

        adapter = new BookingHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        if (filterChips != null) {
            filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
                currentStatus = checkedIds.contains(R.id.chipBookingCompleted)
                        ? STATUS_COMPLETED : STATUS_PAID;
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

        // Warm the tour/hotel caches first so bookings can be enriched, then
        // fetch the bookings themselves.
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

        return new BookingHistoryAdapter.Item(
                title,
                formatDate(booking.getBookingDate()),
                priceText,
                booking.getStatus(),
                imageUrl
        );
    }

    private void applyFilter() {
        List<BookingHistoryAdapter.Item> filtered = new ArrayList<>();
        for (BookingHistoryAdapter.Item item : allItems) {
            if (item.status != null && item.status.equalsIgnoreCase(currentStatus)) {
                filtered.add(item);
            }
        }
        if (adapter != null) adapter.setData(filtered);
        showEmpty(filtered.isEmpty());
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading && emptyView != null) emptyView.setVisibility(View.GONE);
    }

    private void showEmpty(boolean empty) {
        if (emptyView != null) emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private static String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        // bookingDate is an ISO timestamp; show just the date portion.
        return raw.length() >= 10 ? raw.substring(0, 10) : raw;
    }
}
