package com.example.tourgo.models.response;

public class Session {
    private String access_token;
    private String refresh_token;
    private long expires_at;
    private String token_type;

    public String getAccess_token() {
        return access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public long getExpires_at() {
        return expires_at;
    }

    public String getToken_type() {
        return token_type;
    }
}
