package com.example.tourgo.data;

import android.os.Handler;
import android.os.Looper;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Favorite;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.remote.FavoriteService;
import com.example.tourgo.remote.HotelService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HotelRepository {
    private static HotelRepository instance;
    private List<Hotel> cachedHotels;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private HotelRepository() {}

    public static HotelRepository getInstance() {
        if (instance == null) instance = new HotelRepository();
        return instance;
    }

    public List<Hotel> getCachedHotels() {
        return cachedHotels;
    }

    // Hàm load cũ để không làm hỏng code hiện tại
    public void loadHotels(DataCallback<List<Hotel>> callback) {
        if (cachedHotels != null) {
            callback.onSuccess(cachedHotels);
            return;
        }
        HotelService.getHotels(new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> data) {
                cachedHotels = data;
                mainHandler.post(() -> callback.onSuccess(data));
            }
            @Override
            public void onError(ApiErrorCode code, String msg) {
                mainHandler.post(() -> callback.onError(code, msg));
            }
        });
    }

    public void loadHotels(String userId, String token, DataCallback<List<Hotel>> callback) {
        HotelService.getHotels(new DataCallback<List<Hotel>>() {
            @Override
            public void onSuccess(List<Hotel> hotels) {
                cachedHotels = hotels;
                if (userId != null && token != null) {
                    syncFavorites(userId, token, new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            mainHandler.post(() -> callback.onSuccess(cachedHotels));
                        }
                        @Override
                        public void onError(ApiErrorCode code, String msg) {
                            mainHandler.post(() -> callback.onSuccess(cachedHotels));
                        }
                    });
                } else {
                    mainHandler.post(() -> callback.onSuccess(hotels));
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                mainHandler.post(() -> callback.onError(code, msg));
            }
        });
    }

    public void syncFavorites(String userId, String token) {
        syncFavorites(userId, token, null);
    }

    public void syncFavorites(String userId, String token, DataCallback<Void> callback) {
        if (userId == null || token == null || cachedHotels == null) {
            if (callback != null) callback.onSuccess(null);
            return;
        }

        FavoriteService.getMyFavorites(userId, token, new DataCallback<List<Favorite>>() {
            @Override
            public void onSuccess(List<Favorite> favorites) {
                Set<String> favHotelIds = new HashSet<>();
                for (Favorite f : favorites) {
                    if (f.getHotelId() != null) favHotelIds.add(f.getHotelId());
                }

                for (Hotel h : cachedHotels) {
                    h.setFavorite(favHotelIds.contains(h.getId()));
                }
                if (callback != null) callback.onSuccess(null);
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (callback != null) callback.onError(code, msg);
            }
        });
    }

    public void clearCache() {
        cachedHotels = null;
    }
}
