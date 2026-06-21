package com.example.tourgo.remote.api;

import com.example.tourgo.models.request.CreateHotelRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Hotel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import okhttp3.MultipartBody;
import com.example.tourgo.models.response.UploadImageResponse;

public interface HotelApi {
    @GET("api/hotels")
    Call<ApiResponse<List<Hotel>>> getHotels();

    @GET("api/hotels/{id}")
    Call<ApiResponse<Hotel>> getHotelById(@Path("id") String id);

    @GET("api/hotels/search")
    Call<ApiResponse<List<Hotel>>> searchHotels(
            @Query("q") String query,
            @Query("city") String city,
            @Query("min_price") Double minPrice,
            @Query("max_price") Double maxPrice,
            @Query("min_rating") Double minRating,
            @Query("sort_by") String sortBy,
            @Query("order") String order
    );

    @POST("api/hotels")
    Call<ApiResponse<Hotel>> createHotel(@Body CreateHotelRequest request);

    @Multipart
    @POST("api/hotels/{id}/images")
    Call<ApiResponse<UploadImageResponse>> uploadHotelImage(@Path("id") String hotelId,
                                                           @Part MultipartBody.Part image);

    @GET("api/hotels/pending")
    Call<ApiResponse<List<Hotel>>> getPendingHotels();

    @PUT("api/hotels/{id}/approve")
    Call<ApiResponse<Hotel>> approveHotel(@Path("id") String id);

    @PUT("api/hotels/{id}/reject")
    Call<ApiResponse<Void>> rejectHotel(@Path("id") String id);
}
