package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.PaymentResponse;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.remote.api.PaymentRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentService {

    /**
     * Tạo thông tin thanh toán từ backend.
     * Backend sẽ tạo transaction_code unique và trả về thông tin tương ứng với payment method:
     * - bank_transfer: trả về bank_info với transaction_code làm nội dung CK
     * - cod: không cần thông tin đặc biệt
     *
     * @param context Android context
     * @param bookingId ID của booking cần thanh toán
     * @param amount Số tiền thanh toán (VNĐ)
     * @param paymentMethod Phuong thuc: "bank_transfer", "cod"
     * @param callback Callback trả về PaymentResponse hoặc lỗi
     */
    public static void createPayment(Context context, String bookingId, double amount,
                                     String paymentMethod, DataCallback<PaymentResponse> callback) {
        PaymentRequest request = new PaymentRequest(bookingId, amount, paymentMethod);

        RetrofitClient.getInstance(context)
                .getPaymentApi()
                .createPayment(request)
                .enqueue(new Callback<ApiResponse<PaymentResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PaymentResponse>> call, Response<ApiResponse<PaymentResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<PaymentResponse> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()
                                && apiResponse.getData() != null) {
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
                    public void onFailure(Call<ApiResponse<PaymentResponse>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }
}
