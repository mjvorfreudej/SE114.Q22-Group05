package com.example.tourgo.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tourgo.fragments.HotelListFragment;
import com.example.tourgo.fragments.TourListFragment;

public class SearchPagerAdapter extends FragmentStateAdapter {

    public SearchPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new HotelListFragment();
        }
        return new TourListFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}