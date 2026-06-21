package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.PaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentApi {
    @POST("api/payments/create")
    Call<ApiResponse<PaymentResponse>> createPayment(@Body PaymentRequest request);
}
