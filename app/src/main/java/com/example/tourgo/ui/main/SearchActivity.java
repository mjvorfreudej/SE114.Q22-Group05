package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tourgo.R;
import com.example.tourgo.databinding.ActivitySearchBinding;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackSearch.setOnClickListener(v -> finish());
        
        setupRecentSearches();
    }

    private void setupRecentSearches() {
        String[] recent = {"Phuket", "Pattaya City", "Surat Thani"};
        for (String city : recent) {
            TextView tv = new TextView(this);
            tv.setText(city);
            tv.setPadding(0, 16, 0, 16);
            tv.setTextColor(getResources().getColor(R.color.black));
            binding.llRecentSearches.addView(tv);
        }
        
        binding.btnClearRecent.setOnClickListener(v -> binding.llRecentSearches.removeAllViews());
    }
}
