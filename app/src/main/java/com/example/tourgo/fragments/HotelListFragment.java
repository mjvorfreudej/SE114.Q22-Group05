package com.example.tourgo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.AppFakeData;

public class HotelListFragment extends Fragment {

    private HotelListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerHotels);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HotelListAdapter(AppFakeData.getHotels());
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void filter(String query) {
        if (adapter != null) {
            adapter.filter(query);
        }
    }
}