package com.example.tourgo.remote.interceptor;

import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.models.request.RefreshTokenRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.AuthData;
import com.example.tourgo.remote.api.AuthApi;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

public class AuthInterceptor implements Interceptor {
    private SessionManager sessionManager;
    private AuthApi authApi;

    public AuthInterceptor(SessionManager sessionManager, AuthApi authApi) {
        this.sessionManager = sessionManager;
        this.authApi = authApi;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String token = sessionManager.getAccessToken();

        if (token == null || token.isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        Response response = chain.proceed(authenticatedRequest);

        if (response.code() == 401) {
            synchronized (this) {
                String currentToken = sessionManager.getAccessToken();

                if (currentToken != null && !currentToken.equals(token)) {
                    response.close();
                    Request newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + currentToken)
                            .build();
                    return chain.proceed(newRequest);
                }

                String refreshToken = sessionManager.getRefreshToken();
                if (refreshToken != null && !refreshToken.isEmpty()) {
                    if (refreshAccessToken(refreshToken)) {
                        response.close();
                        String newToken = sessionManager.getAccessToken();
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + newToken)
                                .build();
                        return chain.proceed(newRequest);
                    } else {
                        sessionManager.clear();
                    }
                }
            }
        }

        return response;
    }

    private boolean refreshAccessToken(String refreshToken) {
        try {
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
            Call<ApiResponse<AuthData>> call = authApi.refreshToken(request);
            retrofit2.Response<ApiResponse<AuthData>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                ApiResponse<AuthData> apiResponse = response.body();
                if (apiResponse.getSuccess() && apiResponse.getData() != null) {
                    AuthData authData = apiResponse.getData();

                    sessionManager.saveSession(
                            authData.getSession().getAccess_token(),
                            authData.getSession().getRefresh_token(),
                            authData.getSession().getExpires_at()
                    );

                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
