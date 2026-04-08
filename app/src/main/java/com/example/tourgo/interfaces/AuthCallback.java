package com.example.tourgo.interfaces;

public interface AuthCallback {
    void onSuccess(String responseData);
    void onError(String errorMessage);
}
