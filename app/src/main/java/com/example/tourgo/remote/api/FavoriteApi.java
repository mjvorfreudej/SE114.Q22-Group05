package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.Favorite;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoriteApi {
    @GET("api/favorites")
    Call<ApiResponse<List<Favorite>>> getMyFavorites();

    @POST("api/favorites")
    Call<ApiResponse<Favorite>> addFavorite(@Body Favorite favorite);

    @DELETE("api/favorites/{id}")
    Call<ApiResponse<Void>> removeFavorite(@Path("id") String favoriteId);
}

