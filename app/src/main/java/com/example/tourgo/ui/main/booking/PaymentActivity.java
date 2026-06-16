package com.example.tourgo.ui.main.booking;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tourgo.R;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_TOTAL_PRICE = "total_price";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        String bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        double totalPrice = getIntent().getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0);

        TextView tvBookingId = findViewById(R.id.tvPaymentBookingId);
        TextView tvTotal = findViewById(R.id.tvPaymentTotal);

        tvBookingId.setText(getString(R.string.payment_booking_id,
                bookingId != null ? bookingId : "-"));
        tvTotal.setText(getString(R.string.payment_total, formatPrice(totalPrice)));
    }

    private static String formatPrice(double amount) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        fmt.setMaximumFractionDigits(0);
        return fmt.format(amount) + "₫";
    }
}
