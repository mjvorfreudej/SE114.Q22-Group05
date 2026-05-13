package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.adapters.TrendingHotelAdapter;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.utils.ImageLoader;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HotelScreenFragment extends Fragment {

    private PopularHotelAdapter popularAdapter;
    private TrendingHotelAdapter trendingAdapter;
    private ProgressBar progressBar;
    private RecyclerView rvPopular;
    private RecyclerView rvTrending;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_hotel_screen, container, false);

        session = new SessionManager(requireContext());
        progressBar = root.findViewById(R.id.progressBarHotelScreen);
        rvPopular = root.findViewById(R.id.rvHotelScreenPopular);
        rvTrending = root.findViewById(R.id.rvHotelScreenTrending);

        popularAdapter = new PopularHotelAdapter(new ArrayList<>());
        rvPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopular.setAdapter(popularAdapter);

        trendingAdapter = new TrendingHotelAdapter(new ArrayList<>());
        rvTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTrending.setAdapter(trendingAdapter);

        applyStatusBarInset(root.findViewById(R.id.hotelScreenHeader));

        root.findViewById(R.id.btnHotelScreenBack).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToHome();
            }
        });

        TextView tvSeeAllPopular = root.findViewById(R.id.tvSeeAllPopularHotels);
        TextView tvSeeAllTrending = root.findViewById(R.id.tvSeeAllTrendingHotels);
        tvSeeAllPopular.setOnClickListener(v -> openFullList("Most Popular"));
        tvSeeAllTrending.setOnClickListener(v -> openFullList("Trending Hotels"));

        loadHotels();
        return root;
    }

    private void applyStatusBarInset(View header) {
        if (header == null) return;
        final int basePaddingTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(header);
    }

    private void openFullList(String title) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).switchToHotelList(title, "", "", "");
        }
    }

    private void loadHotels() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = session.getUserId();
        String token = session.getAccessToken();

        HotelRepository.getInstance().loadHotels(userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (data != null && !data.isEmpty()) {
                        popularAdapter.setData(data.size() > 5 ? data.subList(0, 5) : data);
                        if (data.size() > 5) {
                            trendingAdapter.setData(data.subList(5, Math.min(data.size(), 10)));
                        } else {
                            trendingAdapter.setData(new ArrayList<>(data));
                        }
                        preloadImages(data);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), R.string.err_network, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void preloadImages(List<Hotel> hotels) {
        List<String> urls = new ArrayList<>();
        for (Hotel hotel : hotels) {
            if (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty()) {
                urls.add(hotel.getImageUrls().get(0));
            }
        }
        ImageLoader.preload(getContext(), urls);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (popularAdapter != null) popularAdapter.notifyDataSetChanged();
        if (trendingAdapter != null) trendingAdapter.notifyDataSetChanged();
    }
}
