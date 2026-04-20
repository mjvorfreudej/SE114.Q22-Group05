package com.example.tourgo.remote;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Booking;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookingService {


    public static void createBooking(Booking booking, String accessToken, DataCallback<Booking> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/bookings";

        RequestBody body = RequestBody.create(
                booking.toJson().toString(),
                SupabaseConfig.JSON
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();

        SupabaseConfig.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(resBody);
                        if (array.length() > 0) {
                            Booking created = Booking.fromJson(array.getJSONObject(0));
                            callback.onSuccess(created);
                        } else {
                            callback.onError("Không thể tạo booking");
                        }
                    } catch (Exception e) {
                        callback.onError("Lỗi parse dữ liệu: " + e.getMessage());
                    }
                } else {
                    callback.onError("Lỗi server: " + resBody);
                }
            }
        });
    }


    public static void getMyBookings(String userId, String accessToken, DataCallback<List<Booking>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/bookings"
                + "?user_id=eq." + userId
                + "&order=created_at.desc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        SupabaseConfig.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        List<Booking> bookings = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            bookings.add(Booking.fromJson(array.getJSONObject(i)));
                        }
                        callback.onSuccess(bookings);
                    } catch (Exception e) {
                        callback.onError("Lỗi parse dữ liệu: " + e.getMessage());
                    }
                } else {
                    callback.onError("Lỗi server: " + body);
                }
            }
        });
    }


    public static void cancelBooking(String bookingId, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/bookings"
                + "?id=eq." + bookingId;

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("status", "CANCELLED");

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    SupabaseConfig.JSON
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .patch(body)
                    .build();

            SupabaseConfig.client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Lỗi kết nối: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        String resBody = response.body() != null ? response.body().string() : "";
                        callback.onError("Lỗi server: " + resBody);
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Lỗi: " + e.getMessage());
        }
    }
}
