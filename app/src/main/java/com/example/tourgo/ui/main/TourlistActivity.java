package com.example.tourgo.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.data.AppFakeData;

public class TourlistActivity extends AppCompatActivity {

    private RecyclerView recyclerTours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_list);

        recyclerTours = findViewById(R.id.recyclerTours);
        recyclerTours.setLayoutManager(new LinearLayoutManager(this));
        recyclerTours.setAdapter(new TourAdapter(AppFakeData.getTours()));
    }
}