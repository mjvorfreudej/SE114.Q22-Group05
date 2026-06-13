package com.example.tourgo.remote.api;

import com.example.tourgo.models.request.CreateTourRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.models.response.UploadImageResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TourApi {
    @GET("api/tours")
    Call<ApiResponse<List<Tour>>> getTours();

    @GET("api/tours/{id}")
    Call<ApiResponse<Tour>> getTourById(@Path("id") String id);

    @GET("api/tours/search")
    Call<ApiResponse<List<Tour>>> searchTours(
            @Query("q") String query,
            @Query("region") String region,
            @Query("min_price") Double minPrice,
            @Query("max_price") Double maxPrice,
            @Query("min_rating") Double minRating,
            @Query("sort_by") String sortBy,
            @Query("order") String order
    );

    // ── Provider / Admin tour moderation flow ───────────────────────────────

    /** Submit a new tour (server stores it with status = "PENDING"). */
    @POST("api/tours")
    Call<ApiResponse<Tour>> createTour(@Body CreateTourRequest request);

    /** Upload a cover image for a tour (multipart). */
    @Multipart
    @POST("api/tours/{id}/images")
    Call<ApiResponse<UploadImageResponse>> uploadTourImage(@Path("id") String tourId,
                                                           @Part MultipartBody.Part image);

    /** Admin: list every tour awaiting moderation (status = "PENDING"). */
    @GET("api/tours/pending")
    Call<ApiResponse<List<Tour>>> getPendingTours();

    /** Admin: approve a pending tour (server flips status to "APPROVED"). */
    @PUT("api/tours/{id}/approve")
    Call<ApiResponse<Tour>> approveTour(@Path("id") String tourId);

    @PUT("api/tours/{id}/reject")
    Call<ApiResponse<Void>> rejectTour(@Path("id") String tourId);
}
