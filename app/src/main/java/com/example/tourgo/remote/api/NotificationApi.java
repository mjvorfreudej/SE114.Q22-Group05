package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.NotificationDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {
    @GET("api/notifications")
    Call<ApiResponse<List<NotificationDto>>> getNotifications(@Query("role") String role);

    @PATCH("api/notifications/{id}/read")
    Call<ApiResponse<Void>> markAsRead(@Path("id") String id);

    @PATCH("api/notifications/read-all")
    Call<ApiResponse<Void>> markAllAsRead(@Query("role") String role);
}
