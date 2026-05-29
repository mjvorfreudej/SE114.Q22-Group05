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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.HotelListAdapter;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.data.local.SessionManager;

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
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        return view;
    }

    private void setupHeader() {
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("title", "");
            String dest = args.getString("destination", "");
            String date = args.getString("date", "");
            String guest = args.getString("guest", "");

            if (tvLocationHeader != null) {
                if (!dest.isEmpty()) {
                    tvLocationHeader.setText(getString(R.string.hotel_in_location, dest));
                } else if (!title.isEmpty()) {
                    tvLocationHeader.setText(title);
                } else {
                    tvLocationHeader.setText(R.string.hotel_list_default_title);
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

        HotelRepository.getInstance().loadHotels(getContext(), userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (data != null) {
                        Bundle args = getArguments();
                        String title = args != null ? args.getString("title", "") : "";
                        
                        List<Hotel> filteredData;
                        if ("Most Popular".equals(title)) {
                            // Giả sử lấy 5 cái đầu làm Popular
                            filteredData = new ArrayList<>(data.size() > 5 ? data.subList(0, 5) : data);
                        } else if ("Trending Hotels".equals(title)) {
                            // Giả sử lấy từ 5-10 làm Trending
                            if (data.size() > 5) {
                                filteredData = new ArrayList<>(data.subList(5, Math.min(data.size(), 10)));
                            } else {
                                filteredData = new ArrayList<>(data);
                            }
                        } else {
                            filteredData = new ArrayList<>(data);
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