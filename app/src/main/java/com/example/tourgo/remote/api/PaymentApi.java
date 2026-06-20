package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface PaymentApi {
    @FormUrlEncoded
    @POST("api/payments/create")
    Call<ApiResponse<String>> createPaymentUrl(
            @Field("bookingId") String bookingId,
            @Field("amount") double amount,
            @Field("bankCode") String bankCode
    );
}
