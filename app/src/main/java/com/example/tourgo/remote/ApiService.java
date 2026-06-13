package com.example.tourgo.remote;

import com.example.tourgo.models.Review;
import com.example.tourgo.models.Tour;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    // Lấy danh sách bài đăng của tôi
    @GET("posts/my-listings")
    Call<List<Tour>> getMyPosts(@Header("Authorization") String token);

    // Đăng bài mới (Dùng Multipart nếu có upload ảnh trực tiếp lên Node.js)
    @POST("posts/create")
    Call<ResponseBody> createPost(@Header("Authorization") String token, @Body Tour tour);

    // Cập nhật bài đăng
    @PUT("posts/update/{id}")
    Call<ResponseBody> updatePost(@Header("Authorization") String token, @Path("id") String id, @Body Tour tour);

    // Xóa bài đăng
    @DELETE("posts/delete/{id}")
    Call<Void> deletePost(@Header("Authorization") String token, @Path("id") String id);

    // Upload ảnh lên server Node.js
    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file);

    // Lấy danh sách reviews của bài đăng
    @GET("reviews/{postId}")
    Call<List<Review>> getPostReviews(@Path("postId") String postId);
}
