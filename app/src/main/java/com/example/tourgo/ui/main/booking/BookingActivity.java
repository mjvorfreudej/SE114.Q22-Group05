package com.example.tourgo.ui.main.booking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Booking;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.BookingService;
import com.example.tourgo.data.local.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BookingActivity extends AppCompatActivity {
    public static final String EXTRA_HOTEL = "hotel_item";
    public static final String EXTRA_TOUR = "tour_item";

    private Hotel hotel;
    private Tour tour;
    private View loadingOverlay;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        hotel = (Hotel) getIntent().getSerializableExtra(EXTRA_HOTEL);
        tour = (Tour) getIntent().getSerializableExtra(EXTRA_TOUR);

        loadingOverlay = findViewById(R.id.bookingLoadingOverlay);
        sessionManager = new SessionManager(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.booking_container, new BookingRequestFragment(), "request")
                    .commit();
        }
    }

    public void showStep(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );

        Fragment current = fm.findFragmentById(R.id.booking_container);
        if (current != null) {
            ft.hide(current);
        }

        ft.add(R.id.booking_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public Hotel getHotel() {
        return hotel;
    }

    public Tour getTour() {
        return tour;
    }

    public void submitBooking(long checkInMillis, long checkOutMillis, int guests, double totalPrice) {
        if (!validateBookingInputs(checkInMillis, checkOutMillis, guests)) {
            return;
        }

        String userId = sessionManager.getUserId();
        String accessToken = sessionManager.getAccessToken();
        if (userId == null || accessToken == null) {
            Toast.makeText(this, R.string.booking_error_not_logged_in, Toast.LENGTH_LONG).show();
            return;
        }

        String hotelId = hotel != null ? hotel.getId() : null;
        String tourId = tour != null ? tour.getId() : null;
        if (hotelId == null && tourId == null) {
            Toast.makeText(this, R.string.booking_error_missing_target, Toast.LENGTH_LONG).show();
            return;
        }
        String bookingType = hotelId != null ? "HOTEL" : "TOUR";

        String checkIn = formatDate(checkInMillis);
        String checkOut = formatDate(checkOutMillis);

        Booking booking = new Booking(userId, tourId, hotelId);
        booking.setBookingDate(checkIn);
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setGuests(guests);

        showLoading(true);
        BookingService.createBooking(this, booking, new DataCallback<Booking>() {
            @Override
            public void onSuccess(Booking created) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (created == null || created.getId() == null) {
                        Toast.makeText(BookingActivity.this,
                                getString(R.string.booking_error_create_failed, "NO_ID"),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    launchPayment(created.getId(), totalPrice);
                });
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                Log.e("BookingError", "API Error: " + rawMessage);
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.e("BookingError", "API Error: " + rawMessage);
                    Toast.makeText(BookingActivity.this,
                            getString(R.string.booking_error_create_failed, code.name())
                                    + "\nDetail: " + rawMessage,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean validateBookingInputs(long checkInMillis, long checkOutMillis, int guests) {
        if (checkInMillis <= 0L || checkOutMillis <= 0L) {
            Toast.makeText(this, R.string.booking_error_dates_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        long todayStart = startOfToday();
        if (checkInMillis < todayStart) {
            Toast.makeText(this, R.string.booking_error_checkin_past, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (checkOutMillis <= checkInMillis) {
            Toast.makeText(this, R.string.booking_error_checkout_after_checkin, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (guests <= 0) {
            Toast.makeText(this, R.string.booking_error_guests_invalid, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showLoading(boolean visible) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void launchPayment(String bookingId, double totalPrice) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_BOOKING_ID, bookingId);
        intent.putExtra(PaymentActivity.EXTRA_TOTAL_PRICE, totalPrice);
        startActivity(intent);
    }

    private static String formatDate(long millis) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        fmt.setTimeZone(TimeZone.getDefault());
        return fmt.format(new Date(millis));
    }

    private static long startOfToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
