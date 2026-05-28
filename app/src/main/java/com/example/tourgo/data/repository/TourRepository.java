package com.example.tourgo.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Favorite;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.TourService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TourRepository {
    private static TourRepository instance;
    private List<Tour> cachedTours;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TourRepository() {}

    public static TourRepository getInstance() {
        if (instance == null) instance = new TourRepository();
        return instance;
    }

    public void loadTours(Context context, String userId, String token, DataCallback<List<Tour>> callback) {
        if (cachedTours != null) {
            callback.onSuccess(cachedTours);
            return;
        }

        TourService.getTours(context, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> tours) {
                cachedTours = tours;
                if (userId != null && token != null) {
                    syncFavorites(userId, token, new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            mainHandler.post(() -> callback.onSuccess(cachedTours));
                        }

                        @Override
                        public void onError(ApiErrorCode code, String msg) {
                            mainHandler.post(() -> callback.onSuccess(cachedTours));
                        }
                    });
                } else {
                    mainHandler.post(() -> callback.onSuccess(tours));
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
        if (userId == null || token == null || cachedTours == null) {
            if (callback != null) callback.onSuccess(null);
            return;
        }

        FavoriteRepository.getInstance().loadFavorites(null, false, new DataCallback<List<Favorite>>() {
            @Override
            public void onSuccess(List<Favorite> favorites) {
                Set<String> favTourIds = new HashSet<>();
                for (Favorite f : favorites) {
                    if (f.getTourId() != null) favTourIds.add(f.getTourId());
                }

                for (Tour t : cachedTours) {
                    t.setFavorite(favTourIds.contains(t.getId()));
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
        cachedTours = null;
    }
}
