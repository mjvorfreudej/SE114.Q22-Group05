package com.example.tourgo.ui.main.favorite;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.ui.main.detail.DetailActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteListFragment extends Fragment {

    public static final String ARG_TYPE = "type";
    public static final String TYPE_HOTEL = "hotel";
    public static final String TYPE_TOUR = "tour";

    private String type;
    private RecyclerView recyclerView;
    private View progressBar;
    private View emptyLayout;
    private SessionManager session;
    private HotelListAdapter hotelAdapter;
    private TourAdapter tourAdapter;

    public static FavoriteListFragment newInstance(String type) {
        FavoriteListFragment fragment = new FavoriteListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());
        recyclerView = view.findViewById(R.id.rvFavoriteList);
        progressBar = view.findViewById(R.id.pbFavoriteList);
        emptyLayout = view.findViewById(R.id.layoutEmptyFavorite);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (TYPE_HOTEL.equals(type)) {
            hotelAdapter = new HotelListAdapter(new ArrayList<>());
            recyclerView.setAdapter(hotelAdapter);
            loadFavoriteHotels();
        } else {
            tourAdapter = new TourAdapter(new ArrayList<>());
            tourAdapter.setOnTourClickListener(tour -> {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("hotel_object", tour); // Match existing logic
                startActivity(intent);
            });
            recyclerView.setAdapter(tourAdapter);
            loadFavoriteTours();
        }
    }

    private void loadFavoriteHotels() {
        setLoading(true);
        String userId = session.getUserId();
        String token = session.getAccessToken();

        HotelRepository.getInstance().loadHotels(getContext(), userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    List<Hotel> favorites = new ArrayList<>();
                    if (data != null) {
                        for (Hotel h : data) if (h.isFavorite()) favorites.add(h);
                    }
                    Collections.reverse(favorites);
                    hotelAdapter.setData(favorites);
                    emptyLayout.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(getContext(), R.string.err_load_favorites, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadFavoriteTours() {
        setLoading(true);
        String userId = session.getUserId();
        String token = session.getAccessToken();

        TourRepository.getInstance().loadTours(requireContext(), userId, token, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    List<Tour> favorites = new ArrayList<>();
                    if (data != null) {
                        for (Tour t : data) if (t.isFavorite()) favorites.add(t);
                    }
                    Collections.reverse(favorites);
                    tourAdapter.setData(favorites);
                    emptyLayout.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(getContext(), R.string.err_load_favorites, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) emptyLayout.setVisibility(View.GONE);
    }
}
