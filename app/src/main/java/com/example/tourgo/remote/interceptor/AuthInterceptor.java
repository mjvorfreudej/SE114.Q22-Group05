package com.example.tourgo.remote.interceptor;

import com.example.tourgo.data.local.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String token = sessionManager.getAccessToken();

        if(token != null && !token.isEmpty()) {
            Request request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();

            return chain.proceed(request);
        }
        return chain.proceed(originalRequest);
    }
}
