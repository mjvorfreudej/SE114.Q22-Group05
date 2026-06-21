package com.example.tourgo.ui.main.booking;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tourgo.R;

public class PaymentActivity extends AppCompatActivity {
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_TOTAL_PRICE = "total_price";
    public static final String EXTRA_CHECK_IN_OUT = "check_in_out";
    public static final String EXTRA_GUEST_INFO = "guest_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString(EXTRA_BOOKING_ID, getIntent().getStringExtra(EXTRA_BOOKING_ID));
            args.putDouble("total_price", getIntent().getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0));
            args.putString("check_in_out", getIntent().getStringExtra(EXTRA_CHECK_IN_OUT));
            args.putString("guest_info", getIntent().getStringExtra(EXTRA_GUEST_INFO));

            BookingSuccessFragment fragment = new BookingSuccessFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.payment_container, fragment)
                    .commit();
        }
    }
}
