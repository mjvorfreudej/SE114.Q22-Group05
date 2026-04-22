package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tourgo.R;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.data.AppFakeData;
import com.example.tourgo.databinding.ActivitySearchBinding;

public class SearchFragment extends Fragment {

    private ActivitySearchBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivitySearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.btnBackSearch.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onBackPressed();
            }
        });
        
        setupRecentSearches();
        setupRecentViewed();
        setupSearchLogic();
    }

    private void setupSearchLogic() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                if (query.length() > 0) {
                    // Logic to show search results
                } else {
                    // Logic to show recent searches
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecentSearches() {
        binding.llRecentSearches.removeAllViews();
        
        addRecentSearchItem("Phuket", "Dominic Hotel, Luxury Royale Hotel, Hotel Santika...");
        addRecentSearchItem("Pattaya City", "Hilton Bandung, Namin Hotel, Clove Garden Hotel...");
        addRecentSearchItem("Surat Thani", "Diamond Heart Hotel, Infinity Castle Hotel, Horizon...");
        
        binding.btnClearRecent.setOnClickListener(v -> binding.llRecentSearches.removeAllViews());
    }

    private void addRecentSearchItem(String city, String details) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_recent_search, binding.llRecentSearches, false);
        TextView tvCity = itemView.findViewById(R.id.tvRecentCity);
        TextView tvDetails = itemView.findViewById(R.id.tvRecentHotelDetails);
        
        tvCity.setText(city);
        tvDetails.setText(details);
        
        binding.llRecentSearches.addView(itemView);
    }

    private void setupRecentViewed() {
        binding.rvRecentViewed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecentViewed.setAdapter(new PopularHotelAdapter(AppFakeData.getPopularHotelItems()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
