package com.example.tourgo.remote;

import android.content.Context;
import android.net.Uri;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Tour;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TourService {

    public static void getTours(DataCallback<List<Tour>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/tours"
                + "?status=eq.approved"
                + "&select=*,tour_images(*)"
                + "&order=created_at.desc"
                + "&tour_images.order=display_order.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
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
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        List<Tour> tours = Tour.fromJsonArray(array);
                        callback.onSuccess(tours);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(ApiErrorCode.SERVER_ERROR, body);
                }
            }
        });
    }

    public static void getToursByRegion(String region, DataCallback<List<Tour>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/tours"
                + "?status=eq.approved"
                + "&region=eq." + region
                + "&select=*,tour_images(*)"
                + "&order=rating.desc"
                + "&tour_images.order=display_order.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
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
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        List<Tour> tours = Tour.fromJsonArray(array);
                        callback.onSuccess(tours);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(ApiErrorCode.SERVER_ERROR, body);
                }
            }
        });
    }

    public static void getTourDetail(String tourId, DataCallback<Tour> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/tours"
                + "?id=eq." + tourId
                + "&select=*,tour_images(*)"
                + "&tour_images.order=display_order.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
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
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        if (array.length() > 0) {
                            Tour tour = Tour.fromJson(array.getJSONObject(0));
                            callback.onSuccess(tour);
                        } else {
                            callback.onError(ApiErrorCode.NOT_FOUND, body);
                        }
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(ApiErrorCode.SERVER_ERROR, body);
                }
            }
        });
    }

    public static void searchTours(String keyword, DataCallback<List<Tour>> callback) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/tours"
                + "?status=eq.approved"
                + "&name=ilike.*" + keyword + "*"
                + "&select=*,tour_images(*)"
                + "&order=rating.desc"
                + "&tour_images.order=display_order.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
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
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(body);
                        List<Tour> tours = Tour.fromJsonArray(array);
                        callback.onSuccess(tours);
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(ApiErrorCode.SERVER_ERROR, body);
                }
            }
        });
    }

    // --- New CRUD Operations ---

    public static void createTour(Tour tour, String accessToken, DataCallback<String> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/tours";
        try {
            JSONObject json = new JSONObject();
            json.put("name", tour.getName());
            json.put("description", tour.getDescription());
            json.put("price", tour.getPrice());
            json.put("destination", tour.getDestination());
            json.put("region", tour.getRegion());
            json.put("status", tour.getStatus());
            json.put("businesses_id", tour.getBusinessesId());

            RequestBody body = RequestBody.create(json.toString(), SupabaseConfig.JSON);
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
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
                        try {
                            JSONArray array = new JSONArray(bodyText);
                            callback.onSuccess(array.getJSONObject(0).getString("id"));
                        } catch (Exception e) {
                            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                        }
                    } else {
                        callback.onError(SupabaseConfig.mapHttp(response.code()), bodyText);
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    public static void updateTour(String tourId, Tour tour, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/tours?id=eq." + tourId;
        try {
            JSONObject json = new JSONObject();
            json.put("name", tour.getName());
            json.put("description", tour.getDescription());
            json.put("price", tour.getPrice());
            json.put("destination", tour.getDestination());
            json.put("region", tour.getRegion());
            json.put("status", tour.getStatus());

            RequestBody body = RequestBody.create(json.toString(), SupabaseConfig.JSON);
            okhttp3.Request request = new okhttp3.Request.Builder()
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
                    if (response.isSuccessful()) callback.onSuccess(null);
                    else callback.onError(SupabaseConfig.mapHttp(response.code()), response.body().string());
                }
            });
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    public static void deleteTour(String tourId, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/tours?id=eq." + tourId;
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
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(SupabaseConfig.mapHttp(response.code()), response.body().string());
            }
        });
    }

    public static void getMyTours(String businessId, String accessToken, DataCallback<List<Tour>> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/tours?businesses_id=eq." + businessId + "&select=*,tour_images(*)&order=created_at.desc";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
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
                        callback.onSuccess(Tour.fromJsonArray(new JSONArray(body)));
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(SupabaseConfig.mapHttp(response.code()), body);
                }
            }
        });
    }

    public static void getPendingTours(String businessId, String accessToken, DataCallback<List<Tour>> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/tours?businesses_id=eq." + businessId + "&status=eq.pending&select=*,tour_images(*)&order=created_at.desc";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
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
                        callback.onSuccess(Tour.fromJsonArray(new JSONArray(body)));
                    } catch (Exception e) {
                        callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
                    }
                } else {
                    callback.onError(SupabaseConfig.mapHttp(response.code()), body);
                }
            }
        });
    }

    public static void uploadTourImage(Context context, Uri imageUri, String tourId, int displayOrder, String accessToken, DataCallback<String> callback) {
        // If it's already a remote URL (e.g. from Demo data), just return it
        if (imageUri.getScheme() != null && (imageUri.getScheme().equals("http") || imageUri.getScheme().equals("https"))) {
            callback.onSuccess(imageUri.toString());
            return;
        }

        try {
            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";
            String extension = mimeType.contains("png") ? "png" : "jpg";
            String fileName = "tours/" + tourId + "/" + System.currentTimeMillis() + "_" + displayOrder + "." + extension;

            byte[] data = readBytes(context, imageUri);
            if (data == null) {
                callback.onError(ApiErrorCode.UNKNOWN, "Could not read image data");
                return;
            }
            RequestBody body = RequestBody.create(data, MediaType.parse(mimeType));
            String url = SupabaseConfig.SUPABASE_URL + "/storage/v1/object/tour-images/" + fileName;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
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
                        callback.onSuccess(SupabaseConfig.SUPABASE_URL + "/storage/v1/object/public/tour-images/" + fileName);
                    } else {
                        callback.onError(SupabaseConfig.mapHttp(response.code()), response.body().string());
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    public static void insertTourImage(String tourId, String imageUrl, int displayOrder, String accessToken, DataCallback<Void> callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/tour_images";
        try {
            JSONObject json = new JSONObject();
            json.put("tour_id", tourId);
            json.put("image_url", imageUrl);

            RequestBody body = RequestBody.create(json.toString(), SupabaseConfig.JSON);
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(body)
                    .build();

            SupabaseConfig.client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(ApiErrorCode.NETWORK, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) callback.onSuccess(null);
                    else callback.onError(SupabaseConfig.mapHttp(response.code()), response.body().string());
                }
            });
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    private static byte[] readBytes(Context context, Uri uri) throws IOException {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (is == null) return null;
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
