package com.example.tourgo.ui.main;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.models.Tour;

public class BookingConfirmFragment extends Fragment {

    private LinearLayout btnWallet, btnBank;
    private ImageView ivWallet, ivBank;
    private TextView tvWallet, tvBank, tvTotalPrice;
    private TextView tvNights, tvRoomPrice, tvTaxes, tvServiceCharge;
    private View layoutPaymentMethods;
    private TextView tvAddMethod;
    private double totalPrice = 0.0;
    private Hotel hotel;
    private Tour tour;

    private final int COLOR_BLUE = Color.parseColor("#4285F4");
    private final int COLOR_BLACK = Color.BLACK;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof BookingActivity) {
            BookingActivity host = (BookingActivity) getActivity();
            hotel = host.getHotel();
            if (hotel == null) {
                tour = host.getTour();
            }
        }

        // Ánh xạ các View
        tvNights = view.findViewById(R.id.tvNights);
        tvRoomPrice = view.findViewById(R.id.tvRoomPrice);
        tvTaxes = view.findViewById(R.id.tvTaxes);
        tvServiceCharge = view.findViewById(R.id.tvServiceCharge);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnWallet = view.findViewById(R.id.btnWallet);
        btnBank = view.findViewById(R.id.btnBank);
        ivWallet = view.findViewById(R.id.ivWalletIcon);
        tvWallet = view.findViewById(R.id.tvWalletLabel);
        ivBank = view.findViewById(R.id.ivBankIcon);
        tvBank = view.findViewById(R.id.tvBankLabel);
        layoutPaymentMethods = view.findViewById(R.id.layoutPaymentMethods);
        tvAddMethod = view.findViewById(R.id.btnAddMethod);
        RadioButton rbMomo = view.findViewById(R.id.rbMomo);
        RadioButton rbZaloPay = view.findViewById(R.id.rbZaloPay);

        // NHẬN DỮ LIỆU VÀ HIỂN THỊ
        if (getArguments() != null && (hotel != null || tour != null)) {
            int nights = getArguments().getInt("num_nights", 0);
            double roomPrice = getArguments().getDouble("room_price", 0.0);
            double taxes = getArguments().getDouble("taxes", 0.0);
            double service = getArguments().getDouble("service_charge", 0.0);
            totalPrice = getArguments().getDouble("total_price", 0.0);

            tvNights.setText(nights + " Night" + (nights > 1 ? "s" : ""));
            tvRoomPrice.setText(formatPrice(roomPrice));
            tvTaxes.setText(formatPrice(taxes));
            tvServiceCharge.setText(formatPrice(service));
            tvTotalPrice.setText(formatPrice(totalPrice));
        }

        // THIẾT LẬP CLICK LISTENERS
        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        btnWallet.setOnClickListener(v -> selectPaymentCategory(true));
        btnBank.setOnClickListener(v -> selectPaymentCategory(false));

        View.OnClickListener radioClick = v -> {
            rbMomo.setChecked(v.getId() == R.id.rbMomo);
            rbZaloPay.setChecked(v.getId() == R.id.rbZaloPay);
        };
        rbMomo.setOnClickListener(radioClick);
        rbZaloPay.setOnClickListener(radioClick);

        view.findViewById(R.id.btnConfirmPayment).setOnClickListener(v -> {
            if (getActivity() instanceof BookingActivity && getArguments() != null) {
                long checkIn = getArguments().getLong("check_in_millis", 0L);
                long checkOut = getArguments().getLong("check_out_millis", 0L);
                int guests = getArguments().getInt("guests", 0);
                ((BookingActivity) getActivity()).submitBooking(checkIn, checkOut, guests, totalPrice);
            }
        });

        selectPaymentCategory(true);
    }

    private String formatPrice(double amount) {
        if (hotel != null) return hotel.formatPrice(amount);
        if (tour != null) return tour.formatPrice(amount);
        return String.format(java.util.Locale.US, "%,.0f₫", amount);
    }

    private void selectPaymentCategory(boolean isWallet) {
        btnWallet.setBackgroundResource(isWallet ? R.drawable.bg_outline_blue : R.drawable.bg_outline_gray);
        ivWallet.setImageTintList(ColorStateList.valueOf(isWallet ? COLOR_BLUE : COLOR_BLACK));
        tvWallet.setTextColor(isWallet ? COLOR_BLUE : COLOR_BLACK);
        tvWallet.setTypeface(null, isWallet ? Typeface.BOLD : Typeface.NORMAL);

        btnBank.setBackgroundResource(!isWallet ? R.drawable.bg_outline_blue : R.drawable.bg_outline_gray);
        ivBank.setImageTintList(ColorStateList.valueOf(!isWallet ? COLOR_BLUE : COLOR_BLACK));
        tvBank.setTextColor(!isWallet ? COLOR_BLUE : COLOR_BLACK);
        tvBank.setTypeface(null, !isWallet ? Typeface.BOLD : Typeface.NORMAL);

        if (layoutPaymentMethods != null) {
            layoutPaymentMethods.setVisibility(isWallet ? View.VISIBLE : View.GONE);
        }
        if (tvAddMethod != null) {
            tvAddMethod.setText(isWallet ? "+ Add New Wallet" : "+ Add New Bank");
        }
    }
}
