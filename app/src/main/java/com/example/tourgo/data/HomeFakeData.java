package com.example.tourgo.data;

import com.example.tourgo.R;
import com.example.tourgo.models.HotelItem;

import java.util.ArrayList;
import java.util.List;

public class HomeFakeData {

    public static List<HotelItem> getPopularHotels() {
        List<HotelItem> list = new ArrayList<>();
        list.add(new HotelItem(R.drawable.hotel_1, "The Grand Orchid Resort", "From $199 / nights", 4.5, false));
        list.add(new HotelItem(R.drawable.hotel_2, "The Prestige Hotel", "From $35 / nights", 4.6, false));
        list.add(new HotelItem(R.drawable.hotel_3, "Sea Breeze Suites", "From $88 / nights", 4.7, true));
        return list;
    }

    public static List<HotelItem> getTrendingHotels() {
        List<HotelItem> list = new ArrayList<>();
        list.add(new HotelItem(R.drawable.hotel_3, "Lagoon Hotel", "From $95 / nights", 4.5, false));
        list.add(new HotelItem(R.drawable.hotel_4, "Urban Bay Inn", "From $110 / nights", 4.5, true));
        list.add(new HotelItem(R.drawable.hotel_1, "Palm Resort", "From $180 / nights", 4.8, false));
        return list;
    }
}