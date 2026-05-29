package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.response.AdminAccount;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Admin-console data access. Every call goes through Retrofit to the Node.js
 * backend; the client performs no database work itself.
 */
public class AdminService {

    public static void getPendingBusinesses(Context context, DataCallback<List<BusinessAccount>> callback) {
        RetrofitClient.getInstance(context)
                .getAdminApi()
                .getPendingBusinesses()
                .enqueue(new Callback<ApiResponse<List<BusinessAccount>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<BusinessAccount>>> call, Response<ApiResponse<List<BusinessAccount>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<BusinessAccount>> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<List<BusinessAccount>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void approveBusiness(Context context, String businessId, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getAdminApi()
                .approveBusiness(businessId)
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

    public static void getUsers(Context context, DataCallback<List<AdminAccount>> callback) {
        RetrofitClient.getInstance(context)
                .getAdminApi()
                .getUsers()
                .enqueue(new Callback<ApiResponse<List<AdminAccount>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<AdminAccount>>> call, Response<ApiResponse<List<AdminAccount>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<AdminAccount>> apiResponse = response.body();
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
                    public void onFailure(Call<ApiResponse<List<AdminAccount>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void suspendUser(Context context, String userId, DataCallback<Void> callback) {
        toggleUser(context, userId, true, callback);
    }

    public static void activateUser(Context context, String userId, DataCallback<Void> callback) {
        toggleUser(context, userId, false, callback);
    }

    private static void toggleUser(Context context, String userId, boolean suspend, DataCallback<Void> callback) {
        Call<ApiResponse<Void>> call = suspend
                ? RetrofitClient.getInstance(context).getAdminApi().suspendUser(userId)
                : RetrofitClient.getInstance(context).getAdminApi().activateUser(userId);

        call.enqueue(new Callback<ApiResponse<Void>>() {
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
