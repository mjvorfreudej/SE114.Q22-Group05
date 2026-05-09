package com.example.tourgo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.utils.LocaleHelper;
import com.example.tourgo.utils.SessionManager;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfilePhone;
    private View btnLogout;
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
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Đổ dữ liệu từ session (tên 2 chữ)
        if (session.isLoggedIn()) {
            if (tvProfileName != null) tvProfileName.setText(session.getShortName());
            if (tvProfileEmail != null) tvProfileEmail.setText(session.getEmail());
            // Số điện thoại có thể bổ sung sau khi Supabase có lưu field này
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                session.clear();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }

        setupLanguage(view);
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
                // Chỉ chuyển đổi nếu ngôn ngữ được chọn khác với ngôn ngữ hiện tại
                if (!newLang.equals(LocaleHelper.getCurrentLanguageTag())) {
                    LocaleHelper.setAppLocale(newLang);
                }
            }
        });
    }
}
