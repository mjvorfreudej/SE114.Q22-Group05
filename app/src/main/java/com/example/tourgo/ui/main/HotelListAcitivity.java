package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.AppFakeData;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;

import java.util.ArrayList;
import java.util.List;

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
        HotelListAdapter adapter = new HotelListAdapter(new ArrayList<>());
        HotelRepository.getInstance().loadHotels(new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                adapter.setData(data);
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                Toast.makeText(HotelListAcitivity.this, R.string.err_network, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerHotels = findViewById(R.id.recyclerHotels);
        recyclerHotels.setLayoutManager(new LinearLayoutManager(this));
        recyclerHotels.setAdapter(adapter);

        findViewById(R.id.btnBackList).setOnClickListener(v -> finish());
    }
}
