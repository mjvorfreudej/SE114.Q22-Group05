package com.example.tourgo.ui.main.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.service.UserService;
import com.example.tourgo.ui.admin.AdminActivity;
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.ui.business.BusinessActivity;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.ui.main.booking.BookingHistorySection;
import com.example.tourgo.utils.LocaleHelper;
import com.example.tourgo.data.local.SessionManager;
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

        // Load user profile from server
        loadUserProfile();

        View btnBack = view.findViewById(R.id.btnProfileBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        setupBookings(view);
        setupSettings(view);
    }

    private void loadUserProfile() {
        if (!session.isLoggedIn()) {
            return;
        }

        // Dùng UserRepository để lấy user từ cache hoặc API
        UserRepository.getInstance().getCurrentUser(requireContext(), false, new DataCallback<User>() {
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

                Toast.makeText(requireContext(), "Failed to load profile: " + message, Toast.LENGTH_SHORT).show();
            }
        });
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
        // Real booking history from the bookings table, with PAID/COMPLETED tabs.
        new BookingHistorySection(requireContext()).bind(root);
    }

    private void setupSettings(View root) {
        // Admin Console — visible only to admins (email suffix / whitelist via SessionManager)
        View rowAdmin = root.findViewById(R.id.rowAdminConsole);
        if (rowAdmin != null) {
            rowAdmin.setVisibility(session.isAdmin() ? View.VISIBLE : View.GONE);
            rowAdmin.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), AdminActivity.class)));
        }

        // Business Console — visible only to business/partner accounts
        View rowBusiness = root.findViewById(R.id.rowBusinessConsole);
        if (rowBusiness != null) {
            rowBusiness.setVisibility(session.isBusiness() ? View.VISIBLE : View.GONE);
            rowBusiness.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), BusinessActivity.class)));
        }

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
        // Shared centered confirm popup (warning icon + red danger button) — same
        // across Traveler / Business / Admin.
        AdminUi.confirm(requireContext(),
                getString(R.string.profile_logout_title),
                getString(R.string.profile_logout_confirm),
                getString(R.string.profile_logout_title),
                true, this::logout);
    }

    private void logout() {
        session.clear();

        // Clear all repository caches khi logout
        UserRepository.getInstance().clearCache();
        FavoriteRepository.getInstance().clearCache();
        HotelRepository.getInstance().clearCache();
        TourRepository.getInstance().clearCache();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
