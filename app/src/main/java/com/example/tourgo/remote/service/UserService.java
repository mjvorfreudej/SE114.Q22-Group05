package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.UpdateProfileRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.UploadImageResponse;
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.RetrofitClient;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserService {

    public static void getCurrentUser(Context context, DataCallback<User> callback) {
        RetrofitClient.getInstance(context)
                .getUserApi()
                .getCurrentUser()
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<User> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void updateProfile(Context context, String name, String phone, String avatar, DataCallback<User> callback) {
        UpdateProfileRequest request = new UpdateProfileRequest(name, phone, avatar);

        RetrofitClient.getInstance(context)
                .getUserApi()
                .updateProfile(request)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<User> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void uploadAvatar(Context context, MultipartBody.Part image, DataCallback<UploadImageResponse> callback) {
        RetrofitClient.getInstance(context)
                .getUserApi()
                .uploadAvatar(image)
                .enqueue(new Callback<ApiResponse<UploadImageResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UploadImageResponse>> call, Response<ApiResponse<UploadImageResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<UploadImageResponse> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<UploadImageResponse>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void deleteAccount(Context context, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getUserApi()
                .deleteAccount()
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
}
