package com.example.tourgo.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.MyBookingAdapter;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.utils.LocaleHelper;
import com.example.tourgo.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail;
    private ImageView ivProfileAvatar;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyTopInset(view);

        session = new SessionManager(requireContext());

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);

        if (session.isLoggedIn()) {
            if (tvProfileName != null) tvProfileName.setText(session.getShortName());
            if (tvProfileEmail != null) tvProfileEmail.setText(session.getEmail());
        }

        View btnBack = view.findViewById(R.id.btnProfileBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        setupBookings(view);
        setupSettings(view);
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

    private void setupBookings(View root) {
        RecyclerView rv = root.findViewById(R.id.rvMyBookings);
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        
        // Mocking data based on currency
        String currency = session.getCurrency();
        String price1 = "VND".equals(currency) ? "2.500.000đ" : "$100";
        String price2 = "VND".equals(currency) ? "1.800.000đ" : "$75";

        List<MyBookingAdapter.Item> items = new ArrayList<>(Arrays.asList(
                new MyBookingAdapter.Item("The Grand Orchid Resort", "13 Aug - 15 Aug", R.drawable.hotel_1),
                new MyBookingAdapter.Item("Hilton Bandung", "20 Sep - 22 Sep", R.drawable.hotel_2)
        ));
        // Note: MyBookingAdapter.Item doesn't currently support prices, but we refresh the list anyway.
        // If the adapter supported it, we would pass price1 and price2 here.
        rv.setAdapter(new MyBookingAdapter(items));
    }

    private void setupSettings(View root) {
        // Edit Profile
        View btnEdit = root.findViewById(R.id.btnProfileEdit);
        if (btnEdit != null) btnEdit.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Profile", Toast.LENGTH_SHORT).show();
        });

        // Language Selection
        MaterialButtonToggleGroup toggleLanguage = root.findViewById(R.id.toggleLanguage);
        if (toggleLanguage != null) {
            String currentLang = LocaleHelper.getCurrentLanguageTag();
            if ("vi".equals(currentLang)) {
                toggleLanguage.check(R.id.btnLangVi);
            } else {
                toggleLanguage.check(R.id.btnLangEn);
            }

            toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    String lang = (checkedId == R.id.btnLangVi) ? "vi" : "en";
                    if (!lang.equals(LocaleHelper.getCurrentLanguageTag())) {
                        LocaleHelper.setAppLocale(lang);
                        // Activity will recreate automatically
                    }
                }
            });
        }

        // Currency Selection
        MaterialButtonToggleGroup toggleCurrency = root.findViewById(R.id.toggleCurrency);
        if (toggleCurrency != null) {
            String currentCurr = session.getCurrency();
            if ("VND".equals(currentCurr)) {
                toggleCurrency.check(R.id.btnCurrVnd);
            } else {
                toggleCurrency.check(R.id.btnCurrUsd);
            }

            toggleCurrency.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    String currency = (checkedId == R.id.btnCurrVnd) ? "VND" : "USD";
                    if (!currency.equals(session.getCurrency())) {
                        session.setCurrency(currency);
                        setupBookings(root); // Reload bookings with new currency
                        Toast.makeText(getContext(), "Currency changed to: " + currency, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Settings Rows
        View rowPersonalInfo = root.findViewById(R.id.rowPersonalInfo);
        if (rowPersonalInfo != null) rowPersonalInfo.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Personal Information", Toast.LENGTH_SHORT).show();
        });

        View rowPayment = root.findViewById(R.id.rowPaymentMethods);
        if (rowPayment != null) rowPayment.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Payment Methods", Toast.LENGTH_SHORT).show();
        });

        View rowNotifications = root.findViewById(R.id.rowNotifications);
        if (rowNotifications != null) rowNotifications.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notifications", Toast.LENGTH_SHORT).show();
        });

        View rowPrivacy = root.findViewById(R.id.rowPrivacy);
        if (rowPrivacy != null) rowPrivacy.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Privacy & Security", Toast.LENGTH_SHORT).show();
        });

        // Logout
        View btnLogout = root.findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_logout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton cancel = dialog.findViewById(R.id.btnLogoutCancel);
        MaterialButton confirm = dialog.findViewById(R.id.btnLogoutConfirm);
        if (cancel != null) cancel.setOnClickListener(v -> dialog.dismiss());
        if (confirm != null) confirm.setOnClickListener(v -> {
            dialog.dismiss();
            session.clear();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        dialog.show();
    }
}
