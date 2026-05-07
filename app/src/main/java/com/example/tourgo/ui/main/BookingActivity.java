package com.example.tourgo.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.tourgo.R;
import com.example.tourgo.models.Hotel;

public class BookingActivity extends AppCompatActivity {
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        hotel = (Hotel) getIntent().getSerializableExtra("hotel_item");

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
}
