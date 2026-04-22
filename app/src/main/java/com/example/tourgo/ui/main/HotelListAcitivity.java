package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.AppFakeData;

public class HotelListAcitivity extends AppCompatActivity {

    private RecyclerView recyclerHotels;
    private TextView tvLocationHeader, tvDateGuestHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hotel_list);

        tvLocationHeader = findViewById(R.id.tvLocationHeader);
        tvDateGuestHeader = findViewById(R.id.tvDateGuestHeader);

        String destination = getIntent().getStringExtra("destination");
        String date = getIntent().getStringExtra("date");
        String guest = getIntent().getStringExtra("guest");

        if (destination != null) {
            tvLocationHeader.setText("Hotel in " + destination);
            tvDateGuestHeader.setText(date + " • " + guest);
        }

        recyclerHotels = findViewById(R.id.recyclerHotels);
        recyclerHotels.setLayoutManager(new LinearLayoutManager(this));
        recyclerHotels.setAdapter(new HotelListAdapter(AppFakeData.getHotels()));

        findViewById(R.id.btnBackList).setOnClickListener(v -> finish());
    }
}
