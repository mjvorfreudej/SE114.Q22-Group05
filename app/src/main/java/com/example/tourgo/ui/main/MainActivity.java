package com.example.tourgo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.tourgo.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    View currentTab;

    LinearLayout navHome, navTours, navHotels, navProfile;
    FloatingActionButton fabAdd;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);

        navHome = findViewById(R.id.navHome);
        navTours = findViewById(R.id.navTours);
        navHotels = findViewById(R.id.navHotels);
        navProfile = findViewById(R.id.navProfile);
        fabAdd = findViewById(R.id.fabAdd);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutNavMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigation();

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateTourActivity.class);
            startActivity(intent);
        });

        if (savedInstanceState == null) {
            updateTabUI(navHome);
            loadFragment(new HomeFragment(), false);
        } else {
            // Khôi phục UI của tab sau khi đổi ngôn ngữ/tái tạo activity
            int selectedId = savedInstanceState.getInt("selected_tab_id", R.id.navHome);
            View savedTab = findViewById(selectedId);
            if (savedTab != null) {
                updateTabUI(savedTab);
            }
        }

        HotelRepository.getInstance().loadHotels(new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                if (session.isLoggedIn()) {
                    HotelRepository.getInstance().syncFavorites(session.getUserId(), session.getAccessToken());
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, getString(R.string.err_network), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentTab != null) {
            outState.putInt("selected_tab_id", currentTab.getId());
        }
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            if (currentTab == navHome) return;
            updateTabUI(navHome);
            loadFragment(new HomeFragment(), true);
        });

        navTours.setOnClickListener(v -> {
            if (currentTab == navTours) return;
            updateTabUI(navTours);
            loadFragment(new SearchFragment(), true);
        });

        navHotels.setOnClickListener(v -> {
            if (currentTab == navHotels) return;
            updateTabUI(navHotels);
            loadFragment(new FavoriteFragment(), true);
        });

        navProfile.setOnClickListener(v -> {
            if (currentTab == navProfile) return;
            updateTabUI(navProfile);
            loadFragment(new ProfileFragment(), true);
        });
    }

    public void switchToHome() {
        updateTabUI(navHome);
        loadFragment(new HomeFragment(), true);
    }

    public void switchToSearch() {
        updateTabUI(navTours);
        loadFragment(new SearchFragment(), true);
    }

    public void switchToHotelList(String title, String destination, String date, String guest) {
        HotelListFragment fragment = new HotelListFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("destination", destination);
        args.putString("date", date);
        args.putString("guest", guest);
        fragment.setArguments(args);
        loadFragment(fragment, true);
    }

    private void loadFragment(Fragment fragment, boolean animate) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            );
        } else {
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (currentTab != navHome) {
            switchToHome();
        } else {
            super.onBackPressed();
        }
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
            icon.setColorFilter(ContextCompat.getColor(this, R.color.black));

            // Text
            TextView tv = (TextView) layout.getChildAt(1);
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(ContextCompat.getColor(this, R.color.black));

            // Weight adjustment
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.weight = 1.5f; 
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
            icon.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray));
            
            // Text
            TextView tv = (TextView) layout.getChildAt(1);
            tv.setVisibility(View.GONE);

            // Weight adjustment
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.weight = 1.0f;
            layout.setLayoutParams(params);
        }
    }
}
