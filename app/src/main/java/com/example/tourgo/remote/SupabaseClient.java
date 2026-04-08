package com.example.tourgo.remote;

import com.example.tourgo.BuildConfig;
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
                    callback.onError("Lỗi mạng: " + e.getMessage());
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
        } catch (Exception e) {
            callback.onError(e.getMessage());
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
                    callback.onError("Lỗi mạng: " + e.getMessage());
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
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public static void logout(String accessToken, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/logout";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create("", JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Lỗi mạng: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess("Đăng xuất thành công");
                } else {
                    String resBody = response.body() != null ? response.body().string() : "";
                    callback.onError(resBody);
                }
            }
        });
    }

    public static void resetPassword(String email, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/recover";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("redirect_to", "https://temp-tour-go-app.vercel.app/");
            //TODO: update redirect at Site URL (URL Configuration)

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = buildAuthRequest(url, body);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Lỗi mạng: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Đã gửi email khôi phục!");
                    } else {
                        String resBody = response.body() != null ? response.body().string() : "";
                        if(response.code() == 429 || resBody.contains("rate_limit")){
                            callback.onError("Quá nhiều yêu cầu, vui lòng thử lại sau vài tiếng :) .");
                            return;
                        }else {
                            callback.onError("Có lỗi xảy ra, vui lòng thử lại.");
                        }
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public static void updatePassword(String accessToken, String newPassword, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/user";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("password", newPassword);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Lỗi mạng: " + e.getMessage());
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
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public static void getUserProfile(String accessToken, AuthCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/users?select=*";

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Lỗi mạng: " + e.getMessage());
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
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public static void updateUserProfile(String accessToken, String userId,
                                         String name, String avatar, AuthCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/users?id=eq." + userId;

        try {
            JSONObject jsonBody = new JSONObject();
            if (name != null) jsonBody.put("name", name);
            if (avatar != null) jsonBody.put("avatar", avatar);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .patch(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Lỗi mạng: " + e.getMessage());
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
}
