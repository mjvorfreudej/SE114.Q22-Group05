package com.example.tourgo.remote;

import com.example.tourgo.BuildConfig;
import com.example.tourgo.interfaces.ApiCallback;
import com.example.tourgo.interfaces.ApiErrorCode;

import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void register(String email, String password, String name, ApiCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/signup";
        try {
            JSONObject metadata = new JSONObject();
            metadata.put("name", name);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("data", metadata);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = buildAuthRequest(url, body);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleError(e.getMessage(), callback);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        callback.onSuccess(resBody);
                    } else {
                        handleError(resBody, callback);
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public static void login(String email, String password, ApiCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/token?grant_type=password";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = buildAuthRequest(url, body);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleError(e.getMessage(), callback);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        callback.onSuccess(resBody);
                    } else {
                        handleError(resBody, callback);
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private static Request buildAuthRequest(String url, RequestBody body) {
        return new Request.Builder()
                .url(url)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
    }

    private static void handleError(String resBody, ApiCallback callback) {
        if (resBody == null || resBody.isEmpty()) {
            callback.onError(ApiErrorCode.UNKNOWN, "");
            return;
        }
        String lower = resBody.toLowerCase();
        if (lower.contains("unable to resolve host")
                || lower.contains("failed to connect")
                || lower.contains("timeout")) {
            callback.onError(ApiErrorCode.NETWORK, resBody);
            return;
        }
        try {
            JSONObject json = new JSONObject(resBody);
            String msg = json.optString("msg",
                    json.optString("message", json.optString("error_description", "")));
            if (msg.isEmpty()) msg = resBody;
            String m = msg.toLowerCase();

            if (m.contains("already registered") || m.contains("already exists")) {
                callback.onError(ApiErrorCode.EMAIL_ALREADY_REGISTERED, msg);
            } else if (m.contains("invalid login credentials")) {
                callback.onError(ApiErrorCode.INVALID_CREDENTIALS, msg);
            } else if (m.contains("at least 6 characters")) {
                callback.onError(ApiErrorCode.PASSWORD_TOO_SHORT, msg);
            } else if (m.contains("user not found")) {
                callback.onError(ApiErrorCode.USER_NOT_FOUND, msg);
            } else if (m.contains("rate limit")) {
                callback.onError(ApiErrorCode.RATE_LIMIT, msg);
            } else if (m.contains("invalid token") || m.contains("expired")) {
                callback.onError(ApiErrorCode.INVALID_TOKEN, msg);
            } else if (m.contains("new password should be different")
                    || m.contains("same as the old")) {
                callback.onError(ApiErrorCode.PASSWORD_SAME_AS_OLD, msg);
            } else {
                callback.onError(ApiErrorCode.UNKNOWN, msg);
            }
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, resBody);
        }
    }

    public static void resetPassword(String email, ApiCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/recover";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("redirect_to", "tourgo://reset");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = buildAuthRequest(url, body);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { handleError(e.getMessage(), callback); }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) callback.onSuccess("OK");
                    else handleError(response.body().string(), callback);
                }
            });
        } catch (Exception e) { callback.onError(e.getMessage()); }
    }

    public static void updatePassword(String accessToken, String newPassword, ApiCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/user";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("password", newPassword);
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder().url(url).addHeader("apikey", ANON_KEY).addHeader("Authorization", "Bearer " + accessToken).put(body).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { handleError(e.getMessage(), callback); }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) callback.onSuccess("OK");
                    else handleError(response.body().string(), callback);
                }
            });
        } catch (Exception e) { callback.onError(e.getMessage()); }
    }
}
