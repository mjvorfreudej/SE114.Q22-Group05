package com.example.tourgo.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.utils.LocaleHelper;

/**
 * Admin › Profile / Settings — account card, moderation & preferences rows, log out.
 *
 * <p>Each settings row opens its full detail screen in {@link AdminDetailActivity}
 * (the "TourGo Admin Detail Screens" hand-off). The notification centre is reached
 * from the admin home bell, not from here.
 */
public class AdminProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        row(v, R.id.rowModPolicy, R.drawable.ic_gavel, R.string.adm_mod_policy, 0,
                () -> openDetail(AdminDetailActivity.SCREEN_MOD_POLICY));
        row(v, R.id.rowAdminTeam, R.drawable.ic_users, R.string.adm_admin_team, R.string.adm_admin_team_count,
                () -> openDetail(AdminDetailActivity.SCREEN_TEAM));
        row(v, R.id.rowAuditLog, R.drawable.ic_history, R.string.adm_audit_log, 0,
                () -> openDetail(AdminDetailActivity.SCREEN_AUDIT));
        row(v, R.id.rowLanguage, R.drawable.ic_globe_24, R.string.adm_language, 0,
                () -> openDetail(AdminDetailActivity.SCREEN_LANGUAGE));
        row(v, R.id.rowPassword, R.drawable.ic_lock_24, R.string.adm_password_2fa, 0,
                () -> openDetail(AdminDetailActivity.SCREEN_SECURITY));
        row(v, R.id.rowHelp, R.drawable.ic_help_circle, R.string.adm_help_center, 0,
                () -> openDetail(AdminDetailActivity.SCREEN_HELP));

        // The language row trails the currently active language (native name).
        setLanguageTrailing(v);

        v.findViewById(R.id.admBtnLogout).setOnClickListener(view -> showLogoutDialog());
    }

    private void openDetail(String screen) {
        Intent i = new Intent(requireContext(), AdminDetailActivity.class);
        i.putExtra(AdminDetailActivity.EXTRA_SCREEN, screen);
        startActivity(i);
    }

    private void setLanguageTrailing(View root) {
        View row = root.findViewById(R.id.rowLanguage);
        TextView trailing = row.findViewById(R.id.admRowTrailing);
        trailing.setText("vi".equals(LocaleHelper.getCurrentLanguageTag()) ? "Tiếng Việt" : "English");
        trailing.setVisibility(View.VISIBLE);
    }

    // ── Logout ──────────────────────────────────────────────────────────────────
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
        new SessionManager(requireContext()).clear();

        // Clear all repository caches on logout (mirrors the user-side ProfileFragment).
        UserRepository.getInstance().clearCache();
        FavoriteRepository.getInstance().clearCache();
        HotelRepository.getInstance().clearCache();
        TourRepository.getInstance().clearCache();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ── Row binding ───────────────────────────────────────────────────────────
    private void row(View root, int rowId, @DrawableRes int iconRes, @StringRes int labelRes,
                     int trailingRes, Runnable onClick) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.admRowIcon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.admRowLabel)).setText(labelRes);
        TextView trailing = row.findViewById(R.id.admRowTrailing);
        if (trailingRes != 0) {
            trailing.setText(trailingRes);
            trailing.setVisibility(View.VISIBLE);
        }
        if (onClick != null) row.setOnClickListener(v -> onClick.run());
    }
}
