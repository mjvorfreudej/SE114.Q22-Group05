package com.example.tourgo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HotelListFragment extends Fragment {

    private HotelListAdapter adapter;
    private ProgressBar progressBar;
    private SessionManager session;
    private TextView tvLocationHeader, tvDateGuestHeader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

        progressBar = view.findViewById(R.id.pbHotelList);
        tvLocationHeader = view.findViewById(R.id.tvLocationHeader);
        tvDateGuestHeader = view.findViewById(R.id.tvDateGuestHeader);
        session = new SessionManager(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerHotels);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new HotelListAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        setupHeader();
        loadHotels();

        View btnBack = view.findViewById(R.id.btnBackList);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        applyTopInset(view.findViewById(R.id.llHotelListHeader));

        return view;
    }

    private void applyTopInset(View header) {
        if (header == null) return;
        final int basePaddingTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(header);
    }

    private void setupHeader() {
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("title", "Hotels");
            String dest = args.getString("destination", "");
            String date = args.getString("date", "");
            String guest = args.getString("guest", "");

            if (tvLocationHeader != null) {
                if (!dest.isEmpty()) {
                    tvLocationHeader.setText("Hotels in " + dest);
                } else {
                    tvLocationHeader.setText(title);
                }
            }

            if (tvDateGuestHeader != null) {
                StringBuilder sb = new StringBuilder();
                if (!date.isEmpty()) sb.append(date);
                if (!guest.isEmpty()) {
                    if (sb.length() > 0) sb.append(" • ");
                    sb.append(guest);
                }
                
                if (sb.length() > 0) {
                    tvDateGuestHeader.setText(sb.toString());
                    tvDateGuestHeader.setVisibility(View.VISIBLE);
                } else {
                    tvDateGuestHeader.setVisibility(View.GONE);
                }
            }
        }
    }

    private void loadHotels() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        String userId = session.getUserId();
        String token = session.getAccessToken();

        HotelRepository.getInstance().loadHotels(userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (data != null) {
                        Bundle args = getArguments();
                        String title = args != null ? args.getString("title", "") : "";
                        
                        List<Hotel> filteredData = new ArrayList<>();
                        if ("Most Popular".equals(title)) {
                            // Giả sử lấy 5 cái đầu làm Popular
                            filteredData = data.size() > 5 ? data.subList(0, 5) : data;
                        } else if ("Trending Hotels".equals(title)) {
                            // Giả sử lấy từ 5-10 làm Trending
                            if (data.size() > 5) {
                                filteredData = data.subList(5, Math.min(data.size(), 10));
                            } else {
                                filteredData = data;
                            }
                        } else {
                            filteredData = data;
                        }
                        adapter.setData(filteredData);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.err_network, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void filter(String query) {
        if (adapter != null) {
            adapter.filter(query);
        }
    }
}