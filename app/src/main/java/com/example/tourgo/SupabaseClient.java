package com.example.tourgo;

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

    // ================================================================
    // 1. HÀM ĐĂNG KÝ
    // Sau khi register thành công, trigger trên Supabase sẽ tự động
    // insert vào bảng users với role phù hợp (admin/user)
    // ================================================================
    public static void register(String email, String password, String name, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/signup";

        try {
            // Truyền name vào user_metadata để trigger function sử dụng
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

    // ================================================================
    // 2. HÀM ĐĂNG NHẬP
    // Trả về access_token + user info
    // ================================================================
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

    // ================================================================
    // 3. HÀM QUÊN MẬT KHẨU
    // Gửi email reset password
    // ================================================================
    public static void resetPassword(String email, AuthCallback callback) {
        String url = SUPABASE_URL + "/auth/v1/recover";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);

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
                        callback.onError("Có lỗi xảy ra, vui lòng thử lại.");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // ================================================================
    // 4. HÀM LẤY THÔNG TIN USER TỪ BẢNG USERS
    // Cần truyền accessToken (lấy từ response login)
    // ================================================================
    public static void getUserProfile(String accessToken, AuthCallback callback) {
        // Dùng PostgREST API để query bảng users, lọc theo user hiện tại
        // select=* sẽ trả về tất cả columns
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

    // ================================================================
    // 5. HÀM CẬP NHẬT THÔNG TIN USER (name, avatar)
    // Cần truyền accessToken và userId
    // ================================================================
    public static void updateUserProfile(String accessToken, String userId,
                                         String name, String avatar, AuthCallback callback) {
        // PATCH request đến bảng users, filter theo id
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

    // ================================================================
    // HÀM PHỤ TRỢ
    // ================================================================

    // Gắn Header (apikey) cho Auth API requests
    private static Request buildAuthRequest(String url, RequestBody body) {
        return new Request.Builder()
                .url(url)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
    }
}