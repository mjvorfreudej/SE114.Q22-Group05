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
}
