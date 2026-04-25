package com.example.tourgo.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.remote.SupabaseConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteFragment extends Fragment {

    public FavoriteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        RecyclerView rv = view.findViewById(R.id.rvFavoriteItem);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        HotelListAdapter adapter = new HotelListAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        HotelRepository.getInstance().loadHotels(new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                List<Hotel> hotels = new ArrayList<>();
                for(Hotel hotel : data) {
                    if(hotel.isFavorite()){
                        hotels.add(hotel);
                    }
                }
                Collections.reverse(hotels);
                adapter.setData(hotels);
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                Toast.makeText(getContext(), R.string.err_network, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}