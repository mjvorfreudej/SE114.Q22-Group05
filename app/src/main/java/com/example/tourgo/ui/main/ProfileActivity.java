package com.example.tourgo.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.MyBookingAdapter;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.utils.LocaleHelper;
import com.example.tourgo.data.local.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        if (session.isLoggedIn()) {
            if (tvProfileName != null) tvProfileName.setText(session.getShortName());
            if (tvProfileEmail != null) tvProfileEmail.setText(session.getEmail());
        }

        View btnBack = findViewById(R.id.btnProfileBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        setupBookings();
        setupLogout();
        setupLanguage();
        setupCurrency();
    }

    private void setupBookings() {
        RecyclerView rv = findViewById(R.id.rvMyBookings);
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        List<MyBookingAdapter.Item> items = new ArrayList<>(Arrays.asList(
                new MyBookingAdapter.Item("The Grand Orchid Resort", "13 Aug - 15 Aug", R.drawable.hotel_1),
                new MyBookingAdapter.Item("Hilton Bandung", "20 Sep - 22 Sep", R.drawable.hotel_2)
        ));
        rv.setAdapter(new MyBookingAdapter(items));
    }

    private void setupLogout() {
        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout == null) return;
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
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
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        dialog.show();
    }

    private void setupLanguage() {
        MaterialButtonToggleGroup toggleLanguage = findViewById(R.id.toggleLanguage);
        if (toggleLanguage == null) return;

        // Mặc định là VN nếu chưa được thiết lập (hoặc là "vi")
        String currentLang = LocaleHelper.getCurrentLanguageTag();
        if ("en".equals(currentLang)) {
            toggleLanguage.check(R.id.btnLangEn);
        } else {
            toggleLanguage.check(R.id.btnLangVi);
        }

        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String newLang = (checkedId == R.id.btnLangVi) ? "vi" : "en";
                if (!newLang.equals(LocaleHelper.getCurrentLanguageTag())) {
                    LocaleHelper.setAppLocale(newLang);
                }
            }
        });
    }

    private void setupCurrency() {
        MaterialButtonToggleGroup toggleCurrency = findViewById(R.id.toggleCurrency);
        if (toggleCurrency == null) return;

        // Mặc định là VND (đã được xử lý trong session.getCurrency())
        String currentCurrency = session.getCurrency();
        if ("USD".equalsIgnoreCase(currentCurrency)) {
            toggleCurrency.check(R.id.btnCurrUsd);
        } else {
            toggleCurrency.check(R.id.btnCurrVnd);
        }

        toggleCurrency.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String newCurrency = (checkedId == R.id.btnCurrVnd) ? "VND" : "USD";
                session.setCurrency(newCurrency);
            }
        });
    }
}
