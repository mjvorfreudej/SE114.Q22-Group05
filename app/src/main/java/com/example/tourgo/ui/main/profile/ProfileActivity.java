package com.example.tourgo.ui.main.profile;
import com.example.tourgo.ui.main.booking.BookingHistorySection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.MyBookingAdapter;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.service.UserService;
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.utils.LocaleHelper;
import com.example.tourgo.data.local.SessionManager;
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

        // Load user profile from server
        loadUserProfile();

        View btnBack = findViewById(R.id.btnProfileBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        setupBookings();
        setupLogout();
        setupLanguage();
        setupCurrency();
    }

    private void loadUserProfile() {
        if (!session.isLoggedIn()) {
            return;
        }

        UserService.getCurrentUser(this, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    if (tvProfileName != null) tvProfileName.setText(user.getName());
                    if (tvProfileEmail != null) tvProfileEmail.setText(user.getEmail());

                    session.saveUserInfo(user.getId(), user.getEmail(), user.getName());
                }
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                if (tvProfileName != null) tvProfileName.setText(session.getShortName());
                if (tvProfileEmail != null) tvProfileEmail.setText(session.getEmail());

                Toast.makeText(ProfileActivity.this, "Failed to load profile: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBookings() {
        // Real booking history from the bookings table, with PAID/COMPLETED tabs.
        new BookingHistorySection(this).bind(findViewById(android.R.id.content));
    }

    private void setupLogout() {
        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout == null) return;
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        // Shared centered confirm popup (warning icon + red danger button) — same
        // across Traveler / Business / Admin.
        AdminUi.confirm(this,
                getString(R.string.profile_logout_title),
                getString(R.string.profile_logout_confirm),
                getString(R.string.profile_logout_title),
                true, this::logout);
    }

    private void logout() {
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
