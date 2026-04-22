package com.example.tourgo.data;

import com.example.tourgo.R;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.models.HotelItem;
import com.example.tourgo.models.Tour;
import com.example.tourgo.models.User;

import java.util.ArrayList;
import java.util.List;

public class AppFakeData {

    public static List<Tour> getTours() {
        List<Tour> list = new ArrayList<>();
        list.add(new Tour(R.drawable.hotel_1, "Bali Sunset Escape", "Bali, Indonesia", "$199", 4.8, "3 Days 2 Nights"));
        list.add(new Tour(R.drawable.hotel_2, "Phuket Luxury Tour", "Phuket, Thailand", "$249", 4.7, "4 Days 3 Nights"));
        list.add(new Tour(R.drawable.hotel_3, "Da Nang Beach Tour", "Da Nang, Vietnam", "$129", 4.6, "2 Days 1 Night"));
        list.add(new Tour(R.drawable.hotel_4, "Singapore City Lights", "Singapore", "$289", 4.9, "3 Days 2 Nights"));
        list.add(new Tour(R.drawable.hotel_1, "Tokyo Sakura Trip", "Tokyo, Japan", "$399", 4.9, "5 Days 4 Nights"));
        list.add(new Tour(R.drawable.hotel_2, "Seoul Winter Holiday", "Seoul, Korea", "$320", 4.7, "4 Days 3 Nights"));
        return list;
    }

    public static List<Hotel> getHotels() {
        List<Hotel> list = new ArrayList<>();
        list.add(new Hotel(R.drawable.hotel_1, "Grand Orchid Resort", "Bali, Indonesia", "$199 / night", 4.5, "WiFi • Pool • Spa"));
        list.add(new Hotel(R.drawable.hotel_2, "Prestige Palm Hotel", "Dubai, UAE", "$235 / night", 4.6, "WiFi • Pool • Breakfast"));
        list.add(new Hotel(R.drawable.hotel_3, "Sea Breeze Suites", "Phuket, Thailand", "$145 / night", 4.7, "WiFi • Beach • Bar"));
        list.add(new Hotel(R.drawable.hotel_4, "Urban Bay Inn", "Singapore", "$180 / night", 4.5, "WiFi • Gym • Restaurant"));
        list.add(new Hotel(R.drawable.hotel_1, "Ocean Pearl Resort", "Da Nang, Vietnam", "$120 / night", 4.4, "WiFi • Pool • Sea View"));
        list.add(new Hotel(R.drawable.hotel_3, "Sunset Coast Hotel", "Nha Trang, Vietnam", "$98 / night", 4.3, "WiFi • Breakfast • Parking"));
        list.add(new Hotel(R.drawable.hotel_2, "Mountain View Retreat", "Da Lat, Vietnam", "$110 / night", 4.6, "WiFi • Mountain View • Cafe"));
        return list;
    }

    public static User getUser() {
        return new User(
                "Harry Bender",
                "harrybender@gmail.com",
                "+84 912 345 678",
                "Harry Bender",
                null,
                "USER"
        );
    }

    public static List<HotelItem> getPopularHotelItems() {
        List<HotelItem> list = new ArrayList<>();
        list.add(new HotelItem(R.drawable.hotel_1, "Grand Orchid Resort", "$199", 4.8, true, "Bangkok, Thailand", "Nestled in the heart of the city, The Grand Orchid offers a luxurious retreat with stunning views and unparalleled service. Our rooms are elegantly designed, providing a serene escape from the bustling city life."));
        list.add(new HotelItem(R.drawable.hotel_2, "Prestige Palm Hotel", "$235", 4.6, false, "Dubai, UAE", "Experience world-class luxury at the heart of Dubai. Featuring an infinity pool and premium dining options."));
        list.add(new HotelItem(R.drawable.hotel_3, "Sea Breeze Suites", "$145", 4.7, true, "Phuket, Thailand", "Enjoy the ultimate beach getaway with private access to crystal clear waters and white sandy beaches."));
        return list;
    }

    public static List<HotelItem> getTrendingHotelItems() {
        List<HotelItem> list = new ArrayList<>();
        list.add(new HotelItem(R.drawable.hotel_4, "Urban Bay Inn", "$180", 4.5, false, "Singapore", "Modern living in the city center. Perfect for business travelers and city explorers."));
        list.add(new HotelItem(R.drawable.hotel_1, "Ocean Pearl Resort", "$120", 4.4, true, "Da Nang, Vietnam", "Beautiful ocean views and authentic Vietnamese hospitality."));
        list.add(new HotelItem(R.drawable.hotel_3, "Sunset Coast Hotel", "$98", 4.3, false, "Nha Trang, Vietnam", "Affordable comfort just steps away from the vibrant nightlife of Nha Trang."));
        return list;
    }
}
