package com.example.tourgo.models.request;

/**
 * Body for POST /api/auth/social — the backend exchanges the provider's OIDC
 * id_token for a Supabase session via signInWithIdToken.
 *
 * provider: "google" (Facebook on Android uses a different web-OAuth flow).
 * token: the provider id_token obtained natively on the device.
 */
public class SocialLoginRequest {
    private String provider;
    private String token;

    public SocialLoginRequest(String provider, String token) {
        this.provider = provider;
        this.token = token;
    }

    public String getProvider() {
        return provider;
    }

    public String getToken() {
        return token;
    }
}
