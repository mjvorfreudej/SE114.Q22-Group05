package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.net.Uri;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.models.response.UploadImageResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class HotelService {

    public static void getHotels(Context context, DataCallback<List<Hotel>> callback) {
        RetrofitClient.getInstance(context)
                .getHotelApi()
                .getHotels()
                .enqueue(new Callback<ApiResponse<List<Hotel>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Hotel>>> call, Response<ApiResponse<List<Hotel>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Hotel>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Hotel>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    public static void getHotelDetail(Context context, String hotelId, DataCallback<Hotel> callback) {
        RetrofitClient.getInstance(context)
                .getHotelApi()
                .getHotelById(hotelId)
                .enqueue(new Callback<ApiResponse<Hotel>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Hotel>> call, Response<ApiResponse<Hotel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Hotel> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Hotel>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    /**
     * Search hotels by keyword
     * API: GET /api/hotels/search?q={keyword}
     */
    public static void searchHotels(Context context, String keyword, DataCallback<List<Hotel>> callback) {
        RetrofitClient.getInstance(context)
                .getHotelApi()
                .searchHotels(keyword, null, null, null, null, "rating", "desc")
                .enqueue(new Callback<ApiResponse<List<Hotel>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Hotel>>> call, Response<ApiResponse<List<Hotel>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Hotel>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Hotel>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    /**
     * Search hotels with advanced filters
     * API: GET /api/hotels/search with multiple query parameters
     */
    public static void searchHotelsAdvanced(Context context, String query, String city,
                                           Double minPrice, Double maxPrice, Double minRating,
                                           String sortBy, String order, DataCallback<List<Hotel>> callback) {
        RetrofitClient.getInstance(context)
                .getHotelApi()
                .searchHotels(query, city, minPrice, maxPrice, minRating, sortBy, order)
                .enqueue(new Callback<ApiResponse<List<Hotel>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Hotel>>> call, Response<ApiResponse<List<Hotel>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Hotel>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Hotel>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void getPendingHotels(Context context, DataCallback<List<Hotel>> callback) {
        RetrofitClient.getInstance(context)
                .getHotelApi()
                .getPendingHotels()
                .enqueue(new Callback<ApiResponse<List<Hotel>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Hotel>>> call, Response<ApiResponse<List<Hotel>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Hotel>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Hotel>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void approveHotel(Context context, String hotelId, DataCallback<Hotel> callback) {
        RetrofitClient.getInstance(context)
                .getHotelApi()
                .approveHotel(hotelId)
                .enqueue(new Callback<ApiResponse<Hotel>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Hotel>> call, Response<ApiResponse<Hotel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Hotel> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Hotel>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void rejectHotel(Context context, String hotelId, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getHotelApi()
                .rejectHotel(hotelId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void uploadHotelImage(Context context, Uri imageUri, String hotelId, DataCallback<String> callback) {
        try {
            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "image/jpeg";
            }

            byte[] imageBytes = readBytesFromUri(context, imageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

            RetrofitClient.getInstance(context)
                    .getHotelApi()
                    .uploadHotelImage(hotelId, body)
                    .enqueue(new Callback<ApiResponse<UploadImageResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<UploadImageResponse>> call, Response<ApiResponse<UploadImageResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<UploadImageResponse> apiResponse = response.body();
                                if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                    callback.onSuccess(apiResponse.getData().getImageUrl());
                                } else {
                                    ApiError error = ErrorHandler.parseError(response);
                                    callback.onError(error.getCode(), error.getMessage());
                                }
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<UploadImageResponse>> call, Throwable t) {
                            ApiError error = ErrorHandler.parseError(t);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    });
        } catch (Exception e) {
            callback.onError(ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    private static byte[] readBytesFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Cannot open input stream");
        }
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } finally {
            inputStream.close();
        }
    }
}
