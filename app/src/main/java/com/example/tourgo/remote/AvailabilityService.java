package com.example.tourgo.remote;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Availability;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AvailabilityService {

    public static void insertAvailabilities(List<Availability> availabilities, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/availabilities";
        try {
            JSONArray array = new JSONArray();
            for (Availability a : availabilities) {
                JSONObject json = new JSONObject();
                json.put("listing_id", a.getListingId());
                json.put("listing_type", a.getListingType());
                json.put("date", a.getDate());
                json.put("is_blocked", a.isBlocked());
                array.put(json);
            }

            RequestBody body = RequestBody.create(array.toString(), SupabaseConfig.JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            SupabaseConfig.client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(ApiErrorCode.NETWORK, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(SupabaseConfig.mapHttp(response.code()), response.body().string());
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }
}
