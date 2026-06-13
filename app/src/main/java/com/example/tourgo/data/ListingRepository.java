package com.example.tourgo.data;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Availability;
import com.example.tourgo.models.Tour;
import com.example.tourgo.remote.AvailabilityService;
import com.example.tourgo.remote.TourService;
import java.util.ArrayList;
import java.util.List;

public class ListingRepository {
    private static ListingRepository instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ListingRepository() {}

    public static ListingRepository getInstance() {
        if (instance == null) instance = new ListingRepository();
        return instance;
    }

    public void createTourWithAvailability(
            Context context,
            Tour tour,
            List<Uri> imageUris,
            List<String> blockedDates,
            String accessToken,
            DataCallback<Void> callback
    ) {
        TourService.createTour(tour, accessToken, new DataCallback<String>() {
            @Override
            public void onSuccess(String tourId) {
                if (imageUris == null || imageUris.isEmpty()) {
                    saveBlockedDates(tourId, "TOUR", blockedDates, accessToken, callback);
                    return;
                }

                uploadImagesSequentially(context, tourId, imageUris, 0, accessToken, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        saveBlockedDates(tourId, "TOUR", blockedDates, accessToken, callback);
                    }

                    @Override
                    public void onError(ApiErrorCode code, String msg) {
                        mainHandler.post(() -> callback.onError(code, msg));
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                mainHandler.post(() -> callback.onError(code, msg));
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

    public void saveBlockedDates(String listingId, String type, List<String> dates, String accessToken, DataCallback<Void> callback) {
        if (dates == null || dates.isEmpty()) {
            mainHandler.post(() -> callback.onSuccess(null));
            return;
        }

        List<Availability> availabilities = new ArrayList<>();
        for (String date : dates) {
            availabilities.add(new Availability(listingId, type, date, true));
        }

        AvailabilityService.insertAvailabilities(availabilities, accessToken, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                mainHandler.post(() -> callback.onSuccess(null));
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                mainHandler.post(() -> callback.onError(code, msg));
            }
        });
    }
}
