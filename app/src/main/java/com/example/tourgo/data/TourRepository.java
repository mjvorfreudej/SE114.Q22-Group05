package com.example.tourgo.data;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Favorite;
import com.example.tourgo.models.Tour;
import com.example.tourgo.remote.FavoriteService;
import com.example.tourgo.remote.TourService;

import java.util.ArrayList;
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

    public List<Tour> getCachedTours() {
        return cachedTours;
    }

    public void loadTours(String userId, String token, DataCallback<List<Tour>> callback) {
        TourService.getTours(new DataCallback<List<Tour>>() {
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

        FavoriteService.getMyFavorites(userId, token, new DataCallback<List<Favorite>>() {
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

    public void createTourWithImages(
            Context context,
            Tour tour,
            List<Uri> imageUris,
            String accessToken,
            DataCallback<Void> callback
    ) {
        TourService.createTour(tour, accessToken, new DataCallback<String>() {
            @Override
            public void onSuccess(String tourId) {
                if (imageUris == null || imageUris.isEmpty()) {
                    clearCache();
                    mainHandler.post(() -> callback.onSuccess(null));
                    return;
                }

                uploadImagesSequentially(
                        context,
                        tourId,
                        imageUris,
                        0,
                        accessToken,
                        callback
                );
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                mainHandler.post(() -> callback.onError(code, rawMessage));
            }
        });
    }

    private void uploadImagesSequentially(
            Context context,
            String tourId,
            List<Uri> imageUris,
            int index,
            String accessToken,
            DataCallback<Void> callback
    ) {
        if (index >= imageUris.size()) {
            clearCache();
            mainHandler.post(() -> callback.onSuccess(null));
            return;
        }

        TourService.uploadTourImage(
                context,
                imageUris.get(index),
                tourId,
                index,
                accessToken,
                new DataCallback<String>() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        TourService.insertTourImage(
                                tourId,
                                imageUrl,
                                index,
                                accessToken,
                                new DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        uploadImagesSequentially(
                                                context,
                                                tourId,
                                                imageUris,
                                                index + 1,
                                                accessToken,
                                                callback
                                        );
                                    }

                                    @Override
                                    public void onError(ApiErrorCode code, String rawMessage) {
                                        mainHandler.post(() -> callback.onError(code, rawMessage));
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(ApiErrorCode code, String rawMessage) {
                        mainHandler.post(() -> callback.onError(code, rawMessage));
                    }
                }
        );
    }

    public void loadMyTours(
            String ownerId,
            String accessToken,
            DataCallback<List<Tour>> callback
    ) {
        TourService.getMyTours(ownerId, accessToken, new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> data) {
                mainHandler.post(() -> callback.onSuccess(data));
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                mainHandler.post(() -> callback.onError(code, rawMessage));
            }
        });
    }

    public void deleteTour(
            String tourId,
            String accessToken,
            DataCallback<Void> callback
    ) {
        TourService.deleteTour(tourId, accessToken, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                clearCache();
                mainHandler.post(() -> callback.onSuccess(null));
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                mainHandler.post(() -> callback.onError(code, rawMessage));
            }
        });
    }
}
