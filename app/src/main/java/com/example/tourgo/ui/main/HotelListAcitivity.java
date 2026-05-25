package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.utils.ImageLoader;
import com.example.tourgo.data.local.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HotelListAcitivity extends AppCompatActivity {

    private RecyclerView recyclerHotels;
    private TextView tvLocationHeader, tvDateGuestHeader;
    private ProgressBar progressBar;
    private View layoutContent;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hotel_list);

        session = new SessionManager(this);
        tvLocationHeader = findViewById(R.id.tvLocationHeader);
        tvDateGuestHeader = findViewById(R.id.tvDateGuestHeader);
        progressBar = findViewById(R.id.pbHotelList);
        layoutContent = findViewById(R.id.layoutListContent);

        String destination = getIntent().getStringExtra("destination");
        String date = getIntent().getStringExtra("date");
        String guest = getIntent().getStringExtra("guest");

        if (destination != null) {
            tvLocationHeader.setText("Hotel in " + destination);
            tvDateGuestHeader.setText(date + " • " + guest);
        }

        HotelListAdapter adapter = new HotelListAdapter(new ArrayList<>());
        recyclerHotels = findViewById(R.id.recyclerHotels);
        recyclerHotels.setLayoutManager(new LinearLayoutManager(this));
        recyclerHotels.setAdapter(adapter);

        showLoading(true);
        
        // Sử dụng phiên bản loadHotels có userId và token để sync favorite
        String userId = session.getUserId();
        String token = session.getAccessToken();

        HotelRepository.getInstance().loadHotels(userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (data != null) {
                        adapter.setData(data);
                        // Preload ảnh
                        List<String> urls = new ArrayList<>();
                        for (Hotel h : data) {
                            if (h.getImageUrls() != null && !h.getImageUrls().isEmpty()) {
                                urls.add(h.getImageUrls().get(0));
                            }
                        }
                        ImageLoader.preload(HotelListAcitivity.this, urls);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(HotelListAcitivity.this, "Lỗi kết nối: " + rawMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });

        findViewById(R.id.btnBackList).setOnClickListener(v -> finish());
    }

    private void showLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (layoutContent != null) layoutContent.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }
}
