package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.ui.notification.NotificationItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface NotificationApi {
    @GET("api/notifications")
    Call<ApiResponse<List<NotificationItem>>> getNotifications();

    @PATCH("api/notifications/{id}/read")
    Call<ApiResponse<Void>> markAsRead(@Path("id") String id);

    @PATCH("api/notifications/read-all")
    Call<ApiResponse<Void>> markAllAsRead();
}
