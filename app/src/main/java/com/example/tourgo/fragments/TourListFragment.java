package com.example.tourgo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.TourAdapter;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.ui.main.DetailActivity;

import java.util.ArrayList;
import java.util.List;

public class TourListFragment extends Fragment {

    private TourAdapter adapter;
    private ProgressBar progressBar;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tour_list, container, false);
        
        progressBar = view.findViewById(R.id.pbTourList);
        session = new SessionManager(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerTours);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new TourAdapter(new ArrayList<>());
        adapter.setOnTourClickListener(this::navigateToDetail);
        recyclerView.setAdapter(adapter);

        loadTours();
        
        return view;
    }

    private void navigateToDetail(Tour tour) {
        Intent intent = new Intent(requireContext(), DetailActivity.class);
        intent.putExtra("hotel_object", tour); // DetailActivity handles both now
        startActivity(intent);
    }

    private void loadTours() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String userId = session.getUserId();
        String token = session.getAccessToken();

        TourRepository.getInstance().loadTours(requireContext(), userId, token, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (data != null) {
                        adapter.setData(data);
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