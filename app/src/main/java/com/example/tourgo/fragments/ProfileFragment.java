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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);

        if (session.isLoggedIn()) {
            if (tvProfileName != null) tvProfileName.setText(session.getShortName());
            if (tvProfileEmail != null) tvProfileEmail.setText(session.getEmail());
        }

        View btnBack = view.findViewById(R.id.btnProfileBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        setupBookings(view);
        setupLogout(view);
        setupLanguage(view);
    }

    private void setupBookings(View root) {
        RecyclerView rv = root.findViewById(R.id.rvMyBookings);
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        List<MyBookingAdapter.Item> items = new ArrayList<>(Arrays.asList(
                new MyBookingAdapter.Item("The Grand Orchid Resort", "13 Aug - 15 Aug", R.drawable.hotel_1),
                new MyBookingAdapter.Item("Hilton Bandung", "20 Sep - 22 Sep", R.drawable.hotel_2)
        ));
        rv.setAdapter(new MyBookingAdapter(items));
    }

    private void setupLogout(View root) {
        View btnLogout = root.findViewById(R.id.btnLogout);
        if (btnLogout == null) return;
        btnLogout.setOnClickListener(v -> showLogoutDialog());
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

    private void setupLanguage(View view) {
        MaterialButtonToggleGroup toggleLanguage = view.findViewById(R.id.toggleLanguage);
        if (toggleLanguage == null) return;

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
}
