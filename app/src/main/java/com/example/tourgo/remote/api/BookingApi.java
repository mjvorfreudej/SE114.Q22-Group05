package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Booking;
import com.example.tourgo.models.response.BookingCheckResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookingApi {
    @POST("api/bookings")
    Call<ApiResponse<Booking>> createBooking(@Body Booking request);

    @GET("api/bookings")
    Call<ApiResponse<List<Booking>>> getMyBookings();

    @GET("api/bookings/{bookingId}")
    Call<ApiResponse<Booking>> getBookingById(@Path("bookingId") String bookingId);

    @GET("api/bookings/check")
    Call<ApiResponse<BookingCheckResponse>> hasBookedHotel(@Query("hotelId") String hotelId);

    @PATCH("api/bookings/{bookingId}/cancel")
    Call<ApiResponse<Void>> cancelBooking(@Path("bookingId") String bookingId);
}
