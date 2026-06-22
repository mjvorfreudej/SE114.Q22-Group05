package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.ChatMessage;
import com.example.tourgo.models.response.ChatRoom;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ChatApi {
    @POST("api/chats/rooms")
    Call<ApiResponse<ChatRoom>> getOrCreateRoom(@Body Map<String, String> body);

    @GET("api/chats/rooms")
    Call<ApiResponse<List<ChatRoom>>> getRooms();

    @GET("api/chats/rooms/{roomId}/messages")
    Call<ApiResponse<List<ChatMessage>>> getMessages(@Path("roomId") String roomId);

    @POST("api/chats/rooms/{roomId}/messages")
    Call<ApiResponse<ChatMessage>> sendMessage(@Path("roomId") String roomId, @Body Map<String, String> body);

    @Multipart
    @POST("api/chats/rooms/{roomId}/messages/image")
    Call<ApiResponse<ChatMessage>> sendImageMessage(@Path("roomId") String roomId, @Part MultipartBody.Part image);
}
