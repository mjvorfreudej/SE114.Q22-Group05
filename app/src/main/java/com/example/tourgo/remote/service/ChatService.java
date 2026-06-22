package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.ChatMessage;
import com.example.tourgo.models.response.ChatRoom;
import com.example.tourgo.remote.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatService {

    public static void getOrCreateRoom(Context context, String businessId, DataCallback<ChatRoom> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("businessId", businessId);

        RetrofitClient.getInstance(context)
                .getChatApi()
                .getOrCreateRoom(body)
                .enqueue(new Callback<ApiResponse<ChatRoom>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ChatRoom>> call, Response<ApiResponse<ChatRoom>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ChatRoom> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
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
                    public void onFailure(Call<ApiResponse<ChatRoom>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void getOrCreateRoomForBusiness(Context context, String userId, DataCallback<ChatRoom> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("userId", userId);

        RetrofitClient.getInstance(context)
                .getChatApi()
                .getOrCreateRoom(body)
                .enqueue(new Callback<ApiResponse<ChatRoom>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ChatRoom>> call, Response<ApiResponse<ChatRoom>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ChatRoom> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
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
                    public void onFailure(Call<ApiResponse<ChatRoom>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void getRooms(Context context, DataCallback<List<ChatRoom>> callback) {
        RetrofitClient.getInstance(context)
                .getChatApi()
                .getRooms()
                .enqueue(new Callback<ApiResponse<List<ChatRoom>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ChatRoom>>> call, Response<ApiResponse<List<ChatRoom>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<ChatRoom>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
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
                    public void onFailure(Call<ApiResponse<List<ChatRoom>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void getMessages(Context context, String roomId, DataCallback<List<ChatMessage>> callback) {
        RetrofitClient.getInstance(context)
                .getChatApi()
                .getMessages(roomId)
                .enqueue(new Callback<ApiResponse<List<ChatMessage>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ChatMessage>>> call, Response<ApiResponse<List<ChatMessage>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<ChatMessage>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
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
                    public void onFailure(Call<ApiResponse<List<ChatMessage>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void sendMessage(Context context, String roomId, String messageText, DataCallback<ChatMessage> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("messageText", messageText);

        RetrofitClient.getInstance(context)
                .getChatApi()
                .sendMessage(roomId, body)
                .enqueue(new Callback<ApiResponse<ChatMessage>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ChatMessage>> call, Response<ApiResponse<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ChatMessage> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
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
                    public void onFailure(Call<ApiResponse<ChatMessage>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    private static final java.util.concurrent.Executor executor = java.util.concurrent.Executors.newSingleThreadExecutor();
    private static final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public static void sendImageMessage(Context context, String roomId, android.net.Uri imageUri, DataCallback<ChatMessage> callback) {
        executor.execute(() -> {
            try {
                String mimeType = context.getContentResolver().getType(imageUri);
                if (mimeType == null || mimeType.isEmpty()) {
                    mimeType = "image/jpeg";
                }

                byte[] imageBytes = readBytesFromUri(context, imageUri);
                okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse(mimeType), imageBytes);
                okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

                RetrofitClient.getInstance(context)
                        .getChatApi()
                        .sendImageMessage(roomId, body)
                        .enqueue(new retrofit2.Callback<ApiResponse<ChatMessage>>() {
                            @Override
                            public void onResponse(retrofit2.Call<ApiResponse<ChatMessage>> call, retrofit2.Response<ApiResponse<ChatMessage>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    ApiResponse<ChatMessage> apiResponse = response.body();
                                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                        mainHandler.post(() -> callback.onSuccess(apiResponse.getData()));
                                    } else {
                                        ApiError error = ErrorHandler.parseError(response);
                                        mainHandler.post(() -> callback.onError(error.getCode(), error.getMessage()));
                                    }
                                } else {
                                    ApiError error = ErrorHandler.parseError(response);
                                    mainHandler.post(() -> callback.onError(error.getCode(), error.getMessage()));
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<ApiResponse<ChatMessage>> call, Throwable t) {
                                ApiError error = ErrorHandler.parseError(t);
                                mainHandler.post(() -> callback.onError(error.getCode(), error.getMessage()));
                            }
                        });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(com.example.tourgo.interfaces.ApiErrorCode.UNKNOWN, e.getMessage()));
            }
        });
    }

    private static byte[] readBytesFromUri(Context context, android.net.Uri uri) throws java.io.IOException {
        java.io.InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new java.io.IOException("Cannot open input stream");
        }

        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }
}
