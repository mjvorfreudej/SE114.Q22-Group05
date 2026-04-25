package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tourgo.R;
import com.example.tourgo.data.AppFakeData;
import com.example.tourgo.models.User;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.utils.LocaleHelper;
import com.example.tourgo.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvProfilePhone, tvProfileDescription;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileDescription = findViewById(R.id.tvProfileDescription);

        User user = AppFakeData.getUser();
        if (user != null) {
            tvProfileName.setText(user.getName());
            tvProfileEmail.setText(user.getEmail());
            tvProfilePhone.setText(user.getPhone());
            // tvProfileDescription.setText(user.getDescription()); // Field might not exist in current User model
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
        TextView tvCurrent = findViewById(R.id.tvCurrentLang);
        tvCurrent.setText(labelOf(LocaleHelper.getCurrentLanguageTag()));

        findViewById(R.id.btnLanguage).setOnClickListener(v -> showLanguageDialog(tvCurrent));
    }

    private void showLanguageDialog(TextView tvCurrent) {
        final String[] tags   = { "vi", "en", "" };
        final String[] labels = {
                getString(R.string.lang_vi),
                getString(R.string.lang_en),
                getString(R.string.lang_system)
        };

        String current = LocaleHelper.getCurrentLanguageTag();
        int checked = 2;
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].equals(current)) { checked = i; break; }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.profile_language)
                .setSingleChoiceItems(labels, checked, (d, which) -> {
                    LocaleHelper.setAppLocale(tags[which]);
                    tvCurrent.setText(labels[which]);
                    d.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private String labelOf(String tag) {
        if ("vi".equals(tag)) return getString(R.string.lang_vi);
        if ("en".equals(tag)) return getString(R.string.lang_en);
        return getString(R.string.lang_system);
    }
}