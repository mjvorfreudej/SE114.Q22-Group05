package com.example.tourgo.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Favorite;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.FavoriteService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteRepository {
    private static FavoriteRepository instance;
    private List<Favorite> cachedFavorites;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private FavoriteRepository() {}

    public static FavoriteRepository getInstance() {
        if (instance == null) {
            instance = new FavoriteRepository();
        }
        return instance;
    }

    public void loadFavorites(Context context, boolean forceRefresh, DataCallback<List<Favorite>> callback) {
        if (!forceRefresh && cachedFavorites != null) {
            mainHandler.post(() -> callback.onSuccess(cachedFavorites));
            return;
        }

        FavoriteService.getMyFavorites(context, new DataCallback<List<Favorite>>() {
            @Override
            public void onSuccess(List<Favorite> data) {
                cachedFavorites = data;
                mainHandler.post(() -> callback.onSuccess(data));
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                mainHandler.post(() -> callback.onError(code, msg));
            }
        });
    }

    public void addFavorite(Context context, Favorite favorite, DataCallback<Favorite> callback) {
        FavoriteService.addFavorite(context, favorite, new DataCallback<Favorite>() {
            @Override
            public void onSuccess(Favorite data) {
                if (cachedFavorites == null) {
                    cachedFavorites = new ArrayList<>();
                }
                cachedFavorites.add(data);

                // Đồng bộ trạng thái favorite vào cache của Tour/Hotel Repository
                if (data.getTourId() != null) {
                    Tour t = TourRepository.getInstance().findTourById(data.getTourId());
                    if (t != null) t.setFavorite(true);
                }
                if (data.getHotelId() != null) {
                    Hotel h = HotelRepository.getInstance().findHotelById(data.getHotelId());
                    if (h != null) h.setFavorite(true);
                }

                mainHandler.post(() -> callback.onSuccess(data));
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                mainHandler.post(() -> callback.onError(code, message));
            }
        });
    }

    public void removeFavorite(Context context, String favoriteId, DataCallback<Void> callback) {
        FavoriteService.removeFavorite(context, favoriteId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (cachedFavorites != null) {
                    Favorite removedItem = null;
                    for (Favorite f : cachedFavorites) {
                        if (favoriteId.equals(f.getId())) {
                            removedItem = f;
                            break;
                        }
                    }

                    if (removedItem != null) {
                        // Cập nhật trạng thái trong các Repository khác
                        if (removedItem.getTourId() != null) {
                            Tour t = TourRepository.getInstance().findTourById(removedItem.getTourId());
                            if (t != null) t.setFavorite(false);
                        }
                        if (removedItem.getHotelId() != null) {
                            Hotel h = HotelRepository.getInstance().findHotelById(removedItem.getHotelId());
                            if (h != null) h.setFavorite(false);
                        }
                        cachedFavorites.remove(removedItem);
                    }
                }

                mainHandler.post(() -> callback.onSuccess(null));
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                mainHandler.post(() -> callback.onError(code, message));
            }
        });
    }

    public Set<String> getFavoriteHotelIds() {
        Set<String> hotelIds = new HashSet<>();
        if (cachedFavorites != null) {
            for (Favorite f : cachedFavorites) {
                if (f.getHotelId() != null) {
                    hotelIds.add(f.getHotelId());
                }
            }
        }
        return hotelIds;
    }

    public Set<String> getFavoriteTourIds() {
        Set<String> tourIds = new HashSet<>();
        if (cachedFavorites != null) {
            for (Favorite f : cachedFavorites) {
                if (f.getTourId() != null) {
                    tourIds.add(f.getTourId());
                }
            }
        }
        return tourIds;
    }

    public String findFavoriteIdByHotelId(String hotelId) {
        if (cachedFavorites != null) {
            for (Favorite f : cachedFavorites) {
                if (hotelId.equals(f.getHotelId())) {
                    return f.getId();
                }
            }
        }
        return null;
    }

    public String findFavoriteIdByTourId(String tourId) {
        if (cachedFavorites != null) {
            for (Favorite f : cachedFavorites) {
                if (tourId.equals(f.getTourId())) {
                    return f.getId();
                }
            }
        }
        return null;
    }

    public List<Favorite> getCachedFavorites() {
        return cachedFavorites;
    }

    public void clearCache() {
        cachedFavorites = null;
    }
}
