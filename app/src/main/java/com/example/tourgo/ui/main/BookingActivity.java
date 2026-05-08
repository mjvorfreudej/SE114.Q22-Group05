package com.example.tourgo.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.tourgo.R;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.models.Tour;

public class BookingActivity extends AppCompatActivity {
    public static final String EXTRA_HOTEL = "hotel_item";
    public static final String EXTRA_TOUR = "tour_item";

    private Hotel hotel;
    private Tour tour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        hotel = (Hotel) getIntent().getSerializableExtra(EXTRA_HOTEL);
        tour = (Tour) getIntent().getSerializableExtra(EXTRA_TOUR);

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
}
