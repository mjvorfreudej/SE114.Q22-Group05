package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.UpdateStatusRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Booking;
import com.example.tourgo.models.response.BookingCheckResponse;
import com.example.tourgo.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingService {

    public static void createBooking(Context context, Booking booking, DataCallback<Booking> callback) {
        RetrofitClient.getInstance(context)
                .getBookingApi()
                .createBooking(booking)
                .enqueue(new Callback<ApiResponse<Booking>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Booking> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void getMyBookings(Context context, DataCallback<List<Booking>> callback) {
        RetrofitClient.getInstance(context)
                .getBookingApi()
                .getMyBookings()
                .enqueue(new Callback<ApiResponse<List<Booking>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Booking>> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void updateBookingStatus(Context context, String bookingId, String status, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getBookingApi()
                .updateBookingStatus(bookingId, new UpdateStatusRequest(status))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getSuccess() != null && response.body().getSuccess()) {
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

    public static void getBookingById(Context context, String bookingId, DataCallback<Booking> callback) {
        RetrofitClient.getInstance(context)
                .getBookingApi()
                .getBookingById(bookingId)
                .enqueue(new Callback<ApiResponse<Booking>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Booking> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void cancelBooking(Context context, String bookingId, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getBookingApi()
                .cancelBooking(bookingId)
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

    public static void hasBookedHotel(Context context, String hotelId, DataCallback<Boolean> callback) {
        RetrofitClient.getInstance(context)
                .getBookingApi()
                .hasBookedHotel(hotelId)
                .enqueue(new Callback<ApiResponse<BookingCheckResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BookingCheckResponse>> call, Response<ApiResponse<BookingCheckResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<BookingCheckResponse> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData().getHasBooked());
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
                    public void onFailure(Call<ApiResponse<BookingCheckResponse>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void hasBookedTour(Context context, String tourId, DataCallback<Boolean> callback) {
        RetrofitClient.getInstance(context)
                .getBookingApi()
                .hasBookedTour(tourId)
                .enqueue(new Callback<ApiResponse<BookingCheckResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BookingCheckResponse>> call, Response<ApiResponse<BookingCheckResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<BookingCheckResponse> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData().getHasBooked());
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
                    public void onFailure(Call<ApiResponse<BookingCheckResponse>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }
}
