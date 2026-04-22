package com.example.tourgo.remote;

import com.example.tourgo.BuildConfig;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.AuthCallback;
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

    public static void register(String email, String password, String name, AuthCallback callback) {
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
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    public static void login(String email, String password, AuthCallback callback) {
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
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
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

    private static void handleError(String resBody, AuthCallback callback) {
        if (resBody == null || resBody.isEmpty()) {
            callback.onError(ApiErrorCode.UNKNOWN, "Có lỗi xảy ra, vui lòng thử lại");
            return;
        }
        String lowerRes = resBody.toLowerCase();
        if (lowerRes.contains("unable to resolve host") || lowerRes.contains("failed to connect") || lowerRes.contains("timeout")) {
            callback.onError(ApiErrorCode.NETWORK, "Lỗi kết nối mạng, vui lòng thử lại");
            return;
        }
        try {
            JSONObject errorJson = new JSONObject(resBody);
            String msg = errorJson.optString("msg", errorJson.optString("message", errorJson.optString("error_description", "")));
            if (msg.isEmpty()) msg = resBody;
            String lowerMsg = msg.toLowerCase();

            if (lowerMsg.contains("already registered") || lowerMsg.contains("already exists")) {
                callback.onError(ApiErrorCode.EMAIL_ALREADY_REGISTERED, "Email này đã được đăng ký");
            } else if (lowerMsg.contains("invalid login credentials")) {
                callback.onError(ApiErrorCode.INVALID_CREDENTIALS, "Email hoặc mật khẩu không chính xác");
            } else if (lowerMsg.contains("at least 6 characters")) {
                callback.onError(ApiErrorCode.PASSWORD_TOO_SHORT, "Mật khẩu phải có ít nhất 6 ký tự");
            } else if (lowerMsg.contains("user not found")) {
                callback.onError(ApiErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại");
            } else if (lowerMsg.contains("rate limit")) {
                callback.onError(ApiErrorCode.RATE_LIMIT, "Gửi quá nhanh, vui lòng thử lại sau vài phút");
            } else {
                callback.onError(ApiErrorCode.UNKNOWN, msg);
            }
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, resBody);
        }
    }

    public static void resetPassword(String email, AuthCallback callback) {
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
                    else {
                        String resBody = response.body() != null ? response.body().string() : "Error";
                        handleError(resBody, callback);
                    }
                }
            });
        } catch (Exception e) { callback.onError(ApiErrorCode.UNKNOWN, e.getMessage()); }
    }

    public static void updatePassword(String accessToken, String newPassword, AuthCallback callback) {
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
                    else {
                        String resBody = response.body() != null ? response.body().string() : "Error";
                        handleError(resBody, callback);
                    }
                }
            });
        } catch (Exception e) { callback.onError(ApiErrorCode.UNKNOWN, e.getMessage()); }
    }
}
