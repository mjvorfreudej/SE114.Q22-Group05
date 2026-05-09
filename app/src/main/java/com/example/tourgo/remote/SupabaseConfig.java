package com.example.tourgo.remote;

import androidx.annotation.Nullable;

import com.example.tourgo.BuildConfig;
import com.example.tourgo.interfaces.ApiCallback;
import com.example.tourgo.interfaces.ApiErrorCode;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;

public class SupabaseConfig {
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Pgrst303Interceptor())
            .authenticator(new TokenAuthenticator())
            .build();

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

    /**
     * Sniffs successful-looking responses for PostgREST's PGRST303 (JWT expired) marker
     * and rewrites them to 401 so the {@link TokenAuthenticator} can take over.
     * PostgREST normally pairs PGRST303 with 401 already, but this acts as a safety net.
     */
    private static class Pgrst303Interceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (response.code() == 401) return response;
            ResponseBody body = response.body();
            if (body == null) return response;
            String contentType = body.contentType() != null ? body.contentType().toString() : "";
            if (!contentType.contains("json")) return response;

            String text = body.string();
            ResponseBody rebuilt = ResponseBody.create(text,
                    body.contentType());

            if (text.contains("PGRST303")) {
                return response.newBuilder()
                        .code(401)
                        .body(rebuilt)
                        .build();
            }
            return response.newBuilder().body(rebuilt).build();
        }
    }

    /**
     * Invoked by OkHttp on any 401. Tries to mint a new access token via
     * {@link SupabaseClient#refreshToken(String)}, then re-sends the original request
     * with the rotated Authorization header. Auth-domain calls and refresh-loops are skipped.
     */
    private static class TokenAuthenticator implements Authenticator {
        @Nullable
        @Override
        public Request authenticate(@Nullable Route route, Response response) {
            String url = response.request().url().toString();
            if (url.contains("/auth/v1/")) return null;

            if (responseRetryCount(response) >= 1) {
                SupabaseClient.forceLogout();
                return null;
            }

            String oldAuth = response.request().header("Authorization");
            if (oldAuth == null || !oldAuth.startsWith("Bearer ")) return null;
            String oldToken = oldAuth.substring("Bearer ".length());

            String newToken = SupabaseClient.refreshToken(oldToken);
            if (newToken == null || newToken.isEmpty()) {
                SupabaseClient.forceLogout();
                return null;
            }

            return response.request().newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer " + newToken)
                    .build();
        }

        private int responseRetryCount(Response response) {
            int count = 0;
            Response prior = response.priorResponse();
            while (prior != null) {
                count++;
                prior = prior.priorResponse();
            }
            return count;
        }
    }
}
