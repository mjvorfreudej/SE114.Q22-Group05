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
