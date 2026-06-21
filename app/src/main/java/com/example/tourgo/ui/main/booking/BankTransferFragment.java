package com.example.tourgo.ui.main.booking;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Booking;
import com.example.tourgo.remote.service.BookingService;

import java.text.NumberFormat;
import java.util.Locale;

public class BankTransferFragment extends Fragment {

    private ImageView imgQrCode;
    private TextView tvBankName, tvAccountNumber, tvAccountHolder, tvAmount, tvTransferNote;
    private Button btnCopyAccount, btnCopyNote, btnCheckPayment;
    private String bookingId;
    private double bankAmount;
    private String transferNote;
    private String transactionCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bank_transfer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgQrCode = view.findViewById(R.id.imgQrCode);
        tvBankName = view.findViewById(R.id.tvBankName);
        tvAccountNumber = view.findViewById(R.id.tvAccountNumber);
        tvAccountHolder = view.findViewById(R.id.tvAccountHolder);
        tvAmount = view.findViewById(R.id.tvAmount);
        tvTransferNote = view.findViewById(R.id.tvTransferNote);
        btnCopyAccount = view.findViewById(R.id.btnCopyAccount);
        btnCopyNote = view.findViewById(R.id.btnCopyNote);
        btnCheckPayment = view.findViewById(R.id.btnCheckPayment);

        if (getArguments() != null) {
            bookingId = getArguments().getString(PaymentActivity.EXTRA_BOOKING_ID, "");
            bankAmount = getArguments().getDouble("bank_amount", 0.0);

            String bankName = getArguments().getString("bank_name", "");
            String accountNumber = getArguments().getString("account_number", "");
            String accountHolder = getArguments().getString("account_holder", "");
            String transferNote = getArguments().getString("transfer_note", bookingId);

            transactionCode = getArguments().getString("transaction_code", null);
            // Theo y?u c?u: transaction code = transfer note
            this.transferNote = transferNote;

            tvBankName.setText(bankName);
            tvAccountNumber.setText(accountNumber);
            tvAccountHolder.setText(accountHolder);
            tvAmount.setText(formatPrice(bankAmount));
            tvTransferNote.setText(transferNote);

            loadVietQr(bankName, accountNumber, bankAmount, transferNote);
        }

        btnCopyAccount.setOnClickListener(v -> copyToClipboard(getString(R.string.account_number), tvAccountNumber.getText().toString()));
        btnCopyNote.setOnClickListener(v -> copyToClipboard(getString(R.string.transfer_detail), tvTransferNote.getText().toString()));
        btnCheckPayment.setOnClickListener(v -> checkTransferPaymentStatus());

        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().finish());
    }

    private String formatPrice(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    private void loadVietQr(String bankName, String accountNumber, double amount, String transferNote) {
        String bankCode = normalizeBankCode(bankName);
        String cleanAccountNumber = accountNumber == null ? "" : accountNumber.trim();

        if (bankCode.isEmpty() || cleanAccountNumber.isEmpty()) {
            return;
        }

        String qrUrl = new Uri.Builder()
                .scheme("https")
                .authority("img.vietqr.io")
                .appendPath("image")
                .appendPath(bankCode + "-" + cleanAccountNumber + "-compact2.png")
                .appendQueryParameter("amount", String.valueOf(Math.round(amount)))
                .appendQueryParameter("addInfo", transferNote)
                .build()
                .toString();

        Glide.with(this)
                .load(qrUrl)
                .placeholder(R.drawable.ic_qr_code_placeholder)
                .error(R.drawable.ic_qr_code_placeholder)
                .into(imgQrCode);
    }

    private String normalizeBankCode(String bankName) {
        if (bankName == null) {
            return "";
        }

        String normalized = bankName.trim().toUpperCase(Locale.ROOT)
                .replace(" ", "")
                .replace("-", "");

        switch (normalized) {
            case "NGOAITHUONGVIETNAM":
            case "VIETCOMBANK":
            case "VCB":
                return "VCB";
            case "DAUTUVAPHATTRIENVIETNAM":
            case "BIDV":
                return "BIDV";
            case "VIETINBANK":
            case "CTG":
                return "CTG";
            case "AGRIBANK":
                return "VBA";
            case "TECHCOMBANK":
            case "TCB":
                return "TCB";
            case "MBBANK":
            case "MBB":
                return "MB";
            case "ACB":
                return "ACB";
            case "SACOMBANK":
            case "STB":
                return "STB";
            case "TPBANK":
            case "TPB":
                return "TPB";
            case "VPBANK":
            case "VPB":
                return "VPB";
            default:
                return normalized;
        }
    }

    private void checkTransferPaymentStatus() {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.payment_error, Toast.LENGTH_SHORT).show();
            return;
        }

        btnCheckPayment.setEnabled(false);
        BookingService.getBookingById(requireContext(), bookingId, new DataCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                btnCheckPayment.setEnabled(true);
                String status = booking != null ? booking.getStatus() : null;
                if (isPaidStatus(status)) {
                    if (getActivity() instanceof PaymentActivity) {
                        ((PaymentActivity) getActivity()).showBookingSuccessAfterTransfer(transactionCode, transferNote);
                    }
                } else {
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.not_paid),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                btnCheckPayment.setEnabled(true);
                Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private boolean isPaidStatus(String status) {
        if (status == null) {
            return false;
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return "PAID".equals(normalized)
                || "COMPLETED".equals(normalized)
                || "CONFIRMED".equals(normalized);
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "Copy " + label, Toast.LENGTH_SHORT).show();
    }
}
