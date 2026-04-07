package com.example.tourgo;

public interface AuthCallback {
    void onSuccess(String responseData);
    void onError(String errorMessage);
}
