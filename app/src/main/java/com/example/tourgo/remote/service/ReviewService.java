package com.example.tourgo.remote.service;

import android.content.Context;
import android.net.Uri;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.CreateReviewRequest;
import com.example.tourgo.models.request.SaveReviewImagesRequest;
import com.example.tourgo.models.request.UpdateReviewRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Review;
import com.example.tourgo.models.response.HotelReview;
import com.example.tourgo.models.response.UploadImageResponse;
import com.example.tourgo.remote.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewService {

    public static void getReviewsByHotelId(Context context, String hotelId, DataCallback<List<Review>> callback) {
        RetrofitClient.getInstance(context)
                .getReviewApi()
                .getReviewsByHotelId(hotelId)
                .enqueue(new Callback<ApiResponse<List<HotelReview>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<HotelReview>>> call, Response<ApiResponse<List<HotelReview>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<HotelReview>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                List<Review> Reviews = new ArrayList<>();
                                for (HotelReview review : apiResponse.getData()) {
                                    Reviews.add(review.toReview());
                                }
                                callback.onSuccess(Reviews);
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
                    public void onFailure(Call<ApiResponse<List<HotelReview>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void getReviewsByTourId(Context context, String tourId, DataCallback<List<Review>> callback) {
        RetrofitClient.getInstance(context)
                .getReviewApi()
                .getReviewsByTourId(tourId)
                .enqueue(new Callback<ApiResponse<List<HotelReview>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<HotelReview>>> call, Response<ApiResponse<List<HotelReview>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<HotelReview>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                List<Review> Reviews = new ArrayList<>();
                                for (HotelReview review : apiResponse.getData()) {
                                    Reviews.add(review.toReview());
                                }
                                callback.onSuccess(Reviews);
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
                    public void onFailure(Call<ApiResponse<List<HotelReview>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void createReview(Context context, String hotelId, String tourId, String reviewText, int stars, DataCallback<String> callback) {
        CreateReviewRequest request = new CreateReviewRequest(hotelId, tourId, reviewText, stars);

        RetrofitClient.getInstance(context)
                .getReviewApi()
                .createReview(request)
                .enqueue(new Callback<ApiResponse<HotelReview>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<HotelReview>> call, Response<ApiResponse<HotelReview>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<HotelReview> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData().getId());
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
                    public void onFailure(Call<ApiResponse<HotelReview>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void updateReview(Context context, String reviewId, String type, String reviewText, int stars, DataCallback<Void> callback) {
        UpdateReviewRequest request = new UpdateReviewRequest(reviewText, stars);

        RetrofitClient.getInstance(context)
                .getReviewApi()
                .updateReview(reviewId, type, request)
                .enqueue(new Callback<ApiResponse<HotelReview>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<HotelReview>> call, Response<ApiResponse<HotelReview>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<HotelReview> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                                callback.onSuccess(null);
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
                    public void onFailure(Call<ApiResponse<HotelReview>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void deleteReview(Context context, String reviewId, String type, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getReviewApi()
                .deleteReview(reviewId, type)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                                callback.onSuccess(null);
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

    public static void saveReviewImages(Context context, String type, String reviewId, List<String> imageUrls, DataCallback<Void> callback) {
        SaveReviewImagesRequest request = new SaveReviewImagesRequest(reviewId, imageUrls);

        RetrofitClient.getInstance(context)
                .getReviewApi()
                .saveReviewImages(type, request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                                callback.onSuccess(null);
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

    public static void uploadReviewImage(Context context, Uri imageUri, String reviewId, DataCallback<String> callback) {
        try {
            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "image/jpeg";
            }

            byte[] imageBytes = readBytesFromUri(context, imageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

            RetrofitClient.getInstance(context)
                    .getReviewApi()
                    .uploadReviewImage(reviewId, body)
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
            callback.onError(com.example.tourgo.interfaces.ApiErrorCode.UNKNOWN, e.getMessage());
        }
    }

    private static byte[] readBytesFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Cannot open input stream");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        inputStream.close();
        return buffer.toByteArray();
    }
}
