package com.example.tourgo.remote;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:3000/api/"; // URL mặc định cho Android Emulator kết nối tới localhost Node.js
    private static Retrofit retrofit = null;

    public static ApiService getService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder().build();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
