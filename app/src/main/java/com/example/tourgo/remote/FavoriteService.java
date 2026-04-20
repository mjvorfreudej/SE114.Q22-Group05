package com.example.tourgo.remote;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Favorite;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FavoriteService {


    public static void addFavorite(Favorite favorite, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/favorites";

        RequestBody body = RequestBody.create(
                favorite.toJson().toString(),
                SupabaseConfig.JSON
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(body)
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
                    if (resBody.contains("duplicate") || resBody.contains("unique")) {
                        callback.onError("Đã có trong danh sách yêu thích");
                    } else {
                        callback.onError("Lỗi server: " + resBody);
                    }
                }
            }
        });
    }


    public static void removeFavorite(String favoriteId, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/favorites"
                + "?id=eq." + favoriteId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .delete()
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
    }


    public static void removeFavoriteTour(String userId, String tourId, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/favorites"
                + "?user_id=eq." + userId
                + "&tour_id=eq." + tourId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .delete()
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
    }


    public static void removeFavoriteHotel(String userId, String hotelId, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/favorites"
                + "?user_id=eq." + userId
                + "&hotel_id=eq." + hotelId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .delete()
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
    }


    public static void getMyFavorites(String userId, String accessToken, DataCallback<List<Favorite>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/favorites"
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
                        List<Favorite> favorites = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            favorites.add(Favorite.fromJson(array.getJSONObject(i)));
                        }
                        callback.onSuccess(favorites);
                    } catch (Exception e) {
                        callback.onError("Lỗi parse dữ liệu: " + e.getMessage());
                    }
                } else {
                    callback.onError("Lỗi server: " + body);
                }
            }
        });
    }
}
