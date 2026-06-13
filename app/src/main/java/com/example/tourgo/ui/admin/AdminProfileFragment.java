package com.example.tourgo.ui.admin;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;

/** Admin › Profile / Settings — account card, moderation & preferences rows, log out. */
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

        row(v, R.id.rowModPolicy, R.drawable.ic_gavel, R.string.adm_mod_policy, 0);
        row(v, R.id.rowAdminTeam, R.drawable.ic_users, R.string.adm_admin_team, R.string.adm_admin_team_count);
        row(v, R.id.rowAuditLog, R.drawable.ic_history, R.string.adm_audit_log, 0);
        row(v, R.id.rowNotifications, R.drawable.ic_bell_20, R.string.adm_notifications, 0);
        row(v, R.id.rowLanguage, R.drawable.ic_globe_24, R.string.adm_language, R.string.adm_language_value);
        row(v, R.id.rowPassword, R.drawable.ic_lock_24, R.string.adm_password_2fa, 0);
        row(v, R.id.rowHelp, R.drawable.ic_help_circle, R.string.adm_help_center, 0);

        v.findViewById(R.id.admBtnLogout).setOnClickListener(view -> showLogoutDialog());
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
            logout();
        });
        dialog.show();
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

    private void row(View root, int rowId, int iconRes, int labelRes, int trailingRes) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.admRowIcon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.admRowLabel)).setText(labelRes);
        TextView trailing = row.findViewById(R.id.admRowTrailing);
        if (trailingRes != 0) {
            trailing.setText(trailingRes);
            trailing.setVisibility(View.VISIBLE);
        }
    }
}
