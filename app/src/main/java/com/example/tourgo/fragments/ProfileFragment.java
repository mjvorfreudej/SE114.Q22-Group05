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
import com.example.tourgo.data.AppFakeData;
import com.example.tourgo.models.User;
import com.example.tourgo.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfilePhone;
    private View btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        btnLogout = view.findViewById(R.id.btnLogout);

        User user = AppFakeData.getUser();
        if (user != null) {
            tvProfileName.setText(user.getName());
            tvProfileEmail.setText(user.getEmail());
            tvProfilePhone.setText(user.getPhone());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }
}