package com.example.tourgo.ui.main.booking;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.PaymentResponse;
import com.example.tourgo.remote.service.PaymentService;

public class PaymentActivity extends AppCompatActivity {
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_TOTAL_PRICE = "total_price";
    public static final String EXTRA_CHECK_IN_OUT = "check_in_out";
    public static final String EXTRA_GUEST_INFO = "guest_info";
    public static final String EXTRA_PAYMENT_METHOD = "payment_method";

    private String bookingId;
    private double totalPrice;
    private String checkInOut;
    private String guestInfo;
    private String paymentMethod;
    private String transactionCode;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        totalPrice = getIntent().getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0);
        checkInOut = getIntent().getStringExtra(EXTRA_CHECK_IN_OUT);
        guestInfo = getIntent().getStringExtra(EXTRA_GUEST_INFO);
        paymentMethod = getIntent().getStringExtra(EXTRA_PAYMENT_METHOD);

        if (paymentMethod == null) {
            paymentMethod = "cod";
        }

        if (savedInstanceState == null) {
            processPayment();
        }
    }

    private void processPayment() {
        if ("cod".equals(paymentMethod)) {
            processCODPayment();
        } else if ("bank_transfer".equals(paymentMethod)) {
            processBankTransferPayment();
        } else {
            Toast.makeText(this, getString(R.string.payment_method_not_support), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processCODPayment() {
        showLoading(getString(R.string.processing));

        PaymentService.createPayment(this, bookingId, totalPrice, "cod", new DataCallback<PaymentResponse>() {
            @Override
            public void onSuccess(PaymentResponse response) {
                hideLoading();
                transactionCode = response.getTransactionCode();
                showBookingSuccess(response.getTransactionCode(), response.getTransactionCode(), response.getAmount());
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                hideLoading();
                Toast.makeText(PaymentActivity.this,
                        getString(R.string.COD_error) + message,
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void processBankTransferPayment() {
        showLoading(getString(R.string.create_transfer_information));

        PaymentService.createPayment(this, bookingId, totalPrice, "bank_transfer", new DataCallback<PaymentResponse>() {
            @Override
            public void onSuccess(PaymentResponse response) {
                hideLoading();
                transactionCode = response.getTransactionCode();
                if (response.getBankInfo() != null) {
                    showBankTransferInfo(response);
                } else {
                    Toast.makeText(PaymentActivity.this,
                            "Error: " + getString(R.string.not_get_transfer_information),
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                hideLoading();
                Toast.makeText(PaymentActivity.this,
                        getString(R.string.error_create_transfer_information) + message,
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void showBankTransferInfo(PaymentResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_BOOKING_ID, bookingId);
        args.putDouble("bank_amount", response.getBankInfo().getAmount());
        args.putString("bank_name", response.getBankInfo().getBankName());
        args.putString("account_number", response.getBankInfo().getAccountNumber());
        args.putString("account_holder", response.getBankInfo().getAccountHolder());
        args.putString("transfer_note", response.getBankInfo().getTransferNote());
        args.putString("transaction_code", response.getTransactionCode());

        BankTransferFragment fragment = new BankTransferFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.payment_container, fragment)
                .commit();
    }

    private void showBookingSuccess(String transactionCode, String transferNote, double paidAmount) {
        Bundle args = new Bundle();
        args.putString(EXTRA_BOOKING_ID, bookingId);
        args.putDouble("total_price", totalPrice);
        args.putDouble("paid_amount", paidAmount);
        args.putString("check_in_out", checkInOut);
        args.putString("guest_info", guestInfo);
        args.putString("transfer_note", transferNote);
        args.putString("transaction_code", transactionCode);

        BookingSuccessFragment fragment = new BookingSuccessFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.payment_container, fragment)
                .commit();
    }

    public void showBookingSuccessAfterTransfer(String transactionCode, String transferNote, double paidAmount) {
        showBookingSuccess(transactionCode, transferNote, paidAmount);
    }

    private void showLoading(String message) {
        if (loadingDialog == null) {
            loadingDialog = new AlertDialog.Builder(this)
                    .setMessage(message)
                    .setCancelable(false)
                    .create();
        }
        loadingDialog.setMessage(message);
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
