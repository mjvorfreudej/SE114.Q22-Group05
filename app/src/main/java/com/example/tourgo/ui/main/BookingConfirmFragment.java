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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.models.Tour;

public class BookingConfirmFragment extends Fragment {

    private static final int CATEGORY_WALLET = 0;
    private static final int CATEGORY_BANK = 1;
    private static final int CATEGORY_CARD = 2;

    private LinearLayout btnWallet, btnBank, btnCard;
    private ImageView ivWallet, ivBank, ivCard;
    private TextView tvWallet, tvBank, tvCard, tvTotalPrice;
    private TextView tvNights, tvRoomPrice, tvTaxes, tvServiceCharge;
    private View layoutPaymentMethods, layoutCardMethods;
    private TextView tvAddMethod;
    private RadioButton rbMomo, rbZaloPay, rbPayPal, rbVisa, rbMastercard;
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
        btnWallet = view.findViewById(R.id.btnWallet);
        btnBank = view.findViewById(R.id.btnBank);
        btnCard = view.findViewById(R.id.btnCard);
        ivWallet = view.findViewById(R.id.ivWalletIcon);
        tvWallet = view.findViewById(R.id.tvWalletLabel);
        ivBank = view.findViewById(R.id.ivBankIcon);
        tvBank = view.findViewById(R.id.tvBankLabel);
        ivCard = view.findViewById(R.id.ivCardIcon);
        tvCard = view.findViewById(R.id.tvCardLabel);
        layoutPaymentMethods = view.findViewById(R.id.layoutPaymentMethods);
        layoutCardMethods = view.findViewById(R.id.layoutCardMethods);
        tvAddMethod = view.findViewById(R.id.btnAddMethod);
        rbMomo = view.findViewById(R.id.rbMomo);
        rbZaloPay = view.findViewById(R.id.rbZaloPay);
        rbPayPal = view.findViewById(R.id.rbPayPal);
        rbVisa = view.findViewById(R.id.rbVisa);
        rbMastercard = view.findViewById(R.id.rbMastercard);

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

        btnWallet.setOnClickListener(v -> selectPaymentCategory(CATEGORY_WALLET));
        btnBank.setOnClickListener(v -> selectPaymentCategory(CATEGORY_BANK));
        btnCard.setOnClickListener(v -> selectPaymentCategory(CATEGORY_CARD));

        View.OnClickListener radioClick = v -> {
            int id = v.getId();
            rbMomo.setChecked(id == R.id.rbMomo);
            rbZaloPay.setChecked(id == R.id.rbZaloPay);
            rbPayPal.setChecked(id == R.id.rbPayPal);
            rbVisa.setChecked(id == R.id.rbVisa);
            rbMastercard.setChecked(id == R.id.rbMastercard);
        };
        rbMomo.setOnClickListener(radioClick);
        rbZaloPay.setOnClickListener(radioClick);
        rbPayPal.setOnClickListener(radioClick);
        rbVisa.setOnClickListener(radioClick);
        rbMastercard.setOnClickListener(radioClick);

        view.findViewById(R.id.btnConfirmPayment).setOnClickListener(v -> {
            if (getActivity() instanceof BookingActivity && getArguments() != null) {
                long checkIn = getArguments().getLong("check_in_millis", 0L);
                long checkOut = getArguments().getLong("check_out_millis", 0L);
                int guests = getArguments().getInt("guests", 0);
                ((BookingActivity) getActivity()).submitBooking(checkIn, checkOut, guests, totalPrice);
            }
        });

        selectPaymentCategory(CATEGORY_WALLET);
    }

    private String formatPrice(double amount) {
        if (hotel != null) return hotel.formatPrice(requireContext(), amount);
        if (tour != null) return tour.formatPrice(requireContext(), amount);
        return String.format(java.util.Locale.getDefault(), "%,.0f₫", amount);
    }

    private void selectPaymentCategory(int category) {
        applyTabState(btnWallet, ivWallet, tvWallet, category == CATEGORY_WALLET);
        applyTabState(btnBank, ivBank, tvBank, category == CATEGORY_BANK);
        applyTabState(btnCard, ivCard, tvCard, category == CATEGORY_CARD);

        if (layoutPaymentMethods != null) {
            layoutPaymentMethods.setVisibility(category == CATEGORY_WALLET ? View.VISIBLE : View.GONE);
        }
        if (layoutCardMethods != null) {
            layoutCardMethods.setVisibility(category == CATEGORY_CARD ? View.VISIBLE : View.GONE);
        }
        if (tvAddMethod != null) {
            int labelRes;
            switch (category) {
                case CATEGORY_BANK:
                    labelRes = R.string.booking_add_bank;
                    break;
                case CATEGORY_CARD:
                    labelRes = R.string.booking_add_card;
                    break;
                case CATEGORY_WALLET:
                default:
                    labelRes = R.string.booking_add_wallet;
                    break;
            }
            tvAddMethod.setText(getString(labelRes));
        }

        clearAllRadios();
        switch (category) {
            case CATEGORY_WALLET:
                rbZaloPay.setChecked(true);
                break;
            case CATEGORY_CARD:
                rbVisa.setChecked(true);
                break;
            case CATEGORY_BANK:
            default:
                break;
        }
    }

    private void applyTabState(LinearLayout tab, ImageView icon, TextView label, boolean active) {
        tab.setBackgroundResource(active ? R.drawable.bg_outline_blue : R.drawable.bg_outline_gray);
        icon.setImageTintList(ColorStateList.valueOf(active ? COLOR_BLUE : COLOR_BLACK));
        label.setTextColor(active ? COLOR_BLUE : COLOR_BLACK);
        label.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void clearAllRadios() {
        rbMomo.setChecked(false);
        rbZaloPay.setChecked(false);
        rbPayPal.setChecked(false);
        rbVisa.setChecked(false);
        rbMastercard.setChecked(false);
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
