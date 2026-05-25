package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Tour;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TourApi {
    @GET("api/tours")
    Call<ApiResponse<List<Tour>>> getTours();

    @GET("api/tours/{id}")
    Call<ApiResponse<List<Tour>>> getToursByID(@Path("id") String id);

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
}
