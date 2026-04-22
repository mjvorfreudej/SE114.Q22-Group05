package com.example.tourgo.remote;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Tour;

import org.json.JSONArray;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class TourService {


    public static void getTours(DataCallback<List<Tour>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/tours"
                + "?status=eq.APPROVED"
                + "&select=*,tour_images(*)"
                + "&order=created_at.desc"
                + "&tour_images.order=display_order.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        SupabaseConfig.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(ApiErrorCode.NETWORK, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        List<Tour> tours = Tour.fromJsonArray(array);
                        callback.onSuccess(tours);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(ApiErrorCode.SERVER_ERROR, body);
                }
            }
        });
    }


    public static void getToursByRegion(String region, DataCallback<List<Tour>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/tours"
                + "?status=eq.APPROVED"
                + "&region=eq." + region
                + "&select=*,tour_images(*)"
                + "&order=rating.desc"
                + "&tour_images.order=display_order.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        SupabaseConfig.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(ApiErrorCode.NETWORK, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        List<Tour> tours = Tour.fromJsonArray(array);
                        callback.onSuccess(tours);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(ApiErrorCode.SERVER_ERROR, body);
                }
            }
        });
    }


    public static void getTourDetail(String tourId, DataCallback<Tour> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/tours"
                + "?id=eq." + tourId
                + "&select=*,tour_images(*)"
                + "&tour_images.order=display_order.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        SupabaseConfig.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(ApiErrorCode.NETWORK, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        if (array.length() > 0) {
                            Tour tour = Tour.fromJson(array.getJSONObject(0));
                            callback.onSuccess(tour);
                        } else {
                            callback.onError(ApiErrorCode.NOT_FOUND, body);
                        }
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(ApiErrorCode.SERVER_ERROR, body);
                }
            }
        });
    }


    public static void searchTours(String keyword, DataCallback<List<Tour>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/tours"
                + "?status=eq.APPROVED"
                + "&name=ilike.*" + keyword + "*"
                + "&select=*,tour_images(*)"
                + "&order=rating.desc"
                + "&tour_images.order=display_order.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        SupabaseConfig.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(ApiErrorCode.NETWORK, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        List<Tour> tours = Tour.fromJsonArray(array);
                        callback.onSuccess(tours);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(ApiErrorCode.SERVER_ERROR, body);
                }
            }
        });
    }
}
