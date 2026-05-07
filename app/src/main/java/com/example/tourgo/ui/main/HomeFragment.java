package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tourgo.R;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.adapters.TrendingHotelAdapter;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.databinding.FragmentHomeBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PopularHotelAdapter popularAdapter;
    private TrendingHotelAdapter trendingAdapter;

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
        loadData();

        binding.btnFind.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToSearch();
            }
        });
        
        binding.layoutSearchDestination.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToSearch();
            }
        });
    }

    private void setupRecyclerViews() {
        popularAdapter = new PopularHotelAdapter(new ArrayList<>());
        binding.rvPopularHotels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopularHotels.setAdapter(popularAdapter);

        trendingAdapter = new TrendingHotelAdapter(new ArrayList<>());
        binding.rvTrendingHotels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTrendingHotels.setAdapter(trendingAdapter);
    }

    private void loadData() {
        HotelRepository.getInstance().loadHotels(new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (data != null && !data.isEmpty()) {
                    // Cắt bớt để hiển thị trang chủ (Ví dụ: 5 cái đầu là Popular, 5 cái sau là Trending)
                    popularAdapter.setData(data.size() > 5 ? data.subList(0, 5) : data);
                    
                    if (data.size() > 5) {
                        trendingAdapter.setData(data.subList(5, Math.min(data.size(), 10)));
                    } else {
                        trendingAdapter.setData(new ArrayList<>(data));
                    }
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.err_network, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại UI để đồng bộ trạng thái tim nếu đã thay đổi ở màn chi tiết
        if (popularAdapter != null) popularAdapter.notifyDataSetChanged();
        if (trendingAdapter != null) trendingAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
