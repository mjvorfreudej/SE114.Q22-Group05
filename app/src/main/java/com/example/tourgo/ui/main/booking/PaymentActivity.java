package com.example.tourgo.ui.main.booking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.remote.service.BookingService;
import com.example.tourgo.ui.main.home.MainActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_TOTAL_PRICE = "total_price";

    private View layoutPaymentInfo;
    private ProgressBar pbPayment;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        double totalPrice = getIntent().getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0);

        layoutPaymentInfo = findViewById(R.id.layoutPaymentInfo);
        pbPayment = findViewById(R.id.pbPayment);
        TextView tvBookingId = findViewById(R.id.tvPaymentBookingId);
        TextView tvTotal = findViewById(R.id.tvPaymentTotal);
        Button btnMockPay = findViewById(R.id.btnMockPay);

        tvBookingId.setText(getString(R.string.payment_booking_id,
                bookingId != null ? bookingId : "-"));
        tvTotal.setText(getString(R.string.payment_total, formatPrice(totalPrice)));

        btnMockPay.setOnClickListener(v -> startMockPayment());
    }

    private void startMockPayment() {
        // Show loading state
        layoutPaymentInfo.setVisibility(View.GONE);
        pbPayment.setVisibility(View.VISIBLE);

        // Simulate network delay of 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            
            // After mock payment succeeds, update status on server
            // Using "COMPLETED" as requested for backend sync (means Paid)
            updateBookingStatusToPaid();
        }, 2000);
    }

    private void updateBookingStatusToPaid() {
        if (bookingId == null) {
            pbPayment.setVisibility(View.GONE);
            showSuccessDialog(); 
            return;
        }

        // According to requirement: Backend "COMPLETED" status means the order is PAID
        BookingService.updateBookingStatus(this, bookingId, "COMPLETED", new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isFinishing()) return;
                pbPayment.setVisibility(View.GONE);
                showSuccessDialog();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (isFinishing()) return;
                pbPayment.setVisibility(View.GONE);
                layoutPaymentInfo.setVisibility(View.VISIBLE);
                Toast.makeText(PaymentActivity.this, "Payment failed to sync: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.booking_success_title)
                .setMessage(R.string.booking_success_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.booking_back_home, (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private static String formatPrice(double amount) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        fmt.setMaximumFractionDigits(0);
        return fmt.format(amount) + "₫";
    }
}
