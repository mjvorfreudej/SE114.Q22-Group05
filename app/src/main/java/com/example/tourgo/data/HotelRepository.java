package com.example.tourgo.data;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Favorite;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.remote.FavoriteService;
import com.example.tourgo.remote.HotelService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// Cache Hotels từ supabase dùng mọi nơi không gọi lại
public class HotelRepository {
    private static HotelRepository instance;
    private List<Hotel> cachedHotels;

    private HotelRepository() {}

    public static HotelRepository getInstance() {
        if (instance == null) instance = new HotelRepository();
        return instance;
    }

    public List<Hotel> getCachedHotels() {
        return cachedHotels;
    }

    public void loadHotels(DataCallback<List<Hotel>> callback) {
        if (cachedHotels != null) {
            runOnMain(() -> callback.onSuccess(cachedHotels));
            return;
        }

        HotelService.getHotels(new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                cachedHotels = data;
                runOnMain(() -> callback.onSuccess(data));
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnMain(() -> callback.onError(code, msg));
            }
        });
    }

    public void syncFavorites(String userId, String token) {
        if (userId == null || token == null || cachedHotels == null) return;

        FavoriteService.getMyFavorites(userId, token, new DataCallback<List<Favorite>>() {
            @Override
            public void onSuccess(List<Favorite> favorites) {
                Set<String> favIds = new HashSet<>();
                for (Favorite f : favorites) {
                    if (f.getHotelId() != null) favIds.add(f.getHotelId());
                }

                for (Hotel h : cachedHotels) {
                    h.setFavorite(favIds.contains(h.getId()));
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {}
        });
    }

    private void runOnMain(Runnable r) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }

    public void clearCache() {
        cachedHotels = null;
    }
}
