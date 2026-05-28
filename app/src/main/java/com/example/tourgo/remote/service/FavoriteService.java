package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Favorite;
import com.example.tourgo.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteService {

    public static void addFavorite(Context context, Favorite favorite, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getFavoriteApi()
                .addFavorite(favorite)
                .enqueue(new Callback<ApiResponse<Favorite>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Favorite>> call, Response<ApiResponse<Favorite>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Favorite> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
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
                    public void onFailure(Call<ApiResponse<Favorite>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void removeFavorite(Context context, String favoriteId, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getFavoriteApi()
                .removeFavorite(favoriteId)
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

    public static void getMyFavorites(Context context, DataCallback<List<Favorite>> callback) {
        RetrofitClient.getInstance(context)
                .getFavoriteApi()
                .getMyFavorites()
                .enqueue(new Callback<ApiResponse<List<Favorite>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Favorite>>> call, Response<ApiResponse<List<Favorite>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Favorite>> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<List<Favorite>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }
}
