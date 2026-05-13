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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tourgo.R;
import com.example.tourgo.adapters.PopularHotelAdapter;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.databinding.ActivitySearchBinding;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private ActivitySearchBinding binding;
    private PopularHotelAdapter recentViewedAdapter;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivitySearchBinding.inflate(inflater, container, false);
        session = new SessionManager(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyTopInset(view);

        binding.btnBackSearch.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onBackPressed();
            }
        });
        
        setupRecentSearches();
        setupRecentViewed();
        setupSearchLogic();
        loadRecentHotels();
    }

    private void applyTopInset(View root) {
        final int basePaddingTop = root.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setupSearchLogic() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                if (query.length() > 0) {
                    // Logic tìm kiếm thực tế có thể thêm ở đây
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecentSearches() {
        binding.llRecentSearches.removeAllViews();
        addRecentSearchItem("Phuket", "Dominic Hotel, Luxury Royale Hotel...");
        addRecentSearchItem("Pattaya City", "Hilton Bandung, Namin Hotel...");
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
        recentViewedAdapter = new PopularHotelAdapter(new ArrayList<>());
        binding.rvRecentViewed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecentViewed.setAdapter(recentViewedAdapter);
    }

    private void loadRecentHotels() {
        String userId = session.getUserId();
        String token = session.getAccessToken();

        // Sử dụng dữ liệu thật từ Repository thay vì AppFakeData
        HotelRepository.getInstance().loadHotels(userId, token, new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (binding == null) return;
                getActivity().runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        // Lấy 5 khách sạn đầu tiên làm "Vừa xem"
                        recentViewedAdapter.setData(data.size() > 5 ? data.subList(0, 5) : data);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recentViewedAdapter != null) recentViewedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
