package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tourgo.R;
import com.example.tourgo.adapters.SearchPagerAdapter;
import com.example.tourgo.fragments.HotelListFragment;
import com.example.tourgo.fragments.TourListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TourlistActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_list);

        initViews();
        setupViewPager();
        setupSearch();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.etSearch);
    }

    private void setupViewPager() {
        SearchPagerAdapter pagerAdapter = new SearchPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Tours" : "Hotels");
        }).attach();
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCurrentFragment(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCurrentFragment(String query) {
        // ViewPager2 uses "f" + position as the internal tag for fragments
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment instanceof TourListFragment) {
            ((TourListFragment) currentFragment).filter(query);
        } else if (currentFragment instanceof HotelListFragment) {
            ((HotelListFragment) currentFragment).filter(query);
        }
    }
}
