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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;

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

        // Nhận và hiển thị dữ liệu từ arguments
        if (getArguments() != null) {
            double total = getArguments().getDouble("paid_amount", getArguments().getDouble("total_price", 0.0));
            String bookingDate = getArguments().getString("check_in_out", "");
            String guestInfo = getArguments().getString("guest_info", "");
            String transactionCode = getArguments().getString("transaction_code", "");
            String transferNote = getArguments().getString("transfer_note", transactionCode);

            if (hotel != null) {
                tvTotalCost.setText(hotel.formatPrice(requireContext(), total));
            } else if (tour != null) {
                tvTotalCost.setText(tour.formatPrice(requireContext(), total));
            } else {
                tvTotalCost.setText(formatCurrency(total));
            }

            tvBookingDate.setText(bookingDate);
            tvGuestInfo.setText(guestInfo);

            String qrContent = transferNote != null && !transferNote.isEmpty() ? transferNote : transactionCode;
            if (qrContent != null && !qrContent.isEmpty()) {
                Bitmap qrBitmap = createQrBitmap(qrContent, 180);
                if (qrBitmap != null) {
                    ivTransactionQR.setImageBitmap(qrBitmap);
                } else {
                    ivTransactionQR.setImageResource(R.drawable.ic_qr_code_placeholder);
                }
            } else {
                ivTransactionQR.setImageResource(R.drawable.ic_qr_code_placeholder);
            }
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
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount);
    }

    private Bitmap createQrBitmap(String content, int sizePx) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new QRCodeWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    sizePx,
                    sizePx,
                    hints
            );

            Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < sizePx; x++) {
                for (int y = 0; y < sizePx; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            return null;
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
