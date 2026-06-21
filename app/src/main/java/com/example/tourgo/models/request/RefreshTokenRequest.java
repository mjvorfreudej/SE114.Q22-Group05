package com.example.tourgo.models.request;

public class RefreshTokenRequest {
    private String refresh_token;

    public RefreshTokenRequest(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getToken() {
        return refresh_token;
    }
}
