package com.example.tourgo.remote;

import static com.example.tourgo.remote.SupabaseConfig.mapHttp;

import android.content.Context;
import android.util.Log;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Hotel;

import org.json.JSONArray;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;
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
}
