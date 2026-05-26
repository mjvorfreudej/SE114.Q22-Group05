package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.RetrofitClient;

import java.util.List;

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
}
