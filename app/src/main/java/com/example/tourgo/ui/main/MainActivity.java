package com.example.tourgo.ui.main;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.fragments.HotelListFragment;
import com.example.tourgo.fragments.ProfileFragment;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.data.local.SessionManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    View currentTab;
    int currentIndex = 0; // Theo dõi vị trí tab hiện tại

    LinearLayout navHome, navTours, navHotels, navProfile;
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

        View bottomNav = findViewById(R.id.bottomNavContainer);
        ViewGroup.MarginLayoutParams navLp = (ViewGroup.MarginLayoutParams) bottomNav.getLayoutParams();
        final int baseNavBottomMargin = navLp.bottomMargin;
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = baseNavBottomMargin + systemBars.bottom;
            v.setLayoutParams(lp);
            return insets;
        });

        setupNavigation();
        
        if (savedInstanceState == null) {
            currentIndex = 0;
            updateTabUI(navHome);
            loadFragment(new HomeFragment(), 0); // Không animate lần đầu
        } else {
            currentIndex = savedInstanceState.getInt("selected_tab_index", 0);
            int selectedId = savedInstanceState.getInt("selected_tab_id", R.id.navHome);
            View savedTab = findViewById(selectedId);
            if (savedTab != null) updateTabUI(savedTab);
        }

        HotelRepository.getInstance().loadHotels(this, session.getUserId(), session.getAccessToken(), new DataCallback<List<Hotel>>() {
            @Override public void onSuccess(List<Hotel> data) {
                if (session.isLoggedIn()) HotelRepository.getInstance().syncFavorites(MainActivity.this, session.getUserId(), session.getAccessToken());
            }
            @Override public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.err_network), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentTab != null) {
            outState.putInt("selected_tab_id", currentTab.getId());
            outState.putInt("selected_tab_index", currentIndex);
        }
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> switchTab(0, navHome, new HomeFragment()));
        navTours.setOnClickListener(v -> switchTab(1, navTours, new SearchFragment()));
        navHotels.setOnClickListener(v -> switchTab(2, navHotels, new FavoriteFragment()));
        navProfile.setOnClickListener(v -> switchTab(3, navProfile, new ProfileFragment()));
    }

    private void switchTab(int index, View tabView, Fragment fragment) {
        if (currentIndex == index) return;
        
        int direction = (index > currentIndex) ? 1 : -1; // 1: sang phải (tiến), -1: sang trái (lùi)
        currentIndex = index;
        
        updateTabUI(tabView);
        loadFragment(fragment, direction);
    }

    public void switchToSearch() { switchTab(1, navTours, new SearchFragment()); }
    public void switchToHome() { switchTab(0, navHome, new HomeFragment()); }

    public void switchToHotelList(String title, String destination, String date, String guest) {
        HotelListFragment fragment = new HotelListFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("destination", destination);
        args.putString("date", date);
        args.putString("guest", guest);
        fragment.setArguments(args);
        loadFragment(fragment, 1);
    }

    public void switchToHotelScreen() { loadFragment(new HotelScreenFragment(), 1); }
    public void switchToTourScreen() { loadFragment(new TourScreenFragment(), 1); }

    private void loadFragment(Fragment fragment, int direction) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        if (direction > 0) {
            // Tiến sang phải (tab cao hơn): trượt từ phải sang trái
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (direction < 0) {
            // Lùi sang trái (tab thấp hơn): trượt từ trái sang phải
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void updateTabUI(View newTab) {
        if (currentTab != null) resetTabState(currentTab);
        newTab.setBackgroundResource(R.drawable.bg_nav_selected);
        if (newTab instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) newTab;
            ((ImageView) layout.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.black));
            TextView tv = (TextView) layout.getChildAt(1);
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(ContextCompat.getColor(this, R.color.black));
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
            ((ImageView) layout.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.dark_gray));
            layout.getChildAt(1).setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.weight = 1.0f;
            layout.setLayoutParams(params);
        }
    }
}
