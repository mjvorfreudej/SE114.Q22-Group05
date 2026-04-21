package com.example.tourgo.interfaces;

public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(String errorMessage);
}
