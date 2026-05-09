package com.example.tourgo.remote;

import static com.example.tourgo.remote.SupabaseConfig.mapHttp;

import android.widget.Toast;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Comment;
import com.example.tourgo.ui.main.DetailActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HotelReviewService {
    public static void getReviewsByHotelId(
            String hotelId,
            String accessToken,
            DataCallback<List<Comment>> callback
    ) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/hotel_reviews"
                + "?select=id,hotel_id,user_id,review_text,stars,created_at,users!hotel_reviews_user_id_users_fkey(name,avatar)"
                + "&hotel_id=eq." + hotelId
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
                callback.onError(ApiErrorCode.NETWORK, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";

                android.util.Log.d("ReviewLoad", "url = " + url);
                android.util.Log.d("ReviewLoad", "code = " + response.code());
                android.util.Log.d("ReviewLoad", "body = " + body);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        List<Comment> comments = Comment.fromHotelReviewJsonArray(array);
                        callback.onSuccess(comments);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(mapHttp(response.code()), body);
                }
            }
        });
    }

    public static void createHotelReview(
        String hotelId,
        String userId,
        int stars,
        String reviewText,
        String accessToken,
        DataCallback<Void> callback
    ) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/hotel_reviews";

        try {
            JSONObject json = new JSONObject();
            json.put("hotel_id", hotelId);
            json.put("user_id", userId);
            json.put("stars", stars);
            json.put("review_text", reviewText);

            RequestBody body = RequestBody.create(
                    json.toString(),
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
                    callback.onError(ApiErrorCode.NETWORK, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String bodyText = response.body() != null ? response.body().string() : "";

                    if (response.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(SupabaseConfig.mapHttp(response.code()), bodyText);
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    public static void hasReviewedHotel(
        String hotelId,
        String userId,
        String accessToken,
        DataCallback<Boolean> callback
    ) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/hotel_reviews"
                + "?select=id"
                + "&hotel_id=eq." + hotelId
                + "&user_id=eq." + userId
                + "&limit=1";

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
                callback.onError(ApiErrorCode.NETWORK, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";

                if (response.isSuccessful()) {
                    try {
                        callback.onSuccess(new JSONArray(body).length() > 0);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(SupabaseConfig.mapHttp(response.code()), body);
                }
            }
        });
    }

    public static void updateHotelReview(
        String reviewId,
        int stars,
        String reviewText,
        String accessToken,
        DataCallback<Void> callback
    ) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/hotel_reviews"
                + "?id=eq." + reviewId;

        try {
            JSONObject json = new JSONObject();
            json.put("stars", stars);
            json.put("review_text", reviewText);

            RequestBody body = RequestBody.create(json.toString(), SupabaseConfig.JSON);

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
                    callback.onError(ApiErrorCode.NETWORK, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String bodyText = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) callback.onSuccess(null);
                    else callback.onError(SupabaseConfig.mapHttp(response.code()), bodyText);
                }
            });
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    public static void deleteHotelReview(
        String reviewId,
        String accessToken,
        DataCallback<Void> callback
    ) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/hotel_reviews"
                + "?id=eq." + reviewId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .delete()
                .build();

        SupabaseConfig.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(ApiErrorCode.NETWORK, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(SupabaseConfig.mapHttp(response.code()), body);
            }
        });
    }
}
