package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.tourgo.R;
import com.example.tourgo.data.HotelRepository;
import com.example.tourgo.fragments.HotelListFragment;
import com.example.tourgo.fragments.ProfileFragment;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    View currentTab;

    LinearLayout navHome, navTours, navHotels, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        navHome = findViewById(R.id.navHome);
        navTours = findViewById(R.id.navTours);
        navHotels = findViewById(R.id.navHotels);
        navProfile = findViewById(R.id.navProfile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutNavMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigation();
        
        if (savedInstanceState == null) {
            updateTabUI(navHome);
            loadFragment(new HomeFragment());
        }

        HotelRepository.getInstance().loadHotels(new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                // data đã được cache, các màn khác lấy được ngay
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, getString(R.string.err_network), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            if (currentTab == navHome) return;
            updateTabUI(navHome);
            loadFragment(new HomeFragment());
        });

        navTours.setOnClickListener(v -> {
            if (currentTab == navTours) return;
            updateTabUI(navTours);
            loadFragment(new SearchFragment());
        });

        navHotels.setOnClickListener(v -> {
            if (currentTab == navHotels) return;
            updateTabUI(navHotels);
            loadFragment(new FavoriteFragment());
        });

        navProfile.setOnClickListener(v -> {
            if (currentTab == navProfile) return;
            updateTabUI(navProfile);
            loadFragment(new ProfileFragment());
        });
    }

    public void switchToSearch() {
        updateTabUI(navTours);
        loadFragment(new SearchFragment());
    }

    public void switchToHotelList(String destination, String date, String guest) {
        HotelListFragment fragment = new HotelListFragment();
        Bundle args = new Bundle();
        args.putString("destination", destination);
        args.putString("date", date);
        args.putString("guest", guest);
        fragment.setArguments(args);
        loadFragment(fragment);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void updateTabUI(View newTab) {
        if (currentTab != null) {
            resetTabState(currentTab);
        }

        newTab.setBackgroundResource(R.drawable.bg_nav_selected);
        
        if (newTab instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) newTab;
            
            // Icon
            ImageView icon = (ImageView) layout.getChildAt(0);
            icon.setColorFilter(ContextCompat.getColor(this, R.color.nav_selected_text));
            
            // Text
            TextView tv = (TextView) layout.getChildAt(1);
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(ContextCompat.getColor(this, R.color.nav_selected_text));

            // Weight adjustment
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.weight = 1.5f; // Active tab is wider
            layout.setLayoutParams(params);
        }
        
        currentTab = newTab;
    }

    private void resetTabState(View tab) {
        tab.setBackground(null);
        if (tab instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) tab;
            
            // Icon
            ImageView icon = (ImageView) layout.getChildAt(0);
            icon.setColorFilter(ContextCompat.getColor(this, R.color.nav_unselected));
            
            // Text
            TextView tv = (TextView) layout.getChildAt(1);
            tv.setVisibility(View.GONE);

            // Weight adjustment
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.weight = 1.0f; // Inactive tabs are standard
            layout.setLayoutParams(params);
        }
    }
}
