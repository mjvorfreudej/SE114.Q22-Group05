package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class TourScreenFragment extends Fragment {

    private TourAdapter tourAdapter;
    private ProgressBar progressBar;
    private RecyclerView rvTours;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tour_screen, container, false);

        session = new SessionManager(requireContext());
        progressBar = root.findViewById(R.id.progressBarTourScreen);
        rvTours = root.findViewById(R.id.rvTourScreenTrending);

        tourAdapter = new TourAdapter(new ArrayList<>());
        tourAdapter.setOnTourClickListener(tour -> {
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra("hotel_object", tour);
            startActivity(intent);
        });
        rvTours.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTours.setAdapter(tourAdapter);

        applyStatusBarInset(root.findViewById(R.id.tourScreenHeader));

        root.findViewById(R.id.btnTourScreenBack).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToHome();
            }
        });

        loadTours();
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

    private void loadTours() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = session.getUserId();
        String token = session.getAccessToken();

        TourRepository.getInstance().loadTours(getContext(), userId, token, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (data != null) {
                        tourAdapter.setData(data);
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

    private void preloadImages(List<Tour> tours) {
        List<String> urls = new ArrayList<>();
        for (Tour tour : tours) {
            if (tour.getImageUrls() != null && !tour.getImageUrls().isEmpty()) {
                urls.add(tour.getImageUrls().get(0));
            }
        }
        ImageLoader.preload(getContext(), urls);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tourAdapter != null) tourAdapter.notifyDataSetChanged();
    }
}
