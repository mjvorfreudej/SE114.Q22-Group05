package com.example.tourgo.ui.main.booking;
import com.example.tourgo.ui.main.home.MainActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

public class BookingSuccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyTopInset(view);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageView ivHotel = view.findViewById(R.id.ivHotel);
        ImageView ivTransactionQR = view.findViewById(R.id.ivTransactionQR);
        TextView tvTotalCost = view.findViewById(R.id.tvTotalCost);
        TextView tvBookingDate = view.findViewById(R.id.tvBookingDate);
        TextView tvGuestInfo = view.findViewById(R.id.tvGuestInfo);
        TextView tvConfirmationNumber = view.findViewById(R.id.tvConfirmationNumber);
        Button btnViewBooking = view.findViewById(R.id.btnViewBooking);
        Button btnBackHome = view.findViewById(R.id.btnBackHome);

        Hotel hotel = null;
        Tour tour = null;
        if (getActivity() instanceof BookingActivity) {
            hotel = ((BookingActivity) getActivity()).getHotel();
            tour = ((BookingActivity) getActivity()).getTour();
        }

        if (hotel != null) {
            if (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty()) {
                Glide.with(this)
                        .load(hotel.getImageUrls().get(0))
                        .placeholder(R.drawable.hotel_1)
                        .centerCrop()
                        .into(ivHotel);
            } else {
                ivHotel.setImageResource(hotel.getImageResId() != 0 ? hotel.getImageResId() : R.drawable.hotel_1);
            }
        } else if (tour != null) {
             if (tour.getImageUrls() != null && !tour.getImageUrls().isEmpty()) {
                Glide.with(this)
                        .load(tour.getImageUrls().get(0))
                        .placeholder(R.drawable.hotel_1)
                        .centerCrop()
                        .into(ivHotel);
            }
        }

        String transactionCode = null;
        if (getArguments() != null) {
            double total = getArguments().getDouble("total_price", 0.0);
            String bookingDate = getArguments().getString("check_in_out", "");
            String guestInfo = getArguments().getString("guest_info", "");
            // Theo y?u c?u: transaction_code = transfer_note
            String transferNote = getArguments().getString("transfer_note", null);
            transactionCode = transferNote;

            tvTotalCost.setText(formatCurrency(total));
            tvBookingDate.setText(bookingDate);
            tvGuestInfo.setText(guestInfo);
        } else {
            tvTotalCost.setText(formatCurrency(0.0));
        }

        if (transactionCode != null && !transactionCode.isEmpty()) {
            tvConfirmationNumber.setText(transactionCode);
            generateQRCode(transactionCode, ivTransactionQR);
        } else {
            tvConfirmationNumber.setText("N/A");
        }

        btnBack.setOnClickListener(v -> requireActivity().finish());

        btnViewBooking.setOnClickListener(v -> {
            requireActivity().finish();
        });

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private String formatCurrency(double amount) {
        String currency = new com.example.tourgo.data.local.SessionManager(requireContext()).getCurrency();
        if ("USD".equalsIgnoreCase(currency)) {
            return String.format(Locale.getDefault(), "$ %,.0f", amount);
        }
        return String.format(Locale.getDefault(), "%,.0f₫", amount);
    }

    private void generateQRCode(String content, ImageView imageView) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
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