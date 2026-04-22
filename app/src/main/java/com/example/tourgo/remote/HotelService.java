package com.example.tourgo.remote;

import static com.example.tourgo.remote.SupabaseConfig.mapHttp;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Hotel;

import org.json.JSONArray;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class HotelService {


    public static void getHotels(DataCallback<List<Hotel>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/hotels"
                + "?select=*,hotel_images(*)"
                + "&order=rating.desc"
                + "&hotel_images.order=display_order.asc";

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
                        List<Hotel> hotels = Hotel.fromJsonArray(array);
                        callback.onSuccess(hotels);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(mapHttp(response.code()), body);
                }
            }
        });
    }


    public static void getHotelDetail(String hotelId, DataCallback<Hotel> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/hotels"
                + "?id=eq." + hotelId
                + "&select=*,hotel_images(*)"
                + "&hotel_images.order=display_order.asc";

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
                            Hotel hotel = Hotel.fromJson(array.getJSONObject(0));
                            callback.onSuccess(hotel);
                        } else {
                            callback.onError(ApiErrorCode.NOT_FOUND, body);
                        }
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN,e.getMessage());
                    }
                } else {
                    callback.onError(mapHttp(response.code()), body);
                }
            }
        });
    }


    public static void searchHotels(String keyword, DataCallback<List<Hotel>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/hotels"
                + "?name=ilike.*" + keyword + "*"
                + "&select=*,hotel_images(*)"
                + "&order=rating.desc"
                + "&hotel_images.order=display_order.asc";

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
                        List<Hotel> hotels = Hotel.fromJsonArray(array);
                        callback.onSuccess(hotels);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(mapHttp(response.code()), body);
                }
            }
        });
    }
}
