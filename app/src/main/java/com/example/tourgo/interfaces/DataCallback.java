package com.example.tourgo.interfaces;

public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(ApiErrorCode code, String rawMessage);
}
