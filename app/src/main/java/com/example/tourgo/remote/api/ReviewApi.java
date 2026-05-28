package com.example.tourgo.remote.api;

import com.example.tourgo.models.request.CreateReviewRequest;
import com.example.tourgo.models.request.SaveReviewImagesRequest;
import com.example.tourgo.models.request.UpdateReviewRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.HotelReview;
import com.example.tourgo.models.response.UploadImageResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReviewApi {
    @GET("api/reviews")
    Call<ApiResponse<List<HotelReview>>> getReviewsByHotelId(@Query("hotelId") String hotelId);

    @GET("api/reviews")
    Call<ApiResponse<List<HotelReview>>> getReviewsByTourId(@Query("tourId") String tourId);

    @POST("api/reviews")
    Call<ApiResponse<HotelReview>> createReview(@Body CreateReviewRequest request);

    @PATCH("api/reviews/{id}")
    Call<ApiResponse<HotelReview>> updateReview(@Path("id") String reviewId, @Query("type") String type, @Body UpdateReviewRequest request);

    @DELETE("api/reviews/{id}")
    Call<ApiResponse<Void>> deleteReview(@Path("id") String reviewId, @Query("type") String type);

    @Multipart
    @POST("api/reviews/{reviewId}/images")
    Call<ApiResponse<UploadImageResponse>> uploadReviewImage(@Path("reviewId") String reviewId, @Part MultipartBody.Part image);

    @POST("api/reviews/images")
    Call<ApiResponse<Void>> saveReviewImages(@Query("type") String type, @Body SaveReviewImagesRequest request);
}
