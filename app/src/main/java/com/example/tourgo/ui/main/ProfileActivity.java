package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tourgo.R;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.utils.LocaleHelper;
import com.example.tourgo.utils.SessionManager;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvProfilePhone;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);

        // Đổ dữ liệu từ session thực tế
        if (session.isLoggedIn()) {
            if (tvProfileName != null) tvProfileName.setText(session.getShortName());
            if (tvProfileEmail != null) tvProfileEmail.setText(session.getEmail());
        }

        setupLogout();
        setupLanguage();
    }

    private void setupLogout() {
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.clear();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupLanguage() {
        MaterialButtonToggleGroup toggleLanguage = findViewById(R.id.toggleLanguage);
        if (toggleLanguage == null) return;

        // Set trạng thái nút trước khi gán listener
        String currentLang = LocaleHelper.getCurrentLanguageTag();
        if ("en".equals(currentLang)) {
            toggleLanguage.check(R.id.btnLangEn);
        } else {
            toggleLanguage.check(R.id.btnLangVi);
        }

        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String newLang = (checkedId == R.id.btnLangVi) ? "vi" : "en";
                // Chỉ chuyển đổi nếu ngôn ngữ khác với hiện tại để tránh giật màn hình
                if (!newLang.equals(LocaleHelper.getCurrentLanguageTag())) {
                    LocaleHelper.setAppLocale(newLang);
                }
            }
        });
    }
}
