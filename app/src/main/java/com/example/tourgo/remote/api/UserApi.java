package com.example.tourgo.remote.api;

import com.example.tourgo.models.request.BusinessRequest;
import com.example.tourgo.models.request.UpdateProfileRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.models.response.BusinessListing;
import com.example.tourgo.models.response.UploadImageResponse;
import com.example.tourgo.models.response.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface UserApi {
    @GET("api/users/me")
    Call<ApiResponse<User>> getCurrentUser();

    @PUT("api/users/me")
    Call<ApiResponse<User>> updateProfile(@Body UpdateProfileRequest request);

    @Multipart
    @PUT("api/users/me")
    Call<ApiResponse<User>> updateProfileMultipart(
            @Header("Authorization")
            String token,

            @Part("name")
            RequestBody name,

            @Part("phone")
            RequestBody phone,

            @Part
            MultipartBody.Part file
    );

    @Multipart
    @POST("api/users/me/avatar")
    Call<ApiResponse<UploadImageResponse>> uploadAvatar(@Part MultipartBody.Part image);

    @DELETE("api/users/me")
    Call<ApiResponse<Void>> deleteAccount();

    /** User đăng ký làm Business. */
    @POST("api/users/businesses/register")
    Call<ApiResponse<BusinessAccount>> registerBusiness(@Body BusinessRequest request);

    /** User lấy thông tin Business. */
    @GET("api/users/businesses/me")
    Call<ApiResponse<BusinessAccount>> getBusinesses();

    /** User lấy danh sách Listing của Business. */
    @GET("api/users/businesses/me/listings")
    Call<ApiResponse<List<BusinessListing>>> getMyListings();

    /** User sửa Business Profile. */
    @PUT("api/users/businesses/me")
    Call<ApiResponse<BusinessAccount>> updateBusinesses(@Body BusinessRequest request);

    @Multipart
    @PUT("api/users/businesses/me")
    Call<ApiResponse<BusinessAccount>> updateBusinessesMultipart(
            @Part("name") RequestBody name,
            @Part("owner") RequestBody owner,
            @Part("taxCode") RequestBody taxCode,
            @Part("address") RequestBody address,
            @Part("phone") RequestBody phone,
            @Part("email") RequestBody email
    );

    /** User hủy Business. */
    @DELETE("api/users/businesses/me")
    Call<ApiResponse<Void>> deleteBusinesses();
}
