package com.example.tourgo.remote;

import com.example.tourgo.BuildConfig;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.ApiCallback;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class SupabaseConfig {
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    public static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void executeRequest(Request request, ApiCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    callback.onSuccess(resBody);
                } else {
                    callback.onError(resBody);
                }
            }
        });
    }

    public static void handleHttpError(int httpCode, String resBody, ApiCallback cb) {
        if (httpCode == 401) {
            cb.onError(ApiErrorCode.UNAUTHORIZED, resBody);
            return;
        }
        if (httpCode == 403) {
            cb.onError(ApiErrorCode.FORBIDDEN, resBody);
            return;
        }
        if (httpCode == 404) {
            cb.onError(ApiErrorCode.NOT_FOUND, resBody);
            return;
        }
        if (httpCode == 429) {
            cb.onError(ApiErrorCode.RATE_LIMIT, resBody);
            return;
        }
        if (httpCode >= 500) {
            cb.onError(ApiErrorCode.SERVER_ERROR, resBody);
            return;
        }
        cb.onError(ApiErrorCode.UNKNOWN, resBody);
    }

    public static Request buildGet(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build();
    }

    public static ApiErrorCode mapHttp(int code) {
        if (code == 401) return ApiErrorCode.UNAUTHORIZED;
        if (code == 403) return ApiErrorCode.FORBIDDEN;
        if (code == 404) return ApiErrorCode.NOT_FOUND;
        if (code == 429) return ApiErrorCode.RATE_LIMIT;
        if (code >= 500) return ApiErrorCode.SERVER_ERROR;
        return ApiErrorCode.UNKNOWN;
    }
}
