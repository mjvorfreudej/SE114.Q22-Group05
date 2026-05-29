package com.example.tourgo.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;

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

        v.findViewById(R.id.admBtnLogout).setOnClickListener(view ->
                Toast.makeText(requireContext(), R.string.adm_toast_logged_out, Toast.LENGTH_SHORT).show());
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
