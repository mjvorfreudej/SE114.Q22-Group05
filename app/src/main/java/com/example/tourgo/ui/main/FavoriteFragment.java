package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private SessionManager session;
    private HotelListAdapter adapter;
    private View progressBar;

    public FavoriteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        session = new SessionManager(requireContext());
        progressBar = view.findViewById(R.id.pbFavorite);
        
        RecyclerView rv = view.findViewById(R.id.rvFavoriteItem);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HotelListAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFavorites();
    }

    private void loadFavorites() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        String userId = session.getUserId();
        String token = session.getAccessToken();

        HotelRepository.getInstance().loadHotels(userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    List<Hotel> favoriteHotels = new ArrayList<>();
                    if (data != null) {
                        for (Hotel hotel : data) {
                            if (hotel.isFavorite()) {
                                favoriteHotels.add(hotel);
                            }
                        }
                    }
                    Collections.reverse(favoriteHotels);
                    adapter.setData(favoriteHotels);
                    
                    if (favoriteHotels.isEmpty()) {
                        Toast.makeText(getContext(), R.string.no_favorites, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.err_load_favorites, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
