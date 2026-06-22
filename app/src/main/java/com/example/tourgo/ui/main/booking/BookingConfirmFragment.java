package com.example.tourgo.ui.main.booking;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;

public class BookingConfirmFragment extends Fragment {

    private static final int CATEGORY_BANK = 0;
    private static final int CATEGORY_COD = 1;

    private LinearLayout btnBank, btnCOD;
    private ImageView ivBank, ivCOD;
    private TextView tvBank, tvCOD, tvTotalPrice;
    private TextView tvNights, tvRoomPrice, tvTaxes, tvServiceCharge;
    private double totalPrice = 0.0;
    private Hotel hotel;
    private Tour tour;
    private int selectedCategory = CATEGORY_BANK;

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

        applyTopInset(view);

        if (getActivity() instanceof BookingActivity) {
            BookingActivity host = (BookingActivity) getActivity();
            hotel = host.getHotel();
            if (hotel == null) {
                tour = host.getTour();
            }
        }

        tvNights = view.findViewById(R.id.tvNights);
        tvRoomPrice = view.findViewById(R.id.tvRoomPrice);
        tvTaxes = view.findViewById(R.id.tvTaxes);
        tvServiceCharge = view.findViewById(R.id.tvServiceCharge);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnBank = view.findViewById(R.id.btnBank);
        ivBank = view.findViewById(R.id.ivBankIcon);
        tvBank = view.findViewById(R.id.tvBankLabel);
        btnCOD = view.findViewById(R.id.btnCOD);
        ivCOD = view.findViewById(R.id.ivCODIcon);
        tvCOD = view.findViewById(R.id.tvCODLabel);

        if (getArguments() != null && (hotel != null || tour != null)) {
            int nights = getArguments().getInt("num_nights", 0);
            double roomPrice = getArguments().getDouble("room_price", 0.0);
            double taxes = getArguments().getDouble("taxes", 0.0);
            double service = getArguments().getDouble("service_charge", 0.0);
            totalPrice = getArguments().getDouble("total_price", 0.0);

            if (nights == 1) {
                tvNights.setText(getString(R.string.booking_night_single, nights));
            } else {
                tvNights.setText(getString(R.string.booking_night_plural, nights));
            }

            tvRoomPrice.setText(formatPrice(roomPrice));
            tvTaxes.setText(formatPrice(taxes));
            tvServiceCharge.setText(formatPrice(service));
            tvTotalPrice.setText(formatPrice(totalPrice));
        }

        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        if (btnBank != null) {
            btnBank.setOnClickListener(v -> selectPaymentCategory(CATEGORY_BANK));
        }
        if (btnCOD != null) {
            btnCOD.setOnClickListener(v -> selectPaymentCategory(CATEGORY_COD));
        }

        view.findViewById(R.id.btnConfirmPayment).setOnClickListener(v -> {
            if (getActivity() instanceof BookingActivity && getArguments() != null) {
                long checkIn = getArguments().getLong("check_in_millis", 0L);
                long checkOut = getArguments().getLong("check_out_millis", 0L);
                int guests = getArguments().getInt("guests", 0);
                int rooms = getArguments().getInt("rooms", 0);

                String selectedMethod = getSelectedPaymentMethod();
                getArguments().putString("payment_method", selectedMethod);

                ((BookingActivity) getActivity()).submitBooking(checkIn, checkOut, guests, rooms, totalPrice);
            }
        });

        selectPaymentCategory(CATEGORY_BANK);
    }

    private String formatPrice(double amount) {
        if (hotel != null) return hotel.formatPrice(requireContext(), amount);
        if (tour != null) return tour.formatPrice(requireContext(), amount);
        return String.format(java.util.Locale.getDefault(), "%,.0f₫", amount);
    }

    private void selectPaymentCategory(int category) {
        selectedCategory = category;
        if (btnBank != null && ivBank != null && tvBank != null) {
            applyTabState(btnBank, ivBank, tvBank, category == CATEGORY_BANK);
        }
        if (btnCOD != null && ivCOD != null && tvCOD != null) {
            applyTabState(btnCOD, ivCOD, tvCOD, category == CATEGORY_COD);
        }
    }

    private void applyTabState(LinearLayout tab, ImageView icon, TextView label, boolean active) {
        tab.setBackgroundResource(active ? R.drawable.bg_outline_blue : R.drawable.bg_outline_gray);
        icon.setImageTintList(ColorStateList.valueOf(active ? COLOR_BLUE : COLOR_BLACK));
        label.setTextColor(active ? COLOR_BLUE : COLOR_BLACK);
        label.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    /**
     * Xác định phương thức thanh toán user đã chọn.
     * - Bank Transfer → bank_transfer (chuyển khoản Casso, cần webhook xác nhận)
     * - COD → cod (thanh toán khi nhận hàng, chuyển thẳng sang success)
     */
    private String getSelectedPaymentMethod() {
        if (selectedCategory == CATEGORY_COD) {
            return "cod";
        } else {
            return "bank_transfer";
        }
    }

    private void applyTopInset(View root) {
        final int basePaddingTop = root.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
