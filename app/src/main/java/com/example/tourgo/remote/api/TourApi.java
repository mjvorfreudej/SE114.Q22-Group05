package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Tour;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TourApi {
    /**
     * Get all approved tours with images
     */
    @GET("api/tours")
    Call<ApiResponse<List<Tour>>> getTours();

    /**
     * Get tour details by ID
     */
    @GET("api/tours/{id}")
    Call<ApiResponse<Tour>> getTourById(@Path("id") String id);

    /**
     * Search/filter tours with multiple criteria
     * @param query Search keyword (optional)
     * @param region Filter by region (optional)
     * @param minPrice Minimum price filter (optional)
     * @param maxPrice Maximum price filter (optional)
     * @param minRating Minimum rating filter (optional)
     * @param sortBy Sort field: price, rating, created_at (optional)
     * @param order Sort order: asc, desc (optional)
     */
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
