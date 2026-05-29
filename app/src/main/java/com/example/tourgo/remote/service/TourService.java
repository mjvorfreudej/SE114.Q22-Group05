package com.example.tourgo.remote.service;

import android.content.Context;
import android.net.Uri;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.CreateTourRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.models.response.UploadImageResponse;
import com.example.tourgo.remote.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TourService {

    public static void getTours(Context context, DataCallback<List<Tour>> callback) {
        RetrofitClient.getInstance(context)
                .getTourApi()
                .getTours()
                .enqueue(new Callback<ApiResponse<List<Tour>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Tour>>> call, Response<ApiResponse<List<Tour>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Tour>> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<List<Tour>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    public static void getToursByRegion(Context context, String region, DataCallback<List<Tour>> callback) {
        RetrofitClient.getInstance(context)
                .getTourApi()
                .searchTours(null, region, null, null, null, "rating", "desc")
                .enqueue(new Callback<ApiResponse<List<Tour>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Tour>>> call, Response<ApiResponse<List<Tour>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Tour>> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<List<Tour>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    public static void getTourDetail(Context context, String tourId, DataCallback<Tour> callback) {
        RetrofitClient.getInstance(context)
                .getTourApi()
                .getTourById(tourId)
                .enqueue(new Callback<ApiResponse<Tour>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Tour>> call, Response<ApiResponse<Tour>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Tour> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<Tour>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    public static void searchTours(Context context, String keyword, DataCallback<List<Tour>> callback) {
        RetrofitClient.getInstance(context)
                .getTourApi()
                .searchTours(keyword, null, null, null, null, "rating", "desc")
                .enqueue(new Callback<ApiResponse<List<Tour>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Tour>>> call, Response<ApiResponse<List<Tour>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Tour>> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<List<Tour>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    public static void createTour(Context context, CreateTourRequest request, DataCallback<Tour> callback) {
        RetrofitClient.getInstance(context)
                .getTourApi()
                .createTour(request)
                .enqueue(new Callback<ApiResponse<Tour>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Tour>> call, Response<ApiResponse<Tour>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Tour> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<Tour>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    public static void getPendingTours(Context context, DataCallback<List<Tour>> callback) {
        RetrofitClient.getInstance(context)
                .getTourApi()
                .getPendingTours()
                .enqueue(new Callback<ApiResponse<List<Tour>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Tour>>> call, Response<ApiResponse<List<Tour>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Tour>> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<List<Tour>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    public static void approveTour(Context context, String tourId, DataCallback<Tour> callback) {
        RetrofitClient.getInstance(context)
                .getTourApi()
                .approveTour(tourId)
                .enqueue(new Callback<ApiResponse<Tour>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Tour>> call, Response<ApiResponse<Tour>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Tour> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<Tour>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }


    public static void uploadTourImage(Context context, Uri imageUri, String tourId, DataCallback<String> callback) {
        try {
            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "image/jpeg";
            }

            byte[] imageBytes = readBytesFromUri(context, imageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

            RetrofitClient.getInstance(context)
                    .getTourApi()
                    .uploadTourImage(tourId, body)
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
