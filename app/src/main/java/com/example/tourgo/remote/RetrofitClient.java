package com.example.tourgo.remote;

import android.content.Context;

import com.example.tourgo.BuildConfig;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.remote.api.AdminApi;
import com.example.tourgo.remote.api.AuthApi;
import com.example.tourgo.remote.api.BookingApi;
import com.example.tourgo.remote.api.ReviewApi;
import com.example.tourgo.remote.api.FavoriteApi;
import com.example.tourgo.remote.api.HotelApi;
import com.example.tourgo.remote.api.NotificationApi;
import com.example.tourgo.remote.api.PaymentApi;
import com.example.tourgo.remote.api.TourApi;
import com.example.tourgo.remote.api.UserApi;
import com.example.tourgo.remote.interceptor.AuthInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://tourgo-api-service.onrender.com/";
    // GitHub link: https://github.com/trungnha-uit/TourGo_API_Service

    private static RetrofitClient instance;

    private Retrofit retrofit;
    private SessionManager sessionManager;

    private RetrofitClient(Context context) {
        sessionManager = new SessionManager(context);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(
                BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE
        );

        OkHttpClient refreshClient = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit refreshRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(refreshClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthApi authApiForRefresh = refreshRetrofit.create(AuthApi.class);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(sessionManager, authApiForRefresh))
                .addInterceptor(loggingInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public AuthApi getAuthApi() {
        return retrofit.create(AuthApi.class);
    }

    public TourApi getTourApi() {
        return retrofit.create(TourApi.class);
    }

    public HotelApi getHotelApi() {
        return retrofit.create(HotelApi.class);
    }

    public BookingApi getBookingApi() {
        return retrofit.create(BookingApi.class);
    }

    public FavoriteApi getFavoriteApi() {
        return retrofit.create(FavoriteApi.class);
    }

    public ReviewApi getReviewApi() {
        return retrofit.create(ReviewApi.class);
    }

    public UserApi getUserApi() {
        return retrofit.create(UserApi.class);
    }

    public AdminApi getAdminApi() {
        return retrofit.create(AdminApi.class);
    }

    public PaymentApi getPaymentApi() {
        return retrofit.create(PaymentApi.class);
    }

    public NotificationApi getNotificationApi() {
        return retrofit.create(NotificationApi.class);
    }
}
