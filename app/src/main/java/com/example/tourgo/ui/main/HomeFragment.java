package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.adapters.TrendingHotelAdapter;
import com.example.tourgo.data.AppFakeData;
import com.example.tourgo.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();

        binding.btnFind.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HotelListAcitivity.class);
            intent.putExtra("destination", binding.tvSearchDestination.getText().toString());
            intent.putExtra("date", binding.tvCheckInOut.getText().toString());
            intent.putExtra("guest", binding.tvGuest.getText().toString());
            startActivity(intent);
        });
    }

    private void setupRecyclerViews() {
        binding.rvPopularHotels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopularHotels.setAdapter(new PopularHotelAdapter(AppFakeData.getPopularHotelItems()));

        binding.rvTrendingHotels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTrendingHotels.setAdapter(new TrendingHotelAdapter(AppFakeData.getTrendingHotelItems()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
