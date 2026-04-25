package com.example.tourgo.data;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;
import com.example.tourgo.remote.HotelService;

import java.util.List;

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

    private void runOnMain(Runnable r) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }

    public void clearCache() {
        cachedHotels = null;
    }
}
