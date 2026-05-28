package com.example.tourgo.remote.api;

import com.example.tourgo.models.request.UpdateProfileRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UserApi {
    @GET("api/users/me")
    Call<ApiResponse<User>> getCurrentUser();

    @PUT("api/users/me")
    Call<ApiResponse<User>> updateProfile(@Body UpdateProfileRequest request);

    @DELETE("api/users/me")
    Call<ApiResponse<Void>> deleteAccount();

}
