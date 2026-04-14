package com.example.tourgo.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.AppFakeData;

public class HotelListAcitivity extends AppCompatActivity {

    private RecyclerView recyclerHotels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hotel_list);

        recyclerHotels = findViewById(R.id.recyclerHotels);
        recyclerHotels.setLayoutManager(new LinearLayoutManager(this));
        recyclerHotels.setAdapter(new HotelListAdapter(AppFakeData.getHotels()));
    }
}