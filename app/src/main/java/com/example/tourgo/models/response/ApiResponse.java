package com.example.tourgo.models.response;

public class ApiResponse<T> {
    private Boolean success;
    private T data;
    private String error;
    private String message;

    public Boolean getSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
