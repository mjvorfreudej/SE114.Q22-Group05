package com.example.tourgo.remote.api;

import com.example.tourgo.models.request.LoginRequest;
import com.example.tourgo.models.request.RefreshTokenRequest;
import com.example.tourgo.models.request.RegisterRequest;
import com.example.tourgo.models.request.ResetPasswordRequest;
import com.example.tourgo.models.request.UpdatePasswordRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.AuthData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/auth/login")
    Call<ApiResponse<AuthData>> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<ApiResponse<AuthData>> register(@Body RegisterRequest request);

    @POST("api/auth/reset-password")
    Call<ApiResponse<AuthData>> resetPasswrod(@Body ResetPasswordRequest request);

    @POST("api/auth/refresh-token")
    Call<ApiResponse<AuthData>> refreshToken(@Body RefreshTokenRequest request);

    @POST("api/auth/update-password")
    Call<ApiResponse<Void>> updatePassword(@Body UpdatePasswordRequest request);

    @POST("api/auth/logout")
    Call<ApiResponse<Void>> logout();
}
