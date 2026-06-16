package com.example.tourgo.ui.business;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.ui.auth.LoginActivity;

/** Business › Profile — account header, mini stats, settings rows, logout. */
public class BusinessProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        AdminUi.avatar(v.findViewById(R.id.bizProfileAvatar), getString(R.string.biz_owner_name));

        LayoutInflater inf = LayoutInflater.from(requireContext());
        LinearLayout business = v.findViewById(R.id.bizBusinessRows);
        addRow(inf, business, R.drawable.ic_building, R.string.biz_row_details, 0);
        addRow(inf, business, R.drawable.ic_dollar, R.string.biz_row_payouts, 0);
        addRow(inf, business, R.drawable.ic_tag, R.string.biz_row_tax, 0);
        addRow(inf, business, R.drawable.ic_users, R.string.biz_row_team, 0);

        LinearLayout prefs = v.findViewById(R.id.bizPrefRows);
        addRow(inf, prefs, R.drawable.ic_bell_20, R.string.biz_row_notifications, 0);
        addRow(inf, prefs, R.drawable.ic_globe_24, R.string.biz_row_language, R.string.biz_row_language_value);
        addRow(inf, prefs, R.drawable.ic_lock_24, R.string.biz_row_security, 0);
        addRow(inf, prefs, R.drawable.ic_help_circle, R.string.biz_row_help, 0);

        // Shared centered confirm popup (warning icon + red danger button) — same
        // across Traveler / Business / Admin.
        v.findViewById(R.id.bizLogout).setOnClickListener(view ->
                AdminUi.confirm(requireContext(),
                        getString(R.string.profile_logout_title),
                        getString(R.string.profile_logout_confirm),
                        getString(R.string.profile_logout_title), true, this::logout));
    }

    private void addRow(LayoutInflater inf, LinearLayout parent, int iconRes, int labelRes, int trailingRes) {
        View row = inf.inflate(R.layout.item_biz_profile_row, parent, false);
        ((ImageView) row.findViewById(R.id.bizRowIcon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.bizRowLabel)).setText(labelRes);
        if (trailingRes != 0) {
            TextView trailing = row.findViewById(R.id.bizRowTrailing);
            trailing.setText(trailingRes);
            trailing.setVisibility(View.VISIBLE);
        }
        parent.addView(row);
    }

    private void logout() {
        new SessionManager(requireContext()).clear();
        UserRepository.getInstance().clearCache();
        FavoriteRepository.getInstance().clearCache();
        HotelRepository.getInstance().clearCache();
        TourRepository.getInstance().clearCache();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
