package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.PaymentResponse;
import com.example.tourgo.models.request.UpdatePaymentStatusRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PATCH;

public interface PaymentApi {
    @POST("api/payments/create")
    Call<ApiResponse<PaymentResponse>> createPayment(@Body PaymentRequest request);

    @PATCH("api/payments/update-status")
    Call<ApiResponse<Void>> updatePaymentStatus(@Body UpdatePaymentStatusRequest request);
}
