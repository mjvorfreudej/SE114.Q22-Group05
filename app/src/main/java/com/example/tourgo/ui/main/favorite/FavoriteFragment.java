package com.example.tourgo.ui.main.favorite;
import com.example.tourgo.ui.main.home.MainActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tourgo.R;
import com.example.tourgo.ui.main.favorite.FavoriteListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FavoriteFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public FavoriteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyTopInset(view);

        tabLayout = view.findViewById(R.id.tabLayoutFavorite);
        viewPager = view.findViewById(R.id.viewPagerFavorite);

        setupViewPager();

        View btnBack = view.findViewById(R.id.btnFavoriteBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToHome();
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }
    }

    private void setupViewPager() {
        FavoritePagerAdapter adapter = new FavoritePagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.home_category_hotel);
            } else {
                tab.setText(R.string.home_category_tour);
            }
        }).attach();
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

    private static class FavoritePagerAdapter extends FragmentStateAdapter {
        public FavoritePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // 0: Hotels, 1: Tours
            return FavoriteListFragment.newInstance(position == 0 ? FavoriteListFragment.TYPE_HOTEL : FavoriteListFragment.TYPE_TOUR);
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
